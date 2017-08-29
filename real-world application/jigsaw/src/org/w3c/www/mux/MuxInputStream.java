// MuxInputStream.java
// $Id: MuxInputStream.java,v 1.1 2010/06/15 12:26:35 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mux;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class MuxInputStream extends InputStream implements MUX {
    /**
     * Debug flags - debug the push method.
     */
    private static final boolean debugPush = false;

    /**
     * The MuxSession instance this input stream is attached to.
     */
    protected MuxSession session = null;
    /**
     * A quick reference to that session writer.
     */
    protected MuxWriter writer = null;
    /**
     * This input stream associated buffer.
     */
    protected byte buffer[] = null;
    /**
     * The current buffer position.
     */
    protected int bufptr = -1;
    /**
     * The current mark within the input buffer, or <strong>-1</strong>.
     */
    protected int markptr = -1;
    /**
     * The current buffer length.
     */
    protected int buflen = -1;
    /**
     * Has this stream been closed ?
     */
    protected boolean closed = false;
    /**
     * Currently consumed credits.
     */
    protected int consumed_credit = 0;
    /**
     * Current available credit on that session.
     */
    protected int avail_credit = MUX.RECEIVER_DEFAULT_CREDIT;
    /**
     * Yet another push is pending from the reader thread.
     */
    protected boolean pushpending = false;
    /**
     * Error message in case of error.
     */
    protected String errmsg = null;

    private void addCredit(int consumed) 
	throws IOException
    {
	consumed_credit += consumed;
	if (consumed_credit > (avail_credit >> 1)) {
	    // Send more credit:    
	    writer.ctrlSendCredit(session.getIdentifier(), consumed_credit);
	    writer.flush();
	    consumed_credit = 0;
	}
	notifyAll();
    }

    /**
     * Fill in that input stream with more data.
     * This method can only be called from within that package, typically
     * by the session reader, to fill in the buffer.
     * @param data The data read from the socket.
     * @param off Offset of available data within above buffer.
     * @param len Length of available data within above buffer.
     * @param noflush Set to <strong>true</strong> if there is already more
     * data available for that session.
     */

    protected synchronized void push(byte data[], int off, int len
				     , boolean noflush)
	throws IOException
    {
	if ( debugPush )
	    System.out.println("MuxReader[push]: "
                               + len + " bytes"
			       + ", noflush="+noflush);
	// If that stream was closed in the mean time, discard data:
	if ( closed ) {
	    // FIXME this would be the place to send a RESET:
	    return;
	}
	// Otherwise, do the job, until all data has been accepted:
	int bufpos = -1;
	int avail  = -1;
	while (len > 0) {
	    bufpos = bufptr+buflen;
	    avail  = buffer.length - bufpos;
	    // Should we shift the buffer now ?
	    if ((avail < len) && (bufptr > 0)) {
		System.arraycopy(buffer, bufptr, buffer, 0, buflen);
		if ( markptr >= 0 )
		    markptr = (markptr >= bufptr) ? markptr-bufptr : -1;
		bufptr = 0;
		bufpos = buflen;
		avail  = buffer.length-bufpos;
	    } 
	    // Accept as many bytes as possible, until we are done
	    // Wait for some available space in buffer:
	    if ( debugPush )
		System.out.println("push: "+len+" bytes, avail="+avail);
	    while ( avail <= 0 ) {
		if ( buflen > 0 )
		    notifyAll();
		try {
		    wait();
		} catch (InterruptedException ex) {
		    throw new IOException("Interrupted read");
		}
		if ( buflen != 0 ) {
		    bufpos = bufptr+buflen;
		    avail  = buffer.length-bufpos;
		} else {
		    bufpos = 0;
		    bufptr = 0;
		    buflen = 0;
		    avail  = buffer.length;
		}
	    }
	    // Flush data into available buffer space, and notify any reader:
	    if ( len < avail ) {
		System.arraycopy(data, off, buffer, bufpos, len);
		buflen += len;
		len     = 0;
	    } else {
		System.arraycopy(data, off, buffer, bufpos, avail);
		off    += avail;
		len    -= avail;
		buflen += avail;
	    }
	    if ( ! (pushpending = noflush) )
		notifyAll();
	    if ( debugPush )
		System.out.println("push: "+buflen+" total bytes pushed.");
	}
    }

    /**
     * Set a mark in that input stream.
     * @param readlimit The maximum limit of bytes allowed to be read
     * before the mark becomes invalid.
     */

    public synchronized void mark(int readlimit) {
	markptr = bufptr;
    }

    /**
     * Reset buffer to last mark.
     * @exception IOException If the mark has not been set, or if it is no 
     * longer valid.
     */

    public synchronized void reset() 
	throws IOException
    {
	if ( markptr <= bufptr ) {
	    buflen += (bufptr-markptr);
	    bufptr  = markptr;
	} else {
	    throw new IOException("invalid mark.");
	}
    }

    /**
     * Notify that stream of some error condition.
     * When an error condition is detected, all read accesses to the stream
     * will result in an IOException being thrown, with as a message, the
     * message provided here.
     * @param msg Error message to be provided in any future IOException.
     */

    protected synchronized void error(String msg) {
	errmsg = msg;
	try {
	    close();
	} catch (IOException ex) {
	}
    }

    /**
     * Get the number of available bytes on that stream.
     * @return Number of bytes available.
     */

    public synchronized int available() 
	throws IOException
    {
	if ( closed ) {
	    if ( errmsg != null )
		throw new IOException(errmsg);
	    return -1;
	}
	return buflen;
    }

    /**
     * Close that input stream.
     * @exception IOException If some IO error occured during close.
     */

    public synchronized void close() 
	throws IOException
    {
	closed      = true;
	pushpending = false;
	notifyAll();
    }

    /**
     * Read one byte of input from the stream.
     * @return The byte read, or <strong>-1</strong> if end of stream.
     * @exception IOException If an IO error has occured.
     */

    public synchronized int read() 
	throws IOException
    {
	while ( true ) {
	    // Always send back available data:
	    if ( buflen > 0 ) {
		byte ch = buffer[bufptr++];
		buflen--;
		addCredit(1);
		return ((int) ch) & 0xff;
	    } 
	    // If closed, throw an IOException:
	    if ( closed ) {
		if ( errmsg != null )
		    throw new IOException(errmsg);
		return -1;
	    }
	    // Wait for this session's buffer to be filled by the reader
	    try {
		wait();
	    } catch (InterruptedException ex) {
		throw new IOException("Interrupted read.");
	    }
	}	    
    }

    /**
     * Reads into an array of bytes.  This method will
     * block until some input is available.
     * @param b	the buffer into which the data is read
     * @param off the start offset of the data
     * @param len the maximum number of bytes read
     * @return  the actual number of bytes read, -1 is
     * 		returned when the end of the stream is reached.
     * @exception IOException If an I/O error has occurred.
     */

    public synchronized int read(byte b[], int off, int len)
	throws IOException
    {
	while ( true ) {
	    if ((buflen > 0) && ! pushpending ) {
		// Send back that output straight
		int size = Math.min(len, buflen);
		System.arraycopy(buffer, bufptr, b, off, size);
		bufptr += size;
		buflen -= size;
		// Notify the reader thread if it is waiting for buffer space
		addCredit(size);
		return size;
	    } 
	    // If closed, throw an IOException
	    if ( closed ) {
		if ( errmsg != null )
		    throw new IOException(errmsg);
		return -1;
	    }
	    // Wait for this session's buffer to be filled by the reader
	    try {
		wait();
	    } catch (InterruptedException ex) {
		throw new IOException("Interrupted read.");
	    }
	}
    }

    /**
     * Create an input stream to read data from the given session.
     * @param session The session to read data from.
     */

    protected MuxInputStream(MuxSession session) {
	this.session = session;
	this.writer  = session.getMuxStream().getMuxWriter();
	this.buffer  = new byte[session.getInputBufferSize()];
	this.bufptr  = 0;
	this.buflen  = 0;
    }

}

// MuxOutputStream.java
// $Id: MuxOutputStream.java,v 1.1 2010/06/15 12:26:34 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mux;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class MuxOutputStream extends OutputStream {
    protected static final boolean debug = false;

    /**
     * The session this stream is attached to.
     */
    protected MuxSession session = null;
    /**
     * The identifier of above session (fast access).
     */
    protected int sessid = -1;
    /**
     * The writer instance for the multiplexed stream.
     */
    protected MuxWriter writer = null;
    /**
     * The current max allowed fragment size.
     */
    protected int fragsz = MUX.SENDER_DEFAULT_FRAGMENT_SIZE;
    /**
     * The currently available credit.
     */
    protected int avail_credit = MUX.SENDER_DEFAULT_CREDIT;
    /**
     * Has this stream been closed ?
     */
    protected boolean closed = false;

    /**
     * Callback notifying that more credit is available for that stream.
     * @param credit The credit we are getting from our peer.
     */

    protected synchronized void notifyCredit(int credit) {
	if ( debug )
	    System.out.println("> notifyCredit["+sessid+"]: "+credit);
	avail_credit += credit;
	notifyAll();
    }

    /**
     * Callback notifying the the frgament size has changed.
     * @param control The new fragment size.
     */

    protected synchronized void notifyControl(int control) {
	if ( debug )
	    System.out.println("notifyControl: "+control);
	fragsz = control;
    }

    /**
     * Emit the given data on current session.
     * @param b The buffer containing the data to be emitted.
     * @param off Offset of data within above buffer.
     * @param len Length of data to be written,
     */

    private synchronized void send(byte b[], int off, int len) 
	throws IOException
    {
	// Otherwise perform:
	while (len > 0) {
	    // Make sure we have some remaining credit:
	    while ( avail_credit <= 0 ) {
		// If closed, trigger an error:
		if ( closed ) 
		    throw new IOException("Broken pipe");
		writer.flush();
		try {
		    wait();
		} catch (InterruptedException ex) {
		    throw new IOException("Interrupted IO !");
		}
	    }
	    // Chunk (if needed) until all available credit has been consumed
	    while ( avail_credit > 0 ) {
		if ( fragsz <= 0 ) {
		    int sz = Math.min(avail_credit, len);
		    writer.writeData(sessid, b, off, sz);
		    len -= sz;
		    off += sz;
		    avail_credit -= sz;
		} else if (len < fragsz ) {
		    // No fragmentation needed, we can sink all our data:
		    writer.writeData(sessid, b, off, len);
		    avail_credit -= len;
		    return;
		} else {
		    // Emit only one single chunk:
		    writer.writeData(sessid, b, off, fragsz);
		    len -= fragsz;
		    off += fragsz;
		    avail_credit -= fragsz;
		}
	    }
	}
    }

    /**
     * Writes a byte. This method will block until the byte is actually
     * written.
     * It is <em>highly</em> recomended that you use a buffered output
     * stream on top of that stream, or that you don't use that method.
     * @param b	the byte
     * @exception IOException If an I/O error has occurred.
     */

    public void write(int b) 
	throws IOException
    {
	byte bits[] = new byte[1];
	bits[0] = (byte) (b&0xff);
	write(bits, 0, 1);
    }

    /**
     * Writes a sub array of bytes. 
     * @param b	the data to be written
     * @param off	the start offset in the data
     * @param len	the number of bytes that are written
     * @exception IOException If an I/O error has occurred.
     */

    public void write(byte b[], int off, int len) 
	throws IOException
    {
	send(b, off, len);
    }

    /**
     * Flush that output stream, blocking all data has been sent.
     * @exception IOException If some IO errors occur.
     */

    public void flush() 
	throws IOException
    {
	writer.flush();
    }

    /**
     * Close that session output stream.
     * @exception IOException If some IO errors occur.
     */

    public synchronized void close()
	throws IOException
    {
	if ( closed )
	    return;
	closed = true;
	session.sendFIN();
	notifyAll();
	return ;
    }

    protected MuxOutputStream(MuxSession session) {
	this.session = session;
	this.sessid  = session.getIdentifier();
	this.writer  = session.getMuxStream().getMuxWriter();
    }

}

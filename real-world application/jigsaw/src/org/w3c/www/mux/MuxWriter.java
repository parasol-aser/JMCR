// MuxWriter.java
// $Id: MuxWriter.java,v 1.1 2010/06/15 12:26:33 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mux;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This class is dumb. It does no control flow, and nothing clever, just
 * emit appropriate MUX headers before sending some data.
 * <p>The flow control is handled on a per session basis, by both the
 * MuxSession class, and the MuxOutputStream class.
 * @see MuxSession
 * @see MuxOutputStream
 */

class MuxWriter implements MUX {
    private static final byte padbytes[] = {
	(byte) 0, (byte) 0, (byte) 0, (byte) 0,
	(byte) 0, (byte) 0, (byte) 0, (byte) 0
    };
    private static final boolean debug = true;
    /**
     * The MUX Stream this writer is working for.
     */
    protected MuxStream stream = null;
    /**
     * The output buffer.
     */
    protected byte buffer[] = new byte[MUX.WRITER_BUFFER_SIZE] ;
    /**
     * The current buffer size.
     */
    protected int buflen   = 0 ;
    /**
     * The current buffer pointer.
     */
    protected int bufptr = 0;
    /**
     * The output stream to write data to.
     */
    protected OutputStream out   = null ;

    /**
     * Can we get this capacity from our buffer.
     * <p>The caller is responsible to synchronize access to that method.
     * Make best effort (ie flush) to try getting the requested capacity. If
     * success, than return <strong>true</strong> otherwise, return 
     * <strong>false</strong>.
     * @param capacity Requested capacity.
     * @return A boolean <strong>true</strong if requested capacity is 
     *    available.
     * @exception IOException If flushing the buffer trigered some IO errors.
     */

    private boolean ensureCapacity (int capacity) 
	throws IOException
    {
	if ( bufptr + buflen + capacity < buffer.length ) {
	    return true ;
	} else if (buffer.length < capacity) {
	    flush() ;
	    return true ;
	} else {
	    return false ;
	}
    }

    /**
     * Encode a word (little endian)
     * <p>The caller is responsible to synchronize access to that method.
     * <p>The caller is assumed to make sure the required capacity is
     * available.
     * @param word The word to encode.
     */

    private final void encodeWord(int word) {
	int pos = bufptr+buflen;
	buffer[pos++] = (byte)  (word & 0x000000ff);
	buffer[pos++] = (byte) ((word & 0x0000ff00) >> 8) ;
	buffer[pos++] = (byte) ((word & 0x00ff0000) >> 16);
	buffer[pos++] = (byte) ((word & 0xff000000) >> 24);
	buflen += 4;
	if ( debug )
	    System.out.println("[encodeWord] 0x"
                               + Integer.toString(buffer[bufptr], 16)
			       + Integer.toString(buffer[bufptr+1], 16)
			       + Integer.toString(buffer[bufptr+2], 16)
			       + Integer.toString(buffer[bufptr+3], 16));

    }

    /**
     * Encode a short (little endian)
     * <p>The caller is responsible to synchronize access to that method.
     * <p>The caller is assumed to make sure the required capacity is
     * available.
     * @param s The short to encode.
     */

    private final void encodeShort(short s) {
	int pos = bufptr+buflen;
	buffer[pos++] = (byte)  (s & 0x00ff);
	buffer[pos++] = (byte) ((s & 0xff00) >> 8) ;
	buflen += 2;
    }

    /**
     * Encode a small message.
     * <p>The caller is responsible to synchronize access to that method.
     * @param flags The header flags.
     * @param session The session.
     * @param len The message length.
     * @param into Target buffer.
     * @param dst Target buffer position.
     */

    private final void encodeMessage (int flags
				     , int sessid
				     , int length)
	throws IOException
    {
	ensureCapacity(4);
	int word = (flags | ((sessid & 0xff) << 18) | length);
	if ( debug ) 
	    System.out.println("sending h="+Integer.toString(word, 16));
	encodeWord(word);
    }

    /**
     * Encode a big message.
     * <p>The caller is responsible to synchronize access to that method.
     * @param flags The header flags.
     * @param session The session identifier.
     * @param protocol The protocol identifier.
     * @param len The message length.
     */

    private final void encodeLongMessage (int flags
					 , int sessid
					 , int protocol
					 , int length)
	throws IOException
    {
	ensureCapacity(8);
	int word = (flags | ((sessid & 0xff) << 18) | protocol);
	if ( debug )
	    System.out.println("sending h="+Integer.toString(word, 16)
			       +", l="+Integer.toString(length, 16));
	encodeWord(word);
	encodeWord(length);
    }

    /**
     * Emit the given chunk of data.
     * <p>The caller is responsible to synchronize access to that method.
     * <p>The caller is reponsible for having emitted the right header
     * before actually emitting that data.
     */

    private final void emitData(byte data[], int off, int len) 
	throws IOException
    {
	if ( len <= 0 )
	    return;
	if (ensureCapacity(len)) {
	    // Just add to buffer:
	    System.arraycopy(data, off, buffer, bufptr+buflen, len);
	    buflen += len;
	} else {
	    // Write through:
	    flush();
	    out.write(data, off, len);
	}
    }

    /**
     * Emit the given String.
     * <p>The caller is responsible to synchronize access to that method.
     * <p>The caller is reponsible for having emitted the right header
     * @param str The String to be emitted.
     * @param len Length of the String (or <strong>-1</strong> if not computed
     * yet).
     */

    private final void emitData(String str, int len) 
	throws IOException
    {
	if ( len < 0 )
	    len = str.length();
	if ( ! ensureCapacity(len)) 
	    // FIXME
	    throw new RuntimeException("String to big to hold in buffer !");
	str.getBytes(0, len, buffer, bufptr+buflen);
	buflen += len;
    }

    /**
     * Emit the given integer array as a short array (little endian).
     * <p>The caller is responsible to synchronize access to that method.
     * <p>The caller is reponsible for having emitted the right header
     * @param a The array of int to be encoded as an array of shorts.
     */

    private final void emitShortArray(int a[]) 
	throws IOException
    {
	if ( ! ensureCapacity(a.length << 1) )
	    // FIXME
	    throw new RuntimeException("Array to bug to hold in buffer.");
	for (int i = 0 ; i < a.length ; i++)
	    encodeShort((short) (a[i] & 0xffff));
    }

    /**
     * Shutdown the writer.
     */

    protected synchronized void shutdown() {
	buffer = null;
    }

    /**
     * Flush current output buffer.
     */

    protected synchronized void flush() 
	throws IOException
    {
	if ( buflen > 0 ) {
	    out.write(buffer, bufptr, buflen);
	    bufptr = 0;
	    buflen = 0;
	}
    }

    /**
     * Write one message of output.
     * @param sessid The session identifier.
     * @param flags The flags of that message.
     * @param protid The protocol identifier.
     * @param b The buffer containing the data to write.
     * @param o Offset of data within above buffer.
     * @param l Length of data to be written.
     */

    protected synchronized void writeMessage (int sessid
					      , int flags
					      , int protocol
					      , byte b[], int o, int l) 
	throws IOException
    {
	encodeLongMessage(flags, sessid, protocol, l);
	emitData(b, o, l) ;
    }

    /**
     * Write one message of output.
     * @param sessid The session identifier.
     * @param flags The flags of that message.
     * @param protid The protocol identifier.
     */

    protected synchronized void writeMessage (int sessid
					      , int flags
					      , int protocol)
	throws IOException
    {
	encodeMessage(flags, sessid, protocol);
    }

    /**
     * Short cut to write data on a given session.
     * @param sessid The session to write data to.
     * @param b The buffer containing the data to be written.
     * @param o Offset of data within above buffer.
     * @param l Length of data to be written.
     */

    protected synchronized void writeData (int sessid, byte b[], int o, int l) 
	throws IOException
    {
	encodeMessage(0, sessid, l);
	if ( l > 0 ) {
	    // Emit raw data first:
	    emitData(b, o, l);
	    // Emit padding bytes as needed:
	    int padlen = ((l & 0x3) != 0 ) ? (4 - (l & 0x3)) : 0 ;
	    if (padlen != 0)
		emitData(padbytes, 0, padlen);
	}
    }

    protected void ctrlDefineString(int stackid, String id) 
	throws IOException
    {
	int word = ((MUX.LONG_LENGTH | MUX.CONTROL)	// flags
		    | (MUX.CTRL_DEFINE_STRING << 26)	// opcode
		    | (stackid & MUX.LENGTH));		// stack identifier
	int len  = id.length();
	synchronized(this) {
	    encodeWord(word);
	    encodeWord(len);
	    emitData(id, len);
	}
    }

    protected void ctrlDefineStack(int id, int stack[])
	throws IOException
    {
	int word = ((MUX.LONG_LENGTH | MUX.CONTROL)	// flags
		    | (MUX.CTRL_DEFINE_STACK << 26)	// opcode
		    | (id & MUX.LENGTH));
	int len  = (stack.length << 1);
	synchronized(this) {
	    encodeWord(word);
	    encodeWord(len);
	    emitShortArray(stack);
	}
    }

    protected void ctrlMuxControl(int sessid, int fragsz) 
	throws IOException
    {
	int word = ((MUX.LONG_LENGTH | MUX.CONTROL)	// flags
		    | (MUX.CTRL_MUX_CONTROL << 26)	// opcode
		    | (sessid << 18)			// session id
		    | (fragsz & MUX.LENGTH));		// frag size
	synchronized(this) {
	    encodeWord(word);
	    encodeWord(0);
	}
    }

    protected void ctrlSendCredit(int sessid, int credit) 
	throws IOException
    {
	int word = ((MUX.LONG_LENGTH | MUX.CONTROL)	// flags
		    | (MUX.CTRL_SEND_CREDIT << 26)	// opcode
		    | (sessid << 18));			// session id
	synchronized(this) {
	    encodeWord(word);
	    encodeWord(credit);
	}
    }

    protected synchronized boolean needsFlush() {
	return buflen > 0;
    }

    MuxWriter(MuxStream stream, OutputStream out) 
	throws IOException
    {
	this.stream = stream;
	this.out    = out;
	this.buffer = new byte[MUX.WRITER_BUFFER_SIZE];
	this.bufptr = 0;
	this.buflen = 0;
    }

}

// MuxReader.java
// $Id: MuxReader.java,v 1.1 2010/06/15 12:26:33 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mux;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

class MuxMessage {
    int flags  = -1;		// set by parseMuxWord0
    int sessid = -1;		// set by parseMuxWord0
    int len    = -1;		// set by parseMuxWord0
    int llen   = -1;		// set by parseMuxWord1
    int pad    = -1;		// set by setup{Long|Control}Message
    int bytes  = -1;		// set by setup{Long|Control}Message
    int hsize  = -1;		// set by setup{Long|Control}Message
    boolean isctrl = false;	// set by setupControlMessage
    int ctrlop = -1;		// set by setupControlMessage
    MuxMessage() {
    }
}

class MuxReader extends Thread {
    private static final boolean debug = true;

    /**
     * The MuxStream we are reading data for.
     */
    MuxStream stream = null;
    /**
     * Quick access to our MuxStream input stream.
     */
    InputStream in = null;

    /**
     * Parsed message.
     */
    MuxMessage msg = null;
    /**
     * Lookeahead message (when nextAvailable set to <strong>true</strong>.
     */
    MuxMessage nmsg = null;
    /**
     * Current message - flags.
     */
    protected int msgflags = -1;
    /**
     * Current message - session id.
     */
    protected int msgsessid = -1;
    /**
     * Current message - Message length.
     */
    protected int msglen = -1;
    /**
     * Current message - Message long length.
     */
    protected int msgllen = -1;
    /**
     * Current message - padding bytes to skip.
     */
    protected int msgpad = -1;
    /**
     * Current message - Message content size.
     */
    protected int msgbytes = -1;
    /**
     * Current message - Is that a control message.
     * If this is a control message, then msgctrlop is set properly.
     */
    protected boolean msgisctrl = false;
    /**
     * Current message - If is control, control op code.
     */
    protected int msgctrlop = -1;
    /**
     * Were we able to lookahed on next message ?
     * When this variable is set to <strong>true</strong>, all <em>nmsg</em>
     * variables contains the parsed next message.
     */
    protected boolean nextAvailable = false;
    /**
     * Current message - MuxSession to dispatch to.
     */
    protected MuxSession msgsess = null;
    /**
     * Input buffer.
     */
    protected byte buffer[] = null;
    /**
     * Half the buffer size (precomputed once and for all).
     */
    protected int midbuflength = -1;
    /**
     * input buffer pointer.
     */
    int bufptr = 0;
    /**
     * Input buffer length.
     */
    int buflen = 0;
    /**
     * Are we still alive ?
     */
    protected boolean alive = true;

    /**
     * Combine the four bytes into a word, and conform to little endian.
     * @return An integer value for the given four bytes.
     */

    private final int computeWord(byte w0, byte w1, byte w2, byte w3) {
	return (  ((((int) w3) & 0xff) << 24)
		| ((((int) w2) & 0xff) <<  16)
		| ((((int) w1) & 0xff) <<   8)
                | ((((int) w0) & 0xff)));
    }

    /**
     * Parse the first the given integer as the first 32 bits of a MUX message.
     * This method will set all variables appropriately.
     * @see #readMessage.
     * @return A boolean, <strong>true</strong> if next integer of input
     * is to be read as a long length, <strong>false</strong> otherwise.
     */

    private final boolean parseMuxWord0(byte w0, byte w1, byte w2, byte w3
					, MuxMessage into) {
	into.flags  = computeWord(w0, w1, w2, w3);
	into.sessid = (into.flags & 0x03fc0000) >> 18;
	into.len    = (into.flags & 0x3ffff);
	return (into.flags & MUX.LONG_LENGTH) != 0;
    }

    /**
     * Parse the second byte of a mux header.
     * This method will set all variables appropriately.
     * @see #readMessage
     */

    private final void parseMuxWord1(byte w0, byte w1, byte w2, byte w3
				     , MuxMessage into) {
	into.llen = computeWord(w0, w1, w2, w3);
    }

    private final boolean setupControlMessage(MuxMessage m) {
	if (m.isctrl = ((m.flags & MUX.CONTROL) == MUX.CONTROL)) {
	    int a = -1;
	    switch(m.ctrlop = ((m.flags & MUX.CTRL_CODE) >> 26)) {
	      case MUX.CTRL_DEFINE_STRING:
		  // Convert the byte data into a String:
		  m.bytes = m.llen;
		  m.pad   = ((a = (m.llen & 0x7)) != 0) ? 8-a : 0;
		  m.hsize = 8;
		  break;
	      case MUX.CTRL_DEFINE_STACK:
		  m.bytes = m.llen;
		  m.pad   = ((a = (m.llen & 0x7)) != 0) ? 8-a : 0;
		  m.hsize = 8;
		  break;
	      case MUX.CTRL_MUX_CONTROL:
		  m.bytes = 0;
		  m.pad   = 0;
		  m.hsize = 8;
		  break;
	      case MUX.CTRL_SEND_CREDIT:
		  m.bytes = 0;
		  m.pad   = 0;
		  m.hsize = 8;
		  break;
	    }
	    return true;
	} else {
	    return false;
	}
    }

    private final void setupLongMessage(MuxMessage m) {
	if ( setupControlMessage(m) )
	    return;
	int a = -1;
	m.bytes = m.llen;
	m.pad   = ((a = (m.llen & 0x7)) != 0) ? 8-a : 0;
	m.hsize = 8;
    }

    private final void setupMessage(MuxMessage m) {
	if ( setupControlMessage(m) )
	    return;
	if ((m.flags & MUX.SYN) != 0) {
	    m.bytes = 0;
	    m.pad   = 0;
	} else {
	    int a = -1;
	    m.bytes = m.len;
	    m.pad   = ((a = (m.len & 0x3)) != 0) ? 4-a : 0;
	}
	m.hsize = 4;
    }

    /**
     * Parse a full mux header into the given message repository.
     * @return Number of bytes consumed from buffer.
     */

    private final void parseMuxHeader(MuxMessage into) 
	throws IOException
    {
	while (buflen < 4)
	    fillBuffer();
	boolean isLong = parseMuxWord0(buffer[bufptr]
				       , buffer[bufptr+1]
				       , buffer[bufptr+2]
				       , buffer[bufptr+3]
				       , into);
	if ( isLong ) {
	    while (buflen < 4)
		fillBuffer();
	    parseMuxWord1(buffer[bufptr+4]
			  , buffer[bufptr+5]
			  , buffer[bufptr+6]
			  , buffer[bufptr+7]
			  , into);
	    setupLongMessage(into);
	} else {
	    setupMessage(into);
	}
    }

    private final boolean parseMuxHeaderAhead(int ptr, int avail
					      , MuxMessage into) {
	int     a      = -1;

	if ( avail < 4 )
	    return false;
	boolean isLong = parseMuxWord0(buffer[ptr]
				       , buffer[ptr+1]
				       , buffer[ptr+2]
				       , buffer[ptr+3]
				       , into);
	if ( isLong ) {
	    if ( avail < 8 )
		return false;
	    parseMuxWord1(buffer[ptr+4]
			  , buffer[ptr+5]
			  , buffer[ptr+6]
			  , buffer[ptr+7]
			  , into);
	    setupLongMessage(into);
	} else {
	    setupMessage(into);
	}
	return true;
    }

    private final void setCurrent(MuxMessage m) {
	msgflags      = m.flags;
	msgsessid     = m.sessid;
	msglen        = m.len;
	msgllen       = m.llen;
	msgpad        = m.pad;
	msgbytes      = m.bytes;
	msgisctrl     = m.isctrl;
	msgctrlop     = m.ctrlop;
    }

    /**
     * Fill in the read buffer.
     */

    private final void fillBuffer()
	throws IOException
    {
	// Rotate the buffer if needed:
	if ( buflen == 0 ) {
	    bufptr = 0;
	} else if (bufptr > midbuflength) {
	    System.arraycopy(buffer, bufptr, buffer, 0, buflen);
	    bufptr = 0;
	}
	// No more data available, reading the stream is required
	int ptr = bufptr+buflen;
	int got = in.read(buffer, ptr, buffer.length-ptr);
	if ( got > 0 ) {
	    buflen += got;
	    if ( debug )
		System.out.println("MuxReader.fillBuffer: "+buflen+" bytes.");
	} else if ( got < 0 ) {
	    // The socket has been closed, notify the session to shutdown:
	    stream.error(this, "Gracefull close.");
	    // Fake exception to get back to reader's main loop:
	    throw new EOFException("Gracefull close.");
	}
	if ( debug )
	    System.out.println("MuxReader: got "+got+" bytes.");
    }

    /**
     * Read next available message from the stream input stream.
     * This method fills in the following variable:
     * <dl>
     * <dt>msgflags<dd>An integer describing the MUX flags for current message.
     * <dt>msgsessid<dd>The session that is to receive the message.
     * <dt>msglen<dd>The message length (or protocol id).
     * <dt>msgllen<dd>The long message length, when the flags requires
     * it.
     * <dt>msgbytes<dd>The real length of the message body (using either the 
     * long header format or the short one).
     * <dt>msgpad<dd>Number of padding bytes to skip by the end of that 
     * message.
     * </dl>
     * It is up to the caller to read the reminaing bytes of the message
     * before calling this method again.
     */

    protected void readMessage() 
	throws IOException
    {
	int a = -1;

	// Read in current message:
	if ( nextAvailable ) {
	    // Next message was read ahead, use previous result:
	    setCurrent(nmsg);
	    bufptr += nmsg.hsize;
	    buflen -= nmsg.hsize;
	    nextAvailable = false;
	} else {
	    // Parse current message:
	    parseMuxHeader(msg);
	    bufptr += msg.hsize;
	    buflen -= msg.hsize;
	    setCurrent(msg);
	}
	// Try to read ahead next message:
	int nbufptr = bufptr+msgbytes+msgpad;
	if (nextAvailable = (nbufptr < bufptr + buflen)) {
	    nextAvailable = parseMuxHeaderAhead(nbufptr
						, bufptr+buflen-nbufptr
						, nmsg);
	}
	if ( debug ) 
	    System.out.println("[readMessage] bufptr="+bufptr+", f="
                               + Integer.toString(msgflags, 16)
			       + ", i="+msgsessid
			       + ", l="+msglen
			       + ", s="+msgbytes);
    }

    /**
     * Read message body.
     * It is up to the caller of that routine to consume exactly the number
     * of returned bytes from this reader's input buffer.
     * @return A boolean, <strong>true</strong> if more bytes are available for
     * that message, <strong>false</string>otherwise.
     */

    private final int readMessageBody()
	throws IOException
    {
	if ( debug ) 
	    System.out.println("readMessageBody: "+msgbytes+" bytes avail.");
	if ( msgbytes > 0 ) {
	    if ( buflen <= 0 ) 
		fillBuffer();
	    if (msgbytes > buflen) {
		msgbytes -= buflen;
		return buflen;
	    } else {
		int ret  = msgbytes;
		msgbytes = 0;
		return ret;
	    }
	} else if ( msgpad > 0 ) {
	    // Skip padding bytes:
	    while (buflen <= msgpad)
		fillBuffer();
	    bufptr += msgpad;
	    buflen -= msgpad;
	}
	return 0;
    }

    /**
     * Decode the current message body as a String.
     * @return A String instance.
     */

    protected String msgToString() 
	throws IOException
    {
	if (buffer.length < msglen)
	    throw new RuntimeException("String doesn't hold in buffer !");
	while (buflen < msglen)
	    fillBuffer();
	String s = new String(buffer, 0, bufptr, msglen);
	bufptr += msglen;
	buflen -= msglen;
	return s;
    }

    /**
     * Decode the current message body as a shot array.
     * @return A short array instance.
     */

    protected int[] msgShortArrayToIntArray() 
	throws IOException
    {
	if (buffer.length < msglen)
	    throw new RuntimeException("ShortArray doesn't hold in buffer !");
	while (buflen < msglen)
	    fillBuffer();
	int a[] = new int[msglen >> 1];
	for (int i = 0 ; i < a.length ; i++) {
	    a[i] = (buffer[bufptr] | (buffer[bufptr+1] << 8)) & 0xffff;
	    bufptr += 2;
	}
	buflen -= msglen;
	return a;
    }

    /**
     * Handle (decode and dispatch) control messages.
     * This method gets called by the dispatcher whenever a MUX header
     * with the control bit set is the current message to dispatch.
     */

    protected void handleControlMessage() 
	throws IOException
    {
	switch(msgctrlop) {
	  case MUX.CTRL_DEFINE_STRING:
	      // Convert the byte data into a String:
	      String str = msgToString();
	      stream.ctrlDefineString(msglen, str);
	      break;
	  case MUX.CTRL_DEFINE_STACK:
	      int ids[] = msgShortArrayToIntArray();
	      stream.ctrlDefineStack(msgsessid, ids);
	      break;
	  case MUX.CTRL_MUX_CONTROL:
	      stream.ctrlMuxControl(msgsessid, msglen);
	      break;
	  case MUX.CTRL_SEND_CREDIT:
	      stream.ctrlSendCredit(msgsessid, msgllen);
	      break;
	}
    }

    /**
     * Dispatch the current message to the appropriate handler.
     */

    protected void dispatchMessage() 
	throws IOException
    {
	msgsess = stream.lookupSession(msgflags
				       , msgsessid
				       , msglen
				       , msgllen);
	if ( msgsess != null ) {
	    if ( msgisctrl ) {
		// Control message requires special actions:
		handleControlMessage();
	    } else {
		// Dispatch that message body to the given session:
		boolean noflush = (nextAvailable && (nmsg.sessid==msgsessid));
		int     got     = 0;
		while ((got = readMessageBody()) > 0) {
		    msgsess.pushInput(buffer, bufptr, got, noflush);
		    bufptr += got;
		    buflen -= got;
		}
		// Notify the session of any fancy flags:
		if ((msgflags & MUX.FIN) == MUX.FIN) 
		    msgsess.notifyFIN();
		if ((msgflags & MUX.RST) == MUX.RST)
		    msgsess.notifyRST();
		if ((msgflags & MUX.PUSH) == MUX.PUSH)
		    msgsess.notifyPUSH();
	    }
	} else {
	    // Discard that message's data:
	    int got = -1;
	    while ((got = readMessageBody()) > 0) {
		bufptr += got;
		buflen -= got;
	    }
	}
    }

    /**
     * Shutdown the reader for this stream.
     */

    protected synchronized void shutdown() {
	alive  = false;
	buffer = null;
	stop();
    }

    /**
     * Runfor ever, reading available input.
     * Unfortunatelly the Java IO models <em>requires</em> that you consume
     * a full thread, just to read the input stream.
     */

    public void run() {
	try {
	    while ( alive ) {
		readMessage();
		dispatchMessage();
		// Clear up current message descriptor:
		msgflags  = 0;
		msgsessid = 0;
		msglen    = 0;
		msgllen   = 0;
		msgpad    = 0;
		msgisctrl = false;
		msgctrlop = -1;
	    }
	} catch (EOFException ex) {
	    // Already handled, the stream *has* been notified.
	} catch (IOException ex) {
	    stream.error(this, ex);
	}
    }

   
    MuxReader(MuxStream stream, InputStream in) 
	throws IOException
    {
	this.stream       = stream;
	this.in           = in;
	this.buffer       = new byte[MUX.READER_BUFFER_SIZE];
	this.bufptr       = 0;
	this.buflen       = 0;
	this.midbuflength = (MUX.READER_BUFFER_SIZE >> 1);
	this.msg          = new MuxMessage();
	this.nmsg         = new MuxMessage();
	setName("MuxReader");
    }

}



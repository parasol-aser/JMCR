// MuxSession.java
// $Id: MuxSession.java,v 1.1 2010/06/15 12:26:33 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mux;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.net.InetAddress;

public class MuxSession /* extends Socket */ {
    /**
     * The default input buffer size for all sessions.
     */
    public static final int INPUT_BUFFER_SIZE = 4096;

    /**
     * The stream to which that session belongs.
     */
    protected MuxStream stream = null;
    /**
     * The session's input stream.
     */
    protected MuxInputStream in = null;
    /**
     * The session's output stream.
     */
    protected MuxOutputStream out = null;
    /**
     * This session's identifier.
     */
    protected int id = -1;
    /**
     * This session's protocol identifier.
     */
    protected int protid = -1;
    /**
     * Has this session been aborted ?
     */
    protected boolean aborted = false;
    /**
     * Has this session emitted a FIN (is it half-closed ?)
     */
    protected boolean finsent = false;

    /**
     * Push some data into that session's input stream.
     * @param data The buffer containing the data to be pushed.
     * @param off Offset of the data within above buffer.
     * @param len Length of data to be pushed.
     * @param noflush Set to <strong>true</strong> if there is already more
     * data available for that session.
     * @exception IOException If IO was interrupted.
     */

    protected final void pushInput(byte data[], int off, int len
				   , boolean noflush) 
	throws IOException
    {
	in.push(data, off, len, noflush);
    }

    /**
     * Send a FIN message on that session.
     */

    protected final void sendFIN() 
	throws IOException
    {
	if ( ! finsent ) {
	    stream.getMuxWriter().writeMessage(id, MUX.FIN, 0);
	    stream.getMuxWriter().flush();
	    finsent = true;
	}
    }

    /**
     * We have received a FIN on that session's output stream.
     * @exception IOException If some IO error occured.
     */

    protected final void notifyFIN() 
	throws IOException
    {
	in.close();
	if ( finsent )
	    shutdown();
    }

    /**
     * The other end is telling us that something is going wrong. Cleanup.
     */

    protected void notifyRST() 
	throws IOException
    {
        in.error("Broken pipe");
	out.close();
	shutdown();
    }

    protected void notifyPUSH() {
	System.out.println("MuxSession:notifyPUSH: not handled");
    }

    protected final void notifyCredit(int credit) {
	out.notifyCredit(credit);
    }

    protected final void notifyControl(int fragsz) {
	out.notifyControl(fragsz);
    }

    protected void notifyOutputClose() 
	throws IOException
    {
	stream.getMuxWriter().writeMessage(id, MUX.FIN, 0);
    }

    /**
     * Abort that session.
     * The MUX stream erred, the underlying transport streams are broken. 
     * Terminate that session, make sure any further action on it will trigger
     * an IO error.
     */

    protected synchronized void abort() {
	aborted = true;
	try {
	    shutdown();
	} catch (Exception ex) {
	}
    }

    /**
     * Shutdown that session gracefully.
     */

    public void shutdown() 
	throws IOException
    {
	// Close both streams:
	try {
	    in.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	} 
	try {
	    out.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	// Unregister the session
	stream.unregisterSession(this);
    }

    /**
     * Get the Mux stream to which that session is attached.
     * @return A MuxStream instance.
     */

    protected final MuxStream getMuxStream() {
	return stream;
    }

    /**
     * Get this session's input stream buffer size.
     * @return The standard buffer size for that session input stream.
     */

    protected int getInputBufferSize() {
	return INPUT_BUFFER_SIZE;
    }

    /**
     * Get the other end's IP address.
     * @return An InetAddress instance.
     */

    public InetAddress getInetAddress() {
	return getMuxStream().getInetAddress();
    }

    /**
     * Get this session identifier.
     * @return An integer identifier for that session.
     */

    public final int getIdentifier() {
	return id;
    }

    /**
     * Get this session protocol identifier.
     * @return An integer identifying the protocol runnin on that session.
     */

    public final int getProtocolIdentifier() {
	return protid;
    }

    /**
     * Get this session's input stream.
     * @return An InputStream instance.
     */

    public synchronized InputStream getInputStream()
	throws IOException
    {
	if ( aborted )
	    throw new IOException("Aborted mux session");
	return in;
    }

    /**
     * Get this session's output stream.
     * @return An OutputStream instance.
     */

    public synchronized OutputStream getOutputStream()
	throws IOException
    {
	if ( aborted )
	    throw new IOException("Aborted mux session");
	return out;
    }

    protected MuxSession(MuxStream stream, int id, int protid) {
	// Initialize state:
	this.stream = stream;
	this.id     = id;
	this.protid = protid;
	// Create input and output streams:
	this.in  = new MuxInputStream(this);
	this.out = new MuxOutputStream(this);
    }

						

}

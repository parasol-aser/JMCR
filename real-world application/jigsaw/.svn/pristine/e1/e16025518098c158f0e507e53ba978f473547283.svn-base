// MuxStream.java
// $Id: MuxStream.java,v 1.1 2010/06/15 12:26:33 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mux;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.net.InetAddress;
import java.net.Socket;

public class MuxStream {
    /**
     * That stream accept handler.
     */
    protected MuxStreamHandler handler = null;
    /**
     * This stream reader.
     */
    protected MuxReader reader = null;
    /**
     * This stream writer.
     */
    protected MuxWriter writer = null;
    /**
     * Currently defined sessions.
     */
    protected MuxSession sessions[] = null;
    /**
     * Is this the server side of the MUX channel ?
     */
    protected boolean server = false;
    /**
     * Inet address of the other end's connection (maybe <strong>null</strong>)
     */
    protected InetAddress inetaddr = null;
    /**
     * The raw input stream.
     */
    protected InputStream in = null;
    /**
     * The raw output stream.
     */
    protected OutputStream out = null;
    /**
     * Is this muxed stream still alive ?
     */
    protected boolean alive = true;

    // Assumes sessions array is of correct size, and checks have been done

    private synchronized MuxSession createSession(int sessid, int protid)
	throws IOException
    {
	MuxSession session = sessions[sessid];
	if ( session == null ) {
	    session = new MuxSession(this, sessid, protid);
	    sessions[sessid] = session;
	} else {
	    System.out.println("MuxStream:createSession: already existing !");
	}
	return session;
    }

    // Are we willing to accept that new session ?
    // Because we need to accept it internally, we always return something
    // It is up to the caller to make sure flags has SYN set
    // NOTE that the calls to the handler don't lock that object (a feature)

    private MuxSession acceptSession(int flags
				       , int sessid
				       , int protid)
	throws IOException
    {
	if (server & ((sessid & 1) == 0)) 
	    throw new IOException("MUX: Invalid even session id "+sessid);
	// Query the session handler about that new session:
	MuxSession session = null;
	if ((handler != null) && handler.acceptSession(this, sessid, protid)) {
	    // Session accepted, setup handler:
	    session = createSession(sessid, protid);
	    handler.notifySession(session);
	} else {
	    // Session rejected, emit a RST:
	    session = null;
System.out.println(this+": RST (accept) session "+sessid);
	    writer.writeMessage(sessid, MUX.RST, 0);
	    writer.flush();
	}
	return session;
    }

    private final synchronized MuxSession allocateSession(int protid) 
	throws IOException
    {
	// Available sessions ?
	int i = (server ? 2 : 3);
	for ( ; i < sessions.length; i += 2) {
	    if ( sessions[i] == null ) {
		sessions[i] = new MuxSession(this, i, protid);
		return sessions[i];
	    }
	}
	// Create a new session:
	MuxSession session = checkSession(i);
	if ( session == null )
	    session = new MuxSession(this, i, protid);
	sessions[i] = session;
	return session;
    }

    private final synchronized MuxSession checkSession(int sessid) 
	throws IOException
    {
	// Check protocol validity:
	if ( sessid >= MUX.MAX_SESSION ) 
	    throw new IOException("MUX: Invalid session id "+sessid);
	// Get or create the appropriate session:
	if ( sessid >= sessions.length ) {
	    MuxSession ns[] = new MuxSession[sessid+MUX.SESSIONS_INCR];
	    System.arraycopy(sessions, 0, ns, 0, sessions.length);
	    sessions = ns;
	} 
	return sessions[sessid];
    }

    /**
     * This stream is dying, clean it up.
     * It is up to the caller to make sure all existing sessions have been
     * terminated (gracefully or not).
     * <p>This will shutdown all realted threads, and close the transport 
     * streams.
     */

    private synchronized void cleanup() {
	alive = false;
	// Cleanup the reader and writer objects:
	reader.shutdown();
	writer.shutdown();
	reader = null;
	writer = null;
	// Close streams:
	try {
	    in.close();
	    out.close();
	} catch (IOException ex) {
	}
	in  = null;
	out = null;
    }

    /**
     * Get this stream MuxWriter object.
     * @return A MuxWriter instance.
     */

    protected final MuxWriter getMuxWriter() {
	return writer;
    }

    /**
     * A severe (fatal for that connection) errror has occured. Cleanup.
     * @param obj The object that has generated the error.
     * @param ex The exception that triggered the error (or <strong>null
     * </strong> null if this was a logical error).
     */

    protected void error(Object obj, Exception ex) {
	System.out.println("*** Fatal error on "+this);
	ex.printStackTrace();
	System.out.println("No recovery !");
	System.exit(1);
    }

    /**
     * A soft error has occured (eg socket close), Cleanup.
     * @param obj The object that has detected the soft error.
     * @param msg An associated String message.
     */

    protected synchronized void error(Object obj, String msg) {
	// Is there any pending session ?
	boolean problems = false;
	synchronized(this) {
	    for (int i = 0 ; i < sessions.length ; i++) {
		if ( sessions[i] != null ) 
		    sessions[i].abort();
	    }
	}
	// If no problems, close socket, we're done:
	cleanup();
    }

    /**
     * Handle the given DefineString control message.
     * @param strid The identifier for that String in the futur.
     * @param str This String being defined.
     */

    protected void ctrlDefineString(int strid, String str) {
    }

    /**
     * Handle the given DefineStack control message.
     * @param id The identifier for that stack in the future.
     * @param stack The stack description (as an array of shorts).
     */

    protected void ctrlDefineStack(int id, int stack[]) 
	throws IOException
    {
    }

    /**
     * Handle the given MuxControl control message.
     * @param sessid The session to which that message applies.
     * @param fragsz The max allowed fragment size on that session.
     */

    protected void ctrlMuxControl(int sessid, int fragsz)
	throws IOException
    {
	MuxSession session = lookupSession(sessid, true);
	session.notifyControl(fragsz);
    }

    /**
     * Handle the given SendCredit control message.
     * @param sessid The session to which that message applies.
     * @param credit The allowed credits.
     */

    protected void ctrlSendCredit(int sessid, int credit)
	throws IOException
    {
	MuxSession session = lookupSession(sessid, true);
	session.notifyCredit(credit);
    }

    /**
     * Handle that new incomming message.
     * This method is called by the reader of that session, to dispatch
     * the message currently being read.
     * @return A MuxSession instance to dispatch that message to, or
     * <strong>null</strong> otherwise (ie a new session was rejected, etc).
     * In that last case, it is up to the reader of that session to discard 
     * any pending data.
     */

    protected MuxSession lookupSession(int flags
				       , int sessid
				       , int length
				       , int llength) 
	throws IOException
    {
	MuxSession session = checkSession(sessid);
	if (session == null) {
	    if ((flags & MUX.SYN) != 0) {
		// Length really means protid in that case:
		session = acceptSession(flags, sessid, length);
	    } else if ((flags & MUX.FIN) != MUX.FIN) {
		// We don't know about that session, emit some reset:
		System.out.println(this+": RST (lookup) session "+sessid);
		if ((flags & MUX.RST) != MUX.RST) {
		    // Above test breaks a nasty loop !
		    writer.writeMessage(sessid, MUX.RST, 0);
		}
	    }
	}
	return session;
    }

    /**
     * Lookup for an already existing session having the given identifier.
     * @param sessid The identifier of the session to look for.
     * @param check Is <strong>null</strong> a valid answer, if set and
     * the requested session doesn't exist, a runtime exception is thrown.
     * @return A MuxSession instance, or <strong>null</strong> if check is
     * <strong>false</strong> and no session was found.
     */

    protected synchronized MuxSession lookupSession(int sessid
						    , boolean check) {
	if ( sessid < sessions.length ) {
	    MuxSession session = sessions[sessid];
	    if ( session != null )
		return session;
	}
	if ( check ) {
	    throw new RuntimeException("MuxStream:lookupSession: "
				       + " invalid session id "
				       + sessid + ".");
	}
	return null;
    }

    /**
     * Unregiter the given session, it has been closed.
     * @param session The session to unregister.
     */

    protected synchronized void unregisterSession(MuxSession session) {
	sessions[session.getIdentifier()] = null;
    }

    /**
     * Create a new MUX session, by connecting to the other end.
     * @param protid The protocol that is going to be spoken on that new 
     * session.
     * @return A connected MuxSession.
     * @exception IOException If the connection couldn't be set up properly.
     */

    public MuxSession connect(int protid) 
	throws IOException
    {
	// Is this stream still alive ?
	synchronized(this) {
	    if ( ! alive )
		throw new IOException("Broken mux stream");
	}
	// Allocate a new session identifier:
	MuxSession session = allocateSession(protid);
	// If SYN with long-length not set accepted, uncomment following:
	// writer.writeMessage(session.getIdentifier(), MUX.SYN, protid);
	writer.writeMessage(session.getIdentifier()
			    , MUX.SYN
			    , protid
			    , null, 0, 0);
	return session;
    }

    /**
     * Get the InetAddress associated with that MUX stream, if any.
     * MUX streams can run on any kind of Input/Output streams. This method
     * will only return a non-null instance when possible.
     * @return An InetAddress instance, or <strong>null</strong> if not
     * available.
     */

    public InetAddress getInetAddress() {
	return inetaddr;
    }

    /**
     * Shutdown this stream, and associated sessions gracefully.
     * @param force If <strong>true</strong> abort all existing sessions, and
     * close the muxed streams physically. Otherwise, shutdown the muxed stream
     * gracefully only if no more sessions are running.
     * @return A boolean, <strong>true</strong> if shutdown was performed,
     * <strong>false</strong> if it was not performed because <em>force</em>
     * was <strong>false</strong> and some sessions were still running.
     * @exception IOException If some IO error occured.
     */

    public synchronized boolean shutdown(boolean force) 
	throws IOException
    {
	// Has this stream already been killed ?
	if ( ! alive )
	    return true;
	boolean terminate = true;
	// Check sessions status:
	if ( force ) {
	    for (int i = 0 ; i < sessions.length ; i++) {
		MuxSession s = sessions[i];
		if ( s != null )
		    s.abort();
	    }
	} else {
	    for (int i = 0 ; i < sessions.length ; i++) {
		if ( sessions[i] != null ) {
		    terminate = false;
		    break;
		}
	    }
	}
	if ( terminate )  
	    cleanup();
	return terminate;
    }
	
    public MuxStream(boolean server
		     , MuxStreamHandler handler
		     , InputStream in
		     , OutputStream out) 
	throws IOException
    {
	this.server   = server;
	this.handler  = handler;
	this.in       = in;
	this.out      = out;
	this.reader   = new MuxReader(this, in);
	this.writer   = new MuxWriter(this, out);
	this.sessions = new MuxSession[8];
	this.reader.start();
    }

    public MuxStream(boolean server, MuxStreamHandler handler, Socket socket) 
	throws IOException
    {
	this(server
	     , handler
	     , socket.getInputStream()
	     , socket.getOutputStream());
	this.inetaddr = socket.getInetAddress();
    }

}

// SampleMuxHandler.java
// $Id: SampleMuxHandler.java,v 1.2 2010/06/15 17:53:12 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mux;

import java.io.IOException;
import java.io.PrintStream;

import java.util.Hashtable;

public class SampleMuxHandler implements MuxStreamHandler {
    /**
     * Debug state.
     */
    private static final boolean debug = false;
    /**
     * Well known protocols - The echo protocol identifier.
     */
    public static final int ECHO = 7;
    /**
     * Well known protocols - The echo protocol identifier.
     */
    public static final int DISCARD = 9;

    /**
     * The sigle instance of that class.
     */
    protected static SampleMuxHandler sample = null;
    /**
     * The hashtable of accepted protocols.
     */
    protected Hashtable protocols = null;

    /**
     * Log an error.
     * @param msg The message to log.
     */

    protected void log(String msg) {
	System.out.println("[" + getClass().getName() + "]: "+msg);
    }

    /**
     * Get an instance of that sample mux stream handler.
     * @return A MuxStreamHandler conformant instance.
     */

    public static synchronized MuxStreamHandler getStreamHandler() {
	// Of course, we should go through a factory, etc as usual:
	if ( sample == null ) 
	    sample = new SampleMuxHandler();
	return sample;
    }

    /**
     * Are we willing to speak the given protocol on the given session.
     * @param stream The stream that received the new session.
     * @param sessid The proposed session identifier.
     * @param protid The protocol to be spoken on that session.
     * @return A bolean, <strong>true</strong> if the session is accepted,
     * <strong>false</strong> otherwise.
     */

    public boolean acceptSession(MuxStream stream, int sessid, int protid) {
	Object o = protocols.get(new Integer(protid));
	// Reject unknown protocols straight:
	if ( o == null ) {
	    if ( debug )
		System.out.println("Rejecting "+protid+" on "+sessid+".");
	    return false;
	} else {
	    if ( debug )
		System.out.println("Accepting "+protid+" on "+sessid+".");
	    return true;
	} 
    }

    /**
     * Setup the appropriate protocol handler for that accepted session.
     * @param session The newly accepted session.
     */

    public void notifySession(MuxSession session) {
	int     protid  = session.getProtocolIdentifier();
	Integer iprotid = new Integer(protid);
	Object  o       = protocols.get(iprotid);
	// This should not happen (except for some race conditions):
	if ( o == null ) {
	    log("SampleMuxHandler: unknown protocol "+protid);
	    try {
		session.shutdown();
	    } catch (Exception ex) {
	    }
	}
	// Find (or instantiate) appropriate protocol handler:
	MuxProtocolHandler handler = null;
	if ( o instanceof String ) {
	    String strcls = (String) o;
	    try {
		Class c = Class.forName(strcls);
		handler = (MuxProtocolHandler) c.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    } catch (Exception ex) {
		log("Instantiating handler for " + protid
		    + " of class \"" + strcls + "\" failed.");
		ex.printStackTrace();
		try {
		    session.shutdown();
		} catch (IOException exex) {
		}
	    }
	} else if ( o instanceof MuxProtocolHandler ) {
	    handler = (MuxProtocolHandler) o;
	} else {
	    log("SampleMuxHandler: unknown protocol "+protid);
	    try {
		session.shutdown();
	    } catch (Exception ex) {
	    }
	}
	// We now have a handler, launch:
	try {
	    handler.initialize(session);
	} catch (Exception ex) {
	    log("Launching handler for " + protid
		+ " of class \"" + handler.getClass() + "\" failed.");
	    ex.printStackTrace();
	    try {
		session.shutdown();
	    } catch (IOException exex) {
	    }
	}
    }
		
    /**
     * Register a protocol handler for the given protocol identifier.
     * This method register a class to handle all new connections for the 
     * given protocol identifier: each new connection will result in a new 
     * instance of the given class being created (the easy, but slow way).
     * @param protid The protocol identifier.
     * @param handler The name of the class to instantiate in order
     * to get a suitable handler for that protocol.
     * @see MuxProtocolHandler
     */

    public void registerHandler(int protid, String strhandler) {
	protocols.put(new Integer(protid), strhandler);
    }

    /**
     * Register an instantiated protocol handler for the given protocol id.
     * This other method of registering protocol handler is faster then
     * the previous one: it allows you to spare the instantiation of a protocol
     * handler on each new sessions.
     * <p>The given handler will be invoked for all new sessions willing to
     * speak the advertized protocol.
     * @param protid The protocol identifier.
     * @param handler The instantiated protocol handler.
     */

    public void registerHandler(int protid, MuxProtocolHandler handler) {
	protocols.put(new Integer(protid), handler);
    }

    /**
     * Unregister any handler for that protocol.
     * @param protid The identifier of the protocol to unregister.
     */

    public void unregisterHandler(int protid) {
	protocols.remove(new Integer(protid));
    }

    /**
     * Register default protocol handlers for that stream handler.
     * This is the right method to override in order to either prevent
     * well known protocols from being registered, or add new protocol
     * handlers.
     * <p>Default protocols registered by this class are:
     * <dl>
     * <dt>echo<dd>The echo protocol.
     * <dt>discard<dd>The discard protocol.
     * </dt>
     */

    public void registerDefaultHandlers() {
	registerHandler(ECHO   , "org.w3c.www.mux.handlers.Echo");
	registerHandler(DISCARD, "org.w3c.www.mux.handlers.Discard");
    }

    public SampleMuxHandler() {
	super();
	// Initialize instance variables:
	this.protocols = new Hashtable();
	// Register default protocols:
	registerDefaultHandlers();
    }

}



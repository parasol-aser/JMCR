// MuxClient.java
// $Id: MuxClient.java,v 1.1 2010/06/15 12:24:58 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http.mux ;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.net.InetAddress;

import org.w3c.www.mux.MuxSession;

import org.w3c.www.http.HttpMessage;

import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;

public class MuxClient extends Client implements Runnable {
    /**
     * The InetAddress of the session we are currently handling.
     */
    private InetAddress addr   = null;
    /**
     * The MuxSession we are currently handling.
     */
    private MuxSession session   = null;
    /**
     * The MuxHttpHandler that createed us.
     */
    private MuxHttpHandler handler = null;
    /**
     * MuxHttpHandler maintained klist of clients.
     */
    MuxClient next = null;
    /**
     * The thread powering that client connection.
     */
    protected Thread thread = null;

    protected boolean tryKeepConnection (Request request, Reply reply) {
	reply.addConnection("close");
	return false;
    }

    /**
     * Client implementation - Get the IP address of this client.
     * @return An InetAddress instance, or <strong>null</strong> if the
     * client is not currently running.
     */

    public InetAddress getInetAddress() {
	return addr;
    }

    /**
     * Run HTTP on the newly created mux session.
     */

    public void run() {
	try {
	    startConnection(session.getInputStream()
			    , (new DataOutputStream
			       (new BufferedOutputStream
				(session.getOutputStream()))));
	} catch (Exception ex) {
	    System.out.println(this+": erred !");
	    ex.printStackTrace();
	}
    }
	
    /**
     * Client implementation - The current connection is now idle.
     * We always close the mux session at that time, since creating a new
     * mux session has nearly no overhead.
     */

    protected boolean idleConnection() {
	return true;
    }

    /**
     * Client implementation - The current connection is now in use.
     * Nothing special done.
     */

    protected void usedConnection() {
	return;
    }

    /**
     * Client implementation - The current connection was terminated.
     * We make sure the underlying mux session is closed properly, and 
     * terminate the underlying thread.
     */

    protected void stopConnection() {
	// Terminate the session properly:
	try {
	    session.shutdown();
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
	session = null;
	// Mark that MuxClient instance ready for re-used.
	handler.markIdle(this);
    }

    /**
     * Bind that client to the given connection.
     * @param session The mux session to handle.
     */

    protected void bind(MuxSession session) 
	throws IOException
    {
	this.session = session;
	this.addr    = session.getInetAddress();
    }

    /**
     * Get the thread powering that client.
     * @return A Thread instance, or <strong>null</strong>.
     */

    protected Thread getThread() {
	return thread;
    }

    MuxClient(httpd server, MuxHttpHandler handler, int identifier) {
	initialize(server, identifier);
	this.handler = handler;
    }

}

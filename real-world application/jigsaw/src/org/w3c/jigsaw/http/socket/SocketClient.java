// SocketClient.java
// $Id: SocketClient.java,v 1.1 2010/06/15 12:26:10 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http.socket ;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.ClientException;
import org.w3c.jigsaw.http.httpd;

import org.w3c.tools.resources.ServerInterface;

/**
 * This class implements the object that handles client connections. 
 * One such object exists per open connections at any given time.
 * <p>The basic architecture is the following: the httpd instance accepts 
 * new connections on its port. When such a connection is accepted a Client 
 * object is requested to the client pool (which can implement what ever 
 * strategy is suitable). Each request is than managed by looking up an
 *  resource and invoking the resource methods corresponding to the request.
 * @see org.w3c.jigsaw.http.httpd
 * @see org.w3c.jigsaw.http.Request
 * @see org.w3c.jigsaw.http.Reply
 */

public class SocketClient extends Client implements Runnable {
    private static final boolean trace = false;

    /**
     * The ClientFactory that created this client.
     */
    private SocketClientFactory pool = null ;
    /**
     * The socket currently handled by the client.
     */
    protected Socket socket = null ;
    /**
     * Is this client still alive ?
     */
    protected boolean alive = false;
    /**
     * The client state for this client, has managed by the SocketClientFactory
     * @see SocketClientFactory
     */
    SocketClientState state = null;
    /**
     * Number of times this client was bound to a connection.
     */
    protected int bindcount = 0;
    /**
     * The thread that we have been attached to.
     */
    protected Thread thread = null;
    /**
     * Our reusable output buffer.
     */
    protected SocketOutputBuffer bufout = null;
    /**
     * Our we idle (waiting for next request ?)
     */
    protected boolean idle = false;

    /**
     * are we done?
     */
    protected boolean done = false;

    /**
     * Print that client into a String.
     * @return A String instance.
     */

    public String toString() {
	if ( thread != null )
	    return "client-"+state.id+"("+thread.getName()+")";
	else
	    return "client-"+state.id;
    }

    /**
     * If this client is allocated a thread, join it.
     */

    public void join() {
	if (thread != null) {
	    while ( true ) {
		try {
		    thread.join();
		} catch (InterruptedException ex) {
		}
	    }
	}
    }

    /**
     * Run for our newly attached connection.
     * @return A boolean, <strong>true</strong> if the client is to be killed
     * as decided by its SocketClientFactory creator.
     */

    public void run() {
	thread = Thread.currentThread();
	if ( trace )
	    System.out.println(this+": powered by "+thread);
	try {
	    if ( bufout == null ) {
		// Make sure that buffer is a little smaller then the client
		// buffer. so writing to it will not copy it
		int bufsize = getServer().getClientBufferSize() - 1;
		bufout = new SocketOutputBuffer(socket.getOutputStream()
						, bufsize);
	    } else {
		bufout.reuse(socket.getOutputStream());
	    }
	    startConnection(new BufferedInputStream(socket.getInputStream())
			    , new DataOutputStream(bufout));
	} catch (IOException ex) {
	    if ( debug )
		ex.printStackTrace();
	} catch (ClientException ex) {
	    if ( debug )
		ex.printStackTrace();
	    // Emit some debugging traces:
	    if ( debug ) {
		if (ex.ex != null )
		    ex.ex.printStackTrace() ;
		else
		    ex.printStackTrace();
	    }
	    // If output is null, we have killed the connection...
	    if ( alive && ! idle ) {
		error("caught ClientException: [" 
                      + ex.getClass().getName()
		      + "] " + ex.getMessage()) ;
	    }
	} catch (Exception ex) {
	    if (debug) {
		System.out.println("unknown exception caught in client run");
		ex.printStackTrace();
	    }
	} finally {
	    if ( ! pool.clientConnectionFinished(this) ) {
		pool.clientFinished(this);
	    }
	    thread = null;
	}
    }

    /**
     * Client implementation - Get the IP address of this client.
     * @return An InetAddress instance, or <strong>null</strong> if the
     * client is not currently running.
     */

    public InetAddress getInetAddress () {
	return (socket != null) ? socket.getInetAddress() : null;
    }

    /**
     * Client implementation - This connection has been stopped.
     * Make sure the whole socket is closed, and be ready to handle 
     * next connection.
     */

    protected void stopConnection() {
	if ( trace )
	    System.out.println(this+": stopConnection.");
	if ( socket != null ) {
	    try {
		socket.close();
	    } catch (Exception ex) {
	    }
	    socket = null;
//	    alive = false;
//	    if (!pool.idleClientRemove(this)) {
//		pool.clientFinished(this);
//	    }
	}
    }

    /**
     * Get the thread powering that client.
     * @return A Thread instance, or <strong>null</strong>.
     */

    protected Thread getThread() {
	return thread;
    }

    /**
     * Client implementation - The current connection is idle.
     * The client is about to wait for the next request from the net, mark
     * it as idle to make it a candidate for persistent connection closing.
     * @return A boolean, if <strong>true</strong> our client factory wants
     * us to stop right now, in order to handle some other connection.
     */

    protected boolean idleConnection() {
	synchronized (state) {
	    if ( trace )
		System.out.println(this+": idleConnection.");
	    idle = true;
	    return ! pool.notifyIdle(this);
	}
    }

    /**
     * Client implementation - The current connection is in use.
     * A request is about to be processed, mark that connection as used, to
     * remove it from the idle state.
     */

    protected void usedConnection() {
	synchronized (state) {
	    if ( trace )
		System.out.println(this+": usedConnection.");
	    idle = false;
	    pool.notifyUse(this) ;
	}
    }

    /**
     * SocketClientFactory interface - Bind the socket to this client.
     * Binding a socket to a client triggers the processing of the underlying
     * connection. It is assumed that the client was ready to handle a new 
     * connection before this method was called.
     * @param socket The socket this client should now handle.
     */

    protected synchronized void bind(Socket socket) {
	done = false;
	ServerInterface server = getServer();
	if ( trace )
	    System.out.println(this+": bind.");
	this.socket = socket ;
        try {
//            socket.setSoTimeout(server.getRequestTimeOut());
	    socket.setSoTimeout(pool.timeout);
        } catch (SocketException ex) { 
	    if (trace)
		ex.printStackTrace(); 
	    server.errlog("Unable to set socket timeout!");
        } 
	this.idle   = false;
	bindcount++;
	pool.run(this);
    }

    /**
     * SocketClientFactory interface - Unbind this client.
     * This client is handling an idle connection, unbind it to make
     * it free for handling some other more buzy connection.
     */

    protected synchronized void unbind() {
	if ( trace )
	    System.out.println(this+": unbind.");
	interruptConnection(true);
    }

    /**
     * SocketClientFactory interface - Kill this client.
     * The clean way for our client factory to shut us down unconditionally.
     * This will free all resources acquired by this client, stop the current
     * connection processing if needed, and terminate the underlying thread.
     */

    protected synchronized void kill(boolean now) {
	if ( trace )
	    System.out.println(this+": kill.");
	alive = false;
	interruptConnection(now);
    }

    /**
     * Get the total number of times this client was bound to a socket.
     * @return An integer, indicatingthe number of bind calls on this client.
     */

    public final int getBindCount() {
	return bindcount;
    }

    /**
     * Create an empty client, that will be ready to work.
     * The created client will run and wait for it to be <code>bind</code>
     * to some socket before proceeding.
     * @param server The server to which this client is attached.
     * @param id The client identifier.
     * @see org.w3c.jigsaw.http.Client
     * @see org.w3c.jigsaw.http.ClientFactory
     */

    protected SocketClient(httpd server,
			   SocketClientFactory pool,
			   SocketClientState state) {
	initialize(server, state.id);
	this.socket   = null ;
	this.pool     = pool;
	this.state    = state;
	this.alive    = true;
    }

}

// HttpBasicConnection.java
// $Id: HttpBasicConnection.java,v 1.1 2010/06/15 12:25:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.net.InetAddress;
import java.net.Socket;

import org.w3c.www.mime.MimeParser;
import org.w3c.www.mime.MimeParserFactory;

import org.w3c.www.http.HttpStreamObserver;

class HttpBasicConnection extends HttpConnection implements HttpStreamObserver
{
    private static final boolean debug = false;

    static Method sock_m = null;
    static {
	try {
	    Class c = java.net.Socket.class;
	    sock_m = c.getMethod("isClosed", (Class [])null);
	} catch (NoSuchMethodException ex) {
	    // not using a recent jdk...
	    sock_m = null;
	}
    }

    // A small class that start a thread to create a socket
    // while the original thread waits only for some amount of time
    // and indicate the other thread (if not finished) that the newly
    // created socket can be destroyed if it took too long.
    private class TimedSocket implements Runnable {
	Socket _sock = null;
	InetAddress _inetaddr = null;
	int _port = 0;
	boolean _ok = true;
	IOException _ioex = null;

	public synchronized Socket getSocket(InetAddress inetaddr, int port) 
	    throws IOException
	{
	    _inetaddr = inetaddr;
	    _port = port;
	    Thread t = new Thread(this);
	    t.start();

	    try {
                t.join(connect_timeout);
            } catch (InterruptedException iex){
		_ok = false;
		if (_sock != null) {
		    try {
			_sock.close();
		    } catch (IOException ioex) {};
		    _sock = null;
		}
            }
	    if (_sock != null) {
		return _sock;
	    }
	    _ok = false;
	    // should we interrupt the other thread here?
	    if (_ioex != null) {
		throw _ioex;
	    } else {
		throw new IOException("Connect timed out");
	    }
	}

	public void run() {
	    Socket s = null;
	    try {
		s = new Socket(inetaddr, port);
	    } catch (IOException ex) {
		_ioex = ex;
	    }
	    if (_ok) {
		_sock = s;
	    } else {
		try {
		    if (s != null) {
			s.close();
		    }
		} catch (IOException ioex) {};
	    }
	}
    }

    /**
     * The physical socket underlying the connection.
     */
    private Socket       socket = null;
    /**
     * The MIME parser to read input from the connection.
     */
    MimeParser   parser = null;
    /**
     * THe socket output stream, when available.
     */
    OutputStream output = null;
    /**
     * The socket input stream when available.
     */
    InputStream  input  = null;
    /**
     * The MimeParser factory to use to create Reply instances.
     */
    MimeParserFactory reply_factory = null;
    /**
     * The thread that owns the connection, for checking assertions.
     */
    Thread th = null;
    /**
     * The target INET address of this connection.
     */
    InetAddress inetaddr = null;
    /**
     * The target port number for this connection.
     */
    int         port     = -1;
    /**
     * The Timout on the underlying socket
     */
    int        timeout   = 300000;
    /**
     * The Connection timeout for the underlying socket
     */
    int       connect_timeout = 3000;
    /**
     * All connections are associated with a uniq identifier, for debugging.
     */
    protected int id = -1;
    /**
     * if a close is needed at the end of the connection (ie: on a
     * Connection: close client or server side
     */
    protected boolean closeOnEOF = false;
    /**
     * Old thread (same thread will try to reuse the same connection)
     */
    protected Thread old_th = null;

    protected synchronized void setCloseOnEOF(boolean doit) {
	closeOnEOF = doit;
    }

    /**
     * Print this connection into a String.
     * @return A String containing the external representation for the 
     * connection.
     */

    public String toString() {
	return inetaddr + ":" + port +"["+id+"]";
    }

    /**
     * The entity stream we observe has reached its end.
     * Notify the server that it can now reuse the connection safely for some
     * other pending requests.
     * @param in The stream that has reached its end of file.
     */

    public synchronized void notifyEOF(InputStream in) {
	markIdle(closeOnEOF);
    }

    /**
     * The entity stream we were to observe refuse to be observed.
     * The connection will not be reusable, so we should detach it from the 
     * managing server, without closing it, since the entity reader will
     * close it itself.
     * @param in The stream that has been closed.
     */

    public synchronized void notifyFailure(InputStream in) {
	markIdle(true);
    }

    /**
     * The entity stream we observe has been closed.
     * After making sure the entire entity has been read, we can safely hand
     * out the connection to the server, for later reuse.
     * @param in The stream that has been closed.
     */

    public synchronized void notifyClose(InputStream in) {
	boolean _close = false;
	try {
	    if (in.available() > 0) {
		_close = true;
	    }
//	try {
//	    byte buffer[] = new byte[1024];
//	    while (in.read(buffer) >= 0) {
//	    }
	} catch (IOException ex) {
	    _close = true;
	} finally {
	    markIdle(_close);
	}
	
    }

    /**
     * Close this connection to terminate it.
     * This method will only close the streams, and free all the data
     * structures that it keeps.
     */

    public synchronized void close() {
	close(false);
    }

    
    /**
     * Close this connection to terminate it.
     * This method will only close the streams, and free all the data
     * structures that it keeps.
     */

    private synchronized void close(boolean force) {
	boolean doit = ( socket != null || force);
	// Close the socket:
	try {
	    if (socket != null) {
		socket.close();
	    }
	} catch (IOException ex) {
	}
	socket = null;
	// Mark all data as invalid:
	output = null;
	input  = null;
	parser = null;
	cached = false;
	th     = null;
	old_th = null;
	if ( doit ) {
	    // Mark that connection as dead:
	    ((HttpBasicServer) server).deleteConnection(this);
	}
    }

    /**
     * Used only when we can't evaluate the end of the connection.
     * In that case, we are just unregistering it, and wait for the GC
     * to clean the mess afterwards.
     * This method will not close the stream, but will free all the data
     * structures that it keeps to help the GC.
     */

    protected synchronized void detach() {
	boolean doit = (socket != null);
	// wait for the socket to be GCed
	socket = null;
	// Mark all data as invalid:
	output = null;
	input  = null;
	parser = null;
	cached = false;
	th     = null;
	old_th = null;
	if ( doit ) {
	    // Mark that connection as dead:
	    ((HttpBasicServer) server).deleteConnection(this);
	}
    }

    /**
     * Mark this connection as being used.
     * The server, which keeps track of idle connections, has decided to use
     * this connection to run some request. Mark this connection as used
     * and unregister it from the server's list of idle connections.
     * <p>Some assumptions are checked before handing out the connection
     * for use, which can throw an RuntimeException.
     * @return A boolean, <strong>true</strong> if the connection can be used
     * or reused, <strong>false</strong> otherwise (the connection was detected
     * idle, and destroy itself).
     * @exception RuntimeException If the connection is in an invalid state.
     */
    
    public boolean markUsed() {
	cached = false;
	if ( debug )
	    System.out.println(this+ " used !");
	if ( th != null )
	    throw new RuntimeException(this+" already used by "+th);
	th = Thread.currentThread();
	if ( socket != null ) {
	    cached = true;
	    if (sock_m != null) {
		try {
		    // check if socket is good using jdk1.4 feature
		    // would be faster to do it natively...
		    Boolean b = (Boolean) sock_m.invoke(socket, 
							(Object [])null);
		    if (debug) {
			System.out.println("Socket is closed? "+b);
		    }
		    if (b.booleanValue()) {
			try {
			    socket.close();
			} catch (IOException ieox) {}
			socket = null;
		    }
		} catch (InvocationTargetException ex) {
		    if (debug)
			ex.printStackTrace();
		    // weird, let's close it
		    try {
			socket.close();
		    } catch (IOException ieox) {}
		    socket = null;
		} catch (IllegalAccessException ex) {
		    // weird also here :)
		    if (debug)
			ex.printStackTrace();	    
		    try {
			socket.close();
		    } catch (IOException ieox) {}
		    socket = null;
		} catch (Exception fex) {
		    try {
			socket.close();
		    } catch (IOException ieox) {}
		    socket = null;
		}
	    }
	}
	// damn... jdk1.4 is not behaving as it should :(
	if ( socket == null ) {
	    try {
		TimedSocket ts = new TimedSocket();
		socket = ts.getSocket(inetaddr, port);
		socket.setSoTimeout(timeout);
		output = new BufferedOutputStream(socket.getOutputStream());
		input  = new BufferedInputStream(socket.getInputStream());
		parser = new MimeParser(input, reply_factory);
		cached = false;
	    } catch (Throwable ex) {
		if (debug) {
		    ex.printStackTrace();
		}
		// Close that connection (cleanup):
		close(true); 
		return false;
	    }
	} 
	return true;
    }

    /**
     * The connection is now idle again.
     * Mark the connection as idle, and register it to the server's list of 
     * idle connection (if this connection can be reused). If the connection
     * cannot be reused, detach it from the server and forget about it (the
     * caller will close it by closing the entity stream).
     * @param close Should this connection be physically closed (it is not
     * reusable), or should we try to keep track of it for later reuse.
     * @exception RuntimeException If the connection is in an invalid state.
     */

    public void markIdle(boolean close) {
	// Has this connection already been marked idle ?
	synchronized (this) {
	    if ( th == null ) 
		return;
	    if ( debug )
		System.out.println(this+" idle !"+close);
	// Check consistency:
	// if ( Thread.currentThread() != th )
	//    throw new RuntimeException(this +
	//	 		         " th mismatch " +
	//		 	         th + 
	//			         "/" +
	//			         Thread.currentThread());
	// Ok, mark idle for good:
	    old_th = th;
	    th     = null;
	}
	if ( close ) {
	    if ( debug ) 
		System.out.println(this+" closing !");
	    close();
	} else {
	    // Notify the server that a new connection is available:
	    ((HttpBasicServer) server).registerConnection(this);
	}
    }

    /**
     * Some data available on input, while writing to the server.
     * This callback gets called when the client is emitting data to the
     * server and the server has sent us something before we actually sent 
     * all our bytes.
     * <p>Take any appropriate action.
     */

    public void notifyInputAvailable(InputStream in) {
	return;
    }

    /**
     * Get the MIME parser to read from this connection.
     * All access to the connection's input stream should go through the MIME
     * parser to ensure buffering coherency.
     * @return A MimeParser instance suitable to parse the reply input stream.
     * @exception RuntimeException If the connection was not connected.
     */

    public MimeParser getParser() {
	if ( parser == null )
	    throw new RuntimeException("getParser while disconnected.");
	return parser;
    }

    /**
     * Get the connection output stream.
     * @return The output stream to send data on this connection.
     * @exception RuntimeException If the connection was not previously opened.
     */

    public OutputStream getOutputStream() {
	if ( output == null )
	    throw new RuntimeException("getOutputStream while disconnected.");
	return output;
    }

    /**
     * Can this connection be reused as a first choice when requested?
     * This is only a hint, as if all connections fail, the first one will
     * be forced by default
     * @return a boolean, true by default
     */
    protected boolean mayReuse() {
	if (old_th != null) {
	    Thread cur_th = Thread.currentThread();
	    if (old_th == cur_th) {
		return true;
	    }
	    return false;
	}
	return true;
    }

    public void finalize() {
	if (socket != null) {
	    if (debug) {
		System.out.println("HttpBasicConnection closed in finalize");
	    }
	    close();
	}
    }

    /**
     * Create a new connection.
     * To be used only by HttpServer instances.
     */

    HttpBasicConnection(HttpServer server
			, int id
			, InetAddress addr
			, int port
			, int timeout
			, MimeParserFactory reply_factory)
	throws IOException
    {
	this(server, id, addr, port, timeout, 3000, reply_factory);
    }

    /**
     * Create a new connection.
     * To be used only by HttpServer instances.
     */

    HttpBasicConnection(HttpServer server
			, int id
			, InetAddress addr
			, int port
			, int timeout
			, int connect_timeout
			, MimeParserFactory reply_factory)
	throws IOException
    {
	this.server             = server;
	this.inetaddr           = addr;
	this.port               = port;
	this.id                 = id;
	this.timeout            = timeout;
	this.connect_timeout = connect_timeout;
	this.reply_factory      = reply_factory;
    }

}



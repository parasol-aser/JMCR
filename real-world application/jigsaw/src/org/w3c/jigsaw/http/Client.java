// Client.java
// $Id: Client.java,v 1.1 2010/06/15 12:22:00 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.InterruptedIOException;

import java.net.InetAddress;
import java.net.URL;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpParserException;

import org.w3c.www.mime.MimeParser;
import org.w3c.www.mime.MimeParserException;
import org.w3c.www.mime.MimeParserFactory;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;

import org.w3c.tools.timers.EventHandler;

import org.w3c.jigsaw.servlet.JigsawHttpServletResponse;
import org.w3c.jigsaw.servlet.ServletWrapper;

/**
 * The request timeout event, to be delivered by the timer package.
 * Handling timers is expensive, and can be a bottleneck, this is the only
 * event Jigsaw will set during processing a request.
 * No other timeing events are used (ie closing a persistent connection is
 * not triggered by some timing, but rather by the load of the server).
 */

class RequestTimeout {
    Client client = null;

    RequestTimeout (Client client) {
	this.client = client;
    }
}

/**
 * Client instances keep track of a specific connection with a browser.
 * This abstract class is responsible for handling an HTTP connection, as
 * described by an input and output stream, from right after the connection
 * is accepted, until the connection has to be shutdown. It provides
 * all the methods to run the HTTP dialog, and leave it to subclasses to
 * implement the accept connection and the persistent connections strategy.
 * <p>Customizing this class is done by subclassing it, and implementing
 * the abstract methods.
 * <p>For sample implementations, you can check the socket and mux sub
 * packages.
 * @see ClientFactory
 * @see org.w3c.jigsaw.http.socket.SocketClient
 * @see org.w3c.jigsaw.http.mux.MuxClient
 */

public abstract class Client implements EventHandler { 
    private static final boolean debuglog = false;
    private final static byte hexaTable[] = { 
	(byte) '0', (byte) '1', (byte) '2', (byte) '3',
	(byte) '4', (byte) '5', (byte) '6', (byte) '7',	 
	(byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
	(byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F' };
    /**
     * The continue reply, if ever needed is created only once.
     */
    private Reply contreply = null;
    /**
     * The Mime factory instance we use to create requests.
     */
    private MimeParserFactory factory     = null;
    /**
     * The uniq integer identifier for that client.
     */
    protected int identifier = -1;
    /**
     * The server context responsible for that client.
     */
    protected httpd server = null;
    /**
     * Is this client in debug mode ?
     */
    protected boolean debug = false;
    /**
     * The buffer used to emit copy data back to the client.
     */
    protected byte buffer[] = null ; 
    /**
     * Is this client currently <em>running</em>.
     * A client starts <em>running</em> when its <code>startConnection</code>
     * method is invoked, with the HTTP transport streams. It stops running
     * either because of an interruption, triggered by a call to 
     * <code>interruptConnection</code> or by the <code>idleConnection</code>,
     * returning <strong>true</strong>, or because the connection has to be 
     * closed.
     * <p>In all these cases, the <code>stopConnection</code> method is 
     * invoked.
     */
    private boolean running = false;
    /**
     * When runnin, the HTTP major version used to discuss with that client.
     */
    private short major = -1; 
    /**
     * WHen running, the HTTP minor version used to discuss with that client.
     */
    private short minor = -1; 
    /**
     * When running, the input stream from which this client gets HTTP.
     */
    private InputStream input  = null ;
    /**
     * When running, the output stream to which this client emits HTTP.
     */
    private DataOutputStream output = null ;
    /**
     * When processing a request, the handle to the pending timer.
     */
    private Object timer = null;
    /**
     * When running, the Mime parser instance to parse current input stream.
     */
    private MimeParser parser      = null;
    /**
     * When running, the interrupt flag.
     */
    private boolean interrupted = false;
    /**
     * Number of requests handled within this client context.
     */
    protected int reqcount = 0;

    /**
      * flags to avoid multiple 100-Continue during multiple stages of a 
      * request 
      */
    protected boolean cont = false;
    /**
     * The number of bytes in the body of the previously handled request
     */
    protected long prev_body_count = 0;
    /**
     * HTTP lenient mode?
     */
    private boolean lenient = true;

    /**
     * the current URI
     */
    public URL currentURI = null;
    /**
     * timeout on IO?
     */
    private boolean timedout;

    /**
     * Sets this client next timer. 
     * Timers are used only to limit the duration of a request processing.
     * Only one timer can be pending at any time for a given client, 
     * setting some new timer if one is already pending will kill
     * the previous one.
     * @param ms The number of milliseconds after which the timer should
     *    expire.
     * @param data The call data for the tevent timer handler.
     */

    private synchronized void setTimeout (int ms, Object data) {
	if ( timer != null ) {
	    server.timer.recallTimer (timer) ;
	    timer = null ;
	}
	timer = server.timer.registerTimer (ms, this, data) ;
    }

    /**
     * Remove any pending timer.
     */
	
    private synchronized void removeTimeout() {
	if ( timer != null )
	    server.timer.recallTimer (timer) ;
    }

    /**
     * Handle timer events. 
     * For the time being, timer events are only used
     * to detect an overrunning request, so this handler just kills the 
     * correponding client.
     * @param data The timer closure.
     * @param time The absolute time at which the event was triggered.
     * @see org.w3c.tools.timers.EventManager
     * @see org.w3c.tools.timers.EventHandler
     */

    public synchronized void handleTimerEvent (Object data, long time) {
	timer = null ;
	// This request has taken to long to fullfill, abort it
	Reply abort = new Reply (this) ;
	abort.setStatus (HTTP.REQUEST_TIMEOUT) ;
	interruptConnection(true);
    }

    /**
     * Terminate the connection that is currently handled.
     * This method closes the currently handled input and output stream, 
     * removes any pending timers and cleanup the client state, so it is ready
     * to handle any new connection.
     * If the client is bound to a thread, the thread is throw an exception at
     */

    private synchronized void terminate() {
	if ( ! running )
	    return ;
	removeTimeout();
	try {
	    if ( output != null ) {
		output.flush();
		output.close();
	    }
	} catch (IOException ex) {
	}
	try {
	    if ( input != null )
		input.close();
	} catch (IOException ex) {
	}
	input       = null;
	output      = null;
	parser      = null;
	major       = -1;
	minor       = -1;
	interrupted = false;
	running     = false;
    }

    /**
     * Request has been processed into Reply, should we keep connection alive ?
     * Test wether we can keep the connection alive, after the given
     * reply has been emited. 
     * @param request The request to examine.
     * @param reply Its computed reply.
     */

    protected boolean tryKeepConnection (Request request, Reply reply) {
	// The server doesn't want any keep connection
	if ( ! server.getClientKeepConnection() )
	    return false ;
	if (!request.canKeepConnection()) {
	    if (reply.tryKeepConnection()) {
		reply.addConnection("close");
	    }
	    return false;
	}
	return reply.tryKeepConnection();
    }

    /**
     * Read the next request from our current input stream.
     * @return a Request instance, or <strong>null</strong> if interrupted.
     * @exception IOException If some IO error occured.
     * @exception ClientException If either an IO error happened or bad
     * HTTP was received. In both cases the connection needs to be closed.
     */

    protected Request getNextRequest () 
	throws ClientException, IOException
    {
	Request request = null ;
	cont = false; // reinit the continue
	timedout = false;
	try {
	    request = (Request) parser.parse(lenient);
	} catch (InterruptedIOException timex) {
	    timedout = true;
	    return null;
	} catch (IOException ex) {
	    // The connection has probably been closed prematurely:
	    return null;
	} catch (HttpParserException ex) {
	    if ( debug ) {
		System.out.println("+++ "+this+" got exception:");
		ex.printStackTrace();
	    }
	    throw new ClientException(this, ex);
	} catch (MimeParserException ex) {
	    if ( debug ) {
		System.out.println("+++ "+this+" got exception:");
		ex.printStackTrace();
	    }
	    throw new ClientException (this, ex);
	}
	if ( debug )
	    request.dump(System.out) ;
	return request ;
    }

    /**
     * Run chunk encoding on the provided stream to emit reply's body.
     * @param is The reply's body that has to be chunk encoded.
     * @exception IOException If IO error occurs.
     */

    protected int chunkTransfer(InputStream is, Reply reply)
	throws IOException
    {
	byte   zeroChunk[] = { ((byte) 48), ((byte) 13), ((byte) 10) };//0\r\n
	byte   crlf[]      = { ((byte) 13), ((byte) 10) } ; 
	byte   bheader[]   = new byte[32] ;
	int    blen        = 0 ;
	int    written     = 0 ;
	int    got         = 0 ;
	int    sgot;
	String header      = null ;

	try {
	    // Emit the reply stream:
	    while (got >= 0) {
		if (got == 0) {
		    try {
			got = is.read(buffer);
		    } catch (IOException ioex) {
			if (reply.hasState(ServletWrapper.RUNNER) &&
			    (is instanceof PipedInputStream) ) {
			    // here, the problem may be that multiple
			    // threads are writing to the PipedOutputStream
			    // and the IOError may exists
			    if (!reply.hasState(ServletWrapper.ENDED)) {
				got = 0;
				continue;
			    }
			}
			throw ioex;
		    }
		    continue;
		}
		// Emit a full chunk: header first, followed by the body
		// we dump the hexa size of the header backward
		sgot = got;
		blen = 3;
		bheader[30] = ((byte) 13); // \r
		bheader[31] = ((byte) 10); // \n
		while (sgot > 15) {
		    bheader[32-blen] = hexaTable[sgot % 16];
		    sgot >>= 4;
		    blen++;
		}
		bheader[32-blen] = hexaTable[sgot];
		output.write(bheader, 32-blen, blen) ;
		output.write(buffer, 0, got) ;
		output.write(crlf, 0, 2) ;
		output.flush() ;
		written += (blen+got) ;
		try {
		    got = is.read(buffer);
		} catch (IOException ioex) {
		    if (reply.hasState(ServletWrapper.RUNNER) &&
			(is instanceof PipedInputStream) ) {
			// here, the problem may be that multiple
			// threads are writing to the PipedOutputStream
			// and the IOError may exists
			if (!reply.hasState(ServletWrapper.ENDED)) {
			    got = 0;
			    continue;
			}
		    }
		    throw ioex;
		}
	    }
	} catch (IOException ex) {
	    // To cope with Java's exec bug 
	    // Anyway, if this really fails, the output will fail below too
	    if (debug) {
		ex.printStackTrace();
	    }
	}
	// Emit the 0 chunk:
	output.write(zeroChunk, 0, 3) ;
	// FIXME trailers should be sent here
	output.write(crlf, 0, 2) ;
	output.flush() ;
	return written + blen ;
    }

    /**
     * Emit the given reply to the client.
     * @param reply The reply to be emited.
     * @return The number of body bytes emited, or <strong>-1</strong> if
     * no bytes needed to be emitted.
     * @exception IOException If some IO error occurs.
     */

    protected int emitReply (Reply reply) 
	throws IOException
    {
	boolean chunkable = false ;
	// Emit the reply if needed:
	if ( reply.getStatus() == HTTP.DONE ) 
	    return -1;
	InputStream is = reply.openStream() ;
	if ( is == null ) {
	    if ( debug )
		reply.dump(System.out);
	    reply.emit(output);
	    return -1;
	} else {
	    chunkable = reply.canChunkTransfer() ;
	    if ( debug )
		reply.dump(System.out);
	    if ( reply.getStatus() != HTTP.NOHEADER )
		reply.emit(output) ;
	}
	// No shuffler available, perform the job ourselves.
	if ( buffer == null )
	    buffer = new byte[getServer().getClientBufferSize()] ;
	// Check if we can chunk the reply back:
	int written = 0 ;
	try {
	    if ( chunkable ) {
		written = chunkTransfer(is, reply) ;
	    } else {
		int got = 0 ;
		while (got >= 0) {
		    output.write (buffer, 0, got) ;
		    written += got ;
		    try {
			got = is.read(buffer, 0, buffer.length);
		    } catch (IOException ioex) {
			if (reply.hasState(ServletWrapper.RUNNER) &&
			    (is instanceof PipedInputStream) ) {
			    // here, the problem may be that multiple
			    // threads are writing to the PipedOutputStream
			    // and the IOError may exists
			    if (!reply.hasState(ServletWrapper.ENDED)) {
				got = 0;
				continue;
			    }
			}
			throw ioex;
		    }
		}
	    }
	} finally {
	    is.close() ;
	}
	return written ;
    }

    /**
     * Process a request.
     * This methods processs the request to the point that a reply is 
     * available. This methods sets a timeout, to limit the duration of this 
     * request processing. 
     * @param request The request to process.
     * @exception ClientException If either the timeout expires or the entity
     *     was unable to handle the request.
     */

    protected Reply processRequest (Request request) 
	throws ClientException
    {
	Reply        reply    = null ;
	setTimeout(server.getRequestTimeOut(), new RequestTimeout(this)) ;
	try {
	    reply = (Reply)server.perform(request);
	} catch (ProtocolException ex) {
	    if ( debug ) { 
		System.out.println("+++ "+this+" got exception:");
		ex.printStackTrace();
	    }
	    if ((reply != null) && reply.hasStream()) {
		try {
		    reply.openStream().close();
		} catch (Exception cex) {}
	    }
	    if ( ex.hasReply () ) {
		return (Reply) ex.getReply() ;
	    } else {
		throw new ClientException (this, ex) ;
	    }
	} catch (ResourceException ex2) {
	    throw new ClientException(this, ex2);
	}
	if ( reply == null ) {
	    String errmsg = "target resource emited a null Reply.";
	    throw new ClientException (this, errmsg);
	}
	reqcount++;
	return reply ;
    }

    /**
     * Start processing the given connection.
     * This is the entry point for sub classes, in order to make the client
     * start processing the HTTP protocol on the given input and output
     * streams.
     * <p>Before this method returns, both provided streams are <em>always
     * </em> closed, and the <code>stopConnection</code> method invoked.
     * @param in The input stream to receive HTTP requests.
     * @param out The output stream to send HTTP replies.
     * @return A boolean <strong>true</strong> if this method returns because
     * of an interruption, <strong>false</strong> otherwsie (ie the connection
     * was gracefully shutdown).
     * @exception ClientException If some severe error has occured and the
     * current connection needs to be terminated.
     */

    protected boolean startConnection(InputStream in, DataOutputStream out) 
	throws ClientException
    {
	boolean   keep      = true ;
	long      tstart    = 0 ;
	long      tend      = 0 ;
	Request   request   = null ;
	Reply     reply     = null ;
	int       sent      = 0 ;
	int       processed = 0;
	ClientException err = null;

	this.input  = in;
	this.output = out;
	this.parser = new MimeParser(input, factory);
	try {
	    running = true;
	alive_loop:
	    while ( (! interrupted)  && keep ) {
		// Get the next available request, and  mark client as used:
		try {
		    // mark the stream, if some bytes are to be eaten
		    if (prev_body_count > 0) {
			input.mark(2048);
		    }
		    if ( processed == 0 ) {
			// Always run for the first request, 
			// update client's infos
			if ((request = getNextRequest()) == null ) 
			    break alive_loop;
			major = request.getMajorVersion();
			minor = request.getMinorVersion();
		    } else {
			if ( interrupted = idleConnection() )
			    break alive_loop;
			while ((request = getNextRequest()) == null) {
			    if (timedout) {
				continue;
			    } else {
				break alive_loop;
			    }
			}
			// the gateway case, the major/minor _may_ change
			major = request.getMajorVersion();
			minor = request.getMinorVersion();
			usedConnection();
		    }
		} catch (ClientException cex) {
		    if (cex.ex != null && 
			(cex.ex instanceof HttpParserException)) {
			HttpParserException hex = (HttpParserException) cex.ex;
			if (!hex.hasRequest()) {
			    throw (cex);
			}
			request = (Request) hex.getRequest();
			if ( processed == 0 ) {
			    major = request.getMajorVersion();
			    minor = request.getMinorVersion();
			}
			usedConnection();
			reply = (Reply) request.makeBadRequestReply();
			reply.setContentLength(0);
			reply.addConnection("close");
			// Inital keep alive check, emit the reply:
			keep = false;
			sent = emitReply(reply) ;
			// Semi-fake log entry
			log(request, reply, 0, 0) ;
			processed++;
			// a bad request is still a request...
			reqcount++;
			break alive_loop;
		    } else if (cex.ex != null && 
			(cex.ex instanceof MimeParserException)) {
			reply = new Reply(this);
			reply.setStatus(HTTP.BAD_REQUEST);
			reply.setContentLength(0);
			reply.addConnection("close");
			keep = false;
			sent = emitReply(reply) ;
			// Semi-fake log entry
			log(request, reply, 0, 0) ;
			processed++;
			// a bad request is still a request...
			reqcount++;
			break alive_loop;
		    }
		    throw (cex);
		} catch (Exception genex) {
		    if (debug)
			genex.printStackTrace();
		    // some bytes may have to be eaten
		    // abomination, very ugly hack!
		    if (genex.getMessage().startsWith("Bad request") && 
			(prev_body_count > 0)) {
			// check if the error is due to a
			usedConnection();
			if (debug)
			    System.out.println("Error after a body "+
					       "request! (Skipping: " +
					       prev_body_count+" bytes)");
			// now try to eat some data...
			input.reset();
			byte b[] = new byte[(int)prev_body_count];
			prev_body_count -= input.read(b);
			prev_body_count -= input.skip(prev_body_count);
			break alive_loop;
		    }
		    throw (genex);
		}
		// Some traces if required:
		if ( debuglog )
		    System.out.println(this+": request "+request.getURL());
		// Process request, and time it:
		currentURI = request.getURL();
		tstart  = System.currentTimeMillis() ;
		reply   = processRequest (request) ;
		tend    = System.currentTimeMillis() ;
		currentURI = null;
		// Inital keep alive check, emit the reply:
		if ( keep ) 
		    keep = tryKeepConnection(request, reply) ;
		sent = emitReply(reply) ;
		// Second keep alive check:
		if (reply.hasContentLength() 
		    && (sent >= 0 )
		    && (reply.getContentLength() != sent))
		    keep = false;
		// Log and/or traces:
		if ( debuglog )
		    System.out.println(this
				       + ", cl="+reply.getContentLength()
				       + ", size="+sent);
		// will be -1 if there is no body
		prev_body_count = request.getContentLength();
		log(request, reply, sent, tend-tstart) ;
		processed++;
		// If we can keep alive, and if the client doesn't do
		// pipelining, be clever:
		// We should compare against 0 here, we use 2 because of a
		// well-known client bug, that emits an extra CRLF at the end
		// of forms POSTing
		// Note we're doing that very last, so that if the socket is 
		// closed by peer, we really have *already* done everything
		if ( keep  /*&& (in.available() <= 2)*/)
		    output.flush() ;
		// be clever again... in case of protocol switching,
		// we must free everything and let the other client use the
		// streams
		if (reply.getStatus() == HTTP.SWITCHING) {
		    input = null;
		    output = null;
		    throw new Exception ("Switching");
		}
		// hack for servlets
		if (request.hasState(JigsawHttpServletResponse.STREAM)) {
		    try {
			PipedInputStream pis;
			pis = (PipedInputStream) 
			    request.getState(JigsawHttpServletResponse.STREAM);
			pis.close();
		    } catch (ClassCastException ccex) {
			// do nothing
		    } catch (IOException pisioex) {
			// fail silently also
		    }
		}
	    }
	} catch (IOException ex) {
	    if ( debug ) {
		System.out.println("+++ "+this+" got IOException:");
		ex.printStackTrace();
	    }
	    // Close any stream pending
	    try {
		InputStream i = null;
		if ( reply != null ) {
		    if ((i = reply.openStream()) != null)
			i.close();
		}
	    } catch (IOException ioex) {
	    }
	    err = new ClientException(this, ex);
	} catch (ClientException ex) {
	    if ( debug ) {
		System.out.println("+++ "+this+" got ClientException:");
		ex.printStackTrace();
	    }
	    try {
		InputStream i = null;
		if ( reply != null ) {
		    if ((i = reply.openStream()) != null)
			i.close();
		}
	    } catch (IOException ioex) {
	    }
	    err = ex;
	} catch (NullPointerException nex) {
	    if ( debug ) {
		System.out.println("+++ "+this+" got exception:");
		nex.printStackTrace();
	    }
	    if (currentURI != null) {
		err = new ClientException(this, nex, currentURI.toString());
	    } else {
		err = new ClientException(this, nex);
	    }
	} catch (Exception ex) {
	    if ( debug ) {
		System.out.println("+++ "+this+" got exception:");
		ex.printStackTrace();
	    }
	    err = new ClientException(this, ex);
	} finally {
	    currentURI = null;
	    // Absorb incoming data to avoid RST TCP packets:
	    if ((err == null) && (request != null)
		&& (request.getContentLength() > 0)
		&& (reply != null)
		&& (reply.getStatus() != HTTP.SWITCHING)
		&& (reply.getStatus() / 100 != 2)) {
		// The request may have failed...
		try {
		    InputStream i = request.getInputStream();
		    if ( i != null ) {
			while (i.available() > 0)
			    i.read(buffer, 0, buffer.length);
		    }
		} catch (Exception ex) {
		}
	    }
	    if ((request != null) && (
		request.hasState(JigsawHttpServletResponse.STREAM))) {
		try {
		    PipedInputStream pis;
		    pis = (PipedInputStream) 
			request.getState(JigsawHttpServletResponse.STREAM);
		    pis.close();
		} catch (ClassCastException ccex) {
		    // do nothing
		} catch (IOException pisioex) {
		    // fail silently also
		} catch (Exception ex) {
		    // be sure not to cause any trouble :)
		}
	    }
	    terminate();
	    if (reply == null || (reply.getStatus() != HTTP.SWITCHING)) {
		stopConnection();
		if (reply != null) {
		    try {
			reply.openStream().close();
		    } catch (Exception roex) {};
		}
	    }
	    if ( err != null )
		throw err;
	}
	return interrupted;
    }

    /**
     * Interrupt the currently handled connection.
     * This method will make best effort to interrupt any thread currently
     * processing the connection.
     * @param now Make sure the thread is interrupted right now if 
     * <strong>true</strong>, otherwise, just schedule an interruption
     * after the current request (if any) has been processed.
     */

    protected synchronized void interruptConnection(boolean now) {
	if ( running ) {
	    interrupted = true;
	    if ( now )
		terminate();
	}
    }

    /**
     * Send a 100 HTTP continue message on the currently handled connection.
     * This method will take care of creating the appropriate HTTP
     * continue reply, and will emit that reply only if the spoken HTTP
     * version allows for it.
     * @exception IOException If some IO error occured.
     */

    public int sendContinue() 
	throws IOException
    {
	if (cont)
	    return -1;
	if ((major > 1) || ((major == 1) && (minor >= 1))) {
	    if ( contreply == null )
		contreply = new Reply(this, null, major, minor, HTTP.CONTINUE);
	    int len = emitReply(contreply);
	    output.flush();
	    cont = true;
	    return len;
	}
	return -1;
    }

    /**
     * Send a 100 HTTP continue message on the currently handled connection.
     * This method will take care of creating the appropriate HTTP
     * continue reply, and will emit that reply only if the spoken HTTP
     * version allows for it.
     * @exception IOException If some IO error occured.
     */

    public int sendContinue(Reply contReply) 
	throws IOException
    {
	if (contReply == null) {
	    return sendContinue();
	}
	if ((major > 1) || ((major == 1) && (minor >= 1))) {
	    int len = emitReply(contReply);
	    output.flush();
	    cont = true;
	    return len;
	}
	return -1;
    }

    

    /**
     * Get this client identifier.
     * @return An integer identifying uniquely this client's context.
     */

    public final int getIdentifier() {
	return identifier;
    }

    /**
     * Is this client currently <em>running</em> for a connection.
     * @return A boolean, <strong>true</strong> if it is, 
     * <strong>false</strong> otherwise.
     */

    public final synchronized boolean isRunning() {
	return running;
    }

    /**
     * Get the HTTP major version number spoken on the current connection.
     * @return The HTTP major version number, or <strong>-1</strong> if that
     * client is not running.
     */

    public final short getMajorVersion() {
	return major;
    }

    /**
     * Get the HTTP minor version number spoken on the current connection.
     * @return The HTTP minor version number, or <strong>-1</strong> if 
     * that client is not running.
     */

    public final short getMinorVersion() {
	return minor;
    }

    /**
     * Does this client has an interrupt pending ?
     * @return A boolean, <strong>true</strong> if an interrupt is pending,
     * <strong>false</strong> otherwise.
     */

    public final boolean isInterrupted() {
	return interrupted;
    }

    /**
     * Get the total number of requests handled within this client context.
     * @return An integer giving the number of requests handled.
     */

    public final int getRequestCount() {
	return reqcount;
    }

    /**
     * Get the server context responsible for this client context.
     * @return An httpd instance.
     */

    public final httpd getServer() {
	return server;
    }

    /**
     * Emit an error message on behalf of that client.
     * @param msg The error message to output.
     */

    public final void error(String msg) {
	server.errlog(this, msg);
    }

    /**
     * Emit a trace on behalf of the given client.
     * @param msg The trace to output.
     */

    public final void trace(String msg) {
	server.trace(this, msg);
    }

    /**
     * Log the given HTTP transaction.
     * @param request The request that has been processed.
     * @param reply The generated reply.
     * @param nbytes Number of content bytes sent along with the reply.
     * @param duration The processing time for that request in milliseconds.
     */

    public void log(Request request, Reply reply, int nbytes, long duration) {
	server.log (this, request, reply, nbytes, duration) ;
    }

    /**
     * Get this client input stream.
     * @return An instance of InputStream, or <strong>null</strong> if the 
     * client is not handling any connection at that time.
     */

    public InputStream getInputStream() {
	return input;
    }

    /**
     * Get this client output stream.
     * @return An instance of OutputStream, or <strong>null</strong> if the
     * client is not handling any connection at that time.
     */

    public DataOutputStream getOutputStream() {
	return output;
    }

    /**
     * Get the IP address of the host that runs the client described by this
     * context.
     * @return An InetAddress instance, or <strong>null</strong> if that client
     * is not handling any connection at that given time.
     */

    abstract public InetAddress getInetAddress();

    /**
     * Client callback - The client is about to block, getting next request.
     * This method is triggered by the client instance itself, before
     * reading next request from the input stream provided at
     * <code>startConnection</code> time.
     * @return A boolean, if <strong>true</strong>, the client will consider
     * itself interrupted, and terminate the connection it is current handling.
     */

    abstract protected boolean idleConnection();

    /**
     * Client callback - A full request has been received on input stream.
     * This method is called by the client itself, before starting processing
     * the newly received request. The purpose of this callback is typically
     * to mark that client <em>buzy</em>.
     */

    abstract protected void usedConnection();

    /**
     * Client callback -  The current connection has been terminated.
     * This client has finished processing the connection provided
     * at <code>startConnection</code> time, it is now stopped.
     */

    abstract protected void stopConnection();

    /**
     * Get the thread powering that client, if any.
     * This method is called to kill the client (by interrupting the thread
     * used to run it).
     * @return A Thread instance, or <strong>null</strong>.
     */

    abstract protected Thread getThread();

    /**
     * Initialize this client.
     * It is up to this method to initialize:
     * <dl>
     * <dt>parser<dd>The MimeParser to be used to parse incomminf requests.
     * </dl>
     * @param server The server responsible for that client.
     * @param factory The factory that created this client.
     * @param identifier The uniq identifier for this client.
     */

    protected void initialize(httpd server, int identifier) {
	this.server     = server;
	this.lenient    = server.isLenient();
	this.identifier = identifier;
	this.debug      = server.getClientDebug() ;
	this.factory    = server.getMimeClientFactory(this);
    }

}

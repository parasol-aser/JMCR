// HttpMuxServer.java
// $Id: HttpMuxServer.java,v 1.1 2010/06/15 12:25:12 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.w3c.www.mux.MuxSession;

import org.w3c.www.mime.MimeHeaderHolder;
import org.w3c.www.mime.MimeParser;
import org.w3c.www.mime.MimeParserException;
import org.w3c.www.mime.MimeParserFactory;

import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;

public class HttpMuxServer extends HttpServer {
    private static final String PROTOCOL = "http|mux";

    protected HttpMuxConnection conn    = null;
    protected HttpManager       manager = null;
    protected String host = null;
    protected int port    = -1;
    protected int timeout = 300000;
    protected int conn_timeout = 000;

    protected synchronized void acquireConnection()
	throws IOException
    {
	if ( conn != null )
	    return;
	conn = new HttpMuxConnection(this, host, port);
	if ( conn.incrUseCount() )
	    manager.notifyUse(conn);
    }

    protected synchronized void releaseConnection() {
	if ((conn != null) && conn.decrUseCount() )
	    manager.notifyIdle(conn);
    }

    public String getProtocol() {
	return PROTOCOL;
    }

    public short getMajorVersion() {
	return (short) 1;
    }

    public short getMinorVersion() {
	return (short) 1;
    }

    public synchronized void setTimeout(int timeout) {
	this.timeout = timeout;
    }

    public synchronized void setConnTimeout(int conn_timeout) {
	this.conn_timeout = conn_timeout;
    }

    /**
     * Is this request a two stage request.
     * @return A boolean, <strong>true</strong> if the request is two
     * stage, <strong>false</strong> otherwise.
     */

    protected boolean isTwoStage(Request requset) {
	return requset.hasOutputStream();
    }

    // FIXME doc
    protected void notifyObserver(RequestObserver obs
				  , Request request
				  , int code) {
	RequestEvent evt = new RequestEvent(this, request, code);
	obs.notifyProgress(evt);
    }

    protected void notifyObserver(RequestObserver obs, RequestEvent evt) {
	obs.notifyProgress(evt);
    }

    /**
     * Run the given request.
     * @param request The request to run.
     * @return An instance of Reply, containing all the reply 
     * informations.
     * @exception HttpException If something failed during request processing.
     */
    public Reply runRequest(Request req) 
	throws HttpException
    {
	Reply           rep = null;
	MuxSession      s   = null;
	RequestObserver o   = req.getObserver();
	// Run the request, and mark the connection idle again:
	try {
	    // Get a connection:
	    acquireConnection();
	    s = conn.connect(80);
	    OutputStream os     = (new DataOutputStream
				   (new BufferedOutputStream
				    (s.getOutputStream())));
	    MimeParser   parser = new MimeParser(s.getInputStream()
						 , manager.getReplyFactory());
	    if ( isTwoStage(req) ) {
		// Emit the request headers:
		req.emit(os, Request.EMIT_HEADERS);
		os.flush();
		if ( o != null )
		    notifyObserver(o, new ConnectedEvent(this, req, os));
		rep = (Reply) parser.parse();
		// Wait for a 100 status code:
		if ((rep.getStatus() / 100) == 1) {
		    // Notify the observer if any:
		    if ( o != null ) 
			notifyObserver(o, new ContinueEvent(this, req, rep));
		    // Finish the request normally:
		    req.emit(os, Request.EMIT_BODY|Request.EMIT_FOOTERS);
		    os.flush();
		    rep = (Reply) parser.parse();
		}
	    } else {
		req.emit(os, Request.EMIT_HEADERS);
		os.flush();
		if ( o != null ) 
		    notifyObserver(o, new ConnectedEvent(this, req, os));
		rep = (Reply) parser.parse();
		while ((rep.getStatus() / 100) == 1) {
		    if ( o != null ) 
			notifyObserver(o, new ContinueEvent(this, req, rep));
		    // Finish the request normally:
		    req.emit(os, Request.EMIT_BODY|Request.EMIT_FOOTERS);
		    os.flush();
		    rep = (Reply) parser.parse();
		}
	    }
	    os.close();
	} catch (IOException ex) {
	    ex.printStackTrace();
	    throw new HttpException(req, ex);
	} catch (MimeParserException ex) {
	    ex.printStackTrace();
	    throw new HttpException(req, ex);
	} finally {
	    releaseConnection();
	}
	return rep;
    }

    /**
     * Interrupt given request (that we launched).
     * THIS METHID IS NOT IMPLEMENTED !
     * @param request The request to interrupt.
     */

    protected void interruptRequest(Request request) {
	System.out.println("HttpMuxConnection.interruptRequest: "
			   + "not implemented.");
    }

    public synchronized void deleteConnection(HttpConnection conn) {
	conn.close();
	conn = null;
    }

    /**
     * Initialize this server instance for the given target location.
     * @param manager The central HTTP protocol manager.
     * @param state The manager's state for that server.
     * @param host The target server's FQDN.
     * @param port The target server's port number.
     * @param timeout The socket's timeout in millisec
     */

    public void initialize(HttpManager manager
			   , HttpServerState state
			   , String host
			   , int port
			   , int timeout) {
	initialize(manager, state, host, port, timeout, conn_timeout);
    }

    /**
     * Initialize this server instance for the given target location.
     * @param manager The central HTTP protocol manager.
     * @param state The manager's state for that server.
     * @param host The target server's FQDN.
     * @param port The target server's port number.
     * @param timeout The socket's timeout in millisec
     */

    public void initialize(HttpManager manager
			   , HttpServerState state
			   , String host
			   , int port
			   , int timeout, int conn_timeout) {
	this.state = state;
	this.manager = manager;
	this.host = host;
	this.port = port;
	this.timeout = timeout;
	this.conn_timeout = conn_timeout;
	state.state = HttpServerState.OK;
    }
}

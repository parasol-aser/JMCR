// Request.java
// $Id: Request.java,v 1.1 2010/06/15 12:25:15 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpRequestMessage;

/**
 * The client side idea of a request.
 * Requests are created only by the HttpManager, by cloning its template 
 * request that defines the default (application wide) request settings.
 */

public class Request extends HttpRequestMessage {
    /**
     * The manager that created this request.
     */
    protected HttpManager manager = null;
    /**
     * Are we allowed to interact with the user ?
     */
    protected boolean allowuserinteraction = false;
    /**
     * The request output stream, to PUT or POST data.
     */
    protected InputStream output = null;
    /**
     * The observer for the request, if any.
     */
    protected RequestObserver observer = null;
    /**
     * Can we pipeline that request, if appropriate support is detected ?
     */
    protected boolean pipeline = true;
    /**
     * Has this request been interrupted ?
     */
    protected boolean interrupted = false;
    /**
     * The server <em>currently</em> running the request, if any.
     */
    protected HttpServer server = null;

    /**
     * Mark that request has being run by given server.
     * @param server The server in charge for that request.
     */

    protected synchronized void setServer(HttpServer server) {
	this.server = server;
    }

    /**
     * Mark that request as no longer attached to a server object.
     */

    protected synchronized void unsetServer() {
	this.server = null;
    }

    /**
     * Enable/disable pipelining for that request.
     * By default, this HTTP implementation tries it's best to use pipelining,
     * if you take manual control over it, you're responsible for damages.
     * @param onoff The pipelining toggle.
     */

    public void setPipeline(boolean onoff) {
	this.pipeline = onoff;
    }

    /**
     * End of header emiting, continue by sending optional output stream.
     * @param out The output stream to write to.
     */

    protected void endEmit(OutputStream out, int what) 
	throws IOException 
    {
	if ((what & EMIT_BODY) != EMIT_BODY)
	    return;
	if ( output != null ) {
	    byte buf[] = new byte[1024];
	    int  cnt   = 0;
	    int total  = 0;
	    while ((cnt = output.read(buf)) >= 0) {
		total +=cnt;
		out.write(buf, 0, cnt);
	    }
//	    output.close();
	}
	return;
    }

    /**
     * Are we allowed to do some user interaction to run this request.
     * @return A boolean, <strong>true</strong> if user interaction is allowed
     * <strong>false</strong> otherwise.
     */

    public boolean getAllowUserInteraction() {
	return allowuserinteraction;
    }

    /**
     * Decide wether we are allowed to interact wit hthe user.
     * @param onoff A boolean, <strong>true</strong> if interaction is allowed.
     */

    public void setAllowUserInteraction(boolean onoff) {
	allowuserinteraction = onoff;
    }

    /**
     * Interrupt that request processing.
     * Do whatever it takes to interrupt that request processing, as soon
     * as possible.
     */

    public synchronized void interruptRequest() {
	interrupted = true;
	if ( server != null )
	    server.interruptRequest(this);
    }

    /**
     * Has this request been interrupted ?
     * @return A boolean.
     */

    public boolean isInterrupted() {
	return interrupted;
    }

    /**
     * Get this request's manager.
     * @return The instance of the manager taking care of this request.
     */

    public HttpManager getManager() {
	return manager;
    }

    /**
     * Set this request output stream.
     * As a side effect, setting the body of the request will disable
     * pipelining. If you know what you're doing, you can turn it on again by 
     * using the <code>setPipeline</code> method.
     * @param in The data to send to the server.
     */

    public void setOutputStream(InputStream in) {
	this.output   = in;
	this.pipeline = false;
    }

    /**
     * Does this request has an associated input stream ?
     * @return A boolean <strong>true</strong> of it has.
     */

    public boolean hasOutputStream() {
	return output != null;
    }

    /**
     * Get the input stream to read that request body.
     * <strong>Warning</strong> it is up to the caller to make sure to:
     * <ul>
     * <li>Reset the content length if any bytes is read out of the stream 
     * before the request is sent.
     * <li>Reset the entire stream is the filter acting upon it just want to 
     * peek it (without consuming it).
     * </ul>
     * @return An InputStream instance, or <strong>null</strong> if the request
     * has no body.
     */

    public InputStream getOutputStream() {
	return output;
    }

    /**
     * Create a Reply instance matching this request.
     */

    public Reply makeReply(int status) {
	return new Reply(major, minor, status);
    }

    /**
     * Set the observer for this request.
     * @param observer The observer.
     */

    public void setObserver(RequestObserver observer) {
	this.observer = observer;
    }

    /**
     * Get the observer for this request.
     * @return An instance of RequestObserver, or <strong>null</strong> 
     * if undefined.
     */

    public RequestObserver getObserver() {
	return observer;
    }

    protected Request(HttpManager manager) {
	super();
	this.manager = manager;
    }

}

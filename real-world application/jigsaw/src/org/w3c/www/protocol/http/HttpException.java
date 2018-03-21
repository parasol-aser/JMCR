// HttpException.java
// $Id: HttpException.java,v 1.1 2010/06/15 12:25:13 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http;

/**
 * Exception thrown when processing a request failed.
 */

public class HttpException extends Exception {
    Request   request = null;
    Reply     reply   = null;
    Exception exception = null;

    /**
     * Get the original cause for this exception.
     * HttpException can be used to wrap up transport layer problems (such
     * as IOException or other SocketException, etc). In that case, this method
     * will return the original exception that occured.
     * @return An Exception instance, or <strong>null</strong>.
     */

    public final Exception getException() {
	return exception;
    }

    /**
     * Get the request that triggered this exception.
     * @return A Request instance.
     */

    public final Request getRequest() {
	return request;
    }

    /**
     * Get the reply generated (if any)
     * @return A Request instance.
     */

    public final Reply getReply() {
	return reply;
    }

    public HttpException(Request request, Reply reply, String msg) {
	super(msg);
	this.request = request;
	this.reply = reply;
    }

    public HttpException(Request request, Reply reply, Exception ex, 
			 String msg) {
	super(msg);
	this.request = request;
	this.reply = reply;
	this.exception = ex;
    }

    public HttpException(Request request, Reply reply, Exception ex) {
	super(ex.getMessage());
	this.request   = request;
	this.exception = ex;
	this.reply = reply;
    }

    public HttpException(Reply reply, String msg) {
	this(null, reply, msg);
    }

    public HttpException(Request request, String msg) {
	this(request, null, msg);
    }
    
    public HttpException(Reply reply, Exception ex) {
	this(null, reply, ex);
    }

    public HttpException(Request request, Exception ex) {
	this(request, null, ex);
    }    

    public HttpException(Exception ex, String msg) {
	super(msg);
	this.exception = ex;
    }
}

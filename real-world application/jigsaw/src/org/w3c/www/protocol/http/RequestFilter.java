// RequestFilter.java
// $Id: RequestFilter.java,v 1.1 2010/06/15 12:25:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http;

/**
 * The request filter interface.
 * Filters allow application wide request enhancement before they are being
 * actually emited on the wire. 
 * <p>Once registered to the HttpManager, a request filter will be invoked
 * <em>before</em> the request is actualy sent to the wire, and right
 * <em>after</em> the reply headers are available, <em>only</em> if its
 * current scope matches the request URL.
 */

public interface RequestFilter {

    /**
     * The request pre-processing hook.
     * Before each request is launched, all filters will be called back through
     * this method. They will generally set up additional request header
     * fields to enhance the request.
     * @param request The request that is about to be launched.
     * @return An instance of Reply if the filter could handle the request,
     * or <strong>null</strong> if processing should continue normally.
     * @exception HttpException If the filter is supposed to fulfill the
     * request, but some error happened during that processing.
     */

    public Reply ingoingFilter(Request request) 
	throws HttpException;

    /**
     * The request post-processing hook.
     * After each request has been replied to by the target server (be it a 
     * proxy or the actual origin server), each filter's outgoingFilter
     * method is called.
     * <p>It gets the original request, and the actual reply as a parameter,
     * and should return whatever reply it wants the caller to get.
     * @param request The original (handled) request.
     * @param reply The reply, as emited by the target server, or constructed
     * by some other filter.
     * @exception HttpException If the reply emitted by the server is not
     * a valid HTTP reply.
     */

    public Reply outgoingFilter(Request request, Reply reply) 
	throws HttpException;

    /**
     * An exception occured while talking to target server.
     * This method is triggered by the HttpManager, when the target server
     * (which can be a proxy for that request) was not reachable, or some
     * network error occured while emitting the request or reading the reply
     * headers.
     * @param request The request whose processing triggered the exception.
     * @param ex The exception that was triggered.
     * @return A boolean, <strong>true</strong> if that filter did influence
     * the target server used to fulfill the request, and it has fixed the 
     * problem in such a way that the request should be retried.
     */

    public boolean exceptionFilter(Request request, HttpException ex);

    /** 
     * Synchronized any pending state into stable storage.
     * If the filter maintains some in-memory cached state, this method
     * should ensure that cached data are saved to stable storage.
     */

    public void sync();

}

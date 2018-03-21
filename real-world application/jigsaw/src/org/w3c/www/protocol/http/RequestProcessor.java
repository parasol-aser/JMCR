// RequestProcessor.java
// $Id: RequestProcessor.java,v 1.1 2010/06/15 12:25:12 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http;

/**
 * Request processor interface.
 * Request processors are object whose invocation is done prior to any request
 * launching, and who have the opportunity to answer to the request before it
 * actually goes out to the target server.
 * <p>Typicall request processors will probably be local cache manager, or
 * distributed cache managers, or any other kind of caches. They can also 
 * be used to limit what requests are allowed to go out, and other nifty 
 * things you can imagine.
 */

public interface RequestProcessor {

    /**
     * Can this processor handle this request ?
     * Given the actual request, the processor is called to see if it can 
     * actually answer it. It this is the case, the processor should return
     * a valid IngoingReply to be forwarded back to the application.
     * @param request The request to be handled.
     * @return An instance of Reply, or <strong>null</strong> if the
     * processor was unable to answer the request.
     * @exception HttpException If the processor decies that this request
     * is erroneous, and shouldn't proceed further.
     */

    public Reply processRequest(Request request)
	throws HttpException;

}

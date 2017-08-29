// RequestEvent.java
// $Id: RequestEvent.java,v 1.1 2010/06/15 12:25:17 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http;

/**
 * The base class for request events.
 * Request events are emited to request observers (if available) while
 * requests are being processed.
 * <p>This class is the base class for all request events.
 */

public class RequestEvent {
    /**
     * Status definition - The request is now queued for processing.
     */
    public static int EVT_QUEUED = 1 ;
    /**
     * Status definition - Connection is now settle, about to emit 
     * the request to target host.
     */
    public static int EVT_CONNECTED = 2 ;
    /**
     * Status definition - Request headers are now emited, the HttpManager
     * is now waiting for the reply.
     */
    public static int EVT_EMITED = 3 ;
    /**
     * Status definition - Reply headers have been received, the reply
     * will be handed out right after this to the observer.
     */
    public static int EVT_REPLIED = 4 ;
    /**
     * Error definition - The target server has improperly closed the 
     * connection.
     */
    public static int EVT_CLOSED = 5 ;
    // FIXME doc
    public static int EVT_CONTINUE = 6;    
    public static int EVT_UNREACHABLE = 7;

    /**
     * The server instance that issued the event.
     */
    public HttpServer server = null;
    /**
     * The request that trigered the event.
     */
    public Request request = null;
    /**
     * The associated event code.
     */
    public int code = -1;

    public RequestEvent(HttpServer server, Request request, int code) {
	this.server  = server;
	this.request = request;
	this.code    = code;
    }
}



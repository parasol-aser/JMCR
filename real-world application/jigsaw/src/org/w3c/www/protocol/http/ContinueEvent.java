// ContinueEvent.java
// $Id: ContinueEvent.java,v 1.1 2010/06/15 12:25:13 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http;

import org.w3c.www.http.HTTP;

/**
 * The continue event notifies the observer of receipt of a continue packet.
 * Two phases method requires the server to emit an HTTP <code>CONTINUE</code>
 * status code before going into the second phase. This event is generated 
 * by the <code>HttpManager</code> when such an event is received.
 */

public class ContinueEvent extends RequestEvent {
    /**
     * The HTTP <code>CONTINUE</code> packet.
     */
    public Reply packet = null;

    /**
     * Create a continue event.
     * @param s The source of the event.
     * @param request The request being processed.
     * @param packet The <strong>100</strong> class reply.
     */

    public ContinueEvent(HttpServer s, Request request, Reply packet) {
	super(s, request, EVT_CONTINUE);
	this.packet = packet;
    }

    /**
     * Create a fake continue event.
     * This is usefull when upgrading HTTP/1.0 flow to HTTP/1.1.
     * @param s The source of the event.
     * @param request The request being processed.
     */

    public ContinueEvent(HttpServer s, Request request) {
	super(s, request, EVT_CONTINUE);
	this.packet = request.makeReply(HTTP.CONTINUE);
    }
}



// ProxyRequestObserver.java
// $Id: ProxyRequestObserver.java,v 1.1 2010/06/15 12:27:15 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.proxy ;

import java.io.IOException;
import java.io.InputStream;

import org.w3c.jigsaw.http.Reply;

import org.w3c.www.protocol.http.ContinueEvent;
import org.w3c.www.protocol.http.Request;
import org.w3c.www.protocol.http.RequestEvent;
import org.w3c.www.protocol.http.RequestObserver;

public class ProxyRequestObserver implements RequestObserver {
    /**
     * Our client, ie the one that sends us a request to fulfill.
     */
    org.w3c.jigsaw.http.Request request = null;
    // our calling frame, for dupRequest/Reply (FIXME, should be a static
    // somewhere
    ForwardFrame frame = null;
    /**
     * Call back, invoked by the HttpManager callback thread.
     * Each time a request status changes (due to progress in its processing)
     * this callback gets called, with the new status as an argument.
     * @param preq The pending request that has made some progress.
     * @param event The event to broadcast.
     */

    public void notifyProgress(RequestEvent event) {
	Request req = event.request;

	if ( event instanceof ContinueEvent ) {
	    // We need to forward this straight to the client:
	    // FIXME 1/ why not using req?
	    // should be check100Continue() or something
	    ContinueEvent cevent = (ContinueEvent) event;
	    if ((req.getMajorVersion() == 1) && (req.getMinorVersion() == 1)) {
		try {
		    if ((cevent.packet != null) && (frame != null)) {
			Reply r = null;
			try {
			    r = frame.dupReply(request, cevent.packet);
			    r.setStream((InputStream)null);
			} catch (Exception ex) {}
			request.getClient().sendContinue(r);
		    } else {
		        request.getClient().sendContinue();
		    }
		} catch (IOException ex) {
		}
	    }
//	    String exp = request.getExpect();
//	    if (exp != null && (exp.equalsIgnoreCase("100-continue"))) {
//		try {
//		    request.getClient().sendContinue();
//		} catch (IOException ex) {
//		    // This will fail latter on too, forget about it (!)
//		}
//	    }
	}
    }

    public ProxyRequestObserver(org.w3c.jigsaw.http.Request request) {
	this.request = request;
    }

    public ProxyRequestObserver(org.w3c.jigsaw.http.Request request,
				ForwardFrame frame) {
	this.request = request;
	this.frame = frame;
    }
}

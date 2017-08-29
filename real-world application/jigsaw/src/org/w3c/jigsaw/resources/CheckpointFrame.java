// CheckpointFrame.java
// $Id: CheckpointFrame.java,v 1.1 2010/06/15 12:20:42 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// please first read the full copyright statement in file COPYRIGHT.HTML

package org.w3c.jigsaw.resources;

import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpMessage;

import org.w3c.jigsaw.frames.HTTPFrame;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.html.HtmlGenerator;

import java.util.Date;

public class CheckpointFrame extends HTTPFrame {

    public void registerResource(FramedResource resource) {
	super.registerOtherResource(resource);
    }

 
    public CheckpointResource getChekpointResource() {
	if (getResource() instanceof CheckpointResource)
	    return (CheckpointResource) getResource();
	else 
	    return null;
    }

    /**
     * Get the content of that resources.
     * Will display some usefull commands to start/stop the attached thread
     * @param request The request to handle.
     * @exception ProtocolException If request processing failed.
     * @exception ResourceException If this resource got a fatal error.
     */

    protected Reply getOtherResource(Request request) 
	throws ProtocolException, ResourceException
    {
	CheckpointResource chkpr = getChekpointResource();
	if (chkpr == null)
	    throw new ResourceException("this frame is not attached to a "+
					"CheckpointResource. ("+
					getResource().getIdentifier()+")");
	String query = request.getQueryString();
	if ( query != null ) {
	    if ( query.equals("start") ) {
		// Start the thread if needed
		chkpr.activate();
	    } else if (query.equals("stop") ) {
		// Stop the thread
		chkpr.stop();
	    }
	}
	// Emit output:
	HtmlGenerator g = new HtmlGenerator("CheckpointResource");
	addStyleSheet(g);
	g.append("<h1>CheckpointResource status</h1>");
	g.append("<p>Checkpoint is currently "
		 , ((chkpr.thread == null) ? " stopped " : "running")
		 , ".");
	g.append("<hr>You can:<p><dl>");
	g.append("<dt><a href=\""
		 , chkpr.getURLPath()
		 , "?start\">start</a><dd>Start the checkpointer.");
	g.append("<dt><a href=\""
		 , chkpr.getURLPath()
		 , "?stop\">stop</a><dd>Stop the checkpointer.");
	g.append("</dl><hr>Last checkpoint at <strong>"
		 , ((chkpr.checkpoint == null) 
		    ? "no checkpoint run yet" 
		    : chkpr.checkpoint.toString())
		 , "</strong>.");
	Reply reply = createDefaultReply(request, HTTP.OK);
	reply.setNoCache();
	reply.setStream(g);
	return reply;
    }

}

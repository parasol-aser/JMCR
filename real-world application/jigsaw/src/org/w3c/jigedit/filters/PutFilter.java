// PutFilter.java
// $Id: PutFilter.java,v 1.2 2010/06/15 17:53:04 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.filters;

import java.io.IOException;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;

import org.w3c.jigsaw.html.HtmlGenerator;

public class PutFilter extends ResourceFilter {
    /**
     * Attribute index - The companion PutList resource's URL.
     */
    protected static int ATTR_PUTLIST = -1;

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigedit.filters.PutFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register the PutList URL attribute:
	a = new StringAttribute("put-list"
				, null
				, Attribute.EDITABLE|Attribute.MANDATORY);
	ATTR_PUTLIST = AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * Resolve the companion PutList URL attribute into a resource:
     */

    private ResourceReference list = null;
    protected synchronized ResourceReference resolvePutListResource() {
	// Prepare for lookup:
	ResourceReference rr_root = null;
	rr_root = ((httpd) getServer()).getRootReference();
	FramedResource root = null;
	root = ((httpd) getServer()).getRoot();
	String       u  = getPutListURL();
	if ( u == null )
	    return null;
	// Do the lookup:
	ResourceReference r_target = null;
	try {
	    LookupState  ls = new LookupState(u);
	    LookupResult lr = new LookupResult(rr_root);
	    root.lookup(ls, lr);
	    r_target = lr.getTarget();
	} catch (Exception ex) {
	    r_target = null;
	}
	if (r_target != null) {
	  try {
	    Resource target = r_target.lock();
	    if (! (target instanceof PutListResource) )
	      r_target = null;
	    else
	      list = r_target;
	  } catch (InvalidResourceException ex) {
	    // continue
	  } finally {
	    r_target.unlock();
	  }
	}
	return r_target;
    }

    /**
     * Get our companion PutListResource's URL.
     * @return The URL encoded as a String, or <strong>null</strong> if
     * undefined.
     */

    public String getPutListURL() {
	return getString(ATTR_PUTLIST, null);
    }

    /**
     * Catch PUTLIST assignments.
     * @param idx The attribute being updated.
     * @param value It's new value.
     */

    public void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if ( idx == ATTR_PUTLIST ) {
	    synchronized(this) {
		list = null;
	    }
	}
    }

    /**
     * Nothing done in the ingoingFilter.
     * We wait until the outgoigFilter.
     * @param request The request that is about to be processsed.
     */

    public ReplyInterface ingoingFilter(RequestInterface req) {
	Request request = (Request) req;
	String expect = request.getExpect();
	if (expect != null) {
	    if (expect.startsWith("100")) { // expect 100?
		Client client = request.getClient();
		if (client != null) {
		    try {
			client.sendContinue();
		    } catch (java.io.IOException ex) {
		    }
		}
	    }
	}
	//register request, but this must be confirmed.
	if ( request.getMethod().equals("DELETE") &&
	     (request.getTargetResource() != null) ) {
	    boolean done = false;
	    synchronized (this) {
		ResourceReference rr = resolvePutListResource();
		PutListResource   l  = null;
		if (rr != null) {
		    try {
			l = (PutListResource) rr.lock();
			if ( l != null ) {
			    l.registerDeleteRequest(request);
			    done = true;
			}
		    } catch (InvalidResourceException ex) {
			done = false;
		    } finally {
			rr.unlock();
		    }
		}
	    }
	    if (! done) {
		httpd s = (httpd) getServer();
		s.errlog(getClass().getName()+
			 ": unable to resolve companion PutListResource at "+
			 getPutListURL());
	    }
	}
	return null;
    }

    protected HtmlGenerator getHtmlGenerator(String title) {
	HtmlGenerator g = new HtmlGenerator(title);
	g.addStyle("BODY {color: black; background: white; "+
		   "font-family: serif; margin-top: 35px }\n");
	return g;
    }

    /**
     * Catch successfull PUTs, and keep track of them.
     * @param request The original request.
     * @param reply The original reply.
     * @return Always <strong>null</strong>.
     */

    public ReplyInterface outgoingFilter(RequestInterface req, 
					 ReplyInterface rep) 
    {
        Request request = (Request) req;
	Reply   reply   = (Reply) rep;
	int     status  = PutListResource.FILE_UC;
	boolean put     = false;
	// Is this a successfull PUT request ?
	if (((put = request.getMethod().equals("PUT")) ||
	     request.getMethod().equals("DELETE"))
	    && ((reply.getStatus()/100) == 2)) {
	    // Cool, keep track of the modified file:
	    ResourceReference rr   = null;
	    PutListResource   l    = null;
	    boolean           done = false;
	    synchronized (this) {
		rr = resolvePutListResource();
		if (rr != null) {
		    try {
			l = (PutListResource) rr.lock();
			if ( l != null ) {
			    if (put)
				status = l.registerRequest(request);
			    else
				status = l.confirmDelete(request);
			    done = true;
			}
		    } catch (InvalidResourceException ex) {
			done = false;
		    } finally {
			rr.unlock();
		    }
		}
	    }
	    // Make sure we did something:
	    if ( !done ) {
		httpd s = (httpd) getServer();
		s.errlog(getClass().getName()+
			 ": unable to resolve companion PutListResource at "+
			 getPutListURL());
	    } 
	    
	    switch (status) {
	    case PutListResource.FILE_UC:
	    case PutListResource.FILE_PB:
	    case PutListResource.FILE_DEL:
		return null;
	    case PutListResource.FILE_MG:
		Reply   msg = request.makeReply(HTTP.CONFLICT);
		HtmlGenerator g = getHtmlGenerator("Warning");
		g.append ("<H1>Warning</H1> The file on publish space has "+
			  "been modified directly but attempting to merge "+
			  "has succeed.<p>"+
			  "You should update the file before editing "+
			  "it again.");
		msg.setStream(g);
		return msg;
	    case PutListResource.FILE_CF:
		Reply error = request.makeReply(HTTP.CONFLICT);
		HtmlGenerator gerr = getHtmlGenerator("Warning");
		gerr.append ("<H1>Warning</H1> The file on publish space has "+
			     "been modified directly and attempting to merge"+
			     " has failed.<p>"+
			     "Ask your system administrator.");
		error.setStream(gerr);
		return error;
	    default:
		return null;
	    }
	}
	return null;
    }

    public void initialize(Object values[]) {
	super.initialize(values);
    }

}

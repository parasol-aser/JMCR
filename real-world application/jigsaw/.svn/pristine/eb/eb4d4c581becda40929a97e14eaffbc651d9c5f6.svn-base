// ServletMapperFrame.java
// $Id: ServletMapperFrame.java,v 1.2 2010/06/15 17:52:51 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;

import org.w3c.jigsaw.frames.HTTPFrame;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;

/**
 * Perform an internal redirect.
 */
public class ServletMapperFrame extends HTTPFrame {
    /**
     * Attributes index - The index for the target attribute.
     */
    protected static int ATTR_TARGET = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	// Get a pointer to our class:
	try {
	    cls = Class.forName("org.w3c.jigsaw.servlet.ServletMapperFrame") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	a = new StringAttribute("servlet-url"
				, null
				, Attribute.EDITABLE);
	ATTR_TARGET = AttributeRegistry.registerAttribute(cls, a) ;
    }

    protected String getTarget() {
	return (String) getValue(ATTR_TARGET, null);
    }

    /**
     * Gets, from the first line of the HTTP request, 
     * the part of this request's URI that is to the left of any query string.
     */
    public  String getRequestURI(Request request)
    {
	String uri = null;
	//fixme test
	if (request.isProxy()) {
	    uri = request.getURL().toExternalForm();
	} else {
	    uri = request.getURLPath();
	}
	if (request.hasQueryString()) {
	    String query = request.getQueryString();
	    int idx = uri.lastIndexOf(query);
	    if (idx != -1) {
		uri = uri.substring(0, idx-1);
	    }
	}
	return uri;
    }

    /**
     * Perform the request.
     * @param req The request to handle.
     * @exception ProtocolException If request couldn't be processed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public ReplyInterface perform(RequestInterface req) 
	throws ProtocolException, ResourceException
    {
	Reply        reply  = (Reply) performFrames(req);
	if (reply != null) 
	    return reply;
	Request request = (Request) req;
	httpd    server = (httpd) getServer();
	String     host = request.getHost();
	request.setState(Request.ORIG_URL_STATE, request.getURL());
	request.setState(JigsawRequestDispatcher.REQUEST_URI_P, 
			 getRequestURI(request));
	request.setState(JigsawRequestDispatcher.QUERY_STRING_P, 
			 request.getQueryString());
	request.setState(JigsawRequestDispatcher.SERVLET_PATH_P, getURLPath());
	try {
	    String target = null;
	    if (request.hasQueryString())
		target = getTarget()+"?"+request.getQueryString();
	    else
		target = getTarget();

	    if (host == null) {
		request.setURL(new URL(server.getURL(), target));
	    } else {
		int ic = host.indexOf(':');
		// we will take care of '[' later (ipv6 address)
		if ( ic < 0 ) {
		    request.setURL(new URL(server.getURL().getProtocol(),
					   host, target));
		} else {
		    request.setURL(new URL(server.getURL().getProtocol(),
					   host.substring(0, ic),
					   Integer.parseInt(
					       host.substring(ic+1)),
					   target));
		}
	    }
	    request.setInternal(true);
	} catch (MalformedURLException ex) {
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	    error.setContent("<html><head><title>Server Error</title>"+
			     "</head><body><h1>Server misconfigured</h1>"+
			     "<p>The resource <b>"+getIdentifier()+"</b>"+
			     "has an invalid target attribute : <p><b>"+
			     getTarget()+"</b></body></html>");      
	    throw new HTTPException (error);
	}
	return server.perform(request);
    }

    protected  boolean lookupResource(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	// Get the extra path information:
	String extraPath = ls.getRemainingPath(true);
	if ((extraPath == null) || extraPath.equals(""))
	    extraPath = "/";
	// Keep this path info into the request, if possible:
	Request request = (Request) ls.getRequest();
	if ( request != null ) {
	    if (request.getState(JigsawRequestDispatcher.PATH_INFO_P) == null)
		request.setState(JigsawRequestDispatcher.PATH_INFO_P, 
				 extraPath);
	}
	lr.setTarget(resource.getResourceReference());
	return super.lookupResource(ls, lr);
    }
}

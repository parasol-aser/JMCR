// RedirecterFrame.java
// $Id: RedirecterFrame.java,v 1.2 2010/06/15 17:52:52 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.frames;

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
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;

/**
 * Perform an internal redirect.
 */
public class RedirecterFrame extends HTTPFrame {
    /**
     * Name of the state to hold the PATH_INFO in the request.
     */
    public final static 
	String PATH_INFO = 
	"org.w3c.jigsaw.resources.RedirecterFrame.PathInfo";

    /**
     * Attributes index - The index for the target attribute.
     */
    protected static int ATTR_TARGET = -1 ;
    /**
     * Attribute index - The methods affected by this frame
     */
    protected static int ATTR_METHODS = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	// Get a pointer to our class:
	try {
	    cls = Class.forName("org.w3c.jigsaw.frames.RedirecterFrame") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	a = new StringAttribute("target"
				, null
				, Attribute.EDITABLE);
	ATTR_TARGET = AttributeRegistry.registerAttribute(cls, a) ;
	// The affected methods
	a = new StringArrayAttribute("methods"
				     , null
				     , Attribute.EDITABLE) ;
	ATTR_METHODS = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * Get the list of methods affected by the redirection
     * @return An array of String giving the name of the redirected methods,
     *    or <strong>null</strong>, in wich case <em>all</em> methods are
     *    to be redirected.
     */
    public String[] getMethods() {
	return (String[]) getValue(ATTR_METHODS, null) ;
    }

    /**
     * Get the location for the internal redirection
     * @return a string, containing the relative path or absolute PATH.
     */
    protected String getTarget() {
	return (String) getValue(ATTR_TARGET, null);
    }

    /**
     * Lookup the target resource when associated with an unknown resource.
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception ProtocolException If an error relative to the protocol occurs
     */
    protected boolean lookupResource(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	String methods[] = getMethods();
	
	if (ls.hasRequest() && (methods != null)) {
	    Request request = (Request) ls.getRequest();
	    String reqmeth = request.getMethod();
	    boolean affected = false;
	    for (int i=0; i< methods.length; i++) {
		if (reqmeth.equals(methods[i])) {
		    affected = true;
		    break;
		}
	    }
	    if (!affected) {
		return super.lookupResource(ls, lr);
	    }
	}
	// Perform our super-class lookup strategy:
	if ( super.lookupOther(ls, lr) ) {
	    return true;
	} 
	// Compute PATH INFO, store it as a piece of state in the request:
	StringBuffer pathinfo = new StringBuffer();
	while ( ls.hasMoreComponents() ) {
	    pathinfo.append('/');
	    pathinfo.append(ls.getNextComponent());
	}
	if (ls.hasRequest() ) {
	    Request request = (Request) ls.getRequest();
	    String reqfile = request.getURL().getFile();
	    if (reqfile.endsWith("/")) {
		pathinfo.append('/');
	    }
	    request.setState(PATH_INFO, pathinfo.toString());
	}
	lr.setTarget(resource.getResourceReference());
	return true;
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
	// check if we must use our parent perform call
	String methods[] = getMethods();
	
	if (methods != null) {
	    String reqmeth = request.getMethod();
	    boolean affected = false;
	    for (int i=0; i< methods.length; i++) {
		if (reqmeth.equals(methods[i])) {
		    affected = true;
		    break;
		}
	    }
	    if (!affected) {
		return super.perform(request);
	    }
	}
	httpd    server = (httpd) getServer();
	String     host = request.getHost(); 
	// why this??? commenting while investigating
        //request.setReferer(getURLPath());
	try {
	    String target = null;
	    String pathinfo = (String) request.getState(PATH_INFO);
	    if (pathinfo != null)
		target = getTarget()+pathinfo;
	    else
		target = getTarget();

	    if (request.hasQueryString())
		target += "?"+request.getQueryString();

	    // save the original URL
	    request.setState(Request.ORIG_URL_STATE, request.getURL());
	    // and mark content location
	    request.setState(STATE_CONTENT_LOCATION, "true");
	    if (host == null) {
		request.setURL(new URL(server.getURL(), target));
	    } else {
//		URL newurl = new URL (new URL(server.getURL().getProtocol(),
//					      host, 
//					      server.getURL().getFile()),
//				      target);
		URL newurl = new URL(request.getURL(), target);
		String newhost = null;
		if (newurl.getPort() != -1 && 
		    (newurl.getProtocol().equalsIgnoreCase("http") &&
			(newurl.getPort() != 80))) {
		    newhost = newurl.getHost() + ":" + newurl.getPort();
		} else {
		    newhost = newurl.getHost();
		}
		request.setURL(newurl);
		request.setHost(newhost);
	    }
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
}

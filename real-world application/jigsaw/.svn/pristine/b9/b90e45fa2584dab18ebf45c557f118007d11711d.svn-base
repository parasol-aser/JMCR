// RelocateFrame.java
// $Id: RelocateFrame.java,v 1.2 2010/06/15 17:52:52 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.frames;

import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ServerInterface;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;

import org.w3c.jigsaw.html.HtmlGenerator;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;

/**
 * Emit a HTTP redirect.
 */
public class RelocateFrame extends HTTPFrame {

    /**
     * Name of the state to hold the PATH_INFO in the request.
     */
    public final static 
	String PATH_INFO = 
	"org.w3c.jigsaw.resources.RelocateResource.PathInfo";

    /**
     * Attribute index - The relocation location.
     */
    protected static int ATTR_LOCATION = -1 ;
    /**
     * Attribute index - Should we also handle extra path infos ?
     */
    protected static int ATTR_HANDLE_PATHINFO = -1;
    /**
     * Attribute index - Is the relocation permanent?
     */
    protected static int ATTR_PERMANENT_REDIRECT = -1;
    /**
     * Attribute index - SHould we use the ambiguous 302?
     */
    protected static int ATTR_USE_302 = -1;
    /**
     * Attribute index - The methods affected by this frame
     */
    protected static int ATTR_METHODS = -1 ;

    static {
	Attribute a = null ;
	Class     c = null ;

	try {
	    c = Class.forName("org.w3c.jigsaw.frames.RelocateFrame");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The location attribute
	a = new StringAttribute("location"
				, null
				, Attribute.EDITABLE|Attribute.MANDATORY) ;
	ATTR_LOCATION = AttributeRegistry.registerAttribute(c, a) ;
	// The handle path info attribute
	a = new BooleanAttribute("handle-pathinfo"
				 , Boolean.TRUE
				 , Attribute.EDITABLE);
	ATTR_HANDLE_PATHINFO = AttributeRegistry.registerAttribute(c, a);
	// the permanent redirection attribute
	a = new BooleanAttribute("permanent-redirect"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_PERMANENT_REDIRECT = AttributeRegistry.registerAttribute(c, a);
	// should we use the ambiguous 302 response code?
	a = new BooleanAttribute("use-usual-response"
				 , Boolean.TRUE
				 , Attribute.EDITABLE);
	ATTR_USE_302 = AttributeRegistry.registerAttribute(c, a);
	// The affected methods
	a = new StringArrayAttribute("methods"
				     , null
				     , Attribute.EDITABLE) ;
	ATTR_METHODS = AttributeRegistry.registerAttribute(c, a) ;
    }

    /**
     * Get the location for the relocation
     * @return a string, containing the relative path or absolute
     */
    public String getLocation() {
	return (String) getValue(ATTR_LOCATION, null) ;
    }

    /**
     * Get the list of methods affected by the redirect
     * @return An array of String giving the name of the redirected methods,
     *    or <strong>null</strong>, in wich case <em>all</em> methods are
     *    to be redirected.
     */
    public String[] getMethods() {
	return (String[]) getValue(ATTR_METHODS, null) ;
    }

    /**
     * Get the path info value
     * @return a boolean
     */
    public boolean checkHandlePathInfo() {
	return getBoolean(ATTR_HANDLE_PATHINFO, true);
    }

    /**
     * Get the permanent redirect flag
     * @return a boolean
     */
    public boolean checkPermanentRedirect() {
	return getBoolean(ATTR_PERMANENT_REDIRECT, false);
    }

     /**
     * Get the "use ambigous 302 response code" flag
     * @return a boolean
     */
    public boolean checkUse302() {
	return getBoolean(ATTR_USE_302, true);
    }   

     /**
     * Lookup the target resource (dispath to more specific lookup methods).
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception ProtocolException If an error relative to the protocol occurs
     * @see #lookupDirectory
     * @see #lookupFile
     * @see #lookupOther
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
        } else if ( ! checkHandlePathInfo() ) {
            return false;
        }
	// Compute PATH INFO, store it as a piece of state in
	// the request:
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
     * build the redirect reply based on the request and the current
     * configuration
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @return a Reply
     */
    private Reply getRedirectReply(Request request)
	throws ProtocolException
    {
	String location = getLocation() ;
	if ( location == null ) {
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
	    error.setContent("The target RelocateResource doesn't define the"
			     + " relocation location. The server is "
			     + " misconfigured.") ;
	    throw new HTTPException(error) ;
	} else {
	    Reply  reply = null;
	    URL    loc      = null;
	    if (checkUse302()) {
		reply = request.makeReply(HTTP.FOUND) ;
	    } else {
		if (checkPermanentRedirect()) {
		    reply = request.makeReply(HTTP.MOVED_PERMANENTLY) ;
		} else {
		    reply = request.makeReply(HTTP.TEMPORARY_REDIRECT) ;
		}
	    }
	    try {
		httpd  server = (httpd) getServer();
		String   host = request.getHost(); 
		if (host == null)
		    loc = new URL(server.getURL(), location);
		else {
		    int ic = host.indexOf(':');
		    if (ic < 0 ) {
			loc = new URL(new URL(server.getURL().getProtocol(),
					      host,server.getURL().getFile()),
				      location);
		    } else {
			loc = new URL(new URL(server.getURL().getProtocol(),
					      host.substring(0, ic),
					      Integer.parseInt(
						  host.substring(ic+1))
					      ,server.getURL().getFile()),
				      location);
		    }
		}
		if (checkHandlePathInfo()) {
		    String pathinfo = (String) request.getState(PATH_INFO);
		    // Given the way pathinfo is computed, it starts with a /
		    try {
			if (pathinfo != null) {
			    loc = new URL(loc.toExternalForm()+pathinfo);
			}
		    } catch (MalformedURLException ex) {
			resource.getServer().errlog(resource, 
					     "This resource handle Pathinfo "+
					     "but the request has an invalid "+
					     "PATH_INFO state.");
		    }
		    if (request.hasQueryString()) {
			try {
			    loc = new URL(loc.toExternalForm() + "?" +
					  request.getQueryString());
			} catch (MalformedURLException ex) {
			    resource.getServer().errlog(resource, 
							"This resource handle "
							+"Pathinfo but the "
							+"request has an " 
							+"invalid "+
							"PATH_INFO state.");
			}
		    }
		}
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	    reply.setLocation(loc);
	    HtmlGenerator g = new HtmlGenerator("Moved");
	    g.append("<P>This resources has moved, click on the link if your"
		     + " browser doesn't support automatic redirection<BR>"+
		     "<A HREF=\""+loc.toExternalForm()+"\">"+
		     loc.toExternalForm()+"</A>");
	    reply.setStream(g);
	    return reply ;
	}
    }

    /**
     * The GET method, may emit a redirect 
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply get(Request request)
	throws ProtocolException, ResourceException
    {
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
		return super.get(request);
	    }
	}
	// now we can modify it :)
	return getRedirectReply(request);
    }

    /**
     * The HEAD method, may emit a redirect 
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply head(Request request)
	throws ProtocolException, ResourceException
    {
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
		return super.head(request);
	    }
	}
	// now we can modify it :)
	Reply reply = getRedirectReply(request);
	reply.setStream((InputStream) null);
	return reply ;
    }

    /**
     * The PUT method, may emit a redirect, otherwise uses its parent put
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply put(Request request)
	throws ProtocolException, ResourceException
    {
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
		return super.put(request);
	    }
	}
	// now we can modify it :)
	return getRedirectReply(request);
    }
    /**
     * The POST method, may emit a redirect, otherwise uses its parent put
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply post(Request request)
	throws ProtocolException, ResourceException
    {
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
		return super.post(request);
	    }
	}
	// now we can modify it :)
	return getRedirectReply(request);
    }
}

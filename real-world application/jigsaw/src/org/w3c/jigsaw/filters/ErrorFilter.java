// ErrorFilter.java
// $Id: ErrorFilter.java,v 1.2 2010/06/15 17:52:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FilterInterface;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ServerInterface;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HeaderValue;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;


/**
 * The ErroFilter class allows you to customize and enhance error messages.
 * This filter will catch all errors on their way back to the client, and
 * use internal requests to provide a nice customizable error message.
 * <p>You can use any resources (including server side includes, content
 * negotiated resources, etc) to deliver error messages.
 */

public class ErrorFilter extends ResourceFilter {
    /**
     * A request state, to avoid looping on errors about errors.
     */
    protected static final String ERRED = "org.w3c.jigsaw.filters.ErrorFilter";

    /**
     * Attribute index - The base URL for error messages.
     */
    protected static int ATTR_BASEURL = -1;
    /**
     * Attribute index - The common extension for error messages (can be null).
     */
    protected static int ATTR_EXTENSION = -1;

    static {
	Attribute a   = null;
	Class     cls = null;

	try {
	    cls = Class.forName("org.w3c.jigsaw.filters.ErrorFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Declare the error base URL attribute:
	a = new StringAttribute("base-url"
				, "/errors"
				, Attribute.EDITABLE);
	ATTR_BASEURL = AttributeRegistry.registerAttribute(cls, a);
	// Declare the extension attribute:
	a = new StringAttribute("extension"
				, "html"
				, Attribute.EDITABLE);
	ATTR_EXTENSION = AttributeRegistry.registerAttribute(cls, a);
    }

    /**
     * Get the base URL describing the error directory.
     * @return The base URL.
     */

    public String getBaseURL() {
	return (String) getValue(ATTR_BASEURL, null);
    }

    /**
     * Get the value of the extension attribute.
     * @return A String, for the common extension to error messages, or 
     * <strong>null</strong> if undefined.
     */

    public String getExtension() {
	return (String) getValue(ATTR_EXTENSION, null);
    }

    /**
     * Compute the path for the given status code.
     * @return A path leading to the customizable error message for the 
     * given status code.
     */

    public String getErrorResource(int status) {
	String ext = getExtension() ;
	if ( ext != null ) {
	    return getBaseURL()+"/"+Integer.toString(status)+"."+ext;
	} else {
	    return getBaseURL()+"/"+Integer.toString(status);
	}
    }

    /**
     * This one just makes sure the outgoing filter gets called.
     * @param request The original request to be handled.
     * @exception ProtocolException If processing should be interrupted,
     * because an abnormal situation occured. 
     */

    public ReplyInterface ingoingFilter(RequestInterface request) 
	throws ProtocolException
    {
	return null;
    }

    /**
     * Re-compute error message.
     * This filter uses internal redirection to get the error message body.
     * In case of failure, the original reply is returned, otherwise, a new
     * reply is gotten from the appropriate error resource, and is returned.
     * @param request The request that has been handled.
     * @param reply The reply, as emited by the original resource.
     * @return A new error reply, having the same status code, and 
     * authentication information then the given reply, but enhanced
     * with the error resource body.
     * @exception ProtocolException If processing should be interrupted,
     * because an abnormal situation occured. 
     */

    public ReplyInterface outgoingFilter(RequestInterface req, 
					 ReplyInterface rep) 
	throws ProtocolException
    {
	Request request = (Request) req;
	Reply   reply   = (Reply) rep;
	// Filter valid replies:
	int status = reply.getStatus();
	switch (status/100) {
	case 1:
	case 2:
	case 3:
	case 10:
	    return null;
	}
	// Filter replies that are already taken care of:
	if ( request.hasState(ERRED) )
	    return null;
	// Hack error replies:
	Request ereq  = (Request) request.getClone();
	Reply   erep  = null;
	try {
	    ereq.setState(ERRED, Boolean.TRUE);
	    ereq.setURLPath(getErrorResource(status));
	    if (request.getMethod().equals(HTTP.HEAD)) {
		ereq.setMethod(HTTP.HEAD);
	    } else {
		ereq.setMethod(HTTP.GET);
	    }
	    // remove conditional statements
	    ereq.setIfModifiedSince(-1);
	    ereq.setIfUnmodifiedSince(-1);
	    ereq.setIfMatch(null);
	    ereq.setIfNoneMatch(null);
	    if (ereq.getIfRange() != null) {
		ereq.setIfRange(null);
	    }
	    ereq.setCacheControl(null);
	    erep = (Reply) getServer().perform(ereq);
	    // Hack back the original reply into the new reply:
	    // - Put back the status
	    HeaderValue v = null;
	    erep.setStatus(reply.getStatus());
	    // - Put back the authenticate informations
	    v = reply.getHeaderValue(reply.H_WWW_AUTHENTICATE);
	    erep.setHeaderValue(reply.H_WWW_AUTHENTICATE, v);
	    // - Put back the proxy authenticate informations
	    v = reply.getHeaderValue(reply.H_PROXY_AUTHENTICATE);
	    erep.setHeaderValue(reply.H_PROXY_AUTHENTICATE, v);
	} catch (Exception ex) {
	    return reply;
	}
	return erep;
    }

    /**
     * We do catch exceptions, just in case we can customize the error.
     * @param request The request tha triggered the exception.
     * @param ex The exception.
     * @param filters Remaining filters to be called.
     * @param idx Current filter index within above array.
     */

    public ReplyInterface exceptionFilter(RequestInterface request,
					  ProtocolException ex,
					  FilterInterface filters[],
					  int idx) 
    {
	Reply reply = (Reply) ex.getReply();
	if ( reply != null ) {
	    try {
		return outgoingFilter(request, reply, filters, idx);
	    } catch (ProtocolException exx) {
	    }
	}
	return null;
    }

    public void initialize(Object values[]) {
	super.initialize(values);
    }
	
}

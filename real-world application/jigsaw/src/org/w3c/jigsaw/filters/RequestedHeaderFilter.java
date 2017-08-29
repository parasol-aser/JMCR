// RequestedHeaderFilter.java
// $Id: RequestedHeaderFilter.java,v 1.2 2010/06/15 17:52:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.FilterInterface;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.StringArrayAttribute;

import org.w3c.tools.resources.ProtocolException;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpMessage;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.html.HtmlGenerator;

/**
 * Enforces a specific header value on all replies.
 * Usefull for testing.
 */

public class RequestedHeaderFilter extends ResourceFilter {
    /**
     * Attribute index - The header name to add to replies.
     */
    protected static int ATTR_HEADER_NAMES = -1;
    /**
     * Attribute index - Are we sending the list of headers in HTML.
     */
    protected static int ATTR_SEND_HEADER_LIST = -1;

    static {
	Class c = null;
	Attribute a = null;
	try {
	    c = Class.forName("org.w3c.jigsaw.filters.RequestedHeaderFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register the header names attribute:
	a = new StringArrayAttribute("header-names"
				, null
				, Attribute.EDITABLE);
	ATTR_HEADER_NAMES = AttributeRegistry.registerAttribute(c, a);
	// Declare the day_repeat attribute
	a = new BooleanAttribute("send-header-list"
				 , Boolean.FALSE
				 , Attribute.EDITABLE) ;
	ATTR_SEND_HEADER_LIST = AttributeRegistry.registerAttribute(c, a) ;
    }

    private String headerNames[] = null;
    private boolean sendHeaderList = false;

    /**
     * We override setValues to compute locally everything we need
     8 As the purpose here is to bail out fast when needed
     * @param idx The index of the attribute to modify.
     * @param value The new attribute value.
     */
    public void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if (idx == ATTR_HEADER_NAMES) {
		headerNames = (String [])value;
	} else if (idx == ATTR_SEND_HEADER_LIST) {
	    sendHeaderList = ((Boolean)value).booleanValue();
	}
    }

    /**
     * Initialize the filter.
     */

    public void initialize(Object values[]) {
	super.initialize(values);
	headerNames = (String []) getValue(ATTR_HEADER_NAMES, null);
	sendHeaderList = getBoolean(ATTR_SEND_HEADER_LIST, false);
    }

    /**
     * Get the header to set, if any.
     * @return A String encoded header name, or <strong>null</strong>.
     */

    protected String[] getHeaderNames() {
	String value[] = (String []) getValue(ATTR_HEADER_NAMES, null);
	return value;
    }

    protected boolean getSendHeaderList() {
	return getBoolean(ATTR_SEND_HEADER_LIST, false);
    }

    /**
     * @return A boolean, <strong>true</strong> if all headers are present
     */
    private boolean checkHeaders(Request request) {
	if (headerNames != null) {
	    int nlength = headerNames.length;
	    for (int i=0; i<nlength; i++) {
		if (!request.hasHeader(headerNames[i])) {
		    return false;
		}
	    }
	}
	return true;
    }

    /**
     * @return A Reply instance, if the filter did know how to answer
     * the request without further processing, <strong>null</strong> 
     * otherwise. 
     * @exception ProtocolException If processing should be interrupted,
     * because an abnormal situation occured. 
     */ 
    public ReplyInterface ingoingFilter(RequestInterface request) 
	throws ProtocolException
    {
	Request req = (Request) request;
	Reply   rep = null;
	if (checkHeaders(req)) {
	    return null;
	}
	rep = req.makeReply(HTTP.FORBIDDEN);
	HtmlGenerator g = new HtmlGenerator("Forbidden - Headers missing");
	g.append("Some Headers, mandatory for this resource, are missing.");
	if (sendHeaderList && (headerNames != null)) {
	    g.append("<ul>");
	    for (int i=0; i<headerNames.length; i++) {
		g.append("<li>");
		g.appendAndEscape(headerNames[i]);
		g.append("</li>");
	    }
	    g.append("</ul>");
	}
	rep.setStream(g);
	return rep;
    }

    /**
     * The outgoing filter decorates the reply appropriately.
     * @param request The original request.
     * @param reply The originial reply.
     * @return Always <strong>null</strong>.
     * @exception ProtocolException If processing should be interrupted,
     * because an abnormal situation occured. 
     */

    public ReplyInterface outgoingFilter(RequestInterface req,
					 ReplyInterface rep) 
	throws ProtocolException
    {
	return null;
    }

    public ReplyInterface exceptionFilter(RequestInterface request,
					  ProtocolException ex,
					  FilterInterface filters[],
					  int i) {
	return null;
    }
}

// HeaderFilter.java
// $Id: HeaderFilter.java,v 1.2 2010/06/15 17:52:54 smhuang Exp $
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
import org.w3c.tools.resources.StringAttribute;

import org.w3c.tools.resources.ProtocolException;

import org.w3c.www.http.HttpMessage;

import org.w3c.jigsaw.http.Reply;

/**
 * Enforces a specific header value on all replies.
 * Usefull for testing.
 */

public class HeaderFilter extends ResourceFilter {
    /**
     * Attribute index - The header name to add to replies.
     */
    protected static int ATTR_HEADER_NAME = -1;
    /**
     * Attribute index - The header value.
     */
    protected static int ATTR_HEADER_VALUE = -1;
    /**
     * Attribute index - SHould we use no-cache on that header.
     */
    protected static int ATTR_NOCACHE = -1;
    /**
     * Attribute index - Should we use connection on that header.
     */
    protected static int ATTR_CONNECTION = -1;

    static {
	Class c = null;
	Attribute a = null;
	try {
	    c = Class.forName("org.w3c.jigsaw.filters.HeaderFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register the header name attribute:
	a = new StringAttribute("header-name"
				, null
				, Attribute.EDITABLE);
	ATTR_HEADER_NAME = AttributeRegistry.registerAttribute(c, a);
	// Register the header value attribute.
	a = new StringAttribute("header-value"
				, null
				, Attribute.EDITABLE);
	ATTR_HEADER_VALUE = AttributeRegistry.registerAttribute(c, a);
	// Register the nocache attribute.
	a = new BooleanAttribute("no-cache"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_NOCACHE = AttributeRegistry.registerAttribute(c, a);
	// Register the connection attribute.
	a = new BooleanAttribute("connection"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_CONNECTION = AttributeRegistry.registerAttribute(c, a);
    }

	
    /**
     * Get the header to set, if any.
     * @return A String encoded header name, or <strong>null</strong>.
     */

    public String getHeaderName() {
	String value = getString(ATTR_HEADER_NAME, null);
	return value;
    }

    /**
     * Get the header value to set, if any.
     * @return A String encoded value for the header to set, or <strong>
     * null</strong>.
     */

    public String getHeaderValue() {
	return getString(ATTR_HEADER_VALUE, null);
    }

    /**
     * Should we add this header's name to the <code>no-cache</code> 
     * directive.
     * @return A boolean.
     */

    public boolean checkNoCache() {
	return getBoolean(ATTR_NOCACHE, false);
    }

    /**
     * Should we add this header to the connection header.
     * @return A boolean.
     */

    public boolean checkConnection() {
	return getBoolean(ATTR_CONNECTION, false);
    }

    private ReplyInterface modifyHeaders(ReplyInterface rep) {
	Reply reply = (Reply) rep;
	String hname = getHeaderName();
	if ( hname != null ) {
	    String hvalue = getHeaderValue();
	    if ( hvalue == null ) {
		reply.removeHeader(hname);
	    } else {
		reply.setValue(hname, hvalue);
	    }
	    if ( checkNoCache() )
		reply.addNoCache(hname);
	    if ( checkConnection() )
		reply.addConnection(hname);
	}
	return null;
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
	return null;
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
	return modifyHeaders(rep);
    }

    public ReplyInterface exceptionFilter(RequestInterface request,
					  ProtocolException ex,
					  FilterInterface filters[],
					  int i) {
	Reply rep = (Reply) ex.getReply();
	if (rep != null) {
	    return modifyHeaders(rep);
	}
	return null;
    }
}

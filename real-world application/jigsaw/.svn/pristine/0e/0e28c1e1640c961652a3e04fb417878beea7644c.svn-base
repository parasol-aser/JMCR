// URISizeLimiterFilter.java
// $Id: URISizeLimiterFilter.java,v 1.2 2010/06/15 17:52:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters ;

import java.util.Hashtable ;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceFilter;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.html.HtmlGenerator ;

/**
 * This filters limit the size of URI.
 */

public class URISizeLimiterFilter extends ResourceFilter {
    /**
     * Attribute index - The maximum allowed size of the requested URL
     */
    protected static int ATTR_SIZE_LIMIT = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;

	try {
	    cls = Class.forName("org.w3c.jigsaw.filters.URISizeLimiterFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The limit attribute
	a = new IntegerAttribute("limit"
				 , new Integer(8192)
				 , Attribute.EDITABLE) ;
	ATTR_SIZE_LIMIT = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * Get the maximum size of the URL.
     */

    public int getLimit() {
	return getInt(ATTR_SIZE_LIMIT, 8192) ; /* default to 8k */
    }

    /**
     * Check the size of the URL
     * if it is more than the size defined, an error is sent back
     * @return a Reply or null if successful
     */

    public synchronized 
	ReplyInterface ingoingFilter (RequestInterface req) 
    {
	Request request = (Request) req;
	int limit = getLimit();

	if (request.getURL().toExternalForm().length() > limit) {
	    Reply error = request.makeReply(HTTP.REQUEST_URI_TOO_LONG);
	    HtmlGenerator g = new HtmlGenerator("Request URI Too Long");
	    g.append ("Your request should have an URI of less than " +
		      limit + " bytes");
	    error.setStream(g);
	    return error;
	}
	return null;
    }
}



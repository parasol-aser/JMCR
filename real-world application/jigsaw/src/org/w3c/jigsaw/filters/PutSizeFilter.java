// PutSizeFilter.java
// $Id: PutSizeFilter.java,v 1.2 2010/06/15 17:52:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceFilter;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

public class PutSizeFilter extends ResourceFilter {
    /**
     * Attribute index - The maximum size of the put document
     */

    protected static int ATTR_PUTSIZE = -1;
    protected static int ATTR_STRICT  = -1;

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigsaw.filters.PutSizeFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register the PutList URL attribute:
	a = new IntegerAttribute("put-size"
				 , new Integer(65536)
				 , Attribute.EDITABLE|Attribute.MANDATORY);
	ATTR_PUTSIZE = AttributeRegistry.registerAttribute(c, a);
	a = new BooleanAttribute("strict"
				 , new Boolean(true)
				 , Attribute.EDITABLE|Attribute.MANDATORY);
	ATTR_STRICT = AttributeRegistry.registerAttribute(c, a);
    }

    private Reply notifyFailure(Request request, boolean no_size) {
	Reply er = null;
	if (request.getExpect() != null)
	    er = request.makeReply(HTTP.EXPECTATION_FAILED);
	else {
	    if (no_size)
		er = request.makeReply(HTTP.LENGTH_REQUIRED);
	    else
		er = request.makeReply(HTTP.REQUEST_ENTITY_TOO_LARGE);
	}
	er.setContent("<P>You are not allowed to PUT documents more than " +
		      getInt(ATTR_PUTSIZE, -1) + " bytes long</P>");
	return er;
    }

    public ReplyInterface ingoingFilter(RequestInterface req) {
	Request request = (Request) req;
	if(request.getMethod().equals("PUT")) {
	    if(getBoolean(ATTR_STRICT, true) && !request.hasContentLength()) 
		return notifyFailure(request, true);
	    if(request.getContentLength() > getInt(ATTR_PUTSIZE, -1))
		return notifyFailure(request, false);
	}
	return null;
    }

    public ReplyInterface outgoingFilter(RequestInterface req, 
					 ReplyInterface rep) {
	return null;
    }
}

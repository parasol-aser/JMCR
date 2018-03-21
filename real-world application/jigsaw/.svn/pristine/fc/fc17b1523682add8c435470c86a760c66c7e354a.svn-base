// DebugFilter.java
// $Id: DebugFilter.java,v 1.2 2010/06/15 17:52:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceFilter;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

/**
 * Print incoming request and outgoing replies.
 */

public class DebugFilter extends ResourceFilter {
    /**
     * Attribute index - The on/off toggle.
     */
    protected static int ATTR_ONOFF = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	try {
	    cls = Class.forName("org.w3c.jigsaw.filters.DebugFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The onoff toggle
	a = new BooleanAttribute("onoff"
				 , Boolean.TRUE
				 , Attribute.EDITABLE) ;
	ATTR_ONOFF = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * Get the onoff toggle value.
     */

    public boolean getOnOffFlag() {
	return getBoolean(ATTR_ONOFF, true) ;
    }

    /**
     * The ingoing filter - fearly easy !
     * @param request The incomming request.
     * @return Always <strong>null</strong>.
     */

    public ReplyInterface ingoingFilter(RequestInterface req) {
	Request request = (Request) req;
	if ( getOnOffFlag() ) 
	    request.dump(System.out);
	return null;
    }

    /**
     * The outgoing filter - As easy as the ingoing filter.
     * @param request The original request.
     * @param reply The target's reply.
     */

    public ReplyInterface outgoingFilter(RequestInterface req,
					 ReplyInterface rep) 
    {
	Reply reply = (Reply) rep;
	if ( getOnOffFlag() ) 
	    reply.dump(System.out);
	return null ;
    }

}



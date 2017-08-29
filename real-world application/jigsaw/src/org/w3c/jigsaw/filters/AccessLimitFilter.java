// AccessLimitFilter.java
// $Id: AccessLimitFilter.java,v 1.2 2010/06/15 17:52:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
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

import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

/**
 * This filters limit the simultaneous accesses to its target resource.
 */

public class AccessLimitFilter extends ResourceFilter {
    /**
     * Attribute index - The maximum allowed simultaneous accesses.
     */
    protected static int ATTR_LIMIT = -1 ;
    /**
     * Attribute index - The time to wait for the lock (if limit reached)
     */
    protected static int ATTR_TIMEOUT = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;

	try {
	    cls = Class.forName("org.w3c.jigsaw.filters.AccessLimitFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The limit attribute
	a = new IntegerAttribute("limit"
				 , new Integer(1)
				 , Attribute.EDITABLE) ;
	ATTR_LIMIT = AttributeRegistry.registerAttribute(cls, a) ;
	// The timeout attribute
	a = new IntegerAttribute("timeout"
				 , new Integer(60000)
				 , Attribute.EDITABLE) ;
	ATTR_TIMEOUT = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * Current number of requets that have reached the target.
     */

    protected int count   = 0 ;

    /**
     * Get the maximum number of allowed simultaneous accesses.
     */

    public int getLimit() {
	return getInt(ATTR_LIMIT, 1) ;
    }

    /**
     * Get the timeout before we send back an error.
     * A client will wait only for this duration before being thrown an
     * error.
     */

    public int getTimeout() {
	return getInt(ATTR_TIMEOUT, -1);
    }

    /**
     * Count number of hits to the page, block when limit exceeded.
     * This filter maintains the actual number of hits to its target. When this
     * number exceeds the predefined limit, it blocks the caller until some
     * othre thread exits the target.
     * @param request The request to be handled.
     * @exception HTTPException if access limit is reached.
     */

    public synchronized 
	ReplyInterface ingoingFilter (RequestInterface req) 
	throws HTTPException
    {
	Request request = (Request) req;
	int limit   = getLimit() ;
	int timeout = getTimeout() ;

	if ( limit < 0 )
	    return null;
	while ( count >= limit ) {
	    if ( timeout > 0 ) {
		try {
		    wait((long) timeout, 0) ;
		} catch (InterruptedException ex) {
		}
		if ( count >= limit ) {
		    String msg = "Simultaneous number of access to this page "
			+ "is limited to " + limit + " you was not able to "
			+ "get in." ;
		    Reply error = request.makeReply(HTTP.SERVICE_UNAVAILABLE);
		    error.setContent (msg) ;
		    throw new HTTPException (error) ;
		}
	    } else {
		try {
		    wait() ;
		} catch (InterruptedException ex) {
		}
	    }
	}
	count++ ;
	return null;
    }

    /**
     * Notify that someone has exit the target entity.
     * This method decrements the actual number of hits to the filter's
     * target, and notify any awaiting threads that they can now enter
     * safely.
     * @param request The request being handled.
     * @param reply The reply to be emited.
     */

    public synchronized 
	ReplyInterface outgoingFilter (RequestInterface req, 
				       ReplyInterface rep) 
    {
	count-- ;
	notifyAll() ;
	return null ;
    }

}



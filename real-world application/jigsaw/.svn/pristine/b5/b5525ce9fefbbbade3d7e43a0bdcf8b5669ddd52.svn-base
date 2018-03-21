// CounterFilter.java
// $Id: CounterFilter.java,v 1.2 2010/06/15 17:52:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceFilter;

import org.w3c.jigsaw.http.Request;

/**
 * Count the number of hits to the target.
 * This resource maintains the number of hits to some target resource, as
 * one of its persistent attribute.
 * It will decorate the request on the way in with a fake field
 * <code>org.w3c.jigsaw.filters.CounterFilter.count</code>, that will
 * hold the current hit counts for the target resource to use.
 */

public class CounterFilter extends ResourceFilter {
    /**
     * The name of the piece if state that receives the hit count value.
     * To get to the hit-count, use the <code>getState</code> method of 
     * Request, with the following key.
     */
    public static final 
	String STATE_COUNT = "org.w3c.jigsaw.filters.CounterFilter.count";

    /**
     * Attribute index - The counter attribute.
     */
    protected static int ATTR_COUNTER = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	
	try {
	    cls = Class.forName("org.w3c.jigsaw.filters.CounterFilter") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// Declare the counter attribute
	a = new IntegerAttribute("counter"
				 , new Integer(0)
				 , Attribute.EDITABLE) ;
	ATTR_COUNTER = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * We count all accesses, even the one that failed.
     * We also define the 
     * <code>org.w3c.jigsaw.filters.CounterFilter.count</code>
     * request state as the number of hits on that resource (stored as
     * an Integer instance).
     * @param request The request being processed.
     * @return Always <strong>null</strong>.
     */

    public synchronized ReplyInterface ingoingFilter(RequestInterface req) {
	Request request = (Request) req;
	int i = getInt (ATTR_COUNTER, 0) + 1;
	setInt(ATTR_COUNTER, i) ;
	if(! request.hasState(STATE_COUNT))
	    request.setState(STATE_COUNT, new Integer(i)) ;
	return null;
    }

}



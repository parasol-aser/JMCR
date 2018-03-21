// ResourceFilter.java
// $Id: ResourceFilter.java,v 1.1 2010/06/15 12:20:17 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.io.OutputStream;
import java.util.Hashtable;

public class ResourceFilter extends ResourceFrame 
    implements FilterInterface 
{

    /**
     * Get our target resource.
     */
    public Resource getTargetResource() {
	Resource target = (Resource) getResource();
	while (target instanceof ResourceFrame) {
	    target = ((ResourceFrame)target).getResource();
	}
	return target;
    }

    /**
     * Lookup time filtering.
     * This method is called while Jigsaw performs resource lookup. Each time
     * a lookup end up on the target resource of that filter, this method
     * will be called.
     * @param ls The current lookup state.
     * @param lr The current lookup result.
     * @return A boolean, <strong>true</strong> if this filter has performed
     * the whole lookup, and side-effect the lookup result appropriatelly,
     * <strong>false</strong> otherwise.
     * @exception ProtocolException If processing should be interrupted,
     * because an abnormal situation occured.
     */

    public boolean lookup(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	return false;
    }

    /**
     * Simplified ingoingFilter API.
     * This is a default, simplified API to the ingoing filter method.
     * @param request The request to filter.
     * @return A Reply instance, or <strong>null</strong> if processing
     * should continue normally.
     * @exception ProtocolException If processing should be interrupted,
     * because an abnormal situation occured.
     */

    public ReplyInterface ingoingFilter(RequestInterface request) 
	throws ProtocolException
    {
	return null;
    }

    /**
     * The ingoingFilter method.
     * This is the method that really gets called by Jigsaw core. By default
     * it will invoke the simpler <code>ingoingFilter</code> method,
     * taking only the request has a parameter.
     * @param request The request that is about to be handled.
     * @param filters The whole filter list to be applied to the resource.
     * @param i A pointer into the above array, indicating which filters
     * have already been triggered (the one whose index are lower than 
     * <strong>i</strong>), and what filters have to be triggered (the one
     * whose index are greater or equal to <strong>i+1</strong>).
     * @return A Reply instance, if the filter did know how to answer
     * the request without further processing, <strong>null</strong>
     * otherwise.
     * @exception ProtocolException If processing should be interrupted,
     * because an abnormal situation occured.
     */

    public ReplyInterface ingoingFilter(RequestInterface request,
					FilterInterface filters[], 
					int i)
	throws ProtocolException
    {
	return ingoingFilter(request);
    }

    /**
     * Simplified API to the outgoing filter metho.
     * This is a simplified API to the ougoing filter method, you can either
     * overide this method, or the more powerfull one.
     * @param request The original request.
     * @param reply It's original reply.
     * @return A Reply instance, or <strong>null</strong> if processing
     * should continue normally.
     * @exception ProtocolException If processing should be interrupted, 
     * because an abnormal situation occured.
     */

    public ReplyInterface outgoingFilter(RequestInterface request,
					 ReplyInterface reply) 
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

    /**
     * The outgoingFilter method.
     * This method is the one that gets called by Jigsaw core. By default it
     * will call the simpler <code>outgoingFilter</code> method that takes
     * only the request and the reply as parameters.
     * @param request The request that has been processed.
     * @param reply The original reply as emitted by the resource.
     * @param filters The whole filter that applies to the resource.
     * @param i The current index of filters. The <em>i</em> filter is ourself,
     * filters with lower indexes have already been applied, and filters with
     * greater indexes are still to be applied.
     * @return A Reply instance, if that filter know how to complete the
     * request processing, or <strong>null</strong> if reminaing filters
     * are to be called by Jigsaw engine.
     * @exception ProtocolException If processing should be interrupted,
     * because an abnormal situation occured.
     */

    public ReplyInterface outgoingFilter(RequestInterface request,
					 ReplyInterface reply,
					 FilterInterface filters[],
					 int fidx) 
	throws ProtocolException
    {
	ReplyInterface fr = outgoingFilter(request, reply);
	return (fr != reply) ? fr : null;
    }

    public OutputStream outputFilter(RequestInterface request,
                                     ReplyInterface reply,
                                     OutputStream output) {
        return output;
    }

    /**
     * Set the values. (MUST be called before initialize).
     * @param defs The Hashtable containing the values.
     */
    public void pickleValues(Hashtable defs) {
	Object nvalues[] = new Object[attributes.length];
	for (int i = 0 ; i < nvalues.length ; i++) {
	    String attrname = attributes[i].getName() ;
	    Object def      = defs.get(attrname) ;
	    // note that protocol frames have usually names in the
	    // frame-XX serie, so it is valid to save (big) on this.
	    if ((def != null) && ((i == ATTR_HELP_URL) ||
				  (i == ATTR_IDENTIFIER))
		&& (def instanceof String)) {
		nvalues[i] = ((String) def).intern();
	    } else {	    
		nvalues[i] = def ;
	    }
	}
	this.values = nvalues ;
    }

   /**
     * Initialization method for attribute holders.
     * This method allows to initialize an attribute holder by providing
     * its attributes values through a Hashtable mapping attribute names
     * to attribute values.
     * @param defs The Hashtable containing the default values.
     */

    public void initialize(Hashtable defs) {
	Object values[] = ((this.values == null)
			   ? new Object[attributes.length] 
			   : this.values);
	for (int i = 0 ; i < values.length ; i++) {
	    String attrname = attributes[i].getName() ;
	    Object def      = defs.get(attrname) ;
	    if ( values[i] == null ) {
		// for help_url, we can save lots of space by using 
		// String.intern()
		if ((def != null)  && ((i == ATTR_HELP_URL) ||
				       (i == ATTR_IDENTIFIER))
		    && (def instanceof String)) {
		    values[i] = ((String) def).intern();
		} else {	    
		    values[i] = def ;
		}
	    }
	}
	initialize(values) ;
    }
}

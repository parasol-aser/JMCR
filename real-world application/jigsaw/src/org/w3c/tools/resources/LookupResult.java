// LookupResult.java
// $Id: LookupResult.java,v 1.1 2010/06/15 12:20:21 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

/**
 * This class is the result of the lookup algorithm.
 */
public class LookupResult {
    public static final int FILTERS_INIT_SIZE = 8;
    public static final int FILTERS_INCR      = 4;

    /**
     * Any reply that can be computed during lookup.
     */
    protected ReplyInterface reply = null;
    /**
     * The current target of the lookup operation.
     */
    protected ResourceReference target = null;
    /**
     * The current set of computed filters to be applied on the resource.
     */
    protected ResourceFilter filters[] = null;
    /**
     * The number of registered filters at this point.
     */
    protected int flength = -1;

    /**
     * Get the current lookup target of the lookup in progress.	
     * @return An ResourceReference, that may be <strong>null</strong>.
     */

    public ResourceReference getTarget() {
	return target;
    }

    /**
     * Set the current target of the lookup operation.
     * @param target The new current target of the lookup in progress.
     */
    public void setTarget(ResourceReference target) {
	this.target = target;
    }

    /**
     * Get the reply generated during the lookup.
     * @return A ReplyInterface instance.
     */
    public ReplyInterface getReply() {
	return reply;
    }

    /**
     * Set the reply generated during the lookup.
     * @param reply A ReplyInterface instance.
     */
    public void setReply(ReplyInterface reply) {
	this.reply = reply;
    }

    /**
     * Does this LookupResult has a Reply.
     * @return a boolean.
     */
    public boolean hasReply() {
	return reply != null;
    }

    /**
     * Add a filter, to be invoked by the resource's <code>perform</code> 
     * method.
     * @param filter The HTTPFIlter to be called.
     */
    public void addFilter(ResourceFilter filter) {
	if ( filters == null ) {
	    // Create the filters array:
	    filters = new ResourceFilter[FILTERS_INIT_SIZE];
	    flength = 0;
	    filters[flength++] = filter;
	} else {
	    if ( flength >= filters.length ) {
		// Resize the filters array:
		ResourceFilter nf[] = 
		    new ResourceFilter[filters.length+FILTERS_INCR];
		System.arraycopy(filters, 0, nf, 0, filters.length);
		filters = nf;
	    }
	    filters[flength++] = filter;
	}
	return;
    }

    /**
     * Add a set of filters to be invoked by the resource's <code>
     * perform</code> method.
     * @param filters The array of filters to register.
     */

    public void addFilters(ResourceFilter fs[]) {
	if ( filters == null ) {
	    flength = fs.length;
	    filters = new ResourceFilter[Math.max(FILTERS_INIT_SIZE, flength)];
	    System.arraycopy(fs, 0, filters, 0, flength);
	} else {
	    int rs = flength + fs.length;
	    if ( rs >= filters.length ) {
		int ns = Math.max(rs ,filters.length+FILTERS_INCR);
		ResourceFilter nf[] = new ResourceFilter[ns];
		System.arraycopy(filters, 0, nf, 0, flength);
		filters = nf;
	    }
	    System.arraycopy(fs, 0, filters, flength, fs.length);
	    flength += fs.length;
	}
	return;
    }

    /**
     * Get the full list of filters to be applied when performing on the 
     * resource.
     * @return An array of ResourceFilter instances, or <strong>null</strong>
     * if none is defined.
     */

    public ResourceFilter[] getFilters() {
	if ( filters != null ) {
	    // Fix the filter array size first:
	    if ( filters.length != flength ) {
		ResourceFilter f[] = new ResourceFilter[flength];
		System.arraycopy(filters, 0, f, 0, flength);
		filters = f;
	    }
	}
	return filters;
    }

    /**
     * Create a new empty lookup result object.
     * @param target The root target of the lookup operation to run.
     */

    public LookupResult(ResourceReference target) {
	this.target = target;
    }
}

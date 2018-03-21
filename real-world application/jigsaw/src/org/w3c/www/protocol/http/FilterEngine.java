// FilterEngine.java
// $Id: FilterEngine.java,v 1.1 2010/06/15 12:25:12 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http;

import java.util.StringTokenizer;
import java.util.Vector;

import java.net.URL;

class ScopeNode {
    private static final int FILTER_INIT_SIZE = 2;
    private static final int CHILD_INIT_SIZE  = 4;

    String        key       = null;
    RequestFilter filters[] = null;
    boolean       inex[]    = null;
    ScopeNode     child[]   = null;

    /**
     * Trigger the sync method of filters set on this node, and recurse.
     */

    synchronized void sync() {
	// Sync our own filters
	if ( filters != null ) {
	    for (int i = 0 ; i < filters.length ; i++) {
		RequestFilter f = filters[i];
		if ( f == null )
		    continue;
		f.sync();
	    }
	}
	// Sync all our children filters:
	if ( child != null ) {
	    for (int i = 0 ; i < child.length ; i++) {
		ScopeNode c = child[i];
		if ( c == null )
		    continue;
		c.sync();
	    }
	}
    }

    /**
     * Resolve this scope node into the provided vector.
     * @param into The vector containing the list of filter settings.
     */

    synchronized void resolve(Vector into) {
	// Anything to be done here ?
	if ( filters == null )
	    return;
	// Apply the filters:
	for (int i = 0 ; i < filters.length ; i++) {
	    if ( filters[i] == null ) 
		continue;
	    boolean is = into.contains(filters[i]); 
	    if ((! inex[i]) && ( is )) {
		// The filter is to be excluded, but is actually present:
		into.removeElement(filters[i]);
	    } else if (inex[i] && ( !is)) {
		// The filter is to be included, but is not present:
		into.addElement(filters[i]);
	    }
	}
    }

    synchronized void setFilter(boolean ie, RequestFilter filter) {
	int slot = -1;
	// Find a slot:
	if ( filters == null ) {
	    // Initialize the filters list:
	    filters = new RequestFilter[FILTER_INIT_SIZE];
	    inex    = new boolean[FILTER_INIT_SIZE];
	    slot    = 0;
	} else {
	    // Look for a free slot:
	    for (int i = 0 ; i < filters.length ; i++) {
		if ( filters[i] == null ) {
		    slot = i;
		    break;
		}
	    }
	    // Do we need to resize the filters arrays:
	    if ( slot == -1 ) {
		slot = filters.length;
		RequestFilter nf[] = new RequestFilter[slot<<1];
		boolean       ni[] = new boolean[slot<<1];
		System.arraycopy(filters, 0, nf, 0, slot);
		System.arraycopy(inex, 0, ni, 0, slot);
		filters = nf;
		inex    = ni;
	    }
	}
	// Updae the appropriate slot:
	filters[slot] = filter;
	inex[slot]    = ie;
    }

    synchronized ScopeNode lookup(String key) {
	// No children ?
	if ( child == null )
	    return null;
	// Lookup children, may be made more efficient if prooves usefull:
	for (int i = 0 ; i < child.length ; i++) {
	    if ( child[i] == null )
		continue;
	    if ( key.equals(child[i].key) )
		return child[i];
	}
	return null;
    }

    synchronized ScopeNode create(String key) {
	int slot = -1;
	// Get a slot:
	if ( child == null ) {
	    child = new ScopeNode[CHILD_INIT_SIZE];
	    slot  = 0;
	} else {
	    // Look for a free slot:
	    for (int i = 0 ; i < child.length ; i++) {
		if ( child[i] == null ) {
		    slot = i;
		    break;
		}
	    }
	    // Do we need to resize ?
	    if ( slot == -1 ) {
		slot = child.length;
		ScopeNode nc[] = new ScopeNode[slot << 1];
		System.arraycopy(child, 0, nc, 0, slot);
		child = nc;
	    }
	}
	// Update the slot:
	return child[slot] = new ScopeNode(key);
    }

    ScopeNode(String key) {
	this.key = key;
    }

    ScopeNode() {
	// Valid only for the root node
    }

}

class FilterEngine {
    ScopeNode root = null;

    /**
     * Split an URL into its various parts.
     * @return An array of Strings containing the URL parts.
     */

    private String[] urlParts(URL url) {
	Vector parts = new Vector(8);
	// The protocol is always the first part:
	parts.addElement(url.getProtocol());
	// Then comes the host:port identifier (we deal *only* with http):
	if ((url.getPort() == -1) || (url.getPort() == 80)) {
	    parts.addElement(url.getHost());
	} else {
	    parts.addElement(url.getHost()+":"+url.getPort());
	}
	// get the "file" part of URI with a fix for jdk1.4
	String sUrl = url.getFile();
	if (sUrl.length() == 0) {
	    sUrl = "/";
	}
	// And last but not least, the parsed path (really not efficient !)
	StringTokenizer st = new StringTokenizer(sUrl, "/");
	while ( st.hasMoreTokens() )
	    parts.addElement(st.nextElement());
	// Build the vector into an array:
	String p[] = new String[parts.size()];
	parts.copyInto(p);
	return p;
    }

    /**
     * Register this given filter in the given scope.
     * @param scope The URL prefix defining the scope of the filter.
     * @param inex Is the scope an include or an exclude scope.
     * @param filter The filter to register in the given scope.
     */

    synchronized void setFilter(URL scope, boolean ie, RequestFilter filter) {
	String parts[] = urlParts(scope);
	ScopeNode node = root;
	// Find or create the appropriate scope node for the filter:
	for (int i = 0 ; i < parts.length ; i++) {
	    ScopeNode child = node.lookup(parts[i]);
	    if ( child == null ) 
		child = node.create(parts[i]);
	    node = child;
	}
	// Setup the filter in the scope node:
	node.setFilter(ie, filter);
    }

    synchronized void setFilter(RequestFilter filter) {
	root.setFilter(true, filter);
    }

    /**
     * Get a global filter of the given class.
     * @return A RequestFilter instance, or <strong>null</strong> if none
     * was found.
     */

    synchronized RequestFilter getGlobalFilter(Class cls) {
	RequestFilter filters[] = root.filters;
	for (int i = 0 ; i < filters.length ; i++) {
	    if ( filters[i] == null )
		continue;
	    Class fc = filters[i].getClass();
	    while (fc != null) {
		if ( fc == cls )
		    return filters[i];
		fc = fc.getSuperclass();
	    }
	}
	return null;
    }

    /**
     * Trigger the sync method of all installed filters.
     * This method walk through the entire filter tree, and sync all filters
     * found on the way.
     */

    synchronized void sync() {
	root.sync();
    }

    /**
     * Compute the set of filters that apply to this request.
     * This method examine the current scopes of all filters, and determine
     * the list of filters to run for the given request.
     * @return An array of filters to run for the given request, or
     * <strong>null</strong> if no filters apply.
     */

    RequestFilter[] run(Request request) {
	String    parts[] = urlParts(request.getURL());
	int       ipart   = 0;
	ScopeNode node    = root;
	// Compute the filters apply list:
	Vector    applies = new Vector();
	while (node != null) {
	    node.resolve(applies);
            if ( ipart < parts.length )
		node = node.lookup(parts[ipart++]);
	    else
		break;
	}
	// Optional (not done for now) order the filters:

	// Now run them, and keep the state in the request:
	if ( applies.size() == 0 )
	    return null;
	RequestFilter f[] = new RequestFilter[applies.size()];
	applies.copyInto(f);
	return f;
    }

    FilterEngine() {
	this.root = new ScopeNode("_root_");
    }

   
}

// CachedResourceFactory.java
// $Id: CachedResourceFactory.java,v 1.1 2010/06/15 12:25:11 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache;

import java.io.IOException;

import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;

/** 
 * The factory for cache entries.
 * All cache entries have to be sub-classes of CachedResource, that's the
 * only limitation to the fun you can have down here.
 */
public class CachedResourceFactory {
    /**
     * Create a suitable instance of some subclass of CachedResource.
     * @param filter The cache filter that ones to create a new entry.
     * @param request The original request we emitted.
     * @param reply The reply we got from the origin server.
     * @return An instance of CachedResource, or <strong>null</strong>
     * if no resource was created.
     */
    public static CachedResource createResource(CacheFilter filter
						, Request request
						, Reply reply)
	throws IOException
    {
	CachedResource r = null;
	String v[] = reply.getVary();
	
	// this is a nightmare, as there is no capitalization
	// and someone may add other headers
	// on top of that Apache use Vary: negotiate every time the 
	// Content-Location is not the same as the request URI...
	
	if (v == null) {
	    // no vary, the easy way :)
	    r = new EntityCachedResource(filter, request, reply);
	} else {
	// Check for a varying resource first:
//	if ( reply.hasHeader(reply.H_VARY) ) 
//	    r = new VaryResource(filter, request, reply);
//	else 
	    r = new EntityCachedResource(filter, request, reply);
	}
	return r;
    }
}



// CacheState.java
// $Id: CacheState.java,v 1.1 2010/06/15 12:25:08 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.htm
package org.w3c.www.protocol.http.cache;

public class CacheState {

    /**
     * The state used to disable that filter per request. Also set by the cache
     * if the request cannot be fullfilled by caches, as detected by this 
     * filter.
     */
    public static final String
    STATE_NOCACHE = "org.w3c.www.protocol.http.cache.dont";

    /**
     * Name of the state used to collect warnings. (request)
     */
    public static final 
    String STATE_WARNINGS = 
    "org.w3c.www.protocol.http.cache.CacheFilter.warns";

    /**
     * Name of the request state used to keep track of original request
     */
    public static final
    String STATE_ORIGREQ = 
    "org.w3c.www.protocol.http.cache.CacheFilter.origreq";

    /**
     * Name of the request state that marks a request as being a revalidation.
     */
    public static final 
    String STATE_REVALIDATION = "org.w3c.www.protocol.http.cache.revalidation";

    /**
     * Name of the property that indicates that the cache can be used.
     */
    public static final 
    String STATE_CACHABLE = "org.w3c.www.protocol.http.cache.cachable";

    /**
     * Name of the property that indicates that the entity may be stored.
     */
    public static final 
    String STATE_STORABLE = "org.w3c.www.protocol.http.cache.storable";

    /**
     * name of the state containing the cached resource
     */
    public static final
    String STATE_RESOURCE = "org.w3c.www.protocol.http.cache.resource";

    /**
     * STATE_HOW value - Indicates a cache hit.
     */
    public static final Integer HOW_HIT = new Integer(1);
    /**
     * STATE_HOW value - Indicates a cache miss.
     */
    public static final Integer HOW_MISS = new Integer(2);
    /**
     * STATE_HOW value - indicates a served cached entry after a revalidation
     */
    public static final Integer HOW_REVALIDATION_SUCCESS = new Integer(3);
    /**
     * STATE_HOW value - indicates a remote served entry after a failed
     * revalidation of a cached entry
     */
    public static final Integer HOW_REVALIDATION_FAILURE = new Integer(4);

   
}



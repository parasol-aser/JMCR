// CacheValidator.java
// $Id: CacheValidator.java,v 1.1 2010/06/15 12:25:07 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache;

import org.w3c.www.protocol.http.Request;
import org.w3c.www.protocol.http.Reply;

public abstract class CacheValidator {
    /**
     * The CacheFilter we are working for
     */
    CacheFilter filter = null;

    /**
     * Check if the request is stale or not
     * @return a boolean, true if the resource is still valid
     * false it if needs a revalidation.
     */
    public abstract boolean checkStaleness(CachedResource cr);

    /**
     * Is the currently cached version usable to answer the given request ?
     * @return A boolean, <strong>true</strong> if we are able to generate
     * a valid answer to this request by the <code>perform</code> method,
     * <strong>false</strong> otherwise (the resource needs to be refreshed).
     */
    public abstract boolean isValid(CachedResource cr, Request request);

    /**
     * Update the expiration information on a cached resource, even if it was
     * not used.
     * @param cr, the CachedResource we are upgrading.
     * @param request, the Request
     * @param reply, the Reply
     */
    public abstract void updateExpirationInfo(CachedResource cr, 
					      Request request, Reply reply);

    /**
     * reset all the ages after a revalidation
     * @param cr, the CachedResource we are upgrading.
     * @param request, the Request
     * @param reply, the Reply
     */ 
    public abstract void revalidateResource(CachedResource cr, 
					    Request request, Reply reply);

    /**
     * initialize this validator
     */
    public void initialize(CacheFilter filter) {
	this.filter = filter;
    }
}

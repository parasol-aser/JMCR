// SimplePushCacheValidator.java
// $Id: SimplePushCacheValidator.java,v 1.1 2010/06/15 12:25:43 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2001.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache.push;

import org.w3c.www.http.HTTP;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;
import org.w3c.www.protocol.http.cache.SimpleCacheValidator;
import org.w3c.www.protocol.http.cache.CachedResource;

import java.net.URL;

/**
 * SimpleCacheValidator with modified behaviour to avoid cleaning 
 * resources that have been pushed into the cache
 *
 * @author Paul Henshaw, The Fantastic Corporation, Paul.Henshaw@fantastic.com
 * @version $Revision: 1.1 $
 * $Id: SimplePushCacheValidator.java,v 1.1 2010/06/15 12:25:43 smhuang Exp $
 */
public class SimplePushCacheValidator extends SimpleCacheValidator {

    /**
     * Check if the request is stale or not
     * @return false if resourse was pushed into cache, 
     * calls super.checkStaleness(cr) otherwise
     */
    public boolean checkStaleness(CachedResource cr) {
	if(PushCacheManager.instance().isPushResource(cr)) {
	    return(false);
	}
	return(super.checkStaleness(cr));
    }

   /**
     * reset all the ages after a revalidation
     * @param cr, the CachedResource we are upgrading.
     * @param request, the Request
     * @param reply, the Reply
     */ 
    public void revalidateResource(CachedResource cr, 
				   Request request, Reply reply) {
	
	if (PushCacheManager.instance().isPushResource(cr) ||
	    reply.getStatus() == HTTP.NOT_MODIFIED) {
	    updateExpirationInfo(cr, request, reply);
	} 
	else {
	    super.revalidateResource(cr,request,reply);
	}
    }
}

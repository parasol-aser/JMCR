// SimpleCacheValidator.java
// $Id: SimpleCacheValidator.java,v 1.1 2010/06/15 12:25:10 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.htm

package org.w3c.www.protocol.http.cache;

import java.net.URL;

import org.w3c.util.ArrayDictionary;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityTag;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;

public class SimpleCacheValidator extends CacheValidator {
    private static final boolean debug = false;
    /**
     * Check if the request is stale or not
     * @return a boolean, false if the resource is still valid
     * true it if needs a revalidation.
     */
    public boolean checkStaleness(CachedResource cr) {
	return (cr.getCurrentAge() >= cr.getFreshnessLifetime());
    }

    /**
     * Is the currently cached version usable to answer the given request ?
     * @return A boolean, <strong>true</strong> if we are able to generate
     * a valid answer to this request by the <code>perform</code> method,
     * <strong>false</strong> otherwise (the resource needs to be refreshed).
     */
    public boolean isValid(CachedResource cr, Request request) {
	EntityCachedResource rcr; 
	rcr = (EntityCachedResource) cr.lookupResource(request);
	// no real resource, or it has been marked as invalid already
	if ((rcr == null) || rcr.getWillRevalidate()) {
	    return false;
	}
	// RFC2616: 14.9.2 revalidation - end-to-end revalidation
	if ( request.getMaxAge() == 0) {
	    rcr.setWillRevalidate(true);
	    return false;
	}
	// RFC2616: 13.6 Check Vary header
	String vary[] = cr.getVary();
	if (vary != null) {
	    for (int i=0; i< vary.length; i++) {
		// always revalidate on *
		if (vary[i].equals("*")) {
		    return false;
		}
		ArrayDictionary a = cr.getConnegHeaders();
		String rh = null;
		String crh = null;
		String lowh = null;
		// we have a header checked, but nothing in the conneg
		// it must not happen, but it's not a good sign for validity
		if (a == null) {
		    return false;
		}
		lowh = vary[i].toLowerCase();
		crh = (String) a.get(lowh);
		rh = request.getValue(vary[i]);
		if (crh == null) {
		    if (rh == null) {
			continue;
		    }
		    return false;
		} else if (rh == null) {
		    return false;
		}
		if (!rh.equals(crh)) {
		    return false;
		}
	    }
	}
	// RFC2616:14.9.3 Modification of basic expiration 
	int maxage    = request.getMaxAge();
	int minfresh  = request.getMinFresh();
	int maxstale  = request.getMaxStale();
	int currage   = rcr.getCurrentAge();
	int freshtime = rcr.getFreshnessLifetime();

	if (debug) {
	    System.out.println("* Maxage    :" +maxage);
	    System.out.println("* MinFresh  :" +minfresh);
	    System.out.println("* MaxStale  :" +maxstale);
	    System.out.println("* CurrAge   :" +currage);
	    System.out.println("* Freshtime :" +freshtime);
	}
	// RFC2616:14.9.3 maxage set, it overrides the freshness lifetime
	if (maxage == -1) {
	    maxage = freshtime;
	}
	if (currage < maxage) {
	    // yeah! hum, not yet ;)
	    if (minfresh != -1) {
		if (currage + minfresh > freshtime) {
		    // FIXME should we revalidate? avoid the cache?
		    return false;
		}
	    }
	    // yes!
	    return true;
	}
	// if we have max-stale and min-fresh, we should kill someone ;)
	if ((maxstale != -1) && (minfresh == -1)) {
	    if (currage < maxage + maxstale) {
		// FIXME add a Warning 110 to the request there!
		return true;
	    }
	}
	// we have max age, the resource is stale according to that
	// so... revalidate!
	return false;
    }

    /**
     * Update the expiration information on a cached resource, even if it was
     * not used. Note that it is the right place to update also information
     * for other cache behaviour used by the sweeper.
     * @param cr, the CachedResource we are upgrading.
     * @param request, the Request
     * @param reply, the Reply
     */
    public void updateExpirationInfo(CachedResource cr, Request request,
				     Reply reply) {
	// we update only "real" resources
	if (!(cr instanceof EntityCachedResource))
	    return;
	EntityCachedResource ecr = (EntityCachedResource) cr;

	// First, compute the initial age, the date and the response time.
	int age_value;                           // s
	long date_value;                         // ms
	long request_time;                       // ms
	long response_time;                      // ms
	long now = System.currentTimeMillis();   // ms
	
	int apparent_age;            // s
	int corrected_received_age;  // s
	int response_delay;          // s
	int corrected_initial_age;   // s

	// RFC2616: 13.2.3: Age Calculation
	age_value = reply.getAge();
	date_value = reply.getDate();
	// update the cached resource date
	ecr.setDate(date_value);
	request_time = request.getEmitDate();
	// small hack, as we cache directly, the time is the same
	// If we were to use an external database or other things, 
	// we may implement it another way
	response_time = now;
	if (date_value == -1) {
	    // RFC2616: 14.18 the recipient MUST assign a date if not present
	    // We assume that the reply was generated instantly, it is
	    // then response_time - request_time, in our case... now
	    date_value = now/2 +  request_time/2;
	}

	apparent_age = (int) Math.max(0, (response_time - date_value) / 1000);
	corrected_received_age = Math.max(apparent_age, age_value);
	response_delay = (int) ((response_time - request_time) / 1000);
	corrected_initial_age = corrected_received_age + response_delay;
	
	ecr.setInitialAge(corrected_initial_age);
	ecr.setResponseTime(response_time);
	// Now check in the response all the cacheability information
	// and uptade some flags directly
	
	// first calculate the freshness-lifetime
	int freshness_lifetime = -1;
	// if it is a shared cache and s-maxage is there it overrides
	// everything
	int s_maxage = reply.getSMaxAge();
	if ( filter.isShared() &&  (s_maxage != -1) ) { 
	    freshness_lifetime = s_maxage;
	    // RFC2616: 14.9.3: s-maxage implies proxy-revalidate
	    ecr.setRevalidate(true);
	} else {
	    int maxage = reply.getMaxAge();
	    // no maxage, check the expire
	    if (maxage < 0) {
		// get from the Expire now...
		long expires = reply.getExpires();
		if ( expires >0 ) {
		    freshness_lifetime = (int) 
			Math.max(0, (expires - date_value) / 1000);
		    if ((freshness_lifetime > 31536000) && 
			(reply.getMinorVersion() == 1 ) &&
			(reply.getMajorVersion() == 1 )) {
			// a HTTP/1.1 Cache sent an expire date more than one 
			// year in the future, it is invalid, as of
			// RFC2616: 14.23 *sigh*
			freshness_lifetime = 31536000;
		    }
		} else if (reply.hasHeader(reply.H_EXPIRES)) {
		    // a bad Expires: header!
		    // RFC2616: 14.21, invalid expires, set it to 0!
		    freshness_lifetime = 0;
		} else {
		   // no expires, no maxage, figure out a default value
		    if (reply.hasHeader(reply.H_LAST_MODIFIED)) {
			// if we have a last_modified, try 10%
			long last_mod = reply.getLastModified();
			int difftime = (int)Math.max(0, (now-last_mod) / 1000);
			// ...but no more than one day is a good rule
			freshness_lifetime = Math.min(86400, difftime / 10);
		    } else {
			// no last modified, no expires, no cache control...
			// let's use a low default setting that allow people
			// to share resources without caching too much, as 
			// there are many badly configured dynamic servers.
			int cr_lifetime = ecr.getFreshnessLifetime();
			if (cr_lifetime == -1) {
			    URL requrl = request.getURL();
			    if (requrl != null) {
				String surl = requrl.toExternalForm();
				if ((surl.indexOf('?') == -1) &&
				    (surl.indexOf("cgi") == -1 )) {
				    freshness_lifetime = 300;
				} else {
				    // for cgi let's be safe and use 0
				    freshness_lifetime = 0;
				}
			    }
			} else {
			    freshness_lifetime = cr_lifetime;
			}
		    }
		}
	    } else {
		freshness_lifetime = maxage;
	    }
	}
	// Yeah! we got the maxtime!
	if (freshness_lifetime != -1) {
	    ecr.setFreshnessLifetime(freshness_lifetime);
	}
	// check the must/proxy-revalidate flags RCC2616: 14.9.4
	if ( reply.checkMustRevalidate() ) {
	    ecr.setRevalidate(true);
	} else if ( filter.isShared() && reply.checkProxyRevalidate() ) {
	    ecr.setRevalidate(true);
	}
    }

    private void checkConsistency(CachedResource cr, Request request,
				  Reply reply) {
	// taken from Alex Rousskov HTTP compliance tests
	// available at http://coad.measurement-factory.com/

	// we update only "real" resources
//	if (!(cr instanceof EntityCachedResource))
//	    return;
//	EntityCachedResource ecr = (EntityCachedResource) cr;
	// test MD5, check only if MD5 is present in the reply
	// or should it be done if !=, taking the first option for now
	String cmd5 = cr.getContentMD5();
	String rmd5 = reply.getContentMD5();
	if (cmd5 == null) {
	    if (rmd5 != null) {
		cr.setWillRevalidate(true);
	    }
	} else {
	    if ((rmd5 != null) && (!rmd5.equals(cmd5))) {
		cr.setWillRevalidate(true);
	    }
	}
	// check Content Length
	int ccl = cr.getContentLength();
	int rcl = reply.getContentLength();
	if (rcl >= 0 ) {
	    if (ccl != rcl) {
		cr.setWillRevalidate(true);
	    }
	}
	// check ETag
	HttpEntityTag rtag = reply.getETag();
	if (rtag != null) {
	    String retag = rtag.toString();
	    String cetag = cr.getETag();
	    if ((cetag == null) || !retag.equals(cetag)) {
		cr.setWillRevalidate(true);
	    }
	}
	// check last modified
	long rlmt = reply.getLastModified();
	if (rlmt >= 0 ) {
	    long clmt = cr.getLastModified();
	    if (clmt != rlmt) {
		cr.setWillRevalidate(true);
	    }
	}
    }
   /**
     * reset all the ages after a revalidation
     * @param cr, the CachedResource we are upgrading.
     * @param request, the Request
     * @param reply, the Reply
     */ 
    public void revalidateResource(CachedResource cr, 
				   Request request, Reply reply) {

	cr.setWillRevalidate(false);
	if (reply.getStatus() == HTTP.NOT_MODIFIED) {
	    updateExpirationInfo(cr, request, reply);
	} else {
	    // do something good
	}
	checkConsistency(cr, request, reply);
    }
}

// CacheFilter.java
// $Id: CacheFilter.java,v 1.2 2010/06/15 17:53:00 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Vector;
import java.util.Hashtable;

import org.w3c.util.ObservableProperties;
import org.w3c.util.PropertyMonitoring;
import org.w3c.util.URLUtils;

import org.w3c.www.protocol.http.HttpException;
import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.PropRequestFilter;
import org.w3c.www.protocol.http.PropRequestFilterException;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;
import org.w3c.www.protocol.http.RequestFilter;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpCacheControl;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpInvalidValueException;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.www.http.HttpSetCookieList;
import org.w3c.www.http.HttpWarning;

public class CacheFilter implements PropRequestFilter, PropertyMonitoring {
    // serializer class
    public static final String SERIALIZER_P = 
    "org.w3c.www.protocol.http.cache.serializerclass";
    // sweeper class
    public static final String SWEEPER_P    = 
    "org.w3c.www.protocol.http.cache.sweeperclass";
    // validator class
    public static final String VALIDATOR_P  = 
    "org.w3c.www.protocol.http.cache.validatorclass";

    /**
     * Name of the property enabling the connected/disconnected mode
     */
    public static final String CACHE_CONNECTED_P
    = "org.w3c.www.protocol.http.cache.connected";
   /**
     * Name of the property indicating if this cache is shared.
     * <p>This property defaults to <strong>true</strong>.
     */
    public static final
    String SHARED_P = "org.w3c.www.protocol.http.cache.shared";

    /**
     * The name of the properties indicating the size of the cache (in bytes).
     * This property will give the value of the disk-based cache size. This
     * value only takes into account the size of the entities saved, not
     * the size of the associated headers, and not the physical size on the
     * disc.
     * <p>This property defaults to <strong>5000000</strong> bytes.
     */
    public static final 
    String CACHE_SIZE_P = "org.w3c.www.protocol.http.cache.size";

    /**
     * Name of the property indicating if the cache is in debug mode.
     * <p>This property defaults to <strong>false</strong>.
     */
    public static final 
    String DEBUG_P = "org.w3c.www.protocol.http.cache.debug";

    /**
     * The state used to disable that filter per request. Also set by the cache
     * if the request cannot be fullfilled by caches, as detected by this 
     * filter.
     */
    public final static String
    STATE_NOCACHE = "org.w3c.www.protocol.http.cache.dont";
    /**
     * Name of the request state used to collect warnings.
     */
    public final static 
    String STATE_WARNINGS = 
    "org.w3c.www.protocol.http.cache.CacheFilter.warns";
    /**
     * Name of the request state used tokeep track of original request
     */
    public final static
    String STATE_ORIGREQ = 
    "org.w3c.www.protocol.http.cache.CacheFilter.origreq";
    /**
     * Name of the request state that marks a request as being a revalidation.
     */
    public final static 
    String STATE_REVALIDATION = "org.w3c.www.protocol.http.cache.revalidation";

    /**
     * The HTTP warning used to notify of a disconnected cache.
     */
    protected static HttpWarning WARN_DISCONNECTED = null;
    /**
     * The HTTP warning used to mark invalid entries
     */
    protected static HttpWarning WARN_STALE = null;
    /**
     * The HTTP warning used to indicate a heuristic expiration time.
     */
    protected static HttpWarning WARN_HEURISTIC = null;

    static {
	// Build the std "disconnected" warning
	HttpWarning w = null;
	w = HttpFactory.makeWarning(HttpWarning.DISCONNECTED_OPERATION);
	w.setAgent("Jigsaw");
	w.setText("The required cached resource is stale.");
	WARN_DISCONNECTED = w;
	// Build the stale std warning
	w = HttpFactory.makeWarning(HttpWarning.STALE);
	w.setAgent("Jigsaw");
	w.setText("The returned entry is stale.");
	WARN_STALE = w;
	// Build the heuristic expiration warning:
	w = HttpFactory.makeWarning(HttpWarning.HEURISTIC_EXPIRATION);
	w.setAgent("Jigsaw");
	w.setText("Heuristic expiration time used on this entry.");
	WARN_HEURISTIC = w;
    }

    /**
     * The properties we initialized ourself from.
     */
    protected ObservableProperties props = null;
    // our validator
    protected CacheValidator validator;
    // our caches tore
    protected CacheStore     store;
    // our cache sweeper
    protected CacheSweeper   sweeper;
    // our cache serializer
    protected CacheSerializer serializer;
    // is the cache connected?
    protected boolean connected = true;
    // is the cache shared?
    protected boolean shared = true;
    // The cache size
    protected long size = 20971520; // 20Mo is the default
    protected File directory = null;
    // should ew debug this?
    protected boolean debug = false;
    // the hastable of not downloaded resources
    protected Hashtable precache = new Hashtable(10); 
    // the hastable of the URI to be downloaded
    protected Hashtable uritable = new Hashtable(10);

    /**
     * return the cache sweeper used by the cache
     * @return an instance of CacheSweeper
     */
    public CacheSweeper getSweeper() {
	return sweeper;
    }

    /**
     * return the serializer used by the cache
     * @return an instance of Serializer
     */
    public CacheSerializer getSerializer() {
	return serializer;
    }

    /**
     * return the cache validator used by the cache
     * @return an instance of CacheValidator
     */
    public CacheValidator getValidator() {
        return validator;
    }

    /**
     * is the cache shared?
     * @return a boolean, true if the cache is shared
     */
    public boolean isShared() {
	return shared;
    }
    /**
     * is the cache connected?
     * @return a boolean, true if the cache is connected
     */
    public boolean isConnected() {
	return connected;
    }

    /**
     * Display some output, related to the request (used for debugging)
     */
    protected final void trace(Request request, String msg) {
	System.out.println(request.getURL()+": "+msg);
    }

    /**
     * Add a warning, to be emitted at reply time.
     * The cache filter keeps track, through a specific piece of request state
     * of the warnings to be emitted at reply time (if any).
     * <p>During request processing, cached resources can add any kind
     * of warnings, which will be collected and forwarded back to the reply.
     * @param request The request being process, and whose reply requires
     * some warnings.
     * @param warning The warning to be emitted if ever we use the cache
     * filter to answer the above request.
     */
    protected void addWarning(Request request, HttpWarning warning) {
	Vector vw = (Vector) request.getState(STATE_WARNINGS);
	if ( vw == null ) {
	    vw = new Vector(4);
	    request.setState(STATE_WARNINGS, vw);
	}
	vw.addElement(warning);
    }

    /**
     * Copy all warnings colllected into the given reply.
     * This method collects all HTTP warnings saved during request processing
     * and create (if needed) the approporiate warning header in the given
     * reply.
     * @param request The request that has been processed by the cache filter.
     * @param reply The reply that has been constructed from the cache.
     * @see #addWarning
     */
    protected final void setWarnings(Request request, Reply reply) {
	Vector vw = (Vector) request.getState(STATE_WARNINGS);
	if ( vw == null )
	    return;
	HttpWarning ws[] = new HttpWarning[vw.size()];
	vw.copyInto(ws);
	reply.setWarning(ws);
    }

    /**
     * check if we can use the cache or not for this request
     * It marks the request as being not cachable if false.
     * @param a request, the incoming client-side request
     * @return a boolean, true if we can use the cache
     */
    public boolean canUseCache(Request req) {
	// RFC2616: 14.32 Cache-Control equivalence of Pragma: no-cache
	// RFC2616: 14.9.2 no-store directive
	// RFC2616: 14.9.4 End-to-end reload
	if (req.hasPragma("no-cache") || (req.getNoCache() != null)) {
	    req.setState(CacheState.STATE_NOCACHE, Boolean.TRUE);
	    return false;
	}
	if (req.checkNoStore()) {
	    req.setState(CacheState.STATE_STORABLE, Boolean.FALSE);
	    return false;
	}
	String method = req.getMethod();
	if (!method.equals("GET") && !method.equals("HEAD") ) {
	    req.setState(CacheState.STATE_NOCACHE, Boolean.TRUE);
	    return false;
	}
	return true;
    }

    /**
     * Checks if, according to the headers of the reply, an entity may
     * be cached or not, it decorates also the reply
     * @param a request, the client side request
     * @param a reply, the client side reply
     * @return a boolean, true if the resource can be cached
     */
    public boolean canCache(Request req, Reply rep) {
	String method = req.getMethod();
	// only cache GET and HEAD
	if (!method.equals("GET") /* FIXME && !method.equals("HEAD")*/ ) {
	    rep.setState(CacheState.STATE_CACHABLE, Boolean.FALSE);
	    return false;
	}
	// don't cache HTTP/0.9 replies for now
	if (req.getMajorVersion() == 0) {
	    rep.setState(CacheState.STATE_CACHABLE, Boolean.FALSE);
	    return false;
	}
	// Ugly Hack for lame cookies
	if (rep.getSetCookie() != null) {
	    rep.setState(CacheState.STATE_CACHABLE, Boolean.FALSE);
	    return false;
	}
	// RFC2616: 13.4 Response cacheability
	int status = rep.getStatus();
	if ( (status != HTTP.OK) &&
	     (status != HTTP.NON_AUTHORITATIVE_INFORMATION) &&
	     (status != HTTP.PARTIAL_CONTENT) &&
	     (status != HTTP.MULTIPLE_CHOICE) &&
	     (status != HTTP.MOVED_PERMANENTLY) &&
	     (status != HTTP.GONE)) {
	    rep.setState(CacheState.STATE_CACHABLE, Boolean.FALSE);
	    return false;
	}
	HttpCacheControl repcc = null;
	try {
	    repcc = rep.getCacheControl();
	} catch (HttpInvalidValueException ex) {
	    // invalid header, be safe and avoid caching
	    repcc = HttpFactory.parseCacheControl("no-cache");
	    rep.setCacheControl(repcc);
	}
	// first check if we are told that we can cache the resource
	if (repcc != null) {
	    // RFC2616: 14.9.1 Cache-Control: public overrides everything
	    if (repcc.checkPublic()) {
		rep.setState(CacheState.STATE_CACHABLE, Boolean.TRUE);
		return true;
	    }
	    // RFC2616: 14.9.1 Cache-Control: private
	    // We are not handling for now the field names that may be
	    // associated
	    if (isShared() && (repcc.getPrivate() != null)) {
		rep.setState(CacheState.STATE_CACHABLE, Boolean.FALSE);
		return false;
	    }
	}
	// RFC2616: 14.9.1 no-cache, note that we are not using
	// the optional field-names (it is a MAY)
	if (rep.getNoCache() != null) {
	    rep.setState(CacheState.STATE_CACHABLE, Boolean.FALSE);
	    return false;
	} 
	// HTTP/1.[01] Pragma no-cache RFC2616: 14.32 Cache-Control equivalence
	if (rep.hasPragma("no-cache")) {
	    rep.setState(CacheState.STATE_CACHABLE, Boolean.FALSE);
	    return false;
	}
	// Now check for URI and HTTP/1.0, 
	// RFC2616: 13.9 HTTP/1.0 with ? in the URI and no Expires should not
	// be cached.
	if ((req.getURL().getFile().indexOf('?') != -1) && 
	    ((rep.getMajorVersion() == 1) && (rep.getMinorVersion() == 0))
	    && (rep.getExpires() == -1)) {
	    rep.setState(CacheState.STATE_CACHABLE, Boolean.FALSE);
	    return false;
	}
	// Do we have an authentication?
	// without a cache-control, we deny caching for now.
	if (req.hasAuthorization()) {
	    rep.setState(CacheState.STATE_CACHABLE, Boolean.FALSE);
	    return false;
	}
	// by default, it is cacheable
	rep.setState(CacheState.STATE_CACHABLE, Boolean.TRUE);
	return true;
    }

    /**
     * Checks if, according to the headers of a reply we can store
     * the resource.
     * Note that a resource may be cachable, but not storable (memory cache)
     * although is MUST do its best to get rid of it asap, in our case we just
     * don't store it!
     * @param a request, the client side request
     * @param a reply, the client side reply
     * @return a boolean, true if the resource can be stored by the cache
     */
    public boolean canStore(Request req, Reply rep) {
	// RFC2616: 14.9.2 What can be stored...
	if (req.checkNoStore() || rep.checkNoStore()) {
	    rep.setState(CacheState.STATE_STORABLE, Boolean.FALSE);
	    return false;
	}
	rep.setState(CacheState.STATE_CACHABLE, Boolean.TRUE);
	return true;
    }

    /**
     * Modify a request to ask for a revalidation
     * @param the resource to be revalidated
     * @param request, the original request to be modified
     */
    protected Request setRequestRevalidation(CachedResource res, Request req) {
	try {
	    return store.getCachedResource(res).setRequestRevalidation(req);
	} catch (InvalidCacheException ex) {
	    // should never happen as we know it is in the cache
	}
	return null;
    }

    /**
     * The request pre-processing hook.
     * Before each request is launched, all filters will be called back through
     * this method. They will generally set up additional request header
     * fields to enhance the request.
     * @param request The request that is about to be launched.
     * @return An instance of Reply if the filter could handle the request,
     * or <strong>null</strong> if processing should continue normally.
     * @exception HttpException If the filter is supposed to fulfill the
     * request, but some error happened during that processing.
     */
    public Reply ingoingFilter(Request request) 
	throws HttpException
    {
	// can we use the cache?
	if (!canUseCache(request)) {
	    if (debug) {
		trace(request, "*** Can't use cache");
	    }
	    // we will invalidate this resource, will do that only
	    // on real entity resource, not on negotiated ones
	    if (connected) {
		CachedResource res = null;
		EntityCachedResource invalidRes = null;
		try {
		    URL _ru = request.getURL();
		    String requrl = URLUtils.normalize(_ru).toExternalForm();
		    res = store.getCachedResourceReference(requrl);
		    if (res != null) {
			invalidRes = (EntityCachedResource)
			    res.lookupResource(request);
		    }
		} catch (InvalidCacheException ex) {
		    invalidRes = null;
		}
		if (invalidRes != null) {
		    invalidRes.setWillRevalidate(true);
		}
		request.setState(STATE_NOCACHE, Boolean.TRUE);
		return null;
	    } else {
		// disconnected, abort now!
		Reply reply = request.makeReply(HTTP.GATEWAY_TIMEOUT);
		reply.setContent("The cache cannot be use for "
				 + "<p><code>"+request.getMethod()+"</code> "
				 + "<strong>"+request.getURL()+"</strong>"
				 + ". <p>It is disconnected.");
		return reply;
	    }
	}
	// let's try to get the resource!
	URL _ru = request.getURL();
	String requrl = URLUtils.normalize(_ru).toExternalForm();
	// in the pre-cache, wait for full download
	// FIXME should be better than this behaviour...
	// see EntityCachedResource perform's FIXME ;)
	if (precache.containsKey(requrl)) {
	    if (debug)
		System.out.println("*** Already downloading: "+ requrl);
	    try {
		CachedResource cr = (CachedResource)precache.get(requrl);
		return cr.perform(request);
	    } catch (Exception ex) {
		// there was a problem with the previous request, 
		// it may be better to do it by ourself
	    }
	}
	
	CachedResource res = null;
	try {
	    res = store.getCachedResourceReference(requrl);
	} catch (InvalidCacheException ex) {
	    res = null;
	}
	// are we disconnected?
	if (request.checkOnlyIfCached() || !connected ) {
	    // and no entries...
	    EntityCachedResource ecr = null;
	    if (res != null) {
		ecr = (EntityCachedResource) res.lookupResource(request);
	    }
	    if ((res == null) || (ecr == null)) {
		if ( debug )
		    trace(request, "unavailable (disconnected).");
		Reply reply = request.makeReply(HTTP.GATEWAY_TIMEOUT);
		reply.setContent("The cache doesn't have an entry for "
				 + "<p><strong>"+request.getURL()+"</strong>"
				 + ". <p>And it is disconnected.");
		return reply;
	    }
	    // yeah!
	    if (debug) {
		trace(request, (connected) ? " hit - only if cached" : 
		      " hit while disconneced" );
	    }
	    if (!validator.isValid(ecr, request)) {
		addWarning(request, WARN_STALE);
	    }
	    addWarning(request, WARN_DISCONNECTED);
	    Reply reply = ecr.perform(request);
		// Add any warnings collected during processing to the reply:
	    setWarnings(request, reply);
//FIXME	    request.setState(STATE_HOW, HOW_HIT);
	    return reply;
	}
	// in connected mode, we should now take care of revalidation and such
	if (res != null) {
	    // if not fully loaded, ask for a revalidation FIXME
	    if ((res.getLoadState() == CachedResource.STATE_LOAD_PARTIAL) ||
		(res.getLoadState() == CachedResource.STATE_LOAD_ERROR)) {
		setRequestRevalidation(res, request);
		return null;
	    }
	    if ( validator.isValid(res, request) ) {
		try {
		    store.updateResourceGeneration(res);
		} catch (InvalidCacheException ex) {
		    // should be ok so...
		}
//FIXME	    request.setState(STATE_HOW, HOW_HIT);
		Reply rep = res.perform(request);
		return rep;
	    } else {
		if (debug) {
		    System.out.println("*** Revalidation asked for " + requrl);
		}
		// ask for a revalidation
		setRequestRevalidation(res, request);
		return null;
	    }
	}
	// lock here while we are waiting for the download
	while (uritable.containsKey(requrl)) {
	    synchronized (uritable) {
		try {
		    uritable.wait();
		} catch (InterruptedException ex) {}
	    }
	    if (precache.containsKey(requrl)) {
		if (debug)
		    System.out.println("*** Already downloading: "+ requrl);
		CachedResource cr = (CachedResource)precache.get(requrl);
		return cr.perform(request);
	    }
	    uritable.put(requrl, requrl);
	}
	return null;
    }

    /**
     * This filter  handle exceptions.
     * @param request The request that triggered the exception.
     * @param ex The triggered exception.
     * @return Always <strong>false</strong>.
     */
    public boolean exceptionFilter(Request request, HttpException ex) {
	URL _ru;
	_ru = request.getURL();
	String requrl = URLUtils.normalize(_ru).toExternalForm();
	synchronized (uritable) {
	    uritable.remove(requrl);
	    uritable.notifyAll();
	}	
	return false;
    }    

    /**
     * The request post-processing hook.
     * After each request has been replied to by the target server (be it a 
     * proxy or the actual origin server), each filter's outgoingFilter
     * method is called.
     * <p>It gets the original request, and the actual reply as a parameter,
     * and should return whatever reply it wants the caller to get.
     * @param request The original (handled) request.
     * @param reply The reply, as emited by the target server, or constructed
     * by some other filter.
     * @exception HttpException If the reply emitted by the server is not
     * a valid HTTP reply.
     */
    public Reply outgoingFilter(Request request, Reply reply) 
	throws HttpException
    {
	URL url = URLUtils.normalize(request.getURL());
	// Yeah ! We win:
	CachedResource c;
	c = (CachedResource) request.getState(CacheState.STATE_RESOURCE);
	if ( c != null ) {
	    if ( debug )
		trace(request, "revalidated "+reply.getStatus());
//		request.setState(STATE_HOW
//				 , ((reply.getStatus() == HTTP.NOT_MODIFIED)
//				    ? HOW_REVALIDATION_SUCCESS
//				    : HOW_REVALIDATION_FAILURE));
//		Reply valrep = c.validate(request, reply);
		// Client or Server error, ask for revalidation
//		if (valrep.getStatus()/100 >= 4) {
//		    c.setWillRevalidate(true);
//		}
	    if (reply.getStatus() == HTTP.NOT_MODIFIED) {
		// FIXME revalidateResource(request, reply)
		validator.revalidateResource(c, request, reply);
		try {
		    store.storeCachedResource(c, c.getCurrentLength());
		} catch (InvalidCacheException ex) {
		    if (debug) {
			ex.printStackTrace();
		    }
		}
		// extract the original request
		Request origreq;
		origreq = (Request) request.getState(CacheState.STATE_ORIGREQ);
		return c.perform(origreq);
	    } else {
		// delete it
		c.delete();
		store.getState().notifyResourceDeleted(c);
	    }
	}
	// don't use cache, exit asap
	if (!canCache(request, reply)) {
	    request.setState(STATE_NOCACHE, Boolean.TRUE);
	    if (debug) {
		System.out.println("*** Can't cache reply");
	    }
	    String requrl = url.toExternalForm();
	    precache.remove(requrl);
	    synchronized (uritable) {
		uritable.remove(requrl);
		uritable.notifyAll();
	    }
	    invalidateOnReply(request, reply);
	    return null;
	}
	if (!canStore(request, reply)) {
//	    request.setState(STATE_NOSTORE, Boolean.TRUE);
	    if (debug) {
		System.out.println("*** Can't store reply");
	    }
	    String requrl = url.toExternalForm();
	    precache.remove(requrl);
	    synchronized (uritable) {
		uritable.remove(requrl);
		uritable.notifyAll();
	    }
	    invalidateOnReply(request, reply);
	    return null;
	}
	pushDocument(request, reply);
	return null;
    }

    private void invalidateOnReply(Request request, Reply reply) {
	URL url = request.getURL();
	// now invalidate the entities per rfc2616#13.11
	if ((request.getMethod() == HTTP.POST) ||
	    (request.getMethod() == HTTP.PUT) ||
	    (request.getMethod() == HTTP.DELETE)) {
	    String rloc = reply.getLocation();
	    String rcloc = reply.getContentLocation();
	    URL urloc, urcloc;
	    // FIXME use a private method instead of duplicating
	    if (rloc != null) {
		try {
		    urloc = new URL(rloc);
		    // only if host match
		    if (URLUtils.equalsProtocolHostPort(url,urloc)) {
			CachedResource res = null;
			try {
			    res = store.getCachedResourceReference(rloc);
			    if (res != null) {
				res.setWillRevalidate(true);
			    }
			} catch (InvalidCacheException ex) {
				// weird, but it won't stop us :)
			}
		    }
		} catch (MalformedURLException mule) {
		    // nothing to do
		}
	    }
	    if (rcloc != null) {
		try {
		    urcloc = new URL(url, rcloc);
		    // only if host match
		    if (URLUtils.equalsProtocolHostPort(url,urcloc)) {
			CachedResource res = null;
			try {
			    String surcloc = urcloc.toExternalForm();
			    res = store.getCachedResourceReference(surcloc);
			    if (res != null) {
				res.setWillRevalidate(true);
			    }
			} catch (InvalidCacheException ex) {
				// weird, but it won't stop us :)
			}
		    }
		} catch (MalformedURLException mule) {
		    // nothing to do
		}
	    }
	}
    }
	    

    public void sync() {
	if (debug) {
	    System.out.println("*** Synching the CacheFilter");
	}
	try {
	    store.sync();
	} catch (Exception ex) {
	    System.err.println(getClass().getName()+": Unable to save cache.");
	}
    }

    /**
     * Property monitoring for the CacheFilter.
     * The CacheFilter allows you to dynamically (typically through the 
     * property setter) change the class of the sweeper, the validator, 
     * the size...
     * @param name The name of the property that has changed.
     * @return A boolean, <strong>true</strong> if the change was made, 
     *    <strong>false</strong> otherwise.
     */
    public boolean propertyChanged(String name) {
	if ( name.equals(SERIALIZER_P) ) {
	    CacheSerializer cs = null;
	    try {
		Class c;
		c = Class.forName(props.getString(name, null));
		cs = (CacheSerializer) c.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    } catch (Exception ex) {
		return false;
	    }
	    serializer = cs;
	    return true;
	} else if ( name.equals(SWEEPER_P) ) {
	    CacheSweeper cs = null;
	    try {
		Class c;
		c = Class.forName(props.getString(name, null));
		cs = (CacheSweeper) c.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    } catch (Exception ex) {
		return false;
	    }
	    // looks good, let's restart with this one!
	    sweeper.destroy();
	    sweeper = cs;
	    sweeper.start();
	    return true;
	} else if ( name.equals(VALIDATOR_P) ) {
	    CacheValidator cv = null;
	    try {
		Class c;
		c = Class.forName(props.getString(name, null));
		cv = (CacheValidator) c.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    } catch (Exception ex) {
		return false;
	    }
	    validator = cv;
	    return true;
	} else if ( name.equals(DEBUG_P) ) {
	    debug = props.getBoolean(name, debug);
	    return true;
	} else if ( name.equals(SHARED_P) ) {
	    shared = props.getBoolean(name, shared);
	    return true;
	} else if ( name.equals(CACHE_CONNECTED_P)) {
	    connected = props.getBoolean(name, true);
	    return true;
	}
	// ask the store if it can do something
	return store.propertyChanged(name);
    }

    /**
     * Push a document in the cache.
     * The caller has to forge a a request and a reply before being able
     * to make something
     * enter the cache. 
     * The request should provide at least:
     * <dl>
     * <dt>URL<dl>The URL (key for cache lookups)
     * <dt>Method<dl>The method that was "applied" to URL to get forged
     * reply.
     * </dl>
     * <p>It is recommended that the reply provides at least
     * the following informations:
     * <dl>
     * <dt>Status Code</dl>A valid HTTP/1.1 status code (probably <strong>
     * 200</code>)
     * <dt>InputStream<dl>Containing the entity to be cached,
     * <dt>EntityTag<dl>A valid entity tag for the document,
     * <dt>CacheControl<dl>Appropriate HTTP/1.1 cache controls for that
     *     document,
     * <dt>Mime headers<dl>At least a valid content type, and probably a
     *     content length (to check consistency with the reply body).
     * </dt>
     */
    public void pushDocument(Request request, Reply reply) {
	URL url = URLUtils.normalize(request.getURL());
	try {
	    synchronized (uritable) {
		CachedResource r = null;
		String requrl = url.toExternalForm();
		r = CachedResourceFactory.createResource(this, request,
							 reply);
		if (r.uploading)
		    precache.put(requrl, r);
		uritable.remove(requrl);
		uritable.notifyAll();
	    }
	    if ( debug )
		trace(request, "enters cache.");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    /**
     * do what is needed when an upload is done!
     * ie: remove from the precache and put in the store
     * @param the CachedResource to be moved.
     */
    protected synchronized void cleanUpload(CachedResource cr) {
	// FIXME we should check the state!
	precache.remove(cr.getIdentifier());
	try {
	    store.storeCachedResource(cr);
	} catch (InvalidCacheException ex) {
	    if (debug) {
		ex.printStackTrace();
	    }
	}
    }

    /**
     * @return the store used a CacheStore
     */
    public CacheStore getStore() {
	return store;
    }

    public void initialize(HttpManager manager) 
	throws PropRequestFilterException 
    {
	String validator_c;
	String sweeper_c;
	String serializer_c;
	props = manager.getProperties();

	shared    = props.getBoolean(SHARED_P, true);
	connected = props.getBoolean(CACHE_CONNECTED_P, true);
	debug     = props.getBoolean(DEBUG_P, false);
	// now create the add-on classes
	validator_c = props.getString(VALIDATOR_P,
		       "org.w3c.www.protocol.http.cache.SimpleCacheValidator");
	sweeper_c = props.getString(SWEEPER_P,
			 "org.w3c.www.protocol.http.cache.SimpleCacheSweeper");
	serializer_c = props.getString(SERIALIZER_P,
		      "org.w3c.www.protocol.http.cache.SimpleCacheSerializer");
	try {
	    Class c;
	    c = Class.forName(validator_c);
	    validator =  (CacheValidator) c.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    validator.initialize(this);
	    c = Class.forName(sweeper_c);
	    sweeper = (CacheSweeper) c.newInstance();
	    sweeper.initialize(this);
	    c = Class.forName(serializer_c);
	    serializer = (CacheSerializer) c.newInstance();
	} catch (Exception ex) {
	    // a fatal error! The cache won't be loaded...
	    ex.printStackTrace();
	    throw new PropRequestFilterException("Unable to start cache");
	}
	// now create the store as we have the basic things here
	store = new CacheStore();
	try {
	    store.initialize(this);
	} catch (InvalidCacheException ex) {
	    // hum no worky, should do some action there!
	    if (debug) {
		ex.printStackTrace();
	    }
	}
	// now start the sweeper
	sweeper.start();
	// Start the ActiveStream handler:
	ActiveStream.initialize();
	// Register for property changes:
	props.registerObserver(this);
	// Now, we are ready, register that filter:
	manager.setFilter(this);
    }
}

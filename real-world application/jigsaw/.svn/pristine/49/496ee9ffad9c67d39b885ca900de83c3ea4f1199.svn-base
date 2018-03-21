// EntityCachedResource.java
// $Id: EntityCachedResource.java,v 1.2 2010/06/15 17:53:00 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache;

import java.net.URL;

import java.util.Enumeration;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.w3c.util.ArrayDictionary;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.LongAttribute;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.www.http.HeaderDescription;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpCacheControl;
import org.w3c.www.http.HttpContentRange;
import org.w3c.www.http.HttpRange;
import org.w3c.www.http.HttpEntityTag;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.ByteRangeOutputStream;
import org.w3c.www.protocol.http.HttpException;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;
import org.w3c.www.mime.MimeType;
import org.w3c.jigsaw.frames.MimeTypeAttribute;

/**
 * A cached resource with an entity
 */
public class EntityCachedResource extends CachedResource {

    /**
     * Condition check return code - Condition existed but failed.
     */
    public static final int COND_FAILED = 1;
    /**
     * Condition check return code - Condition existed and succeeded.
     */
    public static final int COND_OK = 2;  
    /**
     * Condition check return code - Condition existed and succeeded 
     *                               but is a weak validation.
     */
    public static final int COND_WEAK = 3;    

    /**
     * Attribute index - The Content-Type of the resource
     */
    protected static int ATTR_CONTENT_TYPE = -1;
    /**
     * Attribute index - The resource's max age.
     */
    protected static int ATTR_FRESHNESS_LIFETIME = -1;
    /**
     * Attribute index - The initial age of this resource.
     */
    protected static int ATTR_INITIAL_AGE = -1;
    /**
     * Attribute index - The response time
     */
    protected static int ATTR_RESPONSE_TIME = -1;    
    /**
     * Attribute index - Revalidate flag
     */
    protected static int ATTR_REVALIDATE = -1;    
    /**
     * Attribute index - The download state
     */
    protected static int ATTR_LOAD_STATE = -1;    

    static {
	Attribute a = null;
	Class     c = null;
	try {
	    c = Class.forName(
		"org.w3c.www.protocol.http.cache.EntityCachedResource");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Declare the contenht type attribute:
	a = new MimeTypeAttribute("content-type"
				  , null
				  , Attribute.COMPUTED);
	ATTR_CONTENT_TYPE = AttributeRegistry.registerAttribute(c, a);
	// Declare the max-age (freshness lifetime) value.
	a = new IntegerAttribute("freshness-lifetime"
				 , null
				 , Attribute.COMPUTED);
	ATTR_FRESHNESS_LIFETIME = AttributeRegistry.registerAttribute(c, a);
	// Declare the initial age value.
	a = new IntegerAttribute("initial-age"
				 , null
				 , Attribute.COMPUTED);
	ATTR_INITIAL_AGE = AttributeRegistry.registerAttribute(c, a);
	// Declare the response time value.
	a = new LongAttribute("response-time"
			      , null
			      , Attribute.COMPUTED);
	ATTR_RESPONSE_TIME = AttributeRegistry.registerAttribute(c, a);
	// Declare the response time value.
	a = new BooleanAttribute("revalidate"
			      , Boolean.FALSE
			      , Attribute.COMPUTED);
	ATTR_REVALIDATE = AttributeRegistry.registerAttribute(c, a);	
    }

    // some download specific variables
    protected boolean revalidating = false;
    protected boolean regetting    = false;
    protected boolean hasEntity    = false;
    protected int       oldsize = -1;
    protected int    wantedsize = -1;
    // our cache filter, if we need to notify it
    protected CacheFilter filter;

    /**
     * Get the Content-Type of the cached resource of <code>null</code> if
     * there is no mime type (it should NEVER happen!)
     * @return a MimeType
     */
    public MimeType getContentType() {	
	return (MimeType) getValue(ATTR_CONTENT_TYPE, null);
    }

    /**
     * Set the Content-Type of this cached resource
     * @param a MimeType, the mime type of this resource
     */
    public void setContentType(MimeType type) {
	setValue(ATTR_CONTENT_TYPE, type);
    }

    /**
     * Get this resource's freshness lifetime (RFC2616: 13.2.4).
     * @return A long number of seconds for which that entry will remain
     * valid, or <strong>-1</strong> if undefined.
     */
    public int getFreshnessLifetime() {
	return getInt(ATTR_FRESHNESS_LIFETIME, -1);
    }

    /**
     * Set this cached entry . freshness lifetime (RFC2616: 13.2.4).
     * @param maxage A number of seconds during which the entry will 
     * remain valid, or <strong>-1</strong> to undefine previous setting.
     */
    public void setFreshnessLifetime(int freshnessLifetime) {
	setInt(ATTR_FRESHNESS_LIFETIME, freshnessLifetime);
    }

    /**
     * Get this cached entry initial age.
     * @return A long number of seconds giving the initial age
     * or <strong>-1</strong> if undefined.
     */
    public int getInitialAge() {
	return getInt(ATTR_INITIAL_AGE, -1);
    }

    /**
     * Set this resource's initial age.
     * @param initage The initial age as a number of seconds
     * or <strong>-1</strong> to undefine previous setting.
     */
    public void setInitialAge(int initage) {
	setInt(ATTR_INITIAL_AGE, initage);
    }

    /**
     * Get the time of the response used to cached that entry.
     * @return A long number of milliseconds since Java epoch, or <strong>
     * -1</strong> if undefined.
     */
    public long getResponseTime() {
	return getLong(ATTR_RESPONSE_TIME, -1);
    }

    /**
     * Set this cached entry response time.
     * @param responsetime A long number of milliseconds indicating the 
     * response time relative to Java epoch, or <strong>-1</strong> to 
     * undefined previous setting.
     */
    public void setResponseTime(long responsetime) {
	setLong(ATTR_RESPONSE_TIME, responsetime);
    }

    /**
     * Get the revalidate flag
     * @return a boolean, <code>true</code> if the proxy must revalidate
     * stale entries
     * -1</strong> if undefined.
     */
    public boolean getRevalidate() {
	return getBoolean(ATTR_REVALIDATE, false);
    }

    /**
     * Set this cached entry revalidate flag.
     * @param validate, a boolean, <code>true</code> if this entry needs
     * to be revalidated while stale.
     */
    public void setRevalidate(boolean validate) {
	setBoolean(ATTR_REVALIDATE, validate);
    }

    /**
     * Get the entity tag associated with that cached entry
     * @return the entity tag or <strong>null</strong> if undefined
     */
    public HttpEntityTag getHETag() {
	if (definesAttribute(ATTR_ETAG)) {
	    if (etags == null) {
		etags    = new HttpEntityTag[1];
		etags[0] = HttpFactory.parseETag(getETag());
	    }
	    return etags[0];
	}
	return null;
    }
    // FIXME add entity tag here

    // end of the basic accessors

    /**
     * Get the cached data for that cached entry.
     * @return A <em>non-buffered</em> output stream.
     */
    public synchronized InputStream getInputStream() 
	throws IOException
    {
	return new BufferedInputStream(new FileInputStream(getFile()));
    }

    /**
     * Get the current age of this resource
     * @return a long the current age of this resource
     */
    public int getCurrentAge() {
	long now = System.currentTimeMillis();
	// RFC2616: 13.2.3 Age Calculation
	 return (int) (getInitialAge() + ((now - getResponseTime()) / 1000));
    }

    /**
     * Try to validate an <code>If-Modified-Since</code> request.
     * @param request The request to validate.
     * @return An integer, <code>COND_FAILED</code>, if the condition  was
     * checked, but failed; <code>COND_OK</code> of condition was checked
     * and succeeded, <strong>0</strong> otherwise.
     */

    public int checkIfModifiedSince(Request request) {
	// Check for an If-Modified-Since conditional:
	long ims = request.getIfModifiedSince();
	long cmt = getLastModified();
	if (ims >= 0) {
	    if (cmt > 0) {
		long s_cmt = cmt / 1000;
		long s_ims = ims / 1000;
		if (s_cmt < s_ims) {
		    return COND_FAILED;
		} else if (s_cmt == s_ims) {
		    return COND_WEAK;
		}
		return COND_OK;
	    }
	}
	return 0;
    }

    /**
     * Try to validate an <code>If-Unmodified-Since</code> request.
     * @param request The request to validate.
     * @return An integer, <code>COND_FAILED</code>, if the condition  was
     * checked, but failed; <code>COND_OK</code> of condition was checked
     * and succeeded, <strong>0</strong> otherwise.
     */

    public int checkIfUnmodifiedSince(Request request) {
	// Check for an If-Unmodified-Since conditional:
	long iums = request.getIfUnmodifiedSince();
	long cmt = getLastModified();
	if ( iums >= 0 ) 
	    return ((cmt > 0) && (cmt - 1000) >= iums) ? COND_FAILED : COND_OK;
	return 0;
    }

    /**
     * Try to validate an <code>If-Match</code> request.
     * @param request The request to validate.
     * @return An integer, <code>COND_FAILED</code>, if the condition  was
     * checked, but failed; <code>COND_OK</code> of condition was checked
     * and succeeded, <strong>0</strong> otherwise.
     */

    public int checkIfMatch(Request request) {
	HttpEntityTag tags[] = request.getIfMatch();
	if ( tags != null ) {
	    HttpEntityTag etag = getHETag();
	    // Good, real validators in use:
	    if ( etag != null ) {
		for (int i = 0 ; i < tags.length ; i++) {
		    HttpEntityTag t = tags[i];
		    if (t.getTag().equals(etag.getTag())) {
			if (t.isWeak() || etag.isWeak()) {
			    return COND_WEAK;
			} else {
			    return COND_OK;
			}
		    }
		}
	    }
	    return COND_FAILED;
	}
	return 0;
    }

    /**
     * Try to validate an <code>If-None-Match</code> request.
     * @param request The request to validate.
     * @return An integer, <code>COND_FAILED</code>, if the condition  was
     * checked, but failed; <code>COND_OK</code> of condition was checked
     * and succeeded, <strong>0</strong> otherwise.
     */

    public int checkIfNoneMatch(Request request) {
	String setag = getETag();
	HttpEntityTag etag = null;
	// Check for an If-None-Match conditional:
	HttpEntityTag tags[] = request.getIfNoneMatch();
	if (setag != null) {
	    etag = HttpFactory.parseETag(getETag());
	}
	if ( tags != null ) {
	    if ( etag == null ) {
		return COND_OK;
	    }
	    int status = COND_OK;
	    for (int i = 0 ; i < tags.length ; i++) {
		HttpEntityTag t = tags[i];
		if (t.getTag().equals(etag.getTag())) {
//		    if (t.isWeak() && !etag.isWeak()) {
		    if (t.isWeak() || etag.isWeak()) {
			status = COND_WEAK;
		    } else {
			return COND_FAILED;
		    }
		}
		if (t.getTag().equals("*")) {
		    return COND_FAILED;
		}
	    }
	    return status;
	}
	return 0;
    }

    /**
     * Called when the tee succeed, it allows you to notify a listener of the 
     * Tee that the download completed succesfully with a specific size
     * @parameter the size received, an integer
     */
    public synchronized void notifyTeeSuccess(int size) {
	int state = getLoadState();
        try {
	    if ( wantedsize > 0 ) {
		if (!regetting) {
		    // sanity check
		    if (state == STATE_NOT_LOADED) {
			if (size == wantedsize) {
			    // cool! the right size and it was the first 
			    // download!
			    setCurrentLength(size);
			    setLoadState(STATE_LOAD_COMPLETE);
			} else {
			    // argh! wrong size and a success hum...
			    setCurrentLength(size);
			    setLoadState(STATE_LOAD_ERROR);
			    System.out.println(getIdentifier()
					       +": tee stream mismatch, "
					       + "bytes(adv/got)="+
					       wantedsize+"/"+size);
			}
		    } else {
			// how can we end up here, I frankly don't know
			setCurrentLength(size);
			setLoadState(STATE_LOAD_ERROR);
			System.out.println(getIdentifier()
					   +": UNKNOWN STATE for "
					   +"tee stream!, bytes(adv/got)="+
					   wantedsize+"/"+size);
		    }
		} else {
		    // we asked for the diff, and we have it!!!
		    if (size == wantedsize) {
			setCurrentLength(oldsize+wantedsize);
			setLoadState(STATE_LOAD_COMPLETE);
		    } else {
			// argh! wrong size and a success hum...
			setCurrentLength(size);
			setLoadState(STATE_LOAD_ERROR);
			System.out.println(getIdentifier()
					   +": tee stream mismatch in reget, "
					   + "bytes(adv/got)="+
					   wantedsize+"/"+size);
		    }
		}
	    } else {
		// we didn't knew the size, we should trust what we got
		// (unless it is HTTP/1.0)
		setCurrentLength(size);
		// FIXME (a trust flag to select btw COMPLETE and UNKNOWN
		setLoadState(STATE_LOAD_COMPLETE);
	    }
	    // Update cache filter space usage:
//	    filter.markUsed(this, oldsize, wantedsize);
	  
	} finally {
	    cleanUpload();
	}
    }

    public void notifyTeeFailure(int size) {
	System.out.println(getIdentifier()+": tee streaming failed !");
	int state = getLoadState();
	
	setCurrentLength(size);
	setLoadState(STATE_LOAD_ERROR);
	System.out.println(getIdentifier()
			   +": tee stream mismatch, "
			   + "bytes(adv/got)="+
			   wantedsize+"/"+size);
	// and finish the thing!
	cleanUpload();
    }

    // FIXME should be called after every upload
    protected synchronized void cleanUpload() {
	// FIXME reset a whole bunch of stuff
	uploading = false;
	filter.cleanUpload(this);
	notifyAll();
    }

    /**
     * FIXME Will be replaced soon, so that multiple people may share 
     * the same temporary resource.
     * Wait for the upload to finish, if needed.
     */
    protected final synchronized void waitUpload() {
	while ( uploading ) {
	    try {
		wait();
	    } catch (InterruptedException ex) {
	    }
	}
    }

    /**
     * handle a range request, according to the first range or the
     * request FIXME we should handle all the ranges at some point...
     */
    protected Reply handleRangeRequest(Request request, HttpRange r) {
	// Should we check against a IfRange header ?
	HttpEntityTag t = request.getIfRange();
	if ( t != null ) {
	    if (t.isWeak() || ! t.getTag().equals(getHETag().getTag()))
		return null;
	}
	// Check the range:
	int cl = getContentLength();
	int fb = r.getFirstPosition();
	int lb = r.getLastPosition();
	int sz;
	if (fb > cl-1) { // first byte already out of range
	    HttpContentRange cr = HttpFactory.makeContentRange("bytes", 0,
							       cl - 1, cl);
	    Reply rr;
	    rr = request.makeReply(HTTP.REQUESTED_RANGE_NOT_SATISFIABLE);
	    rr.setContentLength(-1);
	    rr.setHeaderValue(rr.H_CONTENT_RANGE, cr);
	    rr.setContentMD5(null);
	    return rr;
	}
	if ((fb < 0) && (lb >= 0)) { // ex: bytes=-20 final 20 bytes
	    if (lb >= cl)   // cut the end
		lb = cl;
	    sz = lb;
	    fb = cl - lb;
	    lb = cl - 1;
	} else if (lb < 0) {  // ex: bytes=10- the last size - 10
	    lb = cl-1;
	    sz = lb-fb+1;
	} else {              // ex: bytes=10-20
	    if (lb >= cl)  // cut the end
		lb = cl-1;
	    sz = lb-fb+1;
	}
	if ((fb < 0) || (lb < 0) || (fb <= lb)) {
	    HttpContentRange cr = null;
	    fb = (fb < 0) ? 0 : fb;
	    lb = ((lb > cl) || (lb < 0)) ? cl : lb;
	    cr = HttpFactory.makeContentRange("bytes", fb, lb, cl);
	    // Emit reply:
	    Reply rr = request.makeReply(HTTP.PARTIAL_CONTENT);
	    try {
		rr.setContentMD5(null); // just in case :)
		rr.setContentLength(sz);
		rr.setHeaderValue(rr.H_CONTENT_RANGE, cr);
		rr.setStream(new ByteRangeOutputStream(getFile(), fb, lb+1));
		return rr;
	    } catch (IOException ex) {
	    }
	} 
	return null;
    }
	
    /**
     * decorate the reply header with some meta information taken
     * from the cached resource
     * @return a reply, the one we just updated
     */
    protected Reply setReplyHeaders(Reply reply) {
	int status = reply.getStatus();
	if (status != HTTP.NOT_MODIFIED) {
	    // FIXME check for byte range replies
	    reply.setContentLength(getContentLength());
	    // dump the headers we know.
	    reply.setContentMD5(getContentMD5());
	    reply.setContentLanguage(getContentLanguage());
	    reply.setContentEncoding(getContentEncoding());
	    reply.setContentType(getContentType());
	    reply.setLastModified(getLastModified());
	    reply.setVary(getVary());
	}
	reply.setETag(getHETag());
	long date = getDate();
	if ( date > 0 )
	    reply.setDate(getDate());
	reply.setAge(getCurrentAge());
	ArrayDictionary a = getExtraHeaders();
	if ( a != null ) {
	    // This is the slowest operation of the whole cache :-(
	    Enumeration e = a.keys();
	    while (e.hasMoreElements() ) {
		String hname  = (String) e.nextElement();
		String hvalue = (String) a.get(hname);
		reply.setValue(hname, hvalue);
	    }
	}
	if ((filter != null) && filter.isShared()) {
	    HttpCacheControl hcc = reply.getCacheControl();
	    if (hcc != null) {
		String priv[] = hcc.getPrivate();
		if (priv != null) {
		    for (int i=0; i<priv.length; i++) {
			// remove headers that are private if we are
			// a shared cache (rfc2616#14.9, rfc2616#14.9.1)
			reply.setHeaderValue(priv[i], null);
		    }
		}
	    }
	}
	return reply;
    }

    /**
     * check the validators namely LMT/Etags according to rfc2616 rules
     * @return An integer, either <code>COND_FAILED</cond> if condition
     * was checked, but failed, <code>COND_OK</code> if condition was checked
     * and succeeded, or <strong>0</strong> if the condition was not checked
     * at all (eg because the resource or the request didn't support it).
     */
    public int checkValidators(Request request) {
	int v_inm = checkIfNoneMatch(request);
	int v_ims = checkIfModifiedSince(request);

	if ((v_inm == COND_OK) || (v_ims == COND_OK)) {
	    return COND_OK;
	}
	if ((v_inm == COND_FAILED) || (v_ims == COND_FAILED)) {
	    return COND_FAILED;
	}
	if ((v_inm == COND_WEAK) || (v_ims == COND_WEAK)) {
	    return COND_FAILED;
	}
	return 0;
    }

    /**
     * This cached entry has been checked valid, perform given request.
     * @param request The request to perform.
     * @return An Reply instance.
     * @exception HttpException If something went wrong.
     */
    public Reply perform(Request request)
	throws HttpException
    {
	// If the resource is currently being uploaded, wait:
	waitUpload();
	// Now perform the request:
	try {
	    Reply   reply       = null;
	    boolean needsEntity = true;
	    // Handle range requests:
	    HttpRange ranges[] = request.getRange();
	    if ((ranges != null) && (ranges.length == 1)) 
		reply = handleRangeRequest(request, ranges[0]);
	    // Handle full retreivals:
	    if ( reply == null ) {
		int status = getStatus();
		// Try validating first 
		// NOTE: We know we are only dealing with GETs and HEADs here
		// otherwise the cache wouldn't be used...
		int cim = checkIfMatch(request);
		if ( (cim == COND_FAILED) || (cim == COND_WEAK) ) {
		    status      = HTTP.PRECONDITION_FAILED;
		    needsEntity = false;
		    reply = request.makeReply(status);
		    reply.setContent("Pre-conditions failed.");
		    throw new HttpException(request, reply, "pre-condition");
	        } else if ( checkIfUnmodifiedSince(request) == COND_FAILED ) {
		    status      = HTTP.PRECONDITION_FAILED;
		    reply = request.makeReply(status);
		    reply.setContent("Pre-conditions failed.");
 		    throw new HttpException(request, reply, "pre-condition");
		} else if ( checkValidators(request) == COND_FAILED ) {
		    status      = HTTP.NOT_MODIFIED;
		    needsEntity = false;
		}
		// Emit reply:
		reply = request.makeReply(status);
		if ( needsEntity ) {
		    reply.setStream(getInputStream());
		}
	    }
	    setReplyHeaders(reply);
	    // Check if entity is needed:
	    String mth = request.getMethod();
	    if ( mth.equals("HEAD") || mth.equals("OPTIONS") )
		reply.setStream(null);
//	    filter.markUsed(this);
	    return reply;
	} catch (IOException ex) {
//	    if (debug)
//		ex.printStackTrace();
	    // Some exception occured, delete that resource (no longer usefull)
//	    delete();
	}
	return null;
    }

    /**
     * Try using an active stream to cache the content.
     * Byte size usage is taken care of only at the end of the download
     * to make sure we get the right sizes (might different from the
     * advertized ones).
     * @return An InputStream instance if active caching was possible,
     * <strong>null</strong> otherwise.
     */
    public synchronized InputStream tryActiveCacheContent(InputStream in)
	throws IOException
    {
	// If we don't return null, we *are* responsible for cleaning up
	// the upload *whatever* happens ...
	InputStream  tee = null;
	OutputStream out = null;
	uploading = true;
	// Open the output stream:
	try {
	    out = new FileOutputStream(getFile());
	} catch (IOException ex) {
//	    if (debug)
		ex.printStackTrace();
	    // We'll let cacheContent take care of that situation:
	    return null;
	}
	// We might be able to use active streams:
//	if (upnewsize > ACTIVE_STREAM_THRESOLD ) {
	    tee = ActiveStream.createTee(this, in, out);
	    if ( tee != null )
		return tee;
//	} 
	// We were not able to active stream:
	try {
	    out.close();
	} catch (IOException ex) {
	}
	return null;
    }

    /**
     * The basic initialization
     */
    public void initialize(Object values[]) {
	super.initialize(values);
    }    

    /**
     * sets some useful information about the entity
     * @param the request that requested this entity
     * @param the reply triggered by this request
     */
    protected void updateInfo(Request request, Reply rep) {
	String   mth       = request.getMethod();
	Reply    reply     = (Reply) rep.getClone();
	boolean  hasEntity = !(mth.equals("HEAD") || mth.equals("OPTIONS"));
	// is it a revalidation?
	if (!request.hasState(CacheState.STATE_REVALIDATION)) {
	    // no, go for it!
	    HttpCacheControl hcc = reply.getCacheControl();
	    // first we should NOT cache headers protected by a no-cache
	    // per rfc2616@14.9
	    if (hcc != null) {
		String nocache[] = hcc.getNoCache();
		if (nocache != null) {
		    for (int i=0; i< nocache.length; i++) {
			reply.setHeaderValue(nocache[i], null);
		    }
		}
	    }
	    setStatus(reply.getStatus());
	    setContentType(reply.getContentType());
	    setContentLength(reply.getContentLength());
	    setLastModified(reply.getLastModified());
	    setContentMD5(reply.getContentMD5());
	    String vary[] = reply.getVary();
	    setVary(vary);
	    if (vary != null) {
		// update the conneg headers
		ArrayDictionary a = null;
		for (int i=0; i< vary.length; i++) {
		    if (vary[i].equals("*")) {
			continue;
		    }
		    if (a == null) {
			a = new ArrayDictionary(vary.length);
		    }
		    a.put (vary[i].toLowerCase(), request.getValue(vary[i]));
		}
		// FIXME we should be able to update to save multiple
		// matches, but with a limitation of course
		if (a != null) {
		    setConnegHeaders(a);
		}
	    }
	    if (reply.hasHeader(reply.H_ETAG)) {
		setETag(reply.getETag().toString());
	    } else {
		// be safe here!
		setETag(null);
	    }
	    ArrayDictionary a = new ArrayDictionary(5, 5);
	    Enumeration     e = reply.enumerateHeaderDescriptions();
	    while ( e.hasMoreElements() ) {
		HeaderDescription d = (HeaderDescription) e.nextElement();
		// Skip all well-known headers:
		if ( d.isHeader(Reply.H_CONTENT_TYPE)
		     || d.isHeader(Reply.H_CONTENT_LENGTH)
		     || d.isHeader(Reply.H_LAST_MODIFIED)
		     || d.isHeader(Reply.H_ETAG)
		     || d.isHeader(Reply.H_AGE)
		     || d.isHeader(Reply.H_DATE)
		     || d.isHeader(Reply.H_VARY)
		     || d.isHeader(Reply.H_CONNECTION)
		     || d.isHeader(Reply.H_PROXY_CONNECTION)
		     || d.isHeader(Reply.H_TRANSFER_ENCODING)
		     || d.isHeader(Reply.H_CONTENT_MD5)
		     || d.getName().equalsIgnoreCase("keep-alive"))
		    continue;
		// This is an extra header:
		a.put(d.getName(), reply.getValue(d));
	    }
	    setExtraHeaders(a);
	    // FIXME add the headers ;)
	    
	}
    }

    /**
     * This cached entry needs revalidation, it will modify the 
     * request to do that.
     */
    public Request setRequestRevalidation(Request request) {
	Request origreq = (Request) request.getClone();
	request.setState(CacheState.STATE_RESOURCE, this);
	request.setState(CacheState.STATE_ORIGREQ, origreq);
	// At this point, we use the suggested way of using date as etag:
	request.setIfModifiedSince(getLastModified());
	// But if we do have an etag, we also uses it, as recommended:
	if ((etags == null) && (getETag() != null)) {
	    etags    = new HttpEntityTag[1];
	    etags[0] = HttpFactory.parseETag(getETag());
	}
	request.setIfNoneMatch(etags);
	// We have to remove all other conditionals here:
	request.setIfRange(null);
	request.setRange(null);
	request.setIfUnmodifiedSince(-1);
	request.setIfMatch(null);
	return request;
    }	

    /**
     * A constructor for new resources that will get some data
     * directly
     * FIXME params
     */
    public EntityCachedResource(CacheFilter filter, Request req, Reply rep) {
	invalidated = false;
	setValue(ATTR_IDENTIFIER, req.getURL().toExternalForm());
	// Keep fast track of the filter:
	this.filter = filter;
	// update the headers
	updateInfo(req, rep);
	// and do some calculation according to the validator
	filter.getValidator().updateExpirationInfo(this, req, rep);
	// Save the content of resource into the content cache:
	setFile(filter.getStore().getNewEntryFile());
	wantedsize = rep.getContentLength();
	InputStream in;
	try {
	    in = tryActiveCacheContent(rep.getInputStream());
	    if (in == null) {
		// something bad happened
		// in = cacheContent(reply.getInputStream());
	    }
	    rep.setStream(in);
	} catch (IOException ex) {
	    // FIXME
	}	
    }

    public EntityCachedResource() {
	super();
    }
}



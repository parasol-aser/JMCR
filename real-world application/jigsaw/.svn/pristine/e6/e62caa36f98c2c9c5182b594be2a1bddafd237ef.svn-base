// CacheFilter.java
// $Id: CacheFilter.java,v 1.2 2010/06/15 17:52:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters ;

import java.util.Dictionary;
import java.util.Hashtable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import java.net.URL ;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FilterInterface;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFilter;

import org.w3c.tools.resources.ProtocolException;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpEntityTag;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.util.AsyncLRUList;
import org.w3c.util.LRUList;
import org.w3c.util.LRUNode;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

class CacheException extends Exception {
    public CacheException(String msg) { super(msg) ; }
}

class CacheEntry extends LRUNode {
    /** The normalized url. (Ready to be used as key) */
    private String url ;

    /** The actual cached content */
    byte[] content ;

    /** The model reply */
    Reply reply ;

    /** The maximum allowed age */
    private int maxage ;

    public String toString()
    {
	return "[\"" + url + "\" " + maxage + "]" ;
    }

    public final String getURL()
    {
	return url ;
    }

    public final int getSize()
    {
	return content.length ;
    }

    private void readContent(Reply reply)
	throws IOException
    {
	ByteArrayOutputStream out = null ;
	InputStream in = reply.openStream() ;

	if(reply.hasContentLength()) 
	    out = new ByteArrayOutputStream(reply.getContentLength()) ;
	else
	    out = new ByteArrayOutputStream(8192) ;

	byte[] buf = new byte[4096] ;
	int len = 0 ;
	while( (len = in.read(buf)) != -1)
	    out.write(buf,0,len) ;

	in.close() ;
	out.close() ;

	content = out.toByteArray() ;

	reply.setStream(new ByteArrayInputStream(content)) ;
    }

    /**
     * Construct a CacheEntry from the given reply,
     * maybe using the given default max age
     */
    CacheEntry(Request request, Reply reply, int defMaxAge)
	throws CacheException 
    {
	url = Cache.getNormalizedURL(request) ;
	
	try {
	    readContent(reply) ;
	} catch(IOException ex) {
	    throw new CacheException("cannot read reply content") ;
	}

	this.reply = (Reply) reply.getClone() ;
	this.reply.setStream((InputStream) null) ;

	// Set the date artificially, since Jigsaw only sets the date
	// header on ultimate emission of the reply.
	long date = this.reply.getDate() ;
	if(date == -1) {
	    date = System.currentTimeMillis() ;
	    date -= date % 1000 ;
	    this.reply.setDate(date) ;
	}

	setMaxAge(reply,defMaxAge) ;

    }

    /**
     * Sets this entry's maxage from available data, or
     * falls back to the specified default.
     */
    private void setMaxAge(Reply reply, int def)
    {
	if( ( maxage = reply.getMaxAge() ) == -1 ) {
	    long exp = reply.getExpires() ;
	    long date = reply.getDate() ;
	    
	    if(exp != -1 && date != -1) {
		
		maxage = (int) (reply.getExpires() - reply.getDate()) ;
		if(maxage<0) maxage = 0 ; 
		
	    } else {
		maxage = def ;
	    }
	}
    }

    /**
     * Sets this entry's maxage from available data, or
     * leaves it unchanged if reply doesn't say anything.
     */
    private void setMaxAge(Reply reply)
    {
	setMaxAge(reply,maxage) ;
    }

    /**
     * Make a reply for this entry.
     */
    Reply getReply(Request request)
    {
	Reply newReply = (Reply) reply.getClone() ;

	int age = getAge() ; 
	if(age!=-1) newReply.setAge(age) ;

	boolean notMod = false ;

	HttpEntityTag[] etags = request.getIfNoneMatch() ;
	HttpEntityTag tag = this.reply.getETag() ;
	if(etags != null && tag != null) {
	    boolean noneMatch = true ;
	    String sTag = tag.getTag() ;
	    for(int i=0;i<etags.length;i++) {
		if(sTag.equals(etags[i].getTag())) {
		    noneMatch = false ;
		    break ;
		}
	    }
	    notMod = !noneMatch ;
	} else {
	    long ims = request.getIfModifiedSince() ;
	    long lmd = this.reply.getLastModified() ;
	    if(ims != -1 && lmd != -1) 
		notMod = lmd > ims ;
	}

	if(notMod) {
	    System.out.println("**** replying NOT_MODIFIED") ;
	    newReply.setStatus(HTTP.NOT_MODIFIED) ;
	}
	else if(! request.getMethod().equals("HEAD"))
	    newReply.setStream(new ByteArrayInputStream(content)) ;
	else
	    newReply.setStream((InputStream) null) ;
	return newReply ;	
    }

    /**
     * Returns the age of this entry in seconds,
     * or -1 if age cannot be determined.
     */
    int getAge()
    {
	int age1 = reply.getAge() ;
	
	long age2 = -1 ;
	long date = reply.getDate() ;
	if(date != -1 ) {
	    age2 = System.currentTimeMillis() ;
	    age2 -= date ;
	    age2 /= 1000 ;
	}
	
	return age1>=age2 ? age1 : (int) age2 ;
    }	

    /**
     * Make a reply for this entry, which was validated
     * by the server with the given reply.
     */
    final Reply getReply(Request request, Reply servReply)
    {
	System.out.println("**** Validated entry") ;
	setMaxAge(servReply) ;
	long date = servReply.getDate() ;
	if(date == -1) {
	    date = System.currentTimeMillis() ;
	    date -= date % 1000 ;
	}
	this.reply.setDate(date) ;
	return getReply(request) ;
    }

    /** 
     * Turn the given request into a conditional request,
     * using the appropriate validators (if any). 
     */
    void makeConditional(Request request)
    {
	System.out.println("**** Making conditional request for validation") ;
	HttpEntityTag[] et = { reply.getETag() } ;
	if(et[0] != null) request.setIfNoneMatch(et) ;
	
	long lm = reply.getLastModified() ;
	if(lm != -1) request.setIfModifiedSince(lm) ;
    }

    /**
     * Is this entry fresh, according to the requirements
     * of the request?
     */
    boolean isFresh(Request request)
    {
	int age = getAge() ;
	System.out.println("**** age: "+age+" maxage: "+maxage) ;
	return age != -1 ? (maxage > age) : (maxage > 0) ;
    }
}

class Cache {

    private static final String STATE_NORM_URL =
	"org.w3c.jigsaw.filters.Cache.normURL" ;

    /** Our maximum size in bytes */
    private int maxSize ;
    /** Our maximum size in entries */
    private int maxEntries ;

    /** Current size in bytes */
    private int size ;

    /** The default max age */
    private int defaultMaxAge ;

    /**
     * This maps URLs (maybe processed) vs entries
     */
    Dictionary /*<String,CacheEntry>*/ entries ;

    /**
     * This keeps track of LRU entries
     */
    LRUList /*<CacheEntry>*/ lruList ;

    public Cache(int maxSize, int maxEntries, int defaultMaxAge)
    {
	this.maxSize = maxSize ;
	this.maxEntries = maxEntries ;
	this.defaultMaxAge = defaultMaxAge ;

	this.size = 0 ;

	lruList = new AsyncLRUList() ;
	entries = new Hashtable(20) ;
    }

    /**
     * Stores a new reply in a CacheEntry.
     * Takes care of handling the LRU list, and of possible overwriting
     * @exception CacheException fixme doc
     */
    public void store(Request request, Reply reply)
	throws CacheException
    {
	System.out.println("**** Storing reply in cache") ;
	// Enforce maxEntries
	if(maxEntries > 0 && entries.size() == maxEntries)
	    flushLRU() ;

	// Try to enforce maxSize
	if(maxSize > 0 && reply.hasContentLength()) {
	    int maxEntSize = maxSize - reply.getContentLength() ;
	    while(entries.size() > maxEntSize)
		if(!flushLRU()) break ;
	}

	CacheEntry ce = new CacheEntry(request, reply, defaultMaxAge) ;

	synchronized(this) {
	    size += ce.getSize() ;
	    CacheEntry old = (CacheEntry) entries.put(ce.getURL(),ce) ;
	    if(old!=null) lruList.remove(old) ;
	    lruList.toHead(ce) ;
	}
    }
	

    /**
     * Retrieves a CacheEntry corresponding to the request.
     * Should mark it as MRU
     */
    public CacheEntry retrieve(Request request)
    {
	String url = getNormalizedURL(request) ;
	CacheEntry ce = (CacheEntry) entries.get(url) ;
	return ce==null ? null : ce ;
    }

    /**
     * Removes the CacheEntry corresponding to the request.
     */
    public synchronized void remove(Request request)
    {
	System.out.println("**** Removing from cache") ;
	CacheEntry ce = (CacheEntry)
	    entries.remove(getNormalizedURL(request)) ;
	if(ce == null) return ;
	
	lruList.remove(ce) ;
    }

    /**
     * Gets rid of the LRU element
     */
    private synchronized final boolean flushLRU()
    {
	if(entries.size() == 0) return false ;

	CacheEntry ce = (CacheEntry) lruList.removeTail() ;
	entries.remove(ce.getURL()) ;
	size -= ce.getSize() ;

	return true ;
    }

    /** This might be unnecessary */
    static String getNormalizedURL(Request request) {
	String nurl = (String) request.getState(STATE_NORM_URL) ;
	if(nurl!=null) return nurl ;

	URL url = request.getURL() ;
	nurl = url.getFile() ;
	
	request.setState(STATE_NORM_URL,nurl) ;
	return nurl ;
    }
}

public class CacheFilter extends ResourceFilter {
    protected Cache cache = null ;

    protected final static String STATE_TAG
	= "org.w3c.jigsaw.filters.CacheFilter.tag" ;

    protected static int ATTR_MAX_SIZE = -1 ;
    protected static int ATTR_MAX_ENTRIES = -1 ;
    protected static int ATTR_DEFAULT_MAX_AGE = -1 ;

    static {
	Attribute a   = null;
	Class     cls = null;
	
	try {
	    cls = Class.forName("org.w3c.jigsaw.filters.CacheFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Declare the maximum cache size attribute:
	a = new IntegerAttribute("maxSize"
				 , new Integer(8192)
				 , Attribute.EDITABLE);
	ATTR_MAX_SIZE= AttributeRegistry.registerAttribute(cls, a);
	// Declare the maximum number of entries attribute:
	a = new IntegerAttribute("maxEntries"
				 , new Integer(-1)
				 , Attribute.EDITABLE);
	ATTR_MAX_ENTRIES = AttributeRegistry.registerAttribute(cls, a);
	// Declare the default maxage attribute
	a = new IntegerAttribute("defaultMaxAge"
				 , new Integer(300) // 5min
				 , Attribute.EDITABLE);
	ATTR_DEFAULT_MAX_AGE = AttributeRegistry.registerAttribute(cls, a);
    }

    public int getMaxSize()
    {
	return ((Integer) getValue(ATTR_MAX_SIZE, 
				   new Integer(-1))).intValue() ;
    }

    public int getMaxEntries()
    {
	return ((Integer) getValue(ATTR_MAX_ENTRIES, 
				   new Integer(-1))).intValue() ;
    }

    public int getDefaultMaxAge() {
	return ((Integer) getValue(ATTR_DEFAULT_MAX_AGE, new Integer(300)))
	    .intValue() ;
    }

    private final void tag(Request request)
    {
	request.setState(STATE_TAG,Boolean.TRUE) ;
    }

    private final boolean isTagged(Request request)
    {
	return request.hasState(STATE_TAG) ;
    }

    private Reply applyIn(Request request,
			  FilterInterface[] filters,
			  int fidx)
	throws ProtocolException
    {
	// Apply remaining ingoing filters
	Reply fr = null ;
	for(int i = fidx+1 ;
	    i<filters.length && filters[i] != null ;
	    ++i) {
	    fr = (Reply) (filters[i].ingoingFilter(request, filters, i)) ;
	    if(fr != null) 
		return fr ;
	}
	return null ;
    }

    private Reply applyOut(Request request,
			   Reply reply,
			   FilterInterface[] filters,
			   int fidx)
	throws ProtocolException
    {
	Reply fr = null ;
	for(int i=fidx-1;
	    i>=0 && filters[i] != null;
	    i--) {
	    fr = (Reply) (filters[i].outgoingFilter(request,reply,filters,i)) ;
	    if(fr != null)
		return fr ;
	}
	return null ;
    }

    private final Reply applyOut(Request request,
				 Reply reply,
				 FilterInterface[] filters )
	throws ProtocolException
    {
	return applyOut(request,reply,filters,filters.length) ;
    }

    private void makeInconditional(Request request) {
	request.setHeaderValue(request.H_IF_MATCH, null) ;
	request.setHeaderValue(request.H_IF_MODIFIED_SINCE, null) ;
	request.setHeaderValue(request.H_IF_NONE_MATCH, null) ;
	request.setHeaderValue(request.H_IF_RANGE, null) ;
	request.setHeaderValue(request.H_IF_UNMODIFIED_SINCE, null) ;
    }
	
    /**
     * @return A Reply instance, if the filter did know how to answer
     * the request without further processing, <strong>null</strong> 
     * otherwise. 
     * @exception ProtocolException 
     * If processing should be interrupted,
     * because an abnormal situation occured. 
     */ 
    public ReplyInterface ingoingFilter(RequestInterface req,
					FilterInterface[] filters,
					int fidx)
	throws ProtocolException
    {
	Request request = (Request) req;
	if(cache == null)
	    cache = new Cache(getMaxSize(),
			      getMaxEntries(),
			      getDefaultMaxAge()) ;

	String method = request.getMethod() ;
	if(! ( method.equals("HEAD") ||
	       method.equals("GET") ) )
	    return null ;	// Enforce write-through

	tag(request) ;

	if(isCachable(request)) {
	    CacheEntry cachEnt = cache.retrieve(request) ;
	    if(cachEnt != null) {
		System.out.println("**** Examining entry: "+cachEnt) ;
		Reply fRep = null ;
		if( cachEnt.isFresh(request) ) {
		    
		    fRep = applyIn(request,filters,fidx) ;
		    if(fRep != null) return fRep ;

		    // Get the reply (adjusting age too) [?]
		    Reply reply = cachEnt.getReply(request) ;

		    fRep = applyOut(request,reply,filters) ;
		    if(fRep != null) return fRep ;
		    
		    System.out.println("**** Replying from cache") ;
		    return reply ;
		    
		} else {
		    cachEnt.makeConditional(request) ;
		    return null ;
		}
	    } else {
		System.out.println("**** Not in cache") ;
	    }
	} else {
	    System.out.println("**** Request not cachable") ;
	}

	makeInconditional(request) ;
			
	return null ;
    }

    /**
     * @param request The original request.
     * @param reply It's original reply. 
     * @return A Reply instance, or <strong>null</strong> if processing 
     * should continue normally. 
     * @exception ProtocolException If processing should be interrupted
     * because an abnormal situation occured. 
     */
    public ReplyInterface outgoingFilter(RequestInterface req,
					 ReplyInterface rep,
					 FilterInterface[] filters,
					 int fidx)
	throws ProtocolException 
    {
	Request request = (Request) req;
	Reply   reply   = (Reply) rep;
	// Be transparent if request is not "ours"
	if(!isTagged(request))
	    return null ;
	
	if(isCachable(reply)) {
	    switch(reply.getStatus()) {
	    case HTTP.OK:
	    case HTTP.NO_CONTENT:
	    case HTTP.MULTIPLE_CHOICE:
	    case HTTP.MOVED_PERMANENTLY:
		// Store the reply and let it through
		try {
		    cache.store(request,reply) ;
		} catch(CacheException ex) {
		    // not much to do...
		} finally {
		    return null ;
		}
	    case HTTP.NOT_MODIFIED:
		// This means we're validating
		CacheEntry cachEnt = cache.retrieve(request) ;
		if(cachEnt != null)
		    reply = cachEnt.getReply(request,reply) ;
		break ;
	    default:
		cache.remove(request) ; 
		return null ;
	    }
	} else {
	    System.out.println("**** Reply not cachable") ;
	    cache.remove(request) ;
	}
	
	// Apply remaining filters and return modified reply
	Reply fRep = applyOut(request,reply,filters,fidx) ;

	if(fRep != null) return fRep ;
	else return reply ;
    }

    /**
     * Does this request permit caching?
     * (It's still half-baked)
     */
    private boolean isCachable(Request request)
    {
	if(request.checkNoStore()) return false ;

	String[] nc = request.getNoCache() ;
	if(nc != null) return false ; // for now
	
	return true ;
    }

    /**
     * Does this reply permit caching?
     * (It's still half-baked)
     */
    private boolean isCachable(Reply reply)
    {
	if(reply.checkNoStore() ||
	   reply.getPrivate() != null) return false ;
	
	return true ;
	
    }

}



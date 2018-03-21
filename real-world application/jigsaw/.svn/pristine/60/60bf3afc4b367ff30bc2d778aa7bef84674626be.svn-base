// SimpleCacheFilter.java
// $Id: SimpleCacheFilter.java,v 1.2 2010/06/15 17:52:54 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters ;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URL ;

import java.util.Dictionary;
import java.util.Hashtable;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.FilterInterface;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceFilter;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.util.LRUNode;
import org.w3c.util.LRUList;
import org.w3c.util.AsyncLRUList;

final class SimpleCacheException extends Exception {
    public SimpleCacheException(String msg) { super(msg) ; }
}

final class SimpleCacheEntry extends LRUNode {
    /** The cache that we belong to */
    SimpleCacheFilter filter ;

    /** The stripped URL to be used as key */
    private String url ;

    /** The actual cached reply
     * (status line, headers, and content) */
    private byte[] replyText ;

    /** The date of caching */
    protected long date ;

    public String toString()
    {
	return "[\"" + url + "\"]" ;
    }	

    public final String getURL()
    {
	return url ;
    }

    SimpleCacheEntry(Request request,Reply reply, SimpleCacheFilter filter)
	throws SimpleCacheException
    {
	url = SimpleCache.getNormalizedURL(request) ;
	this.filter = filter ;

	try {
	    readReply(reply) ;
	} catch(IOException ex) {
	    throw new SimpleCacheException("cannot read reply content") ;
	}
	
	date = System.currentTimeMillis() ;
	date -= date % 1000 ;
    }

    boolean isFresh() {
	int freshness = (int) ((System.currentTimeMillis() - date) / 1000 ) ;
	return freshness < filter.getDefaultMaxAge() ;
    }
	    

    public final int getSize()
    {
	return replyText.length ;
    }

    private void readReply(Reply reply)
	throws IOException
    {
	int len = -1 ;
	if(reply.hasContentLength()) 
	    len = reply.getContentLength() + 500 ;
	else 
	    len = 8192 ;

	ByteArrayOutputStream out = new ByteArrayOutputStream(len) ;
	
	reply.dump(out) ;
	out.close() ;

	replyText = out.toByteArray() ;

	reply.setStatus(HTTP.NOHEADER) ;
	reply.setStream(new ByteArrayInputStream(replyText)) ;
    }

    public final void dump(OutputStream out)
	throws IOException
    {
	out.write(replyText) ;
    }

}

final class SimpleCache {
    /** The filter that owns this cache */
    private SimpleCacheFilter filter ;

    /** Current stored size, in bytes */
    private int size ;

    /** This maps stripped URLs vs entries */
    private Dictionary /*<String,SimpleCacheEntry*/ entries ;

    /** This keeps track of LRU entries */
    private LRUList /*<SimpleCacheEntry>*/ lruList ;

    public SimpleCache(SimpleCacheFilter filter) {
	this.filter = filter ;
	this.size = 0 ;

	lruList = new AsyncLRUList() ;
	entries = new Hashtable(20) ;
    }

    public SimpleCacheEntry store(Request request,Reply reply)
	throws SimpleCacheException
    {
	if(SimpleCacheFilter.debug)
	    System.out.println("**** Storing reply in cache") ;

	// Enforce maxEntries
	int maxEntries = filter.getMaxEntries() ;
	if(maxEntries > 0 && entries.size() == maxEntries)
	    flushLRU() ;

	SimpleCacheEntry ce = new SimpleCacheEntry(request,reply,filter) ;

	// Enforce maxSize (sort of)
	int ceSize = ce.getSize() ;
	int maxSize = filter.getMaxSize() ;
	if(maxSize > 0) {
	    if(ceSize > maxSize) return null ;
	    int maxEntSize = maxSize - ceSize ;
	    while(entries.size() > maxEntSize)
		if(!flushLRU()) break ;
	}

	synchronized(this) {
	    size += ceSize ;
	    SimpleCacheEntry old = (SimpleCacheEntry)
		entries.put(ce.getURL(),ce) ;
	    if(old!=null) lruList.remove(old) ;
	    lruList.toHead(ce) ;
	}

	return ce ;

    }

    public SimpleCacheEntry retrieve(Request request)
    {
	String url = getNormalizedURL(request) ;
	SimpleCacheEntry ce = (SimpleCacheEntry) entries.get(url) ;
	if(ce==null) return null ;
	if(!ce.isFresh()) {
	    lruList.remove(ce) ;
	    entries.remove(url) ;
	}
	lruList.toHead(ce) ;
	return ce ;
    }

    public synchronized void remove(Request request)
    {
	if(SimpleCacheFilter.debug)
	    System.out.println("**** Removing from cache") ;
	SimpleCacheEntry ce = (SimpleCacheEntry)
	    entries.remove(getNormalizedURL(request)) ;
	if(ce==null) return ;
	lruList.remove(ce) ;
    }

    private synchronized final boolean flushLRU()
    {
	if(entries.size() == 0) return false ;

	if(SimpleCacheFilter.debug)
	    System.out.println("**** flushing LRU entry") ;

	SimpleCacheEntry ce = (SimpleCacheEntry)
	    lruList.removeTail() ;
	entries.remove(ce.getURL()) ;
	size -= ce.getSize() ;
	
	return true ;
    }
	
    static final String getNormalizedURL(Request request)
    {
	return request.getURL().getFile() ;
    }

}

public class SimpleCacheFilter extends ResourceFilter {
    static final boolean debug = true ; 

    protected SimpleCache cache = null ;

    protected final static String STATE_TAG
	= "org.w3c.jigsaw.filter.SimpleCacheFilter.tag" ;

    protected static int ATTR_MAX_SIZE = -1 ;
    protected static int ATTR_MAX_ENTRIES = -1 ;
    protected static int ATTR_DEFAULT_MAX_AGE = -1 ;
    protected static int ATTR_FLUSH = -1 ;

    static {
	Attribute a   = null;
	Class     cls = null;
	
	try {
	    cls = Class.forName("org.w3c.jigsaw.filters.SimpleCacheFilter");
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
	// Declare flush flag attribute
	a = new BooleanAttribute("flush"
				 , Boolean.FALSE
				 , Attribute.EDITABLE) ;
	ATTR_FLUSH = AttributeRegistry.registerAttribute(cls, a) ;
    }
    public int getMaxSize()
    {
	return getInt(ATTR_MAX_SIZE, 8192);
    }

    public int getMaxEntries()
    {
	return getInt(ATTR_MAX_ENTRIES, -1);
    }

    public int getDefaultMaxAge() 
    {
	return getInt(ATTR_DEFAULT_MAX_AGE, 300);
    }
	
    public boolean getFlushFlag()
    {
	return getBoolean(ATTR_DEFAULT_MAX_AGE, false) ;
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

    /**
     * Apply outgoing filters, except the skipth one
     */
    private Reply applyOutSkip(Request request,
			       Reply reply,
			       FilterInterface[] filters,
			       int skip)
	throws ProtocolException
    {
	Reply fr = null ;
	for(int i = filters.length-1 ;
	    i>=0 && i!=skip && filters[i] != null ;
	    i--) {
	    fr = (Reply) (filters[i].outgoingFilter(request,reply,filters,i)) ;
	    if(fr != null)
		return fr ;
	}
	return null ;
    }

    /**
     * @return A Reply instance, if the filter did know how to answer
     * the request without further processing, <strong>null</strong> 
     * otherwise. 
     * @exception org.w3c.tools.resources.ProtocolException 
     * If processing should be interrupted,
     * because an abnormal situation occured. 
     */ 
    public ReplyInterface ingoingFilter(RequestInterface req,
					FilterInterface[] filters,
					int fidx)
	throws ProtocolException
    {
	Request request = (Request) req;
	String method = request.getMethod() ;
	
	if(! method.equals("GET") )
	    return null ;
	
	tag(request) ;
	
	if(isCachable(request)) {
	    SimpleCacheEntry ent = cache.retrieve(request) ;
	    if(ent != null) {
		Reply fRep = null ;
		fRep = applyIn(request,filters,fidx) ;
		if(fRep != null) return fRep ;
		
		Reply reply = request.makeReply(HTTP.NOHEADER) ;
		
		fRep = applyOutSkip(request,reply,filters,fidx) ;
		if(fRep != null) return fRep ;
		
		try {
		    ent.dump(request.getClient().getOutputStream()) ;
		} catch(IOException ex) {
		    return null ;
		}
		return reply ;
	    }
	}
	
	return null ;
    }

    /**
     * @param request The original request.
     * @param reply It's original reply. 
     * @return A Reply instance, or <strong>null</strong> if processing 
     * should continue normally. 
     * @exception ProtocolException If processing should be interrupted,  
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
	if(!isTagged(request))
	    return null ;
	
	if(isCachable(reply)) {
	    switch(reply.getStatus()) {
	    case HTTP.OK:
	    case HTTP.NO_CONTENT:
	    case HTTP.MULTIPLE_CHOICE:
	    case HTTP.MOVED_PERMANENTLY:
		try {
		    SimpleCacheEntry ce = cache.store(request,reply) ;
		    if(ce != null) {
			// storing a reply changes it, so this must be done:
			Reply fRep = applyOut(request,reply,filters,fidx) ;
			if(fRep != null) return fRep ;
			else return reply ;
		    }
		} catch(SimpleCacheException ex) {
		    cache.remove(request) ;
		}
		break ;
	    default:
		cache.remove(request) ;
	    }
	} else {
	    cache.remove(request) ;
	}
		
	return null ;
    }

	    
    private boolean isCachable(HttpMessage hmsg)
    {

	if(hmsg.getCacheControl() != null) return false ;
	String prgm[] = hmsg.getPragma() ;
	if(prgm!=null)
	    for(int i=0;i<prgm.length;i++)
		if(prgm[i].equals("no-cache")) return false ;
	
	return true ;
    }						   

    public void initialize(Object values[]) {
	super.initialize(values) ;
	cache = new SimpleCache(this) ;
    }

}

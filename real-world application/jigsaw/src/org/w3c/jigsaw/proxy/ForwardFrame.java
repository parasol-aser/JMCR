// ForwardFrame.java
// $Id: ForwardFrame.java,v 1.2 2010/06/15 17:53:07 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.proxy ;

import java.io.IOException;
import java.util.Enumeration;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.util.ObservableProperties;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HeaderDescription;
import org.w3c.www.http.HeaderValue;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpWarning;
import org.w3c.www.http.HttpInvalidValueException;
import org.w3c.www.protocol.http.HttpException;

public class ForwardFrame extends HTTPFrame {
    private static final boolean debug = false;
    private static boolean inited = false;
    private static final String nocache[] = { };

 //   private static final 
//    String micp = org.w3c.www.protocol.http.micp.MICPFilter.STATE_HOW;

    /**
     * Attribute index - The local resource, if server-wide request.
     */
    protected static int ATTR_LOCAL_ROOT = -1;
    /**
     * Attribute index - The received by attribute of that proxy.
     */
    protected static int ATTR_RECEIVED_BY = -1;
    /**
     * Attribute index - Try to trace how the request has been processed.
     */
    protected static int ATTR_TRACEREQ = -1;
    /**
     * The HTTP warning used to indicate a heuristic expiration time.
     */
    protected static HttpWarning WARN_HEURISTIC = null;

    static {
	HttpWarning w = null;
	Attribute a = null;
	Class     c = null;
	try {
	    c = Class.forName("org.w3c.jigsaw.proxy.ForwardFrame");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Declare the local root attribute:
	a = new StringAttribute("local-root"
				, null
				, Attribute.EDITABLE);
	ATTR_LOCAL_ROOT = AttributeRegistry.registerAttribute(c, a);
	// Declare the received by attribute:
	a = new StringAttribute("received-by"
				, null
				, Attribute.EDITABLE);
	ATTR_RECEIVED_BY = AttributeRegistry.registerAttribute(c, a);
	// Declare the received by attribute:
	a = new BooleanAttribute("trace-request"
				, null
				, Attribute.EDITABLE);
	ATTR_TRACEREQ = AttributeRegistry.registerAttribute(c, a);
	// Build the heuristic expiration warning:
	w = HttpFactory.makeWarning(HttpWarning.HEURISTIC_EXPIRATION);
	w.setAgent("Jigsaw");
	w.setText("Heuristic expiration time used on this entry but not set"
		  +" by the upstream agent.");
	WARN_HEURISTIC = w;	
    }

    /**
     * The set of properties we inited from.
     */
    ObservableProperties props = null;
    /**
     * The HttpManager we use.
     */
    protected  org.w3c.www.protocol.http.HttpManager manager = null;
    /**
     * Statistics - Number of hits.
     */
    public int cache_hits = 0;
    /**
     * Statistics - Number of misses.
     */
    public int cache_misses = 0;
    /**
     * Statistics - Number of successfull revalidations.
     */
    public int cache_revalidations = 0;
    /**
     * Statistics - Number of successfull revalidations.
     */
    public int cache_retrievals = 0;
    /**
     * Statistics - Number of requests that didn't use cache.
     */
    public int cache_nocache = 0;
    /**
     * Statistics - Number of requests handled.
     */
    public int reqcount = 0;
    /**
     * Statistics - Number of ICP redirects.
     */
    public int cache_icps = 0;
    /**
     * Statistics - Errors.
     */
    public int reqerred = 0;

    /**
     * Get the local root resource name.
     */

    public String getLocalRoot() {
	return getString(ATTR_LOCAL_ROOT, null);
    }

    /**
     * Get the received by attribute value.
     * <p>If this attribute is not defined, it will default to the name of the
     * host running the proxy.
     * @return A String.
     */

    public String getReceivedBy() {
	String value = getString(ATTR_RECEIVED_BY, null);
	if ( value == null )
	    if (getServer().getPort() == 80)
		value = getServer().getHost();
	    else
		value = getServer().getHost() + ":" + getServer().getPort();
	return value;
    }

    /**
     * Should we try to trace request path ?
     * @return A boolean.
     */

    public boolean getTraceRequest() {
	return getBoolean(ATTR_TRACEREQ, false);
    }

    /**
     * Get the value of the <code>via</code> header to be added.
     * @return A String encoded value for the header.
     */

    private String via = null;
    public synchronized String getVia() {
	if ( via == null ) 
	    via = "1.1 "+getReceivedBy()+" ("+getServer().getSoftware()+")";
	return via;
    }

    /**
     * Get the local root resource to use for internal requests.
     */

    protected ResourceReference lroot = null;

    public synchronized ResourceReference getLocalRootResource() {
	if ( lroot == null ) {
	    String lname = getLocalRoot();
	    if ( lname != null ) 
		lroot = getServer().loadResource(lname);
	}
	return lroot;
    }

    /**
     * Update relevant statistics (kind of a hack).
     */

    protected void updateStatistics(org.w3c.www.protocol.http.Request r) {
	reqcount++;
//	Integer how = (Integer) r.getState(CacheFilter.STATE_HOW);
//	if ( how != null ) {
//	    synchronized(this) {
//		switch(how.intValue()) {
//		  case 1:
//		      cache_hits++;
//		      break;
//		  case 2:
//		      cache_misses++;
//		      break;
//		  case 3:
//		      cache_revalidations++;
//		      break;
//		  case 4:
//		      cache_retrievals++;
//		      break;
//		}
//	    }
//	} else if ( ! r.hasState(micp) ) {
//	    synchronized(this) {
//		cache_nocache++;
//	    }
//	} else {
//	    synchronized(this) {
//		cache_icps++;
//	    }
//	}
    }

    /**
     * Duplicate a server side request into a client side request.
     * @param request The server side request.
     * @return A Client side request.
     * @exception HTTPException if processing the request failed.
     * @exception IOException if an IO error occurs.
     */

    protected org.w3c.www.protocol.http.Request dupRequest(Request request) 
	throws HTTPException, IOException
    {
	org.w3c.www.protocol.http.Request     req = null;
	String                            mth = request.getMethod();
	// Create a client request, and initialize its target & method:
	req = manager.createRequest();
	// reset user agentm as it will be set (or not set) by
	// the upstream request
	req.setUserAgent(null);
	req.setAccept(null);
	req.setURL(request.getURL());
	req.setMethod(mth);
	// If we can have 1xx headers to send back, setup an observer:
	if ((request.getMajorVersion() >= 1)
	    && (request.getMinorVersion() >= 1)) {
	    req.setObserver(new ProxyRequestObserver(request, this));
	}
	// Update the client request fields:
	Enumeration e = request.enumerateHeaderDescriptions();
	while ( e.hasMoreElements() ) {
	    HeaderDescription d = (HeaderDescription) e.nextElement();
	    HeaderValue       v = request.getHeaderValue(d);
	    if ( v != null )
		req.setHeaderValue(d, v);
	}
	// Get rid of all hop-by-hop headers:
	req.setHeaderValue(Reply.H_CONNECTION, null);
	req.setHeaderValue(Reply.H_PROXY_CONNECTION, null);
	req.setHeaderValue(Reply.H_PUBLIC, null);
	req.setHeaderValue(Request.H_PROXY_AUTHORIZATION, null);
	req.setHeaderValue(Reply.H_TRANSFER_ENCODING, null);
	req.setHeaderValue(Request.H_TE, null);
	req.setHeaderValue(Request.H_TRAILER, null);
	req.setHeaderValue(Reply.H_UPGRADE, null);
	req.removeHeader("keep-alive");
	// By removing that HOST, we make sure the real Host header gets
	// computed by the client side API
	req.setHeaderValue(Request.H_HOST,null);
	// Get rid of more hop by hop headers:
	String conn[] = request.getConnection();
	if ( conn != null ) {
	    for (int i = 0 ; i < conn.length ; i++) 
		req.removeHeader(conn[i]);
	}
	// check if nasty people are mixing Content-Length and chunking
	if (req.getContentLength() >= 0 ) {
	    String te[] = request.getTransferEncoding() ;
	    if ( te != null ) {
		for (int i = 0 ; i < te.length ; i++) {
		    if (te[i].equals("chunked")) {
			req.setContentLength(-1);
		    }
		}
	    }
	}
	// 14.31 decrement the value of MaxForward for TRACE and OPTIONS
	if ((request.getMaxForwards() != -1) && 
	    (request.getMethod().equals("TRACE") || 
	     request.getMethod().equals("OPTIONS"))) {
	    req.setMaxForwards(request.getMaxForwards()-1);
	}
	// Fix versions mismatches:
	if ( request.hasPragma("no-cache") )
	    req.setNoCache(nocache);
	// Add the via clause:
	req.addVia(getVia());
	// Update the request output stream:
	req.setOutputStream(request.getInputStream());
	return req;
    }

    /**
     * Duplicate the given client side reply into a server side one.
     * Perform any actions requested by HTTP/1.1.
     * @param request The request ebing processed.
     * @param reply The reply to clone.
     * @return A server-side Reply instance.
     * @exception HTTPException If some HTTP errors occured in the process.
     * @exception IOException If setting the streams failed.
     */

    protected Reply dupReply(Request request
			     , org.w3c.www.protocol.http.Reply rep) 
	throws HTTPException, IOException
    {
	Reply reply = request.makeReply(rep.getStatus());
	// get rid of "by default" headers wchich SHOULD NOT be modified
	reply.setHeaderValue(Reply.H_SERVER, null);
        // Duplicate reply header values:
	Enumeration e = rep.enumerateHeaderDescriptions();
	while ( e.hasMoreElements() ) {
	    HeaderDescription d = (HeaderDescription) e.nextElement();
	    HeaderValue       v = rep.getHeaderValue(d);
	    if ( v != null )
		reply.setHeaderValue(d, v);
	}
	// Get rid of hop by hop headers:
	reply.setHeaderValue(Reply.H_CONNECTION, null);
	reply.setHeaderValue(Reply.H_PROXY_CONNECTION, null);
	reply.setHeaderValue(Reply.H_PROXY_AUTHENTICATE, null);
	reply.setHeaderValue(Reply.H_PUBLIC, null);
	reply.setHeaderValue(Reply.H_TRANSFER_ENCODING, null);
	reply.setHeaderValue(Reply.H_UPGRADE, null);
	reply.setHeaderValue(Reply.H_TRAILER, null);
	reply.removeHeader("keep-alive");
	// Get rid of the fields enumerated in the connection header:
	String conn[] = rep.getConnection();
	if (conn != null) {
	    for (int i = 0 ; i < conn.length ; i++) 
		reply.removeHeader(conn[i]);
	}
	// check if nasty people are mixing Content-Length and chunking
	if (reply.getContentLength() >= 0 ) {
	    String te[] = rep.getTransferEncoding() ;
	    if ( te != null ) {
		for (int i = 0 ; i < te.length ; i++) {
		    if (te[i].equals("chunked")) {
			reply.setContentLength(-1);
		    }
		}
	    }
	}
	// Update the via route:
	reply.addVia(getVia());
	// Update the reply output stream:
	try {
	    reply.setStream(rep.getInputStream());
	} catch (Exception ex) {};
	// if reply is using bad HTTP version we should close it
	if (rep.getMajorVersion() == 0) {
	    reply.setKeepConnection(false);
	}
	// if HTTP/1.0 and no Content-Length also...
	if ((rep.getMajorVersion() == 1) && 
	    (rep.getMajorVersion() == 0) &&
	    (reply.getContentLength() == -1)) {
	    reply.setKeepConnection(false);
	}
	// check the age
	int age = rep.getAge();
	if (age >= 0 ) {
	    // check if it is an heuristic expiration without a warning
	    if (age > 86400) {
		if ((rep.getExpires() == -1) && (rep.getMaxAge() == -1) &&
		    (rep.getSMaxAge() == -1)) {
		    // rfc2616: 13.2.4 on behalf of a bad upstream server
		    HttpWarning w[] = rep.getWarning();
		    boolean doit = true;
		    if (w != null) {
			for (int i=0; doit && (i<w.length); i++) {
			    doit = (w[i].getStatus() != 113);
			}
		    }
		    if (doit) {
			reply.addWarning(WARN_HEURISTIC);
		    }
		}
	    }
	}
	reply.setProxy(true);
	return reply;
    }
    /**
     * Perform the given proxied request.
     * @param request The request to perform.
     * @return A Reply instance.
     * @exception org.w3c.tools.resources.ProtocolException if processing
     * the request failed.
     * @exception org.w3c.tools.resources.ResourceException if the resource
     * got a fatal error.
     */

    public ReplyInterface perform(RequestInterface ri) 
	throws org.w3c.tools.resources.ProtocolException,
	       org.w3c.tools.resources.ResourceException
    {
	Request request = (Request) ri;
	Reply   reply  = null;
	boolean stated = false;
	// check the expectations
	if (!checkExpect(request)) {
	    reply = createDefaultReply(request, HTTP.EXPECTATION_FAILED);
	    reply.setContent("The requested expectation could not be"+
			     " met by the resource");
	    return reply;
	}
	// Perform the request:
	try {
	    if (request.getMaxForwards() != -1) { // 14.31 decrement the value
		if (request.getMaxForwards() == 0) {
		    if (request.getMethod().equals("TRACE") ||
			request.getMethod().equals("OPTIONS"))
			return super.perform(request);
		}
	    } 

	    org.w3c.www.protocol.http.Request     req = dupRequest(request);
	    org.w3c.www.protocol.http.Reply       rep = null;
	    // Perform the request
	    rep = manager.runRequest(req);
	    // Dump back the client reply into a server reply:
	    reply = dupReply(request, rep);
	    // Update statistics:
	    updateStatistics(req);
	    stated = true;
	    // Trace the request (debug):
	    if ( getTraceRequest() ) {
//		if ( req.hasState(CacheFilter.STATE_HOW) ) {
//		    System.out.println(request.getURL()+": "+
//				       CacheFilter.getHow(req));
//		}
//		if ( req.hasState(micp) )
//		    System.out.println(req.getURL()+": (icp) "+
//				       req.getState(micp));
	    }
	} catch (HTTPException shex) { 
	    // An server-side HTTP-related error happened
	    if (debug) {
		System.out.println("HTTP Server Exception while running"
				   +" request:");
		shex.printStackTrace();
	    }
	    // Make sure the request is accounted:
	    if ( ! stated ) {
		synchronized(this) {
		    reqcount++;
		    reqerred++;
		}
		stated = true;
	    }
	    Reply r = (Reply) shex.getReply();
	    if (reply == null) {
		// Send an appropriate error message back:
		reply = request.makeReply(HTTP.BAD_GATEWAY);
		boolean showerr = 
		    props.getBoolean(httpd.DISPLAY_URL_ON_ERROR_P, false);
		
		if (showerr) {
		    reply.setContent("An HTTP error occured while getting: "
				     + "<p><strong>"+request.getURL()
				     + "</strong>"
				     + "<p>Details \""+shex.getMessage()+"\"."
				     + "<hr>Generated by <i>"
				     + getServer().getURL());
		} else {
		    reply.setContent("An HTTP error occured while getting the "
				     + "requested URI.</p>"
				     + "<hr>Generated by <i>"
				     + getServer().getURL());
		}
		reply.setContentType(org.w3c.www.mime.MimeType.TEXT_HTML);
	    }
	} catch (HttpInvalidValueException iex) { 
	    // An server-side HTTP-related error happened
	    if (debug) {
		System.out.println("Parsing Exception while running"
				   +" request:");
		iex.printStackTrace();
	    }
	    // Make sure the request is accounted:
	    if ( ! stated ) {
		synchronized(this) {
		    reqcount++;
		    reqerred++;
		}
		stated = true;
	    }
	    reply = request.makeReply(HTTP.BAD_GATEWAY);
	    boolean showerr = 
		props.getBoolean(httpd.DISPLAY_URL_ON_ERROR_P, false);
	    
	    if (showerr) {
		reply.setContent("An HTTP error occured while getting: "
				 + "<p><strong>"+request.getURL()
				 + "</strong>"
				 + "<p>Details \""+iex.getMessage()+"\"."
				 + "<hr>Generated by <i>"
				 + getServer().getURL());
	    } else {
		reply.setContent("An HTTP error occured while getting the "
				 + "requested URI.</p>"
				 + "<hr>Generated by <i>"
				 + getServer().getURL());
	    }
	    reply.setContentType(org.w3c.www.mime.MimeType.TEXT_HTML);
	} catch (HttpException hex) { 
	    // An HTTP-related error happened
	    if (debug) {
		System.out.println("HTTP Exception while running request:");
		hex.printStackTrace();
	    }
	    // Make sure the request is accounted:
	    if ( ! stated ) {
		synchronized(this) {
		    reqcount++;
		    reqerred++;
		}
		stated = true;
	    }
	    org.w3c.www.protocol.http.Reply rep = hex.getReply();
	    if (rep != null) {
		try {
		    reply = dupReply(request, rep);
		} catch (IOException ioex) {
		    reply = null;
		}
	    }
	    if (reply == null) {
		// Send an appropriate error message back:
		String msg = hex.getMessage();
		if (msg.startsWith("Unable to contact target server ")) {
		    reply = request.makeReply(HTTP.GATEWAY_TIMEOUT);
		} else {
		    reply = request.makeReply(HTTP.BAD_GATEWAY);
		}
		boolean showerr = 
		    props.getBoolean(httpd.DISPLAY_URL_ON_ERROR_P, false);
		
		if (showerr) {
		    reply.setContent("An HTTP error occured while getting: "
				     + "<p><strong>"+request.getURL()
				     + "</strong>"
				     + "<p>Details \""+hex.getMessage()+"\"."
				     + "<hr>Generated by <i>"
				     + getServer().getURL());
		} else {
		    reply.setContent("An HTTP error occured while getting the "
				     + "requested URI.</p>"
				     + "<hr>Generated by <i>"
				     + getServer().getURL());
		}
		reply.setContentType(org.w3c.www.mime.MimeType.TEXT_HTML);
	    }
	} catch (Exception ex) {
	    // Debug trace (when debuggging):
	    if ( debug ) {
		System.out.println("Exception while running request:");
		ex.printStackTrace();
	    }
	    // Make sure the request is accounted:
	    if ( ! stated ) {
		synchronized(this) {
		    reqcount++;
		    reqerred++;
		}
		stated = true;
	    }
	    // Send an appropriate error message back:
	    reply = request.makeReply(HTTP.GATEWAY_TIMEOUT);
	    boolean showerr = props.getBoolean(httpd.DISPLAY_URL_ON_ERROR_P, 
					       false);
	    
	    if (showerr) {
		reply.setContent("An HTTP error occured while getting: "
				 + "<p><strong>"+request.getURL()+"</strong>"
				 + "<p>Details \""+ex.getMessage()+"\"."
				 + "<hr>Generated by <i>"
				 + getServer().getURL());
	    } else {
		reply.setContent("An HTTP error occured while getting the "
				 + "requested URI.</p>"
				 + "<hr>Generated by <i>"
				 + getServer().getURL());
	    }
	    reply.setContentType(org.w3c.www.mime.MimeType.TEXT_HTML);
	}
	return reply;
    }

    /**
     * This resource is being unloaded.
     * Tell the HttpManager to save any pending data to stable storage.
     */

    public synchronized void notifyUnload() {
	if (manager != null) {
	    manager.sync();
	}
	super.notifyUnload();
    }

    /**
     * companion to initialize, called after the register
     */

    public void registerResource(FramedResource resource) {
	super.registerOtherResource(resource);
	this.props = getResource().getServer().getProperties();
	synchronized(this.getClass()) {
	    if (!inited) {
		// Register the client side properties (if still needed):
		httpd server = (httpd) getServer();
		server.registerPropertySet(new ProxyProp("proxy", server));
		inited = true;
	    }
	}
	manager = org.w3c.www.protocol.http.HttpManager.getManager(props);
    }

    public void initialize(Object values[]) {
	super.initialize(values) ;
    }
}

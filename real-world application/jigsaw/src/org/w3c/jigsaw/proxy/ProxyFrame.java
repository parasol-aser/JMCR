// ProxyFrame.java
// $Id: ProxyFrame.java,v 1.2 2010/06/15 17:53:07 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.proxy;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import java.util.Date;
import java.util.Hashtable;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.jigsaw.html.HtmlGenerator;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.DirectoryResource;
import org.w3c.tools.resources.DummyResourceReference;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.jigsaw.http.socket.SocketClientFactory;
import org.w3c.util.ObservableProperties;

/*
class FtpTunnel extends HTTPFrame {

    public Reply get(Request request) 
	throws HTTPException
    {
	try {
	    MimeType mt;
	    URLConnection c  = request.getURL().openConnection();
	    InputStream   in = c.getInputStream();
	    boolean markUsable = in.markSupported();
	    // We don't know the content length
	    Reply reply = createDefaultReply(request, HTTP.OK);
	    reply.addPragma("no-cache");
	    reply.setNoCache();
	    reply.setContentLength(c.getContentLength());
	    if (markUsable)
		in.mark(Integer.MAX_VALUE);
	    try {
		mt = new MimeType(c.getContentType());
	    } catch (MimeTypeFormatException me) {
		mt = MimeType.TEXT_PLAIN;
	    }
	    reply.setContentType(mt);
	    if (markUsable) // cope with a well known java bug
		in.reset();
	    reply.setStream(in);
	    return reply;
	} catch (Exception ex) {
	    throw new HTTPException("Unable to ftp \""
				    + request.getURL()
				    + ", details \""+ex.getMessage()+"\"");
	}
    }

}
*/

class Stats extends HTTPFrame {
    ProxyFrame proxy     = null;
    Date       startdate = null;
    boolean    hasICP    = true;

    protected String percentage(int part, int tot) {
	double p = ((double) part / ((double) tot)) * ((double) 100);
	return Integer.toString((int) p)+"%";
    }

    public Reply get(Request request) {
	Reply         r = createDefaultReply(request, HTTP.OK);
	HtmlGenerator g = new HtmlGenerator("Proxy statistics");
	int           c = proxy.reqcount+proxy.reqerred;
	
	if ( c == 0 )
	    c = 1;
	g.addMeta("Refresh", "30");
	proxy.addStyleSheet(g);
	g.append("<h1>Proxy statistics</h1>");
	g.append("<p>The proxy was last started at: <em>"
                 + startdate
                 + "</em>");
	g.append("<p><table align=\"center\" border=\"1\"");
	g.append("<tr><th colspan=\"3\">Counter<th>count<th>percentage");
	// The total number of hits to the proxy:
	g.append("<tr><td colspan=\"3\">Total number of handled requests");
	g.append("<td align=center>", Integer.toString(c));
	g.append("<td align=center>", percentage(c, c));
	// The total number of errors:
	g.append("<tr><td width=50><td colspan=\"2\">Erred requests");
	g.append("<td align=center>", Integer.toString(proxy.reqerred));
	g.append("<td align=center>", percentage(proxy.reqerred, c));
	// The total number of ICP redirects:
	g.append("<tr><td width=50><td colspan=\"2\">ICP redirects");
	g.append("<td align=center>", Integer.toString(proxy.cache_icps));
	g.append("<td align=center>", percentage(proxy.cache_icps, c));
	// The total number of no-cache:
	g.append("<tr><td width=50><td colspan=\"2\">Non cacheable");
	g.append("<td align=center>", Integer.toString(proxy.cache_nocache));
	g.append("<td align=center>", percentage(proxy.cache_nocache, c));
	// Cache accesses:
	int cached = (proxy.cache_hits +
		      proxy.cache_misses +
		      proxy.cache_revalidations +
		      proxy.cache_retrievals);
	g.append("<tr><td width=50><td colspan=\"2\">Cache Accesses");
	g.append("<td align=center>", Integer.toString(cached));
	g.append("<td align=center>", percentage(cached, c));
	// Hits (served by cache)
	g.append("<tr><td width=50><td width=50><td>Hits (served by cache)");
	g.append("<td align=center>", Integer.toString(proxy.cache_hits));
	g.append("<td align=center>", percentage(proxy.cache_hits, c));
	// Hits (revalidations)
	g.append("<tr><td width=50><td width=50><td>Hits (revalidations)");
	g.append("<td align=center>"
		 , Integer.toString(proxy.cache_revalidations));
	g.append("<td align=center>"
		 , percentage(proxy.cache_revalidations, c));
	// Misses (no cache entry)
	g.append("<tr><td width=50><td width=50><td>Misses (no entry)");
	g.append("<td align=center>", Integer.toString(proxy.cache_misses));
	g.append("<td align=center>", percentage(proxy.cache_misses, c));
	// Misses (retrievals)
	g.append("<tr><td width=50><td width=50><td>Misses (retrievals)");
	g.append("<td align=center>",Integer.toString(proxy.cache_retrievals));
	g.append("<td align=center>", percentage(proxy.cache_retrievals, c));
	g.append("</table>");
	// Some goodies:
	g.append("<hr>Generated by <i>"
		 , proxy.getServer().getURL().toExternalForm());
	r.setStream(g);
	r.addPragma("no-cache");
	r.setNoCache();
	return r;
    }

    Stats(ProxyFrame proxy) {
	this.proxy     = proxy;
	this.startdate = new Date(System.currentTimeMillis());
    }
}

/**
 * A proxy module for Jigsaw.
 */

public class ProxyFrame extends ForwardFrame {
    /**
     * Attribute index - Should we tunnel ftp accesses ?
     */
    protected static int ATTR_HANDLEFTP = -1;

    static {
	Attribute a = null;
	Class     c = null;
	try {
	    c = Class.forName("org.w3c.jigsaw.proxy.ProxyFrame");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register the handle-ftp attribute:
	a = new BooleanAttribute("handle-ftp"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_HANDLEFTP = AttributeRegistry.registerAttribute(c, a);
    }

    URL       url        = null;
//    FtpTunnel ftphandler = null;
    Stats     statistics = null;
    DummyResourceReference drr = null;
    FramedResource statsres = null;
    String default_hostaddr = null;

    /**
     * Trap changes to the handleftp attribute.
     * @param idx The attribute being set.
     * @param value The new value for that attribute.
     */

    public void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if ( idx == ATTR_HANDLEFTP ) {
	    boolean b = ((Boolean) value).booleanValue();
//	    if ( b ) {
//		ftphandler = new FtpTunnel();
//	    } else {
//		ftphandler = null;
//	    }
	}
    }

    /**
     * Do we handle ftp ?
     * @return A boolean.
     */

    public boolean checkHandleFTP() {
	return getBoolean(ATTR_HANDLEFTP, false);
    }

    /**
     * Lookup for an proxied resource.
     * @param request The request whose URI is to be looked up.
     * @param ls The current lookup state
     * @param lr The result
     * @exception org.w3c.tools.resources.ProtocolException If something fails.
     */
    public boolean lookupOther(LookupState ls, LookupResult lr) 
	throws org.w3c.tools.resources.ProtocolException
    {
	// Get the full URL from the request:
	Request request = (Request) ls.getRequest();
	URL     requrl  = ((request != null)
			   ? request.getURL()
			   : null);
	boolean host_equiv = false;

	// loop check
	if (request != null) {
	    String vias[] = request.getVia();
	    if ((url != null)
		&& (requrl.getPort() == url.getPort())
		&& (vias != null && vias.length > 5)) {	
		// maybe a loop, let's try to sort it out with an expensive
		// checking on IPs
		String hostaddr;
		
		ObservableProperties props = getServer().getProperties();
		hostaddr = props.getString(SocketClientFactory.BINDADDR_P,
					   default_hostaddr);
		if (requrl != null) {
		    InetAddress targhost;
		    String reqhostaddr;
		    
		    try {
			targhost = InetAddress.getByName(requrl.getHost());
			reqhostaddr = targhost.getHostAddress();
			host_equiv =  reqhostaddr.equals(hostaddr);
		    } catch (UnknownHostException uhex) {};
		}
	    }
	}
	if (((url != null)
	     && (requrl != null)
	     && ((requrl.getPort() == url.getPort()) || 
		 (requrl.getPort() * url.getPort() == -80))
	     && ( host_equiv || 
		  (requrl.getHost().equalsIgnoreCase(url.getHost()))))
	     || (ls.isInternal())) {
	    // Call super.lookup:
	    super.lookupOther(ls, lr);
	    if ( ls.hasMoreComponents() ) {
		ResourceReference root = getLocalRootResource();
		if ( root == null ) {
		    lr.setTarget(this.getResource().getResourceReference());
		    return true;
		}
		try {
		    // because the root eats the lookup state components
		    // we have to return true.
		    // Should not be continued by the caller.
		    FramedResource res = (FramedResource)root.lock();
		    boolean done = res.lookup(ls, lr);
		    if (! done)
			lr.setTarget(null);
		    return true;
		} catch (InvalidResourceException ex) {
		    // should never happen with the root resource
		    ex.printStackTrace();
		} finally {
		    root.unlock();
		}
		return true; // should never be reached 
	    } else {
		request.setState(STATE_CONTENT_LOCATION, "true");
		// return the index file.
		String index = getIndex();
		if ( index != null && index.length() > 0) {
		    ResourceReference root = getLocalRootResource();
		    try {
			DirectoryResource dir = 
			    (DirectoryResource)root.lock();
			ResourceReference rr = dir.lookup(index);
			if (rr != null) {
			    try {
				FramedResource rindex = 
				    (FramedResource) rr.lock();
				return rindex.lookup(ls,lr);
			    } catch (InvalidResourceException ex) {
			    } finally {
				rr.unlock();
			    }
			}
		    } catch (InvalidResourceException ex) {
			root.unlock();
		    }
		}
		lr.setTarget(drr);
		return true;
	    }
	} else {
	    // Always invoke super lookup, after notification that its a proxy
	    request.setProxy(true);
	    super.lookupOther(ls, lr);
	    if ( requrl.getProtocol().equals("ftp") ) {
//		if (ftphandler != null)
//		    lr.setTarget(ftphandler);
//		else 
		    lr.setTarget(null);
		return true;
	    } else {
		lr.setTarget(this.getResource().getResourceReference());
		return true;
	    }
	}
    }

    /**
     * do the normal lookup, and set the proxy boolean flag if needed
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception org.w3c.tools.resources.ProtocolException If an error 
     * relative to the protocol occurs
     */

    public boolean lookup(LookupState ls, LookupResult lr) 
	throws org.w3c.tools.resources.ProtocolException
    {
	// Internal lookup:
	if ( ls.isInternal() )
	    return super.lookup(ls, lr);

	Request request = (Request) ls.getRequest();
	URL     requrl  = request.getURL() ;

	if ((url != null)
	    && ((requrl.getPort() == url.getPort()) || 
		(requrl.getPort() * url.getPort() == -80))
	    && (requrl.getHost().equalsIgnoreCase(url.getHost()))) {
	    return super.lookup(ls, lr);
	} else {
	    String vias[] = request.getVia();
	    if ((url != null)
		&& (requrl.getPort() == url.getPort())
		&& (vias != null && vias.length > 5)) {
		// maybe a loop, let's try to sort it out with an expensive
		// checking on IPs
		String hostaddr = null;
		ObservableProperties props = getServer().getProperties();
		hostaddr = props.getString(SocketClientFactory.BINDADDR_P,
					   default_hostaddr);
		if (requrl != null) {
		    InetAddress targhost;
		    String reqhostaddr;
		    
		    try {
			targhost = InetAddress.getByName(requrl.getHost());
			reqhostaddr = targhost.getHostAddress();
			if (reqhostaddr.equals(hostaddr))
			    return super.lookup(ls, lr);
		    } catch (Exception ex) {
			// can't get it, let the proxy bark!
		    }
		}
	    }
	    // not internal, so set it as a proxied call for lookup
	    request.setProxy(true);
	}
	return super.lookup(ls, lr);
    }
    /**
     * companion to initialize, called after the register
     */

    public void registerResource(FramedResource resource) {
	super.registerResource(resource);
	// Our home url:
	url = getServer().getURL();
	// If we do handle ftp, initialize:
//	if ( checkHandleFTP() )
//	    ftphandler = new FtpTunnel();
	// Initialize the stats:
	ResourceFrame frame = null;
	try {
	    frame = getFrame(Class.forName("org.w3c.jigsaw.proxy.Stats"));
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    // don't give a damn
	}
	if (frame != null)
	    return;
	statistics = new Stats(this);
	statsres = new FramedResource(); // create an empty resource
	statsres.registerFrame(statistics, new Hashtable(1));
	drr = new DummyResourceReference(statsres);
    }    

    /**
     * Update the URL in which we are installed.
     * @param values The default attribute values.
     */

    public void initialize(Object values[]) {
	super.initialize(values);
	try {
	    default_hostaddr = InetAddress.getLocalHost().getHostAddress();
	} catch (UnknownHostException ex) {
	    // can't get localhost? Force it (ugly but...)
	    default_hostaddr = "127.0.0.1";
	}
    }
}

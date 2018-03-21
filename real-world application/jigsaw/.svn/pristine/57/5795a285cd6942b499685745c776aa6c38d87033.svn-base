// UseProxyFilter.java
// $Id: UseProxyFilter.java,v 1.2 2010/06/15 17:52:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.w3c.www.http.HTTP;
import org.w3c.jigsaw.html.HtmlGenerator;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.http.socket.SocketClient;

/**
 * Restrict access to a proxy, to acces the protected resource, you
 * must go to a specific proxy. It acts as a demontrator for the HTTP/1.1
 * spec.
 */

public class UseProxyFilter extends ResourceFilter {

    /**
     * Attribute index - The IP of the proxy
     */
    protected static int ATTR_PROXY = -1 ;

    /**
     * the InetAdress of the proxy
     */

    private InetAddress proxy_ia  = null;
    private URL         proxy_url = null;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	
	try {
	    cls = Class.forName("org.w3c.jigsaw.filters.UseProxyFilter") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// Declare the counter attribute
	a = new StringAttribute("proxyURL"
				, null
				, Attribute.EDITABLE) ;
	ATTR_PROXY = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * We check for the IP of the incoming request
     * If the IP of the incoming request is not the on of the proxy
     * it returns a Use_Proxy reply
     * @param request The request being processed.
     * @return <strong>null</strong> if ok a "Use Proxy" otherwise.
     */

    public synchronized ReplyInterface ingoingFilter(RequestInterface req) {
	Request request = (Request) req;
	Client cl = request.getClient();
	if (cl instanceof org.w3c.jigsaw.http.socket.SocketClient) {
	    SocketClient sc = (SocketClient) cl;
	    InetAddress ia = sc.getInetAddress();
	    if (ia.equals(proxy_ia)) // same, it is ok :)
		return null;
	}
	// failed, restrict access
	Reply r = request.makeReply(HTTP.USE_PROXY);
	if (r != null) {
	    HtmlGenerator g = new HtmlGenerator("Use Proxy");
	    g.append("You should use the following proxy to access" +
		     " this resource: " + getString(ATTR_PROXY, "localhost"));
	    r.setStream(g);
	}
	r.setLocation(proxy_url);
	return r;
    }

    /**
     * cache the right values
     */

    private void updateValues() {
	String proxy = getString(ATTR_PROXY, null);
	if (proxy != null) {
	    try {
		proxy_url = new URL(proxy);
	    } catch (MalformedURLException ex) {
		// error this is not a valid URL!
		ex.printStackTrace();
	    }
	}
	if (proxy_url != null) {
	    try {
		proxy_ia = InetAddress.getByName(proxy_url.getHost());
	    } catch (UnknownHostException ex) {
		// well.. keep the old one!
		ex.printStackTrace();
	    }
	}
    }

    /**
     * We override setValues to compute locally everything we need
     * @param idx The index of the attribute to modify.
     * @param value The new attribute value.
     */
    public void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if (idx == ATTR_PROXY) {
	    updateValues();
	}
    }

    /**
     * Initialize the filter.
     */
    public void initialize(Object values[]) {
	super.initialize(values);
	updateValues();
    }
}



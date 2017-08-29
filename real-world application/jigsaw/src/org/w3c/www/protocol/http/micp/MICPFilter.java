// MICPFilter.java
// $Id: MICPFilter.java,v 1.2 2010/06/15 17:53:09 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.micp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URL;

import java.io.IOException;
import java.io.PrintStream;

import java.util.Vector;

import org.w3c.util.ObservableProperties;
import org.w3c.util.PropertyMonitoring;

import org.w3c.www.protocol.http.HttpException;
import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.PropRequestFilter;
import org.w3c.www.protocol.http.PropRequestFilterException;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;

import org.w3c.www.protocol.http.cache.CacheFilter;

class ReplyWaiter {
    int        id     = -1;
    String     url    = null;
    MICPFilter filter = null;
    // Wait until we get a reply.

    synchronized URL waitReply() {
	// Wait for something to happen:
	try {
	    wait(filter.timeoutValue);
	    if ( url != null ) {
		try {
		    return new URL(url);
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	} finally {
	    filter.removeWaiter(this);
	}
	return null;
    }

    synchronized void wakeup(String url) {
	this.url = url;
	notifyAll();
    }

    ReplyWaiter(MICPFilter filter, int id) {
	this.filter = filter;
	this.id     = id;
    }
}

public class MICPFilter extends Thread 
    implements PropRequestFilter, PropertyMonitoring, MICP
{
    /**
     * State - To mark requests that have been redirected.
     * This state is set to the URL of the proxy that has been used to run
     * the request, if any.
     */
    public static final String 
    STATE_HOW = "org.w3c.www.protocol.micp.how";
    /**
     * Properties - Our debug flag.
     */
    public static final String
    DEBUG_P = "org.w3c.www.protocol.http.micp.debug";
    /**
     * Properties - Our multicast group address.
     */
    public static final String
    ADDRESS_P = "org.w3c.www.protocol.http.micp.address";
    /**
     * Properties - Our own UDP port number.
     */
    public static final String
    PORT_P = "org.w3c.www.protocol.http.micp.port";
    /**
     * Properties - Our default timeout value.
     */
    public static final String
    TIMEOUT_P = "org.w3c.www.protocol.http.micp.timeout";
    /**
     * Properties - disable caching when fetching from a neighbour proxy.
     */
    public static final String
    DISABLE_CACHE_P = "org.w3c.www.protocol.http.micp.disable-cache";
    /**
     * Properties - location of proxy to redirect to (if success)
     */
    public static final String
    PROXY_P = "org.w3c.www.protocol.http.micp.proxy";

    /**
     * The properties we are initialized from.
     */
    protected ObservableProperties props = null;
    /**
     * The CacheFilter we are working for.
     */
    protected CacheFilter cache = null;
    /**
     * Our default timeout value for waiting for replies (in ms).
     */
    protected long timeoutValue = 500;
    /**
     * Our we in debug mode ?
     */
    protected boolean debug = false;
    /**
     * Our sending and source port.
     */
    int port = -1;
    /**
     * Should we disablecaching when fetching through a proxy ?
     */
    protected boolean disableCache = true;
    /**
     * Queue of threads waiting for some replies.
     */
    protected Vector waiters = null;
    /**
     * Our multicast group address.
     */
    protected InetAddress addr = null;
    /**
     * Our local proxy address, in a byte array.
     */
    protected byte localproxy[] = null;
    /**
     * Our local internet address, as a long.
     */
    protected long localsrc = -1;
    /**
     * Our local internet address as a byte array.
     */
    protected byte localaddr[] = null;
    /**
     * Our socket to the group.
     */
    protected MulticastSocket socket = null;
    /**
     * Our TTL for writing packets.
     */
    protected int ttl = 1;

    private MICPReadWrite micprw    = null;
    private byte          sendbuf[] = null;
    private int           sendid    = 0;
    private ReplyWaiter   queue[]   = null;

    private final synchronized int nextId() {
	return ++sendid;
    }
    protected final synchronized ReplyWaiter lookupWaiter(int id) {
	for (int i = 0 ; i < queue.length ; i++) {
	    if ((queue[i] != null) && (queue[i].id == id))
		return queue[i];
	}
	return null;
    }

    protected final synchronized void removeWaiter(ReplyWaiter w) {
	for (int i = 0 ; i < queue.length ; i++) {
	    if ( queue[i] == w ) {
		queue[i] = null;
		return;
	    }
	}
    }

    protected final synchronized ReplyWaiter addWaiter(int id) {
	ReplyWaiter w = new ReplyWaiter(this, id);
	for (int i = 0 ; i < queue.length ; i++) {
	    if ( queue[i] == null ) 
		return queue[i] = w;
	}
	// Resize queue needed:
	ReplyWaiter newqueue[] = new ReplyWaiter[queue.length+5];
	System.arraycopy(queue, 0, newqueue, 0, queue.length);
	newqueue[queue.length] = w;
	queue = newqueue;
	return w;
    }

    /**
     * Wait for the reply on this reqiest identifier.
     * Don't wait for more than the timeout value.
     * @return The replied URL if any, or <strong>null</strong>.
     */

    public URL waitOn(int id) {
	ReplyWaiter w = addWaiter(id);
	return w.waitReply();
    }

    /**
     * Parse and handle the given MICP packet.
     * This method does not use (currently) the reader/writer. The point is 
     * that on queries, it tries to reuse the receive buffer for output
     * (which allows to handle queries without nearly zero allocations).
     * @param buf The packet.
     * @param len Total length of packet.
     */

    protected void handle(byte buf[], int len) 
	throws IOException
    {
	// Is this a reasonable packet ?
	if ( len <= 12 )
	    return;
	// Parse the packet:
	int  packlen = ((buf[2] & 0xff) << 8) + (buf[3] & 0xff);
	if ( len < packlen )
	    return;
	long src = ((  (buf[4] & 0xff) << 24)
		    + ((buf[5] & 0xff) << 16)
		    + ((buf[6] & 0xff) << 8)
		    +  (buf[7] & 0xff));
	int  id  = ((  (buf[8] & 0xff000000) << 24)
		    + ((buf[9] & 0x00ff0000) << 16)
		    + ((buf[10] & 0x0000ff00) << 8)
		    +  (buf[11] & 0x000000ff));
	String url = new String(buf, 12, len-12);
	// Is this a reply or a query ?
	if ( buf[1] == MICP_OP_QUERY ) {
	    // Skip loopback, or unexpected requests:
	    if ((src == localsrc) || (localproxy == null))
		return;
	    // A query, let's run it:
	    if ( false /* FIXME cache.hasResource(url) */) {
		// If possible reuse the avail buffer:
		int newlen = 12 + localproxy.length;
		if ( newlen >= len ) {
		    byte newbuf[] = new byte[newlen];
		    System.arraycopy(buf, 0, newbuf, 0, 12);
		    buf = newbuf;
		}
		// Yet another hack I am going to pay for some day :-)
		buf[1] = MICP_OP_REPLY;
		buf[2] = (byte) ((newlen & 0xff00) >>> 8);
		buf[3] = (byte) (newlen & 0xff);
		System.arraycopy(localproxy, 0, buf, 12, localproxy.length);
		DatagramPacket p = new DatagramPacket(buf, newlen
						      , addr, port);
		socket.send(p);
	    }
	} else {
	    // A reply, handle it:
	    if ( src == localsrc ) {
		// So lazy today....
		ReplyWaiter w = lookupWaiter(id);
		if ( w != null )
		    w.wakeup(url);
		return;
	    } 
	}
    }

    /**
     * Locate some proxy for the given URL.
     * Synchronizing this method allows to reuse the emit buffer (kind of
     * nice), and anyway, there will be a synchronized point when writting
     * to the network (see, I am not convinced)
     * @param u The URL to locate.
     * @return An integer reply identifier to wait on for the reply.
     * @exception IOException If the multicast group wasn't available.
     */

    protected int locateProxy(URL u)
	throws IOException
    {
	String url = u.toExternalForm();
	int waitid = nextId();
	int len    = -1;
	// Encode a query message into our sendbuf:
	while ( true ) {
	    len = micprw.encode(MICP_OP_QUERY
				, (int) (localsrc & 0xffffffff)
				, waitid
				, url
				, sendbuf);
	    if ( len < 0 )
		sendbuf = new byte[-len+1];
	    else
		break;
	}
	// Emit a debug trace if needed:
	if ( debug )
	    System.out.println("mICP: query for "+url);
	// Send that query to the net, and lock calling thread on reply:
	DatagramPacket p = new DatagramPacket(sendbuf, len, addr, port);
	socket.send(p);
	return waitid;
    }

    public boolean propertyChanged(String name) {
	if ( debug )
	    System.out.println("mICPFilter:"+name+": property changed.");
	return true;
    }

    /**
     * This filter doesn't handle exceptions.
     * @param request The request that triggered the exception.
     * @param ex The triggered exception.
     * @return Always <strong>false</strong>.
     */

    public boolean exceptionFilter(Request request, HttpException ex) {
	return false;
    }

    /**
     * Our ingoingFilter method.
     * This method emits (only for GET requestst currently) an ICP query
     * to all our neighbors, and wait for either one of them to
     * reply with a hit, or, our timeout value to expire.
     * <p>If a hit reply is received, we then use the corresponding proxy
     * to fullfill the request.
     * @param request The request that is about to be emitted.
     * @return Always <strong>null</strong>.
     */

    public Reply ingoingFilter(Request request) {
	if ( request.getMethod().equals("GET") 
	     && ( ! request.hasState(CacheFilter.STATE_NOCACHE) )
	     && ( ! request.hasState(CacheFilter.STATE_REVALIDATION)) ) {
	    URL proxy = null;
	    // Try "reolving" the URL:
	    try {
		int waitid = locateProxy(request.getURL());
		proxy      = waitOn(waitid);
	    } catch (Exception ex) {
		return null;
	    }
	    // How did it go ?
	    if ( proxy != null ) {
		if ( debug )
		    System.out.println("*** routing "+request.getURL()
				       + " to "+proxy);
		// Disable caching and set proxy:
		if ( disableCache ) {
		    request.setState(CacheFilter.STATE_NOCACHE, Boolean.TRUE);
		}
		request.setState(STATE_HOW, proxy);
		request.setProxy(proxy);
	    }
	}
	return null;
    }

    /**
     * Our outgoingFilter does nothing (at all).
     * @param request The request that has been processed.
     * @param reply The original reply (from origin server)
     * @return Always <strong>null</strong>.
     */

    public Reply outgoingFilter(Request request, Reply reply) {
	return null;
    }

    /**
     * This filter doesn't maintain dynamic state. 
     */

    public void sync() {
	return ;
    }

    public void run() {
	byte buffer[] = new byte[4096];
	while ( true ) {
	    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	    try {
		socket.receive(packet);
		handle(packet.getData(), packet.getLength());
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }

    /**
     * Initialize the ICP filter.
     * This is where we parse the configuration file in order to know
     * about our neighbors. We then register ourself to the HTTP manager.
     * @param manager The HTTP manager.
     * @exception PropRequestFilterException If the filter cannot launch
     * its server part (listening for incomming ICP requests)
     */

    public void initialize(HttpManager manager) 
	throws PropRequestFilterException
    {
	micprw  = new MICPReadWrite();
	queue   = new ReplyWaiter[16];
	sendbuf = new byte[512];
	// Setup our properties:
	props = manager.getProperties();
	props.registerObserver(this);
	// Get property values:
	if ( debug = props.getBoolean(DEBUG_P, false) )
	    System.out.println("["+getClass().getName()+"]: debugging on");
	port         = props.getInteger(PORT_P, 2005);
	timeoutValue = props.getInteger(TIMEOUT_P, (int) timeoutValue);
	disableCache = props.getBoolean(DISABLE_CACHE_P, disableCache);
	String saddr = props.getString(ADDRESS_P, null);
	// Get our local proxy address:
	String proxy = props.getString(PROXY_P, null);
	if ( proxy == null ) {
	    // A present to Jigsaw server, in fact:
	    String host = props.getString("org.w3c.jigsaw.host", null);
	    int    port = props.getInteger("org.w3c.jigsaw.port", -1);
	    if ((host != null) && (port != -1))
		proxy = "http://"+host+":"+port;
	}
	if ( proxy != null )
	    localproxy = proxy.getBytes();
	// Gain access to the cache filter:
	try {
	    Class c = Class.forName(
				"org.w3c.www.protocol.http.cache.CacheFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    cache   = (CacheFilter) manager.getGlobalFilter(c);
	} catch (Exception ex) {
	}
	if ( cache == null ) 
	    throw new PropRequestFilterException("no cache filter.");
	// Initialize our ICPReceiver:
	try {
	    // Various formats for local address:
	    this.localaddr = InetAddress.getLocalHost().getAddress();
	    this.localsrc  = ((  (localaddr[0] & 0xff) << 24)
			      + ((localaddr[1] & 0xff) << 16)
			      + ((localaddr[2] & 0xff) << 8)
			      +  (localaddr[3] & 0xff));
	    this.addr   = InetAddress.getByName(saddr);
	    this.socket = new MulticastSocket(port);
	    this.socket.setTimeToLive(ttl);
	    this.socket.joinGroup(addr);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new PropRequestFilterException(ex.getMessage());
	}
	setName("mICP");
	setDaemon(true);
	start();
	manager.setFilter(this);
    }

}

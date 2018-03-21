// ICPFilter.java
// $Id: ICPFilter.java,v 1.1 2010/06/15 12:27:36 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// please first read the full copyright statement in file COPYRIGHT.HTML

package org.w3c.www.protocol.http.icp;

import java.util.Hashtable;
import java.util.StringTokenizer;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.w3c.util.ObservableProperties;
import org.w3c.util.PropertyMonitoring;

import org.w3c.www.protocol.http.HttpException;
import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.PropRequestFilter;
import org.w3c.www.protocol.http.PropRequestFilterException;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;

import org.w3c.www.protocol.http.cache.CacheFilter;

public class ICPFilter implements PropRequestFilter, PropertyMonitoring {
    /**
     * Properties - Our debug flag.
     */
    public static final String
    DEBUG_P = "org.w3c.www.protocol.http.icp.debug";
    /**
     * Properties - Our configuration file.
     */
    public static final String
    CONFIG_P = "org.w3c.www.protocol.http.icp.config";
    /**
     * Properties - Our own UDP port number.
     */
    public static final String
    PORT_P = "org.w3c.www.protocol.http.icp.port";
    /**
     * Properties - Our default timeout value.
     */
    public static final String
    TIMEOUT_P = "org.w3c.www.protocol.http.icp.timeout";
    /**
     * Properties - disable caching when fetching from a neighbour proxy.
     */
    public static final String
    DISABLE_CACHE_P = "org.w3c.www.protocol.http.icp.disable-cache";
    /**
     * The properties we are initialized from.
     */
    protected ObservableProperties props = null;
    /**
     * Our ICP engine.
     */
    protected ICPReceiver icp = null;
    /**
     * Our ICP neighbors.
     */
    ICPSender senders[] = null;
    /**
     * Our senders, indexed by InetAddress.
     */
    protected Hashtable friends = null;
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

    public boolean propertyChanged(String name) {
	System.out.println("ICPFilter:"+name+": property changed.");
	return true;
    }

    protected DatagramSocket getSocket() {
	return icp.getSocket();
    }

    protected void createICPSender(String host, int dstport, String http) 
	throws UnknownHostException, MalformedURLException, SocketException
    {
	InetAddress addr   = InetAddress.getByName(host);
	URL         url    = new URL(http);
	ICPSender   sender = new ICPSender(this, port, addr, dstport, url);
	// Add it to the array of senders:
	if ( senders == null ) {
	    senders    = new ICPSender[1];
	    senders[0] = sender;
	} else {
	    ICPSender ns[]     = new ICPSender[senders.length+1];
	    System.arraycopy(senders, 0, ns, 0, senders.length);
	    ns[senders.length] = sender;
	    senders            = ns;
	}
	// Add it to our hashtable of hosts:
	byte    baddr[] = addr.getAddress();
	Long    key     = new Long((((long) dstport) << 32)
				    + ((baddr[0] & 0xff) << 24)
				    + ((baddr[1] & 0xff) << 16)
				    + ((baddr[2] & 0xff) << 8)
				    + (baddr[3] & 0xff));
	friends.put(key, sender);
	if ( debug )
	    System.out.println("icp: friend "+key+" http="+http);
    }

    /**
     * Parse the configuration file.
     */

    protected void parseConfiguration() {
	DataInputStream in = null;
	String          host = null;
	int             port = -1;
	String          http = null;
	File            file = props.getFile(CONFIG_P, new File("icp.conf"));
	try {
	    in = (new DataInputStream
		  (new BufferedInputStream
		   (new FileInputStream(file))));
	} catch (IOException ex) {
	    System.out.println("*** ICP, unable to read config file "
			       + file.getAbsolutePath());
	    return;
	}
	// Parse the file in:
	try {
	    for (String line = null; (line = in.readLine()) != null; ) {
		// Syntax (FIXME) 
		//   host udp-port http-location
		// | '#' comments
		if ( line.startsWith("#") || line.length() == 0 )
		    continue;
		StringTokenizer st = new StringTokenizer(line, " \t");
		host = st.nextToken();
		port = Integer.parseInt(st.nextToken());
		http = st.nextToken();
		createICPSender(host, port, http);
	    }
	} catch (Exception ex) {
	    System.out.println("*** ICP, unable to create "
			       + host + "@" + port + "[" + http + "]: "
			       + ex.getMessage());
	} finally {
	    try { if ( in != null) in.close(); } catch (IOException ex) {}
	}
    }

    /**
     * Get the sender object for the given InetAddress instance.
     * @param addr The InetAddress of the sender.
     * @return An ICPSender instance, if available, <strong>null</strong>
     * otherwise.
     */

    public ICPSender getSender(InetAddress addr, int port) {
	byte    baddr[] = addr.getAddress();
	Long    key     = new Long((((long) port) << 32)
				   + ((baddr[0] & 0xff) << 24)
				   + ((baddr[1] & 0xff) << 16)
				   + ((baddr[2] & 0xff) << 8)
				   + (baddr[3] & 0xff));
	return (ICPSender) friends.get(key);
    }

    /**
     * Locate the HTTP service of the proxy that has emitted that reply.
     * @param reply The reply emitted by the host that alos host the HTTP
     * service we are looking for.
     * @return The URL of the proxy, or <strong>null</strong> if no matching
     * proxy was found.
     */

    protected URL locateProxy(ICPReply reply) {
	ICPSender sender = getSender(reply.getSenderAddress()
				     , reply.getSenderPort());
	return (sender != null) ? sender.getProxyLocation() : null;
    }

    /**
     * Send the given query to all our neighbors.
     * @return The number of times we emitted the query.
     */

    protected int sendQuery(ICPQuery query) {
	int count = 0;
	if ( senders != null ) {
	    for (int i = 0 ; i < senders.length ; i++) {
		if ( debug )
		    System.out.println("icp: query@"
				       + senders[i].getAddress()
				       + "/" + senders[i].getPort()
				       + " for " + query.getURL());
		if ( senders[i].send(query) )
		    count++;
	    }
	}
	return count;
    }
	
    /**
     * Run the ICP query, and return the proxy we should go to.
     * @param url The URL we are looking for.
     * @return The URL of the proxy we should go to for that URL, or <strong>
     * null</strong> if none was found.
     */

    protected URL runQuery(ICPQuery query) {
	// Create a new waiter block for this query, and register it:
	ICPWaiter waiter = new ICPWaiter(query.getIdentifier());
	icp.addReplyWaiter(waiter);
	// Emit the query, and wait for a suitable reply:
	try {
	    long curtime = -1;
	    long nxttime = -1;
	    long timeout = timeoutValue;
	    int  sent    = sendQuery(query);
	    while ((sent > 0) && (timeout > 0)) {
		ICPReply reply = waiter.getNextReply(timeout);
		if (reply != null) {
		    sent--;
		    if ( reply.isHit() ) {
			return locateProxy(reply);
		    }
		} else {
		    // Our timeout has expired, notify failure
		    return null;
		}
		nxttime  = System.currentTimeMillis();
		timeout -= (nxttime -curtime);
		curtime  = nxttime;
	    }
	} finally {
	    icp.removeReplyWaiter(waiter);
	}
	return null;
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
	    ICPQuery query = icp.createQuery(request.getURL());
	    URL      proxy = runQuery(query);
	    if ( proxy != null ) {
		if ( debug )
		    System.out.println("*** routing "+request.getURL()
				       + " to "+proxy);
		// Disable caching and set proxy:
		if ( disableCache ) {
		    request.setState(CacheFilter.STATE_NOCACHE, Boolean.TRUE);
		}
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

    /**
     * Initialize the ICP filter.
     * This is where we parse the configuration file in order to know
     * about our neighbors. We then register ourself to the HTTP manager.
     * @param manager The HTTP manager.
     * @exception PropRequestFilterException If the filter cannot 
     * launch its server part (listening for incomming ICP requests)
     */

    public void initialize(HttpManager manager) 
	throws PropRequestFilterException
    {
	// Setup our properties:
	props = manager.getProperties();
	props.registerObserver(this);
	port = props.getInteger(PORT_P, 2005);
	// Get property values:
	this.friends = new Hashtable(10);
	if ( debug = props.getBoolean(DEBUG_P, false) )
	    System.out.println("["+getClass().getName()+"]: debugging on");
	parseConfiguration();
	timeoutValue = props.getInteger(TIMEOUT_P, (int) timeoutValue);
	disableCache = props.getBoolean(DISABLE_CACHE_P, disableCache);
	// Initialize our ICPReceiver:
	try {
	    icp = new ICPReceiver(manager, this, port);
	} catch (SocketException ex) {
	    ex.printStackTrace();
	    throw new PropRequestFilterException(ex.getMessage());
	}
	if ( debug )
	    System.out.println("icp: listening on port "+port);
	manager.setFilter(this);
    }

}

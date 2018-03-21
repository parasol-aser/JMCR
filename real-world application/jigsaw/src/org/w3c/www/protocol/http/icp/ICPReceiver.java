// ICPReceiver.java
// $Id: ICPReceiver.java,v 1.2 2010/06/15 17:53:08 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// please first read the full copyright statement in file COPYRIGHT.HTML

package org.w3c.www.protocol.http.icp;

import java.io.IOException;
import java.io.PrintStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.URL;

import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.PropRequestFilterException;

import org.w3c.www.protocol.http.cache.CacheFilter;

class ICPReceiver extends Thread implements ICP {
    private final static boolean debug = false;
    /**
     * The default waiter queue size.
     */
    public static final int DEFAULT_QUEUE_SIZE = 4;
    /**
     * The default received datagram packet size.
     */
    public static final int DEFAULT_PACKET_SIZE = 512;
    /**
     * The port number this receiver listens on.
     */
    protected int port = -1;
    /**
     * Our socket to receive packets.
     */
    DatagramSocket socket = null;
    /**
     * Our current request identifier.
     */
    protected int nextid = 1;
    /**
     * The CacheFilter we use for answering queries.
     */
    CacheFilter cache = null;
    /**
     * The queue of objects waiting for an ICP reply.
     */
    ICPWaiter queue[] = null;
    /**
     * The ICP filter we are working with.
     */
    protected ICPFilter filter = null;

    protected DatagramSocket getSocket() {
	return socket;
    }

    /**
     * Create a new ICP query instance.
     * @param url The URL to be queried.
     */

    protected ICPQuery createQuery(URL url) {
	int rid = -1;
	synchronized(this) {
	    rid = nextid++;
	}
	return new ICPQuery(rid, url);
    }

    /**
     * Add a waiter for on the given request identifier.
     * @param waiter The ICPWaiter instance for that request.
     */

    protected synchronized void addReplyWaiter(ICPWaiter waiter) {
	if ( queue == null ) {
	    queue    = new ICPWaiter[DEFAULT_QUEUE_SIZE];
	    queue[0] = waiter;
	} else {
	    // Look for a free slot:
	    for (int i = 0 ; i < queue.length ; i++) {
		if ( queue[i] == null ) {
		    queue[i] = waiter;
		    return;
		}
	    }
	    // Resize queue:
	    ICPWaiter nqueue[] = new ICPWaiter[queue.length << 1];
	    System.arraycopy(queue, 0, nqueue, 0, queue.length);
	    nqueue[queue.length] = waiter;
	    queue = nqueue;
	    return;
	}
    }

    /**
     * Remove the given waiter from the waiters queue.
     * This waiter has completed his job, he doesn't care about what happens
     * next at the ICP level.
     * @param waiter The wauter to remove from our queue.
     */

    protected synchronized void removeReplyWaiter(ICPWaiter waiter) {
	if ( queue != null ) {
	    for (int i = 0 ; i < queue.length ; i++) {
		if ( queue[i] == waiter ) {
		    queue[i] = null;
		    return;
		}
	    }
	}
    }

    /**
     * Handle the given ICP reply.
     */

    protected synchronized void handleReply(ICPReply reply) 
	throws ICPProtocolException
    {
	int id = reply.getIdentifier();
	// Do we have someone waiting for this reply ?
	for (int i = 0 ; i < queue.length ; i++) {
	    if ( queue[i] == null )
		continue;
	    if ( queue[i].getIdentifier() == id ) {
		queue[i].notifyReply(reply);
		return;
	    }
	}
	// No one was waiting for this packet throw it away:
	if ( debug )
	    System.out.println("icp: discarding reply "+id);
	return;
    }

    /**
     * Handle the given ICP query.
     * @param p The DatagramPacket that wraps up the query.
     */

    protected synchronized void handleQuery(ICPQuery query) 
	throws ICPProtocolException
    {
	// Process the query:
	if ( debug )
	    System.out.println("icp["+port+"]: query for "
                               + query.getURL()
			       + " from "
			       + query.getSenderAddress() 
			       + "/" + query.getSenderPort());
// FIXME  boolean   hit   = cache.hasResource(query.getURL().toExternalForm());
	boolean   hit   = false;
	ICPReply  reply = new ICPReply(query.getIdentifier()
				       , hit ? ICP_OP_HIT : ICP_OP_MISS);
	// Emit the reply:
	ICPSender sender = filter.getSender(query.getSenderAddress()
					    , query.getSenderPort());
	if ( sender != null ) {
	    if ( debug )
		System.out.println("icp["+port+"]: reports "
				   + reply.getOpcode()
				   + " for "+reply.getIdentifier()
				   + " to "+sender);
	    sender.send(reply);
	} else {
	    if ( debug )
		System.out.println("icp["+port+"]: couldn't locate peer at "
				   + query.getSenderAddress()
				   + "/" + query.getSenderPort());
	}
    }

    /**
     * Run the ICP manager.
     */

    public void run() {
	byte           pbuf[] = new byte[DEFAULT_PACKET_SIZE];
    readloop:
	while (true) {
	    // Receive next ICP packet:
	    DatagramPacket p = new DatagramPacket(pbuf, pbuf.length);
	    try {
		socket.receive(p);
	    } catch (IOException ex) {
		ex.printStackTrace();
		continue readloop;
	    }
	    // Is this a query or a reply ?
	    try {
		ICPMessage m = ICPMessage.parse(p);
		if (m instanceof ICPQuery )
		    handleQuery((ICPQuery) m);
		else
		    handleReply((ICPReply) m);
	    } catch (ICPProtocolException ex) {
		ex.printStackTrace();
	    }
	}
    }

    ICPReceiver(HttpManager manager, ICPFilter filter, int port) 
	throws SocketException, PropRequestFilterException
    {
	// Thread setting:
	setName("ICP-Receiver");
	setDaemon(true);
	// Initialize instance variables:
	this.port   = port;
	this.filter = filter;
	this.socket = new DatagramSocket(port);
	// Get a pointer to the cache filter os that environment:
	try {
	    Class c = Class.forName("org.w3c.www.protocol.http.cache.CacheFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    cache   = (CacheFilter) manager.getGlobalFilter(c);
	} catch (Exception ex) {
	}
	if ( cache == null ) 
	    throw new PropRequestFilterException("no cache filter.");
	start();
    }
}

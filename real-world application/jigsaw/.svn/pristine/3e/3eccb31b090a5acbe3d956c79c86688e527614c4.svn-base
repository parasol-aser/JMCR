// ICPSender.java
// /$Id: ICPSender.java,v 1.1 2010/06/15 12:27:36 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// please first read the full copyright statement in file COPYRIGHT.HTML

package org.w3c.www.protocol.http.icp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;

import java.io.IOException;

class ICPSender {
    /**
     * The filter we are attached to.
     */
    protected ICPFilter filter = null;
    /**
     * Our target host address.
     */
    protected InetAddress addr = null;
    /**
     * Our target port number.
     */
    protected int port = -1;
    /**
     * The HTTP service provided by the target ICP host.
     */
    protected URL proxy = null;
    /**
     * The buffer to emit messages.
     */
    protected byte buffer[] = null;

    /**
     * Return a String representation of this sender.
     * @return A String.
     */

    public String toString() {
	return getAddress() + "/" + getPort();
    }

    /**
     * Get the HTTP service location of that sender.
     * @return The HTTP service location, as a URL.
     */

    public final URL getProxyLocation() {
	return proxy;
    }

    /**
     * Get the ICP address for this neighbor.
     * @return An InetAddress instance.
     */

    public final InetAddress getAddress() {
	return addr;
    }

    /**
     * Get the port number for that sender target.
     * @return An integer port number.
     */

    public final int getPort() {
	return port;
    }

    /**
     * Send the given ICP message to our target host.
     * @param msg The ICP message to send.
     * @return A boolean, <strong>true</strong> if message was emitted.
     */

    public boolean send(ICPMessage msg) {
	// Encode the message in a byte array:
	int len = msg.toByteArray(buffer);
	if ( len < 0 ) {
	    buffer = new byte[-len+1];
	    len = msg.toByteArray(buffer);
	}
	// Wrap it up in a datagram:
	DatagramPacket p = new DatagramPacket(buffer, len, addr, port);
	try {
	    filter.getSocket().send(p);
	    return true;
	} catch (IOException ex) {
	}
	// FIXME Should mark that host down
	return false;
    }

    /**
     * Create a ICPSender to query the given host.
     * @param addr The InetAddress of the ICP host.
     * @param port The port number it is listening on.
     * @exception SocketException If we couldn't create the datagram socket.
     */

    ICPSender(ICPFilter filter
	      , int srcport
	      , InetAddress addr, int dstport
	      , URL proxy)
	throws SocketException
    {
	this.filter = filter;
	this.addr   = addr;
	this.port   = dstport;
	this.proxy  = proxy;
	this.buffer = new byte[512];
    }

   
}

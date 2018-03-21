// ICPMessage.java
// $Id: ICPMessage.java,v 1.1 2010/06/15 12:27:36 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// please first read the full copyright statement in file COPYRIGHT.HTML

package org.w3c.www.protocol.http.icp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.URL;

class ICPMessage implements ICP {
    /**
     * The ICP opcde for that message.
     */
    protected int opcode = -1;
    /**
     * The ICP major version of the message.
     */
    protected int version = -1;
    /**
     * The identifier of the message.
     */
    protected int id = -1;
    /**
     * The options for this message.
     */
    protected int options = 0;
    /**
     * The InetAddress of the sender or <strong>null</strong> otherwise.
     */
    protected InetAddress addr = null;
    /**
     * The port number of the sender of that message, or <strong>-1</strong>
     * if that message was created locally.
     */
    protected int port = -1;
    /**
     * The URL of the message.
     */
    URL url = null;

    /**
     * Parse the given datagram into an ICP message.
     * @exception ICPProtocolException If that reply doesn't conform to ICP 
     * protocol.
     */

    protected static ICPMessage parse(DatagramPacket p)
	throws ICPProtocolException
    {
	byte buf[] = p.getData();
	int  len   = p.getLength();
	if ( len < 4 ) 
	    throw new ICPProtocolException("Invalid ICP datagram length");
	InetAddress addr    = p.getAddress();
	int         port    = p.getPort();
	// Decode the first word:
	int         opcode  = buf[0];
	int         version = buf[1];
	int plen = (((buf[2] & 0xff) << 8)+(buf[3] & 0xff));
	if ( plen != len )
	    throw new ICPProtocolException("Didn't got full message: "
					   + len + "/" + plen);
	// Construct an appropriate message:
	ICPMessage msg = null;
	switch(opcode) {
	  case ICP_OP_QUERY:
	      msg = new ICPQuery(addr, port, opcode, version, buf, 4, len);
	      break;
	  case ICP_OP_HIT:
	  case ICP_OP_MISS:
	      msg = new ICPReply(addr, port, opcode, version, buf, 4, len);
	      break;
	  default:
	      throw new ICPProtocolException("Unsupported opcode: "+opcode);
	}
	return msg;
    }

    protected int parse(byte buf[], int off, int len) 
	throws ICPProtocolException
    {
	// Decode the request identifier:
	this.id      = (  ((buf[off]   & 0xff) << 24)
			+ ((buf[off+1] & 0xff) << 16)
			+ ((buf[off+2] & 0xff) << 8)
			+ ((buf[off+3] & 0xff) << 0));
	off += 4;
	// Decode the options:
	this.options = (  ((buf[off]   & 0xff) << 24)
			+ ((buf[off+1] & 0xff) << 16)
			+ ((buf[off+2] & 0xff) << 8)
			+ ((buf[off+3] & 0xff) << 0));
	off += 4;
	// Skip padding (four bytes):
	off += 4;
	return off;
    }

    /**
     * Get the length of that ICP message, if it were to be emitted.
     * @return The length of the packet that would be emitted.
     */

    protected int getByteArrayLength() {
	return 16;
    }

    /**
     * Pack that message into a byte array suitable for emitting.
     * @param buf The array to pack the request in.
     * @return The length of the query, in bytes, or a negative number
     * representing the size of the query.
     */

    protected int toByteArray(byte buf[]) {
	// Check that we have enough room:
	int length = getByteArrayLength();
	if ( length > buf.length )
	    return -length;
	// Setup the ICP header:
	buf[0] = (byte) opcode;
	buf[1] = (byte) ICP.ICP_VERSION;
	// Encode the length:
	buf[2] = (byte) ((length >>> 8) & 0xff);
	buf[3] = (byte) ((length >>> 0) & 0xff);
	// Encode the identifier:
	buf[4] = (byte) ((id >>> 24) & 0xff);
	buf[5] = (byte) ((id >>> 16) & 0xff);
	buf[6] = (byte) ((id >>> 8)  & 0xff);
	buf[7] = (byte) ((id >>> 0)  & 0xff);
	// We don't implement any options (four bytes):
	buf[8]  = (byte) ((options >>> 24) & 0xff);
	buf[9]  = (byte) ((options >>> 16) & 0xff);
	buf[10] = (byte) ((options >>> 8)  & 0xff);
	buf[11] = (byte) ((options >>> 0)  & 0xff);
	// Skip padding:

	return 16;
    }

    /**
     * Get the identifier of that ICP message.
     * @return An integer identifier.
     */

    protected final int getIdentifier() {
	return id;
    }

    /**
     * Get the opcode for this message.
     * @return An integer opcode (as defined by the ICP protocol).
     * @see ICP
     */

    protected final int getOpcode() {
	return opcode;
    }

    /**
     * Get the URL contained in that message.
     * @return An URL instance.
     */

    protected final URL getURL() {
	return url;
    }

    /**
     * Get the address of the host that sent this packet.
     * @return An InetAddress instance, or <strong>null</strong> if the message
     * was created locally.
     */

    public InetAddress getSenderAddress() {
	return addr;
    }

    /**
     * Get the port number from which this message was emitted.
     * @return A integer port number.
     */

    public int getSenderPort() {
	return port;
    }

}

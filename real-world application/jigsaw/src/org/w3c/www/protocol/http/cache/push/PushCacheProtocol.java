// PushCacheProtocol.java
// $Id: PushCacheProtocol.java,v 1.1 2010/06/15 12:25:43 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2001.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache.push;

import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;

import java.util.TreeMap;

/**
 * PushCacheProtocol
 * Characteristics of the protocol used to control the push cache, and 
 * methods for common operations
 * <p>
 * <b>Protocol Description</b>
 * <p>
 * To request that "/home/abc/page.html" is inserted in cache as 
 * "http://www.abc.com/page.html" the client sends a packet with
 * command="ADD", and remain_len set to sizeof(add_packet_t) plus
 * the sum of the lengths of the path and the urls including their
 * null terminators.  The client then sends an add_packet describing
 * the lengths of the two strings followed by the path and then the
 * url.
 * <p>
 * The server replies with either command="OK" and remain_len=0 or
 * command="ERR" and remain_len set the the length of the error
 * string that follows immediately.  In the event of an "ERR" message
 * the connection is closed by the server.
 * <p>
 * To request that the page associated with "http://www.abc.com/page.html"
 * be removed from the cache the client sends a packet with command="DEL",
 * and remain_len set to sizeof(int) plus the length of the url string
 * including the trailing null character.  The server replies as with
 * ADD above.  Attempting to remove a url that is not present in the cache
 * results in an "OK" packet being returned, the cache is unchanged.
 * <p>
 * The client can ask if a url is present in the cache by sending a packet
 * with command="PRS", and url information as with the DEL command.  The
 * server will reply with "OK" if the url is present, "NO" if the url is
 * not present and "ERR" if an error was encountered.
 * <p>
 * The client can request that the cache be emptied of all urls by sending
 * a packet with command="CLN" (clean).  The remain_len field is set to zero.
 * The server will reply with either OK or ERR.
 * 
 * <p>
 * The client can terminate the dialogue by sending a command="BYE" 
 * packet and then closing the connection.
 * 
 * <p>
 * 'C' code describing the packet structures are shown below
 *
 *
 *<pre>
 * typedef struct {
 *                             // Bytes  Notes
 *                             // -----  -----
 *       char  tag[4];         // 0-3    = {'P','C','P','P'}
 *       short major_version;  // 4-5    = 1
 *       short minor_version;  // 6-7    = 1
 *       char  command[4];     // 8-11   Null terminated command string 
 *       int   remain_len;     // 12-15  number of remaining bytes to read
 * } packet_t;
 *
 * typedef struct {
 *       int   path_len;       // 4      Length of pathname (including null)
 *       int   url_len;        // 8      Length of URL (including null)
 * } add_packet_t;
 * 
 * Note that the command is always 4 characters in length and that the
 * null characters are considered part of the command, so in Java (but
 * not C) we must include the \0 when comparing strings:
 *   "ADD\0", "BYE\0", "OK\0\0", "ERR\0", "CLN\0", "PRS\0", "DEL\0"
 *
 * </pre>
 *
 * @author Paul Henshaw, The Fantastic Corporation, Paul.Henshaw@fantastic.com
 * @version $Revision: 1.1 $
 * $Id: PushCacheProtocol.java,v 1.1 2010/06/15 12:25:43 smhuang Exp $
 */
public class PushCacheProtocol {
    /*
     * Protocol characteristics
     */

    /**
     * Size of basic packet in bytes
     */
    public final static int   PACKET_LEN=16;

    /**
     * Size of command string in bytes (including null terminator)
     */
    public final static int   COMMAND_LEN=4;

    /**
     * Combined size of tag and version information
     */
    public final static int   HEADER_LEN=8;

    /**
     * Size of packet tag
     */
    public final static int   TAG_LEN=4;
    
    /**
     * Maximum size of strings (urls, paths, error messages)
     */
    public final static int   MAX_STRING_LEN=1024;

    /**
     * Maximum size of payload (follows basic packet)
     */
    public final static int   MAX_PAYLOAD_LEN=8192;

    /**
     * Protocol Major version
     */
    public final static short MAJ_PROTO_VERSION=1;

    /**
     * Protocol minor version
     */
    public final static short MIN_PROTO_VERSION=2;

    /**
     * Numeric codes for commands, 
     */
    public static final int NO_SUCH_COMMAND=-1, 
	ERR=0, ADD=1, DEL=2, CLN=3, PRS=4, BYE=5, OK=6, NO=7, NOP=8;

    private static PushCacheProtocol _instance;
    private TreeMap _map;
    private byte[] _ok_packet_bytes=null;
    private byte[] _no_packet_bytes=null;
    private byte[] _err_packet_bytes=null;
    private byte[] _header=null;

    /**
     * Access to single instance of this class
     */
    public static PushCacheProtocol instance() {
	if(_instance==null) {
	    _instance=new PushCacheProtocol();
	}
	return _instance;
    }
    
    /**
     * Utility function for command string parsing
     */
    public int parseCommand(String command) {
	Integer in=(Integer)_map.get(command);
	if(in==null) {
	    return NO_SUCH_COMMAND;
	}
	return(in.intValue());
    }

    /**
     * Byte array for OK packet
     */
    public byte[] okPacket() {
	return(_ok_packet_bytes);
    }

    /**
     * Byte array for NO packet
     */
    public byte[] noPacket() {
	return(_no_packet_bytes);
    }

    public byte[] header() {
	return(_header);
    }

    /**
     * Create error packet for specified error message
     */
    public byte[] errorPacket(String message) {
	try {
	    java.io.ByteArrayOutputStream baos=
		new java.io.ByteArrayOutputStream(16);
	    DataOutputStream dos=new 
		DataOutputStream(baos);
		    
	    dos.write(_header,0,_header.length);
	    dos.writeBytes("ERR\0");
	    dos.writeInt(message.length());
	    dos.writeBytes(message);
	    return baos.toByteArray();
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
	return(null);
    }

    /**
     * True iff first four bytes of packet are identical to the protocol tag
     */
    public boolean isValidProtocolTag(byte[] packet) {
	return(packet[0]==(byte)'P' && packet[1]==(byte)'C' || 
	       packet[2]==(byte)'P' && packet[3]==(byte)'P');
    }

    /**
     * Singleton, no public constructor, use {@link #instance}
     * @see #instance
     */ 
    protected PushCacheProtocol() {
	try {
	    _map=new TreeMap();
	    _map.put("ERR\0",new Integer(ERR));
	    _map.put("ADD\0",new Integer(ADD));
	    _map.put("DEL\0",new Integer(DEL));
	    _map.put("CLN\0",new Integer(CLN));
	    _map.put("PRS\0",new Integer(PRS));
	    _map.put("BYE\0",new Integer(BYE));
	    _map.put("OK\0\0",new Integer(OK));
	    _map.put("NO\0\0",new Integer(NO));
	    _map.put("NOP\0",new Integer(NOP));

	    ByteArrayOutputStream baos=new ByteArrayOutputStream(PACKET_LEN);
	    DataOutputStream dos=new DataOutputStream(baos);
	    dos.writeByte('P');
	    dos.writeByte('C');
	    dos.writeByte('P');
	    dos.writeByte('P');
	    dos.writeShort(MAJ_PROTO_VERSION);
	    dos.writeShort(MIN_PROTO_VERSION);
	    _header=baos.toByteArray();

	    baos=new ByteArrayOutputStream(PACKET_LEN);
	    dos=new DataOutputStream(baos);
	    
	    dos.write(_header,0,_header.length);
	    dos.writeBytes("OK\0\0");
	    dos.writeInt(0);

	    _ok_packet_bytes=baos.toByteArray();

	    baos=null;
	    dos=null;

	    baos=new ByteArrayOutputStream(PACKET_LEN);
	    dos=new DataOutputStream(baos);
	    dos.write(_header,0,_header.length);
	    dos.writeBytes("NO\0\0");
	    dos.writeInt(0);

	    _no_packet_bytes=baos.toByteArray();
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }
}

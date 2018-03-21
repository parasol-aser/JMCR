// MICPReadWrite.java
// $Id: MICPReadWrite.java,v 1.1 2010/06/15 12:21:48 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.micp;

/**
 * A class to parse/emit MICP messages.
 */

public class MICPReadWrite implements MICP {

    private final void encodeShort(byte buf[], int off, int s) {
	buf[off  ] = (byte) ((s & 0xff00) >>> 8);
	buf[off+1] = (byte) (s & 0xff);
    }

    private final int decodeShort(byte buf[], int off) {
	return ((buf[off] & 0xff) << 8) + (buf[off+1] & 0xff);
    }

    private final void encodeInt(byte buf[], int off, int i) {
	buf[off  ] = (byte) ((i & 0xff000000) >>> 24);
	buf[off+1] = (byte) ((i & 0x00ff0000) >>> 16);
	buf[off+2] = (byte) ((i & 0x0000ff00) >>> 8);
	buf[off+3] = (byte) ((i & 0xff0000ff) >>> 0);
    }

    private final int decodeInt(byte buf[], int off) {
	return ((  (buf[off]   & 0xff) << 24)
		+ ((buf[off+1] & 0xff) << 16)
		+ ((buf[off+2] & 0xff) << 8)
		+  (buf[off+3] & 0xff));
    }

    private final void encodeString(byte buf[], int off, String s) {
	byte bits[] = s.getBytes();
	System.arraycopy(bits, 0, buf, off, bits.length);
    }

    /**
     * Parse the given buffer as an mICP message.
     * @param buf The wire data to parse.
     * @param len The length of above buffer.
     * @param into Message structure to fill in.
     * @return The filled in message.
     * @exception MICPProtocolException If the given buffer is not an mICP 
     * wire formatted message.
     */

    public MICPMessage decode(byte buf[], int len, MICPMessage into)
	throws MICPProtocolException
    {
	if ( len < 12 )
	    throw new MICPProtocolException("invalid buffer length "+len);
	int  packlen = decodeShort(buf, 2);
	if ( len < packlen )
	    throw new MICPProtocolException("invalid length "+packlen+"/"+len);
	into.version = (buf[0] & 0xff);
	into.op      = (buf[1] & 0xff);
	into.src     = decodeInt(buf, 4);
	into.id      = decodeInt(buf, 8);
	into.url     = new String(buf, 12, len-12);
	return into;
    }

    /**
     * Emit an MICP message into provided buffer.
     * If the buffer is too small, a new buffer is allocated in place of 
     * the provided one (and returned).
     * @param op The opcode for the message.
     * @param src The source field for the message.
     * @param id The identifier of that message.
     * @param url The URL for that message.
     * @param buf The buffer to encode the message to.
     * @return A positive integer, giving the message length if buffer was big
     * enough to hold the packet, a negative integer, giving required buffer
     * size otherwise.
     */

    public int encode(int op, int src, int id, String url, byte buf[]) {
	int len = 12 + url.length();
	if ( len >= buf.length )
	    return -len;
	buf[0] = (byte) (MICP_VERSION & 0xff);		// mICP version
	buf[1] = (byte) (op & 0xff);			// mICP opcode
	encodeShort(buf, 2, len);			// mICP length
	encodeInt(buf, 4, src);				// mICP source
	encodeInt(buf, 8, id);				// mICP id
	encodeString(buf, 12, url);			// mICP url
	return len;
    }

}

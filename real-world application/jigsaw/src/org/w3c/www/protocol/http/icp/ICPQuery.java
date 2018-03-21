// ICPQuery.java
// $Id: ICPQuery.java,v 1.1 2010/06/15 12:27:36 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// please first read the full copyright statement in file COPYRIGHT.HTML

package org.w3c.www.protocol.http.icp;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

class ICPQuery extends ICPMessage {

    protected int getByteArrayLength() {
	return (super.getByteArrayLength()
                + 4 // sender's address
                + 4 // requestor's adress
		+ ((url == null) ? 0 : url.toExternalForm().length()) + 1);
    }

    protected int toByteArray(byte buf[]) {
	int    off    = super.getByteArrayLength();
	String strurl = (url == null) ? null : url.toExternalForm();
	int    urllen = ((strurl == null) ? 0 : strurl.length());
	// Encode generic payload infos:
	super.toByteArray(buf);
	// Encode query specific fields:
	// Skip the sender's address:
	off += 4;
	// Skip the requestor's address
	off += 4;
	// Encode the URL (null terminated string):
	if ( urllen > 0 )
	    strurl.getBytes(0, urllen, buf, off);
	buf[off+urllen] = 0;
	return off+urllen+1;
    }
	
    protected int parse(byte buf[], int off, int len)
	throws ICPProtocolException
    {
	off = super.parse(buf, off, len);
	// Skip sender's address:
	off += 4;
	// Skip the requestor's host address:
	off += 4;
	// Read in the URL:
	for (int i = off ; i < len ; i++) {
	    if (buf[i] == 0) {
		// Is there a real URL ?
		if ( i - off < 1 )
		    return i;
		// Yep, parse it:
		String strurl = new String(buf, 0, off, i-off);
		try {
		    this.url = new URL(strurl);
		} catch (MalformedURLException ex) {
		    throw new ICPProtocolException("Invalid URL:"+strurl);
		}
		return i;
	    }
	}
	throw new ICPProtocolException("Invalid URL encoding");
    }

    /**
     * Create an ICP query.
     * @param id The identifier of the message.
     * @param url The queried URL.
     */

    ICPQuery(int id, URL url) {
	this.opcode = ICP_OP_QUERY;
	this.id     = id;
	this.url    = url;
    }

    ICPQuery(InetAddress addr, int port
	     , int opcode, int version
	     , byte buf[], int off, int len) 
	throws ICPProtocolException
    {
	this.addr    = addr;
	this.port    = port;
	this.opcode  = opcode;
	this.version = version;
	parse(buf, off, len);
    }

}

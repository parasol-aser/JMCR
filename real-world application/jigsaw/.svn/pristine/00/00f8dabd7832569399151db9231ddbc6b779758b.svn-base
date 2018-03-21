// DAVRequest.java
// $Id: DAVRequest.java,v 1.1 2010/06/15 12:29:41 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigsaw.webdav;

import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.Reply;

import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.ResourceFilter;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HeaderValue;
import org.w3c.www.mime.MimeParser;

import org.w3c.www.webdav.DAVIf;
import org.w3c.www.webdav.DAVIfList;
import org.w3c.www.webdav.WEBDAV;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVRequest extends Request implements WEBDAV {

    static {
	registerHeader(DEPTH_HEADER, 
		       "org.w3c.www.http.HttpString");
	registerHeader(DESTINATION_HEADER, 
		       "org.w3c.www.http.HttpString");
	registerHeader(IF_HEADER, 
		       "org.w3c.www.webdav.DAVIfList");
	registerHeader(LOCK_TOKEN_HEADER,
		       "org.w3c.www.http.HttpString");
	registerHeader(OVERWRITE_HEADER, 
		       "org.w3c.www.http.HttpString");
	registerHeader(TIMEOUT_HEADER, 
		       "org.w3c.www.http.HttpTokenList");
    }
    
    public final static String depthToString(int depth) {
	switch(depth) 
	    {
	    case DEPTH_0:
		return "0";
	    case DEPTH_1:
		return "1";
	    default:
		return "Infinity";
	    }
    }

    public int getDepth() {
	HeaderValue value = getHeaderValue(DEPTH_HEADER);
	if(value == null) {
	    return DEPTH_INFINITY;
	}
	String s = (String) value.getValue();
	if (s.equals("0")) {
	    return DEPTH_0;
	} else if (s.equals("1")) {
	    return DEPTH_1;
	} else { // default
	    return DEPTH_INFINITY;
	}
    }

    public String getDestination() {
	HeaderValue value = getHeaderValue(DESTINATION_HEADER);
	return (value != null) ? (String) value.getValue() : null;
    }

    public DAVIf[] getIf() {
	HeaderValue value = getHeaderValue(IF_HEADER);
	return (value != null) ? (DAVIf[]) value.getValue() : null;
    }

    public boolean isTaggedListIfHeader() {
	DAVIfList value = (DAVIfList)getHeaderValue(IF_HEADER);
	return value.isTaggedList();
    }

    public String getLockToken() {
	HeaderValue value = getHeaderValue(LOCK_TOKEN_HEADER);
	return (value != null) ? (String) value.getValue() : null;
    }

    public boolean getOverwrite() {
	HeaderValue value     = getHeaderValue(OVERWRITE_HEADER);
	String      overwrite = 
	    (value != null) ? (String) value.getValue() : null;
	return (! "F".equals(overwrite));
    }

    public String[] getTimeout() {
	HeaderValue value = getHeaderValue(TIMEOUT_HEADER);
	return (value != null) ? (String[]) value.getValue() : null;
    }

    public ReplyInterface makeBadRequestReply() {
	return makeReply(HTTP.BAD_REQUEST);
    }

    /**
     * Make an empty Reply object matching this request version.
     * @param status The status of the reply.
     */

    public Reply makeReply(int status) {
	return (Reply) makeDAVReply(status);
    }

    /**
     * Make an empty DAV Reply object matching this request version.
     * @param status The status of the reply.
     */

    public DAVReply makeDAVReply(int status) {
	DAVReply reply = 
	    new DAVReply(client, this, getMajorVersion(), getMinorVersion(),
			 status);
	if ((filters != null) && (infilters > 0))
	    reply.setFilters(filters, infilters);
	return reply;
    }

    /**
     * Constructor
     */
    public DAVRequest(Client client, MimeParser parser) {
	super (client, parser);
	this.parser = parser;
	this.client = client ;
    }

}

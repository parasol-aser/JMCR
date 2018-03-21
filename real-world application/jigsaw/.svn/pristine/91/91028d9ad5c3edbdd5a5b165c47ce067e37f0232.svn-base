// DAVReply.java
// $Id: DAVReply.java,v 1.1 2010/06/15 12:28:24 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.protocol.webdav;

import org.w3c.www.mime.MimeParser;

import org.w3c.www.protocol.http.Reply;

import org.w3c.www.http.HeaderValue;

import org.w3c.www.webdav.WEBDAV;
import org.w3c.www.webdav.DAVStatusURIList;
import org.w3c.www.webdav.DAVStatusURI;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVReply extends Reply implements WEBDAV {
    
    static {
	registerHeader(DAV_HEADER, 
		       "org.w3c.www.http.HttpString");
	registerHeader(LOCK_TOKEN_HEADER, 
		       "org.w3c.www.http.HttpString");
	registerHeader(STATUS_URI_HEADER, 
		       "org.w3c.www.webdav.DAVStatusURIList"); 
    }

    public String getLockToken() {
	HeaderValue value = getHeaderValue(LOCK_TOKEN_HEADER);
	return (value != null) ? (String) value.getValue() : null;
    }

    public DAVStatusURI[] getStatusURI() {
	HeaderValue value = getHeaderValue(STATUS_URI_HEADER);
	return (value != null) ? (DAVStatusURI[]) value.getValue() : null;
    }

    public String getDAVHeader() {
	HeaderValue value = getHeaderValue(DAV_HEADER);
	return (value != null) ? (String) value.getValue() : null;
    }
    

    protected DAVReply(short major, short minor, int status) {
	super(major, minor, status);
    }

    protected DAVReply(MimeParser parser) {
	super(parser);
    }

}

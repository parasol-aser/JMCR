// DAVReply.java
// $Id: DAVReply.java,v 1.1 2010/06/15 12:29:40 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigsaw.webdav;

import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Client;

import org.w3c.tools.resources.ResourceFilter;

import org.w3c.www.http.HttpFactory;
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

    public void setLockToken(String token) {
	setHeaderValue(LOCK_TOKEN_HEADER, HttpFactory.makeString(token));
    }

    public String getLockToken() {
	HeaderValue value = getHeaderValue(LOCK_TOKEN_HEADER);
	return (value != null) ? (String) value.getValue() : null;
    }

    public void setStatusURI(int status, String uri) {
	DAVStatusURIList list = 
	    (DAVStatusURIList)getHeaderValue(STATUS_URI_HEADER);
	DAVStatusURI dsu = new DAVStatusURI(status, uri);
	if (list == null) {
	    list = new DAVStatusURIList(dsu);
	} else {
	    list.addStatusURI(dsu);
	}
	setHeaderValue(STATUS_URI_HEADER, list);
    }

    public DAVStatusURI[] getStatusURI() {
	HeaderValue value = getHeaderValue(STATUS_URI_HEADER);
	return (value != null) ? (DAVStatusURI[]) value.getValue() : null;
    }


    public void setDAV(String dav) {
	setHeaderValue(DAV_HEADER, HttpFactory.makeString(dav));
    }

    protected void setFilters(ResourceFilter filters[], int infilters) {
	super.setFilters(filters, infilters);
    }

    /**
     * Get the standard HTTP & WEBDAV reason phrase for the given status code.
     * @param status The given status code.
     * @return A String giving the standard reason phrase, or
     * <strong>null</strong> if the status doesn't match any knowned error.
     */

    public String getStandardReason(int status) {
	return getDAVReason(status);
    }

    public static String getDAVReason(int status) {
	int category = status / 100;
	int catcode  = status % 100;
	switch(category) {
	  case 1:
	      if ((catcode >= 0) && (catcode < dav_msg_100.length))
		  return dav_msg_100[catcode];
	      break;
	  case 2:
	      if ((catcode >= 0) && (catcode < dav_msg_200.length))
		  return dav_msg_200[catcode];
	      break;
	  case 3:
	      if ((catcode >= 0) && (catcode < dav_msg_300.length))
		  return dav_msg_300[catcode];
	      break;
	  case 4:
	      if ((catcode >= 0) && (catcode < dav_msg_400.length))
		  return dav_msg_400[catcode];
	      break;
	  case 5:
	      if ((catcode >= 0) && (catcode < dav_msg_500.length))
		  return dav_msg_500[catcode];
	      break;
	}
	return null;
    }
    
    /**
     * Create a new reply for the given client.
     * @param client The client ot who the reply is directed.
     * @reply status The reply status code.
     */
    public DAVReply(Client client, 
		    Request request, 
		    short major, 
		    short minor,
		    int status) 
    {
	super(client, request, major, minor, status);
    }

}

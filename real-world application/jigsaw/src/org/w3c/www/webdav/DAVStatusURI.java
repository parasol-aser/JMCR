// DAVStatusURI.java
// $Id: DAVStatusURI.java,v 1.1 2010/06/15 12:27:42 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav;

import org.w3c.www.http.BasicValue;
import org.w3c.www.http.HttpInvalidValueException;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVStatusURI  {

    int    status = -1;
    String url    = null;

    public int getStatus() {
	return status;
    }

    public String getURI() {
	return url;
    }

    DAVStatusURI(byte raw[], int start, int end) 
	throws HttpInvalidValueException
    {
	// 404 <http://www.foo.bar/resource1>
	ParseState ps = new ParseState(start, end);
	ps.separator  = (byte) '<';
	ps.spaceIsSep = false;
	ParseState ps2 = new ParseState(0, 0);
	ps2.separator  = (byte) '>';
	ps2.spaceIsSep = false;
	
	if (DAVParser.nextItem(raw, ps) >= 0) { // status
	    this.status = Integer.parseInt(ps.toString(raw).trim());
	}
	if (DAVParser.nextItem(raw, ps) >= 0) { // coded uri
	    ps2.prepare(ps);
	    if (DAVParser.nextItem(raw, ps2) >= 0) {
		this.url = ps2.toString(raw).trim();
	    }
	}
	if ((status == -1) || (url == null)) {
	    throw new HttpInvalidValueException("Invalid Status-URI");
	}
    }

    public DAVStatusURI(int status, String url) {
	this.status = status;
	this.url    = url;
    }
    
}

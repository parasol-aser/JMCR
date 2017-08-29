// HttpCookieList.java
// $Id: HttpCookieList.java,v 1.1 2010/06/15 12:19:56 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.util.Vector;

public class HttpCookieList extends BasicValue {
    Vector cookies = null;

    protected void updateByteValue() {
	HttpBuffer buf = new HttpBuffer();
	// Dump all cookies:
	int sz = cookies.size();
	for (int i = 0 ; i < sz ; i++) {
	    HttpCookie c = (HttpCookie) cookies.elementAt(i);
	    if (i != 0) {
		buf.append((byte) ';');
		buf.append((byte) ' ');
	    }
	    // Dump the cookie values:
	    buf.append(c.getName(), (byte) '=', c.getValue());
	}
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    protected void deprecatedUpdateByteValue() {
	//deprecated because of the specification
	HttpBuffer buf = new HttpBuffer();
	// Dump all cookies:
	int sz = cookies.size();
	for (int i = 0 ; i < sz ; i++) {
	    HttpCookie c = (HttpCookie) cookies.elementAt(i);
	    if ( i == 0 ) {
		// We use the first cookie version here:
		buf.append("$Version", (byte) '=', c.getVersion());
		buf.append((byte) ';');
	    } else {
		buf.append((byte) ',');
		buf.append((byte) ' ');
	    }
	    // Dump the cookie values:
	    buf.append(c.getName(), (byte) '=', c.getValue());
	    //      buf.append((byte) ';');
	    String s = c.getPath();
	    if ( s != null ) {
		buf.append((byte) ';');
		buf.append("$Path", (byte) '=', s);
	    }
	    if ((s = c.getDomain()) != null) {
		buf.append((byte) ';');
		buf.append("$Domain", (byte) '=', s);
	    }
	}
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    /**
     * parse the Cookie Header according to the Netscape Specification:
     * http://www.netscape.com/newsref/std/cookie_spec.html
     * @exception HttpParserException if parsing failed.
     */
    protected void parse() 
	throws HttpParserException
    {
	ParseState cv = new ParseState(roff, rlen);
	ParseState it = new ParseState(0, 0);
	cv.separator = (byte) ';';
	cv.spaceIsSep = false;
	it.separator = (byte) '=';

	while ( HttpParser.nextItem(raw, cv) >= 0 ) {
	    it.ioff   = cv.start;
	    it.bufend = cv.end;
	    if ( HttpParser.nextItem(raw, it) < 0 )
		error("Invalid item in cookie value.");
	    String item = it.toString(raw);
	    if (item.charAt(0) == '$')
		continue;
	    HttpCookie c = new HttpCookie();
	    // Get the item's value:
	    it.prepare();
	    if ( HttpParser.nextItem(raw, it) < 0 ) {
		// if the cookie has no value, simply give it an empty
		// value.  The cookie spec does not say whether valueless
		// cookies are not allowed and to simply set it to a blank
		// string seems to be the most robust behavior because
		// javascripting in browsers can set valueless cookies.
		c.setValue("");
	    } else {
		// if the cookie has a value, it has only one, and the
		// parser will cut to the next instance of the separator
		// so either we crawl using a simple loop, or we set the
		// end of the ParseState to the one previously computed (safer)
		it.end = cv.end;
		c.setValue(it.toString(raw));
	    }
	    c.setName(item);
	    cookies.addElement(c);
	}
    }

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected void deprecatedParse()
	throws HttpParserException
    {
	// Requires a small twist, but:
	ParseState cv = new ParseState(roff, rlen);
	ParseState it = new ParseState(0, 0);
	ParseState val = new ParseState(0, 0);
	cv.separator = (byte) ';';
	cv.spaceIsSep = false;
	it.separator = (byte) '=';
	val.separator = (byte) ';';
	val.spaceIsSep = false;

	// We will get only one cokoie for the time being:
	HttpCookie c = new HttpCookie();
	while ( HttpParser.nextItem(raw, cv) >= 0 ) {
	    it.ioff   = cv.start;
	    it.bufend = cv.end;
	    //      HttpCookie c = new HttpCookie();
	    if ( HttpParser.nextItem(raw, it) < 0 )
		error("Invalid item in cookie value.");
	    String item = it.toString(raw, true);
	    // Get the item's value:
	    it.prepare();
	    if ( HttpParser.nextItem(raw, it) < 0 )
		error("Cookie item ["+item+"] has no value.");
	    if ( item.equals("$path") ) {
		c.setPath(it.toString(raw));
	    } else if ( item.equals("$domain") ) {
		c.setDomain(it.toString(raw));
	    } else if ( item.equals("$version") ) {
		c.setVersion(Integer.parseInt(it.toString(raw)));
		//	c.setVersion(HttpParser.parseInt(raw, it));
	    } else {
		if ( c.getName() != null )
		    error("Invalid cookie item ["+item+"]");
		c.setName(item);
		val.ioff = it.start;
		val.bufend = cv.end;
		HttpParser.nextItem(raw, val);
		c.setValue(it.toString(raw));
	    }
	    cv.prepare();
	}
	cookies.addElement(c);
    }

    /**
     * Get this HTTP value, parsed value.
     */

    public Object getValue() {
	return this;
    }

    /**
     * Add a cookie to this header value.
     * @param name The name of the cookie to add.
     * @param value It's value.
     * @return A HttpCookie instance, tha represents this cookie in the header
     * value.
     */

    public HttpCookie addCookie(String name, String value) {
	validate();
	HttpCookie c = new HttpCookie(true, name, value);
	cookies.addElement(c);
	return c;
    }

    /**
     * Remove a cookie from this header value.
     * @param name The name of the cookie to remove.
     * @return A boolean, <strong>true</strong> If the cookie was found, and
     * removed, <strong>false</strong> otherwise.
     */

    public boolean removeCookie(String name) {
	validate();
	int sz = cookies.size();
	for (int i = 0 ; i < sz ; i++) {
	    HttpCookie c = (HttpCookie) cookies.elementAt(i);
	    if ( c.getName().equals(name) ) {
		cookies.removeElementAt(i);
		return true;
	    }
	}
	return false;
    }

    /**
     * Lookup a cookie by name.
     * @param name The name of the cooie to lookup.
     * @return A HttpCookie instance, or <strong>null</strong> if not found.
     */

    public HttpCookie getCookie(String name) {
	validate();
	int sz = cookies.size();
	for (int i = 0 ; i < sz ; i++) {
	    HttpCookie c = (HttpCookie) cookies.elementAt(i);
	    if ( c.getName().equalsIgnoreCase(name) )
		return c;
	}
	return null;
    }

    public HttpCookie[] getCookies() {
	validate();
	HttpCookie cooks [] = new HttpCookie[cookies.size()];
	cookies.copyInto(cooks);
	return cooks;
    }

    HttpCookieList(HttpCookie c[]) {
	this.isValid = true;
	this.cookies = new Vector(8);
	if ( c != null ) {
	    // FIXME Don't tell me this is broken, I *know* it
	    for (int i = 0 ; i < c.length ; i++)
		cookies.addElement(c[i]);
	}
    }

    public HttpCookieList() {
	this.isValid = false;
	this.cookies = new Vector(2);
    }

}

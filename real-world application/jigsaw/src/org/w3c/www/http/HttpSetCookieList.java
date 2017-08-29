// HttpSetCookieList.java
// $Id: HttpSetCookieList.java,v 1.1 2010/06/15 12:19:44 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.util.Date;
import java.util.Vector;

import java.io.IOException;
import java.io.OutputStream;

public class HttpSetCookieList extends BasicValue {
    Vector setcookies = null;
    Vector raws = null;
    Vector copycookies = null;
    boolean original = true;

    protected int length() {
	if (setcookies == null)
	    return 0;
	return setcookies.size();
    }

    protected int copyLength() {
	if (copycookies == null)
	    return 0;
	return copycookies.size();
    }

    // if the cookie is home made or copied from an upstream source
    protected boolean isOriginal() {
	return original;
    }

    protected void emitCookie(OutputStream out, int cookie) 
	throws IOException
    {
	checkByteValues();
	out.write(((HttpBuffer)raws.elementAt(cookie)).getByteCopy());
    }

    protected void emitCopyCookie(OutputStream out, int cookie) 
	throws IOException
    {
	out.write(((HttpBuffer)copycookies.elementAt(cookie)).getByteCopy());
    }

    protected final void checkByteValues() {
	if (raws == null) {
	    updateByteValue();
	}
    }

    protected void updateByteValue() {
	// Dump all cookies, one after the other:
	int len = setcookies.size();
	raws = new Vector(len);
	for (int i = 0 ; i < len ; i++) {
	    HttpBuffer buf = new HttpBuffer();
	    HttpSetCookie sc = (HttpSetCookie) setcookies.elementAt(i);
	    buf.append(sc.getName(), (byte) '=', sc.getValue());
	    String s = sc.getComment();
	    if (s != null ) {
		buf.append((byte) ';');
		buf.append(" comment", (byte) '=', s);
	    }
	    if ((s = sc.getDomain()) != null ) {
		buf.append((byte) ';');
		buf.append(" domain", (byte) '=', s);
	    } 
	    int ii = sc.getMaxAge();
	    if ( ii >= 0 ) {
		// Append expires time:
		float exp = (float)ii * (float)1000;
		CookieDate expires = 
		    new CookieDate(true,
				   (long)(System.currentTimeMillis()
					  + exp));
		buf.append((byte) ';');
		buf.append(" expires=");
		expires.appendValue(buf);
	    }
	    if ((s = sc.getPath()) != null ) {
		buf.append((byte) ';');
		buf.append(" path", (byte) '=', s);
	    }
	    if ( sc.getSecurity() ) {
		buf.append(';');
		buf.append(" secure");
	    }
	    raws.addElement(buf);
	}
    }

    /**
     * HeaderValue implementation - Add these bytes to the header raw value.
     * @param buf The byte buffer containing some part of the header value.
     * @param off The offset of the header value in above buffer.
     * @param len The length of the header value in above buffer.
     */

    public void addBytes(byte buf[], int off, int len) {
	original = false;
	super.addBytes(buf, off, len);
	HttpBuffer b = new HttpBuffer(len+1);
	b.append(buf, off, len);
	if (copycookies == null) {
	    copycookies = new Vector(2);
	}
	copycookies.addElement(b);
    }

    /**
     * parse set cookie header according to the specification:
     * http://www.netscape.com/newsref/std/cookie_spec.html
     * @exception HttpParserException if parsing failed.
     */
    protected void parse()
	throws HttpParserException
    {
	ParseState cl  = new ParseState(roff, rlen);
	ParseState av  = new ParseState(0, 0);
	ParseState it  = new ParseState(0, 0);
	ParseState val = new ParseState(0, 0);
	// Prepare parser states:
	// FIXME SPEC 
	cl.separator = (byte) ',';
	cl.spaceIsSep = false;
	av.separator = (byte) ';';
	av.spaceIsSep = false;
	it.separator = (byte) '=';
	it.spaceIsSep = false;
	val.separator = (byte) ';';
	val.spaceIsSep = false;

	// Parse the cookie list with cl
	boolean inExpireItem = false;
	String expiresDay = null;
	HttpSetCookie sc = null;
	while (HttpParser.nextItem(raw, cl) >= 0) {
	    // Each cookie is made of attr-value pairs:
	    av.ioff   = cl.start;
	    av.bufend = cl.end;
	    if (!inExpireItem)
		sc = new HttpSetCookie();
	    while ( HttpParser.nextItem(raw, av) >= 0 ) {
		if (inExpireItem) {
		    //get the rest of the expire date
		    try {
			String date = expiresDay+", "+av.toString(raw);
			Date maxage = new Date(date);
			sc.setMaxAge((int) (maxage.getTime()/1000));
		    } catch (Exception e) {}
		    inExpireItem = false;
		} else {
		    // Each av is made of an attr '=' value:
		    it.ioff   = av.start;
		    it.bufend = av.end;
		    if ( HttpParser.nextItem(raw, it) < 0 )
			error("Invalid cookie item ["+av.toString(raw)+"]");
		    String itemNaturalCase = it.toString(raw);
		    String item = it.toString(raw, true);
		    it.prepare();
		    if ( HttpParser.nextItem(raw, it) < 0 ) {
			// There's no value for the item. It's either an error,
			// a cookie without value or the "secure" item.
			if ( item.equals("secure") ) {
			    // Secure is the only item without value.
			    sc.setSecurity(true);
			} else if ( item.equals("comment") || 
				    item.equals("domain") ||
				    item.equals("expires") || 
				    item.equals("max-age") ||
				    item.equals("path") ||
				    item.equals("version") ) {
			    // All these items *must* have a value if they 
			    // are defined. So it's an error.
			    error("No value for attribute ["+item+"]");
			} else {
			    // This is probably a cookie without value.
			    sc.setName(itemNaturalCase);
			    sc.setValue("");
			}
		    } else {
			if ( item.equals("comment") ) {
			    sc.setComment(it.toString(raw));
			} else if ( item.equals("domain") ) {
			    sc.setDomain(it.toString(raw));
			} else if ( item.equals("expires") ) {
			    //Ugly hack because there is a ',' in date
			    expiresDay = it.toString(raw);
			    inExpireItem = true;
			} else if ( item.equals("max-age") ) {
			    sc.setMaxAge(Integer.parseInt(it.toString(raw)));
			} else if ( item.equals("path") ) {
			    sc.setPath(it.toString(raw));
			} else if ( item.equals("version") ) {
			    sc.setVersion(Integer.parseInt(it.toString(raw)));
			} else {
			    // This is probably the name/value for the cookie:
			    if ( sc.getName() != null )
				error("Invalid cookie item ["+item+"]");
			    sc.setName(itemNaturalCase);
			    val.ioff = it.start;
			    val.bufend = av.end;
			    HttpParser.nextItem(raw, val);
			    sc.setValue(val.toString(raw));
			}
		    }
		}
		av.prepare();
	    }
	    if (!inExpireItem) // Add the newly created cookie:
		setcookies.addElement(sc);
	    cl.prepare();
	}
    }

    public Object getValue() {
	return this;
    }

    /**
     * Add a specific cookie to the SetCookie header value.
     * This method creates a new, empty SetCookie holder, attaches it to the
     * SetCookie header, and returns it.
     * @param name The cookie's name.
     * @param value The cookie's value.
     */

    public HttpSetCookie addSetCookie(String name, String value) {
	validate();
	HttpSetCookie setcookie = new HttpSetCookie(true, name, value);
	setcookies.addElement(setcookie);
	return setcookie;
    }

    public void addSetCookie(HttpSetCookie setCookie) {
	validate();
	setcookies.addElement(setCookie);
    }

    /**
     * Remove a predefined cookie from this SetCookie header.
     * @param name The name of the cookie to remove.
     * @return A boolean <strong>true</strong> if removed, <strong>false
     * </strong> otherwise.
     */

    public boolean removeSetCookie(String name) {
	validate();
	for (int i = 0 ; i < setcookies.size() ; i++) {
	    HttpSetCookie setcookie = (HttpSetCookie) setcookies.elementAt(i);
	    if ( setcookie.getName().equals(name) ) {
		setcookies.removeElementAt(i);
		return true;
	    }
	}
	return false;
    }

    /**
     * Get the cookie infos associated with the given cookie name, if any.
     * @param name The cookie's name.
     * @return A HttpSetCookie instance, if found, or <strong>null</strong> 
     * otherwise.
     */

    public HttpSetCookie getSetCookie(String name) {
	validate();
	for (int i = 0 ; i < setcookies.size() ; i++) {
	    HttpSetCookie setcookie = (HttpSetCookie) setcookies.elementAt(i);
	    if ( setcookie.getName().equals(name) )
		return setcookie;
	}
	return null;
    }

    public HttpSetCookie[] getSetCookies() {
	validate();
	HttpSetCookie cooks [] = new HttpSetCookie[setcookies.size()];
	setcookies.copyInto(cooks);
	return cooks;
    }

    HttpSetCookieList() {
	this.isValid    = false;
	this.setcookies = new Vector(2);
    }

    public HttpSetCookieList(HttpSetCookie sc[]) {
	this.isValid    = true;
	this.setcookies = new Vector(2);
	if ( sc != null ) {
	    // FIXME Eouf corse, it's broken
	    for (int i = 0 ; i < sc.length ; i++)
		setcookies.addElement(sc[i]);
	}
    }

}

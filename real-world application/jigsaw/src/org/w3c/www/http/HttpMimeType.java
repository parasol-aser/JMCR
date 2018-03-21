// HttpMimeType.java
// $Id: HttpMimeType.java,v 1.1 2010/06/15 12:19:49 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html


package org.w3c.www.http;

import org.w3c.www.mime.MimeType;

public class HttpMimeType extends BasicValue {
    MimeType type = null;

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected void parse() 
	throws HttpParserException
    {
	String str = new String(raw, 0, 0, raw.length) ;
	try {
	    type = new MimeType(str);
	} catch (Exception ex) {
	    error("Invalid content type.");
	}
    }

    protected void updateByteValue() {
	String str = type.toString();
	raw  = new byte[str.length()];
	roff = 0;
	rlen = raw.length;
	str.getBytes(0, raw.length, raw, 0);
    }

    public Object getValue() {
	validate();
	return type;
    }

    public HttpMimeType() {
	super();
    }

    HttpMimeType(boolean isValid, MimeType type) {
	this.isValid = isValid;
	this.type    = type;
    }
}

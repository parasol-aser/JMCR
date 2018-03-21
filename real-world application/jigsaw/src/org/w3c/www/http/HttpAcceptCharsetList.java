// HttpAcceptCharsetList.java
// $Id: HttpAcceptCharsetList.java,v 1.1 2010/06/15 12:19:50 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.util.Vector;

public class HttpAcceptCharsetList extends BasicValue {
    HttpAcceptCharset charsets[] = null;

    protected void parse() {
	Vector     vl = new Vector(4);
	ParseState ps = new ParseState(roff, rlen);
	ps.separator  = (byte) ',';
	ps.spaceIsSep = false;
	while ( HttpParser.nextItem(raw, ps) >= 0 ) {
	    vl.addElement(new HttpAcceptCharset(this, raw, ps.start, ps.end));
	    ps.prepare();
	}
	charsets = new HttpAcceptCharset[vl.size()];
	vl.copyInto(charsets);
    }

    protected void updateByteValue() {
	HttpBuffer buf = new HttpBuffer();
	if ( charsets != null ) {
	    for (int i = 0 ; i < charsets.length ; i++) {
		if ( i > 0 )
		    buf.append(',');
		charsets[i].appendValue(buf);
	    }
	    raw  = buf.getByteCopy();
	    roff = 0;
	    rlen = raw.length;
	} else {
	    raw  = new byte[0];
	    roff = 0;
	    rlen = 0;
	}
    }

    public Object getValue() {
	validate();
	return charsets;
    }

    /**
     * Add an accpted charset clause to the list.
     * @param charset The new accepted charset clause.
     */

    public void addCharset(HttpAcceptCharset charset) {
	if ( charsets == null ) {
	    charsets    = new HttpAcceptCharset[1];
	    charsets[0] = charset;
	} else {
	    int len = charsets.length;
	    HttpAcceptCharset newset[] = new HttpAcceptCharset[len+1];
	    System.arraycopy(charsets, 0, newset, 0, len);
	    newset[len] = charset;
	    charsets    = newset;
	}
    }

    HttpAcceptCharsetList() {
	this.isValid = false;
    }

    HttpAcceptCharsetList(HttpAcceptCharset charsets[]) {
	this.isValid  = isValid;
	this.charsets = charsets;
    }

}



// HttpAcceptEncodingList.java
// $Id: HttpAcceptEncodingList.java,v 1.1 2010/06/15 12:19:47 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.util.Vector;

public class HttpAcceptEncodingList extends BasicValue {
    HttpAcceptEncoding encodings[] = null;

    protected void parse() {
	Vector     vl = new Vector(4);
	ParseState ps = new ParseState(roff, rlen);
	ps.separator  = (byte) ',';
	ps.spaceIsSep = false;
	while ( HttpParser.nextItem(raw, ps) >= 0 ) {
	    vl.addElement(new HttpAcceptEncoding(this, raw, ps.start, ps.end));
	    ps.prepare();
	}
	encodings = new HttpAcceptEncoding[vl.size()];
	vl.copyInto(encodings);
    }

    protected void updateByteValue() {
	HttpBuffer buf = new HttpBuffer();
	if ( encodings == null ) {
	    for (int i = 0 ; i < encodings.length ; i++) {
		if ( i > 0 )
		    buf.append(',');
		encodings[i].appendValue(buf);
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
	return encodings;
    }

    /**
     * Add a clause to that list of accepted encodings.
     * @param lang The accepted encoding.
     */

    public void addEncoding(HttpAcceptEncoding enc) {
	if ( encodings == null ) {
	    encodings    = new HttpAcceptEncoding[1];
	    encodings[0] = enc;
	} else {
	    int len = encodings.length;
	    HttpAcceptEncoding newenc[] = new HttpAcceptEncoding[len+1];
	    System.arraycopy(encodings, 0, newenc, 0, len);
	    newenc[len] = enc;
	    encodings = newenc;
	}
    }

    HttpAcceptEncodingList() {
	this.isValid = false;
    }

    HttpAcceptEncodingList(HttpAcceptEncoding encodings[]) {
	this.encodings = encodings;
	this.isValid   = true;
    }

}



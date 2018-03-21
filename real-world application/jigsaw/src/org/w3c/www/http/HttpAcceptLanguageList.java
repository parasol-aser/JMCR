// HttpAcceptLanguageList.java
// $Id: HttpAcceptLanguageList.java,v 1.1 2010/06/15 12:19:45 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.util.Vector;

public class HttpAcceptLanguageList extends BasicValue {
    HttpAcceptLanguage languages[] = null;

    protected void parse() {
	Vector     vl = new Vector(4);
	ParseState ps = new ParseState(roff, rlen);
	ps.separator  = (byte) ',';
	ps.spaceIsSep = false;
	while ( HttpParser.nextItem(raw, ps) >= 0 ) {
	    vl.addElement(new HttpAcceptLanguage(this, raw, ps.start, ps.end));
	    ps.prepare();
	}
	languages = new HttpAcceptLanguage[vl.size()];
	vl.copyInto(languages);
    }

    protected void updateByteValue() {
	HttpBuffer buf = new HttpBuffer();
	if ( languages == null ) {
	    for (int i = 0 ; i < languages.length ; i++) {
		if ( i > 0 )
		    buf.append(',');
		languages[i].appendValue(buf);
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
	return languages;
    }

    /**
     * Add a clause to that list of accepted languages.
     * @param lang The accepted language.
     */

    public void addLanguage(HttpAcceptLanguage lang) {
	if ( languages == null ) {
	    languages    = new HttpAcceptLanguage[1];
	    languages[0] = lang;
	} else {
	    int len = languages.length;
	    HttpAcceptLanguage newlang[] = new HttpAcceptLanguage[len+1];
	    System.arraycopy(languages, 0, newlang, 0, len);
	    newlang[len] = lang;
	    languages = newlang;
	}
    }

    HttpAcceptLanguageList() {
	this.isValid = false;
    }

    HttpAcceptLanguageList(HttpAcceptLanguage languages[]) {
	this.languages = languages;
	this.isValid   = true;
    }

}



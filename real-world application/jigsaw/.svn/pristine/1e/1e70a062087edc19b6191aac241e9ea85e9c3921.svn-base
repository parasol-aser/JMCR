// HttpWarningList.java
// $Id: HttpWarningList.java,v 1.1 2010/06/15 12:19:44 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.util.Vector;

public class HttpWarningList extends BasicValue {
    HttpWarning warnings[] = null;

    protected void parse() {
	Vector     ws = new Vector(4);
	ParseState ps = new ParseState(roff, rlen);
	ps.spaceIsSep = false;
	while (HttpParser.nextItem(raw, ps) >= 0) {
	    ws.addElement(new HttpWarning(this, raw, ps.start, ps.end));
	    ps.prepare();
	}
	warnings = new HttpWarning[ws.size()];
	ws.copyInto(warnings);
    }

    protected void updateByteValue() {
	HttpBuffer buf = new HttpBuffer();
	for (int i = 0 ; i < warnings.length ; i++) {
	    if ( i > 0 )
		buf.append(',');
	    warnings[i].appendValue(buf);
	}
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    public Object getValue() {
	validate();
	return warnings;
    }

    /**
     * Add a warning to that list.
     * @param w The warning to add.
     */

    public void addWarning(HttpWarning w) {
	if ( warnings == null ) {
	    warnings    = new HttpWarning[1];
	    warnings[0] = w;
	} else {
	    int len = warnings.length;
	    HttpWarning newwarn[] = new HttpWarning[len+1];
	    System.arraycopy(warnings, 0, newwarn, 0, len);
	    newwarn[len] = w;
	    warnings = newwarn;
	}
    }

    HttpWarningList() {
	this.isValid = false;
    }

    HttpWarningList(HttpWarning warnings[]) {
	this.isValid  = true;
	this.warnings = warnings;
    }

    HttpWarningList(HttpWarning warning) {
	this.isValid     = true;
	this.warnings    = new HttpWarning[1];
	this.warnings[0] = warning;
    }
}

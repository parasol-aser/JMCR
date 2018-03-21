// HttpRangeList.java
// $Id: HttpRangeList.java,v 1.1 2010/06/15 12:19:56 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.util.Vector;

public class HttpRangeList extends BasicValue {
    HttpRange ranges[] = null;

    protected void parse() {
	Vector     vr = new Vector(2);
	ParseState ps = new ParseState(roff, rlen);
	ps.separator  = (byte) ',';
	ps.spaceIsSep = false;
	while ( HttpParser.nextItem(raw, ps) >= 0 ) {
	    vr.addElement(new HttpRange(this, raw, ps.start, ps.end));
	    ps.prepare();
	}
	if ( vr.size() > 0 ) {
	    ranges = new HttpRange[vr.size()];
	    vr.copyInto(ranges);
	}
    }

    protected void updateByteValue() {
	HttpBuffer buf = new HttpBuffer();
	for (int i = 0 ; i < ranges.length ; i++) {
	    if ( i > 0 )
		buf.append(',');
	    ranges[i].appendValue(buf);
	}
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    public Object getValue() {
	validate();
	return ranges;
    }

    /**
     * Add a range to this list.
     * @param range The range to add.
     */

    public void addRange(HttpRange range) {
	if ( ranges == null ) {
	    ranges    = new HttpRange[1];
	    ranges[0] = range;
	} else {
	    int len = ranges.length;
	    HttpRange nranges[] = new HttpRange[len+1];
	    System.arraycopy(ranges, 0, nranges, 0, len);
	    nranges[len] = range;
	    ranges = nranges;
	}
    }

    HttpRangeList(HttpRange ranges[]) {
	this.ranges  = ranges;
	this.isValid = true;
    }

    HttpRangeList() {
	this.isValid = false;
    }

}

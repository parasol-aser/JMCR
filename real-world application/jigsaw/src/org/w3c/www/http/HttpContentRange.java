// HttpContentRange.java
// $Id: HttpContentRange.java,v 1.1 2010/06/15 12:19:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

public class HttpContentRange extends BasicValue {
    int firstpos = -1;
    int lastpos  = -1;
    int length   = -1;
    String unit = null ;

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected void parse() 
	throws HttpParserException
    {
	ParseState ps = new ParseState();
	ps.ioff   = 0;
	ps.bufend = raw.length;
	// Get the byte unit:
	ps.separator = (byte) ' ';
	if ( HttpParser.nextItem(raw, ps) < 0 )
	    error ("Invalid byte range (no byte unit).");
	this.unit = new String(raw, 0, ps.start, ps.end-ps.start);
	// Get the first position
	ps.separator = (byte) '-';
	ps.prepare();
	if ( HttpParser.nextItem(raw, ps) < 0 )
	    error ("Invalid byte range (no first position).");
	ParseState pos = new ParseState();
	pos.ioff   = ps.start;
	pos.bufend = ps.end;
	firstpos   = HttpParser.parseInt(raw, pos);
	// Get the last positon
	ps.prepare();
	ps.separator = (byte) '/';
	if ( HttpParser.nextItem(raw, ps) < 0 )
	    error("Invalid byte range (no last position).");
	pos.ioff   = ps.start;
	pos.bufend = ps.end;
	lastpos    = HttpParser.parseInt(raw, pos);
	// Get the full length:
	ps.prepare();
	if ( HttpParser.nextItem(raw, ps) < 0 )
	    error("Invalid byte range (no full length).");
	pos.ioff   = ps.start;
	pos.bufend = ps.end;
	if ((raw[pos.ioff] == (byte)'*') && (ps.end-ps.start == 1)) {
	    length = -1;
	} else {
	    length     = HttpParser.parseInt(raw, pos);
	}
    }

    protected void updateByteValue() {
	HttpBuffer buf = new HttpBuffer();
	buf.append(unit);
	buf.append((byte) ' ');
	buf.appendInt(firstpos);
	buf.append('-');
	buf.appendInt(lastpos);
	buf.append('/');
	if (length < 0) {
	    buf.append('*');
	} else {
	    buf.appendInt(length);
	}
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    public Object getValue() {
	return this;
    }

    /**
     * Get this range first position.
     * The meaning of the returne integer is to be understood relative to
     * the unit, as obtained by <code>getUnit</code> method.
     * @return An integer value for thr first position.
     */

    public int getFirstPosition() {
	validate();
	return firstpos;
    }

    /**
     * Set this range first position.
     * @param firstpos The firt position of the range.
     */

    public void setFirstPosition(int firstpos) {
	if ( firstpos != this.firstpos )
	    invalidateByteValue();
	this.firstpos = firstpos;
    }

    /**
     * Get this range last position.
     * The meaning of the returne integer is to be understood relative to
     * the unit, as obtained by <code>getUnit</code> method.
     * @return An integer value giving the last position.
     */

    public int getLastPosition() {
	validate();
	return lastpos;
    }

    /**
     * Set this range last position.
     * @param The last position, as an integer.
     */

    public void setLastPosition(int lastpos) {
	if ( lastpos != this.lastpos )
	    invalidateByteValue() ;
	this.lastpos = lastpos;
    }

    /**
     * Get this range entity full length.
     * @return The full length of the entity.
     */

    public int getFullLength() {
	validate();
	return length;
    }

    /**
     * Set this range entity full length.
     * @param length The new full length for the entity, -1 if unknown.
     */

    public void setFullLength(int length) {
	if ( length != this.length )
	    invalidateByteValue();
	this.length = length;
    }

    /**
     * Get this content range's unit.
     * @return A String giving the unit for the range, or <strong>null</strong>
     * if undefined.
     */

    public String getUnit() {
	validate();
	return unit;
    }

    /**
     * Set this content range's unit.
     * @param unit The unit in which this range was measured.
     */

    public void setUnit(String unit) {
	invalidateByteValue();
	this.unit = unit;
    }

    HttpContentRange() {
	this.isValid = false ;
    }

    public HttpContentRange(boolean isValid
			    , String unit
			    , int firstpos, int lastpos, int length) {
	this.isValid = true;
	setUnit(unit);
	setFirstPosition(firstpos);
	setLastPosition(lastpos);
	setFullLength(length);
    }

}

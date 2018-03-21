// HttpRange.java
// $Id: HttpRange.java,v 1.1 2010/06/15 12:19:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

public class HttpRange extends BasicValue {
    /**
     * First position in the range.
     */
    protected int firstpos = -1;
    /**
     * Last position in the range.
     */
    protected int lastpos = -1;
    /**
     * The range's unit.
     */
    protected String unit = null;

    /**
     * The list we belong to, if any.
     */
    protected HttpRangeList list = null;

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected void parse()
	throws HttpParserException
    {
	ParseState ps = new ParseState(roff, rlen);
	ParseState it = new ParseState();
	// Get the range's unit:
	ps.separator = (byte) '=';
	if ( HttpParser.nextItem(raw, ps) < 0 )
	    error("No byte range unit.");
	unit = ps.toString(raw, true);
	ps.prepare();
	// Check for a suffix spec
	int off = HttpParser.skipSpaces(raw, ps);
	if ( raw[off] == '-' ) {
	    // Suffix spec (skip sign, and parse last pos)
	    ps.ioff = ++off ; 
	    this.firstpos = -1;
	    this.lastpos  = HttpParser.parseInt(raw, ps);
	} else {
	    // Normal spec, get first position:
	    ps.separator = (byte) '-';
	    if ( HttpParser.nextItem(raw, ps) < 0 )
		error("Invalid range spec: no first pos.");
	    it.prepare(ps);
	    this.firstpos = HttpParser.parseInt(raw, it);
	    // Get last position:
	    ps.prepare();
	    if ( HttpParser.nextItem(raw, ps) >= 0 ) {
		it.prepare(ps);
		this.lastpos = HttpParser.parseInt(raw, it);
	    }
	}
    }

    protected void invalidateByteValue() {
	super.invalidateByteValue();
	if ( list != null )
	    list.invalidateByteValue();
    }

    protected void updateByteValue() {
	HttpBuffer buf = new HttpBuffer();
	buf.append(unit);
	buf.append((byte) '=');
	if ( firstpos >= 0 ) 
	    buf.appendInt(firstpos);
	else if ( lastpos < 0)
	    buf.append((byte) '0');
	buf.append('-');
	if ( lastpos >= 0 )
	    buf.appendInt(lastpos);
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    public Object getValue() {
	validate();
	return this;
    }

    /**
     * Get the first position of the range.
     * @return An integer giving the first pos for the range, or <strong>-1
     * </strong> if undefined.
     */

    public int getFirstPosition() {
	validate();
	return firstpos;
    }

    /**
     * Set the first position of the range.
     * @param firstpos The first positon for the range.
     */

    public void setFirstPosition(int firstpos) {
	if ( this.firstpos != firstpos )
	    invalidateByteValue();
	this.firstpos = firstpos;
    }

    /**
     * Get the last position for this range.
     * If the first position is negative, then the last position is to be 
     * considered as the number of bytes relative to the end of the content.
     * @return An integer giving the last position.
     */

    public int getLastPosition() {
	validate();
	return lastpos;
    }

    /**
     * Set the last position for this range.
     * If the given number is negative, it won't be displayed
     * meaning that everything from firstpos to the end
     * @param lastpos The new last position.
     */

    public void setLastPosition(int lastpos) {
	if ( this.lastpos != lastpos )
	    invalidateByteValue();
	this.lastpos = lastpos;
    }

    /**
     * Set the unit in which this range is taken.
     * @Param unit The unit in which the range is measured.
     */

    public void setUnit(String unit) {
	invalidateByteValue();
	this.unit = unit;
    }

    /**
     * Get the unit in which this range is taken.
     * @return The unit in which this range is measured, or <strong>
     * null</strong> if undefined.
     */

    public String getUnit() {
	validate();
	return unit;
    }

    HttpRange(HttpRangeList list, byte raw[], int roff, int rlen) {
	this.list = list;
	this.raw = raw;
	this.roff = roff;
	this.rlen = rlen;
	this.isValid = false;
    }

    HttpRange(boolean isValid, String unit, int firstpos, int lastpos) {
	this.isValid = isValid;
	setUnit(unit);
	setFirstPosition(firstpos);
	setLastPosition(lastpos);
    }

    public HttpRange() {
	this.isValid = false;
    }

}

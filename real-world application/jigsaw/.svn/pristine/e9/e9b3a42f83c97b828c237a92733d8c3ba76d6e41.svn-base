// HttpAcceptEncoding.java
// $Id: HttpAcceptEncoding.java,v 1.1 2010/06/15 12:19:46 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

public class HttpAcceptEncoding extends BasicValue {
    String encoding = null;
    double quality  = 1.0;
    HttpAcceptEncodingList list = null;

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected void parse()
	throws HttpParserException
    {
	ParseState ps = new ParseState(roff, rlen);
	ps.separator  = (byte) ';';
	ps.spaceIsSep = false;
	// Get the encoding:
	if ( HttpParser.nextItem(raw, ps) < 0 )
	    error("Invalid Accept-Encoding: no encoding.");
	this.encoding = new String(raw, 0, ps.start, ps.end-ps.start);
	// And the optional quality:
	ps.prepare();
	ps.separator = (byte) '=';
	if ( HttpParser.nextItem(raw, ps) < 0 ) {
	    this.quality = 1.0;
	} else {
	    ps.prepare();
	    this.quality = HttpParser.parseQuality(raw, ps);
	}
    }

    protected void invalidateByteValue() {
	super.invalidateByteValue();
	if ( list != null )
	    list.invalidateByteValue();
    }

    protected void updateByteValue() {
	HttpBuffer buf = new HttpBuffer();
	buf.append(encoding);
	buf.append(';');
	buf.append(quality);
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    public Object getValue() {
	validate();
	return this;
    }

    /**
     * Get this accept encoding clause encoding.
     * @return A String encoding the encoding token.
     */

    public String getEncoding() {
	validate();
	return encoding;
    }

    /**
     * Set the encoding accepted by this clause.
     * @param encoding The accepted encoding.
     */

    public void setEncoding(String encoding) {
        if (this.encoding != null) {
            if ( this.encoding.equals(encoding) ) {
                return;
            }
        }
	invalidateByteValue();
	this.encoding = encoding;
    }

    /**
     * Get the quality at which this encoding is accepted.
     * @return A double value, encoding the quality, or <strong>1.0</strong>
     * if undefined.
     */

    public double getQuality() {
	validate();
	return quality;
    }

    /**
     * Set the quality under which this encoding is accepted.
     * @param q The quality for this encoding.
     */

    public void setQuality(double quality) {
	if ( this.quality != quality )
	    invalidateByteValue();
	this.quality = quality;
    }

    HttpAcceptEncoding(HttpAcceptEncodingList list, byte raw[], int o, int l) {
	this.list = list;
	this.raw  = raw;
	this.roff = o;
	this.rlen = l;
	this.isValid = false;
    }

    HttpAcceptEncoding(boolean isValid, String enc, double quality) {
	this.isValid = isValid;
	setEncoding(enc);
	setQuality(quality);
    }

    public HttpAcceptEncoding() {
	this.isValid = false;
    }
}

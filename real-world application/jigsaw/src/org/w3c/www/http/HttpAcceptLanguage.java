// HttpAcceptLanguage.java
// $Id: HttpAcceptLanguage.java,v 1.1 2010/06/15 12:19:43 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

public class HttpAcceptLanguage extends BasicValue {
    String language = null;
    double quality  = 1.0;
    HttpAcceptLanguageList list = null;

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
	// Get the language:
	if ( HttpParser.nextItem(raw, ps) < 0 )
	    error("Invalid Accept-Language: no language.");
	this.language = new String(raw, 0, ps.start, ps.end-ps.start);
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
	buf.append(language);
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
     * Get this accept language clause language.
     * @return A String encoding the language token.
     */

    public String getLanguage() {
	validate();
	return language;
    }

    /**
     * Set the language accepted by this clause.
     * @param language The accepted language.
     */

    public void setLanguage(String language) {
	if ( this.language.equals(language) )
	    return;
	invalidateByteValue();
	this.language = language;
    }

    /**
     * Get the quality at which this language is accepted.
     * @return A double value, encoding the quality, or <strong>1.0</strong>
     * if undefined.
     */

    public double getQuality() {
	validate();
	return quality;
    }

    /**
     * Set the quality under which this language is accepted.
     * @param q The quality for this language.
     */

    public void setQuality(double quality) {
	if ( this.quality != quality )
	    invalidateByteValue();
	this.quality = quality;
    }

    HttpAcceptLanguage(HttpAcceptLanguageList list, byte raw[], int o, int l) {
	this.list = list;
	this.raw  = raw;
	this.roff = o;
	this.rlen = l;
	this.isValid = false;
    }

    HttpAcceptLanguage(boolean isValid, String lang, double quality) {
	this.isValid = isValid;
	setLanguage(lang);
	setQuality(quality);
    }

    public HttpAcceptLanguage() {
	this.isValid = false;
    }
}

// HttpAccept.java
// $Id: HttpAccept.java,v 1.1 2010/06/15 12:19:50 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.util.Vector;

import org.w3c.www.mime.MimeType;

public class HttpAccept extends BasicValue {
    /**
     * The list of accept parameters name.
     */
    protected String aparams[] = null;
    /**
     * The list of accept parameters value.
     */
    protected String avalues[] = null;
    /**
     * The accepted MIME type.
     */
    protected MimeType type = null;
    /**
     * The list we belong to, if any.
     */
    HttpAcceptList list = null;
    /**
     * the quality
     */
    protected double quality = -1;
    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected void parse()
	throws HttpParserException
    {
	ParseState ps = new ParseState(roff, rlen);
	// We need to do our own MIME type parsing, to avoid ambiguities !
	// Get the type:
	ps.separator  = (byte) '/';
	ps.spaceIsSep = false;
	if ( HttpParser.nextItem(raw, ps) < 0 )
	    error("Invalid Accept: has no type.");
	String type = new String(raw, 0, ps.start, ps.end-ps.start);
	// Get the subtype:
	ps.prepare();
	ps.separator = (byte) ';';
	if ( HttpParser.nextItem(raw, ps) < 0 )
	    error("Invalid accept: no subtype");
	String subtype = new String(raw, 0, ps.start, ps.end-ps.start);
	// Get the parameters:
	ParseState it = new ParseState();
	it.separator  = (byte) '=';
	it.spaceIsSep = false;
	ps.prepare();
	Vector  vparams = new Vector(4);
	Vector  vvalues = new Vector(4);
	boolean accept  = false;
	while (HttpParser.nextItem(raw, ps) >= 0) {
	    it.ioff   = ps.start;
	    it.bufend = ps.end;
	    if ( HttpParser.nextItem(raw, it) < 0 ) 
		error("Invalid parameter: no param name.");
	    // The Hack !
	    String param = new String(raw, 0, it.start, it.end-it.start);
	    if ( param.equalsIgnoreCase("q") ) {
		// Switching to accept-params
		accept = true;
		break ;
	    }
	    vparams.addElement(new String(raw, 0, it.start, it.end-it.start));
	    it.prepare();
	    if ( HttpParser.nextItem(raw, it) < 0 )
		error("Invalid parameter: no value.");
	    vvalues.addElement(new String(raw, 0, it.start, it.end-it.start));
	    ps.prepare();
	}
	if ( vparams.size() > 0 ) {
	    String pparams[] = new String[vparams.size()];
	    String pvalues[] = new String[pparams.length];
	    vparams.copyInto(pparams);
	    vvalues.copyInto(pvalues);
	    this.type = new MimeType(type, subtype, pparams, pvalues);
	} else {
	    this.type = new MimeType(type, subtype);
	}
	// Parse remaining accept parameters:
	if ( accept ) {
	    vparams.setSize(0);
	    vvalues.setSize(0);
	    // Hack to finish with the q parameter
	    it.prepare();
	    if ( HttpParser.nextItem(raw, it) < 0 )
		error("Invalid accept parameter: no value.");
	    vparams.addElement("q");
	    vvalues.addElement(new String(raw, 0, it.start, it.end-it.start));
	    it.prepare();
	    while (HttpParser.nextItem(raw, ps) >= 0) {
		it.ioff   = ps.start;
		it.bufend = ps.end;
		if ( HttpParser.nextItem(raw, it) < 0 )
		    error("Invalid accept parameter: no name.");
		vparams.addElement(new String(raw,0,it.start,it.end-it.start));
		it.prepare();
		if (HttpParser.nextItem(raw, it) < 0 )
		    error("Invalid accept parameter: no value.");
		vvalues.addElement(new String(raw,0,it.start,it.end-it.start));
		ps.prepare();
	    }
	    this.aparams = new String[vparams.size()];
	    this.avalues = new String[aparams.length];
	    vparams.copyInto(aparams);
	    vvalues.copyInto(avalues);
	}
    }

    protected void updateByteValue() {
	HttpBuffer buf = new HttpBuffer();
	buf.append(type.toString());
	if ( aparams != null ) {
	    for (int i = 0 ; i < aparams.length ; i++) {
		if ( i > 0 )
		    buf.append(';');
		buf.append(aparams[i], (byte) '=', avalues[i]);
	    }
	}
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    protected void invalideByteValue() {
	super.invalidateByteValue();
	if ( list != null )
	    list.invalidateByteValue();
    }

    public Object getValue() {
	validate();
	return this;
    }

    /**
     * Lookup for the given parameter binding.
     * @return The slot number for this parameter, or <strong>-1</strong>
     * if undefined.
     */

    protected int lookup(String name) {
	if ( aparams == null )
	    return -1;
	for (int i = 0 ; i < aparams.length ; i++) {
	    if ( name.equalsIgnoreCase(aparams[i]) )
		return i;
	}
	return -1;
    }

    /**
     * Get the String value for a parameter.
     * @return The String value for the accept parameter, or 
     * <strong>null</strong> if undefined.
     */

    public String getAcceptParameter(String name) {
	validate();
	int slot = lookup(name);
	return (slot == -1) ? null : avalues[slot];
    }

    /**
     * Set the value of an accept parameter.
     * @param name The name of the accept parameter to set.
     * @param value The value of the accept parameter to set.
     */

    public void setAcceptParameter(String name, String value) {
	validate();
	int slot = lookup(name);
	if ( slot == -1 ) {
	    // Look for a free slot:
	    for (int i = 0 ; i < aparams.length ; i++) {
		if ( aparams[i] == null ) {
		    slot = i;
		    break;
		}
	    }
	    // Do we need to resize ?
	    if ( slot == -1 ) {
		slot = aparams.length;
		String nap[] = new String[slot<<1];
		String nav[] = new String[nap.length];
		System.arraycopy(aparams, 0, nap, 0, slot);
		System.arraycopy(avalues, 0, nav, 0, slot);
		aparams = nap;
		avalues = nav;
	    }
	}
	// Set the slot value:
	invalidateByteValue();
	aparams[slot] = name;
	avalues[slot] = value;
    }

    /**
     * Get this object accepted MIME type.
     * @return The accepted MIME type.
     */

    public MimeType getMimeType() {
	validate();
	return type;
    }

    /**
     * Set the accepted MIME type.
     * @param type The accepted MIME type.
     */

    public void setMimeType(MimeType type) {
	invalidateByteValue();
	this.type = type;
    }

    /**
     * Get the quality of this accept clause.
     * @return A double value, encoding the quality, or <strong>1.0</strong>
     * if undefined.
     */

    public double getQuality() {
	String value = getAcceptParameter("q");
	if ( value == null ) {
	    if (quality != -1 ) {
		return quality;
	    }
	    String strtype    = type.getType();
	    String strsubtype = type.getSubtype();
	    if (strtype.equals("*"))
		return strsubtype.equals("*") ? 0.1 : 0.5 ;
	    return strsubtype.equals("*") ? 0.5 : 1.0;
	}
	try {
	    return Double.valueOf(value).doubleValue();
	} catch (Exception ex) {
	    String msg = "invalid quality value: "+value;
	    throw new HttpInvalidValueException(msg);
	}
    }

    HttpAccept(HttpAcceptList list, byte raw[], int roff, int rlen) {
	this.list    = list;
	this.raw     = raw;
	this.roff    = roff;
	this.rlen    = rlen;
	this.isValid = false;
    }

    HttpAccept(boolean isValid, MimeType type, double quality) {
	this.isValid = isValid;
	setMimeType(type);
	this.quality = quality;
    }

    public HttpAccept() {
	this.isValid = false;
    }

}

// HttpExt.java
// $Id: HttpExtList.java,v 1.1 2010/06/15 12:19:46 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.util.Vector;
import java.util.Enumeration;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 * Parse a comma separated list of Http extension headers.
 */

public class HttpExtList extends BasicValue {

    //
    // Is this list a Mandatory, Optionnal, hop-by-Hop Mandatory or hop-by-Hop
    // Optionnal?

    public static final int MAN      = 0;
    public static final int CMAN     = 1;
    public static final int OPT      = 2;
    public static final int COPT     = 3;

   
    Vector httpexts = null;
    int    manopt   = MAN;

    public int getManOptFlag() {
	return manopt;
    }

    protected void setManOptFlag(int manopt) {
	this.manopt = manopt;
    }

    /**
     * Parse this header value into its various components.
     * @exception HttpParserException if unable to parse.
     */
    protected void parse() 
	throws HttpParserException
    {
	//extension declaration list parser
	ParseState edl = new ParseState(roff, rlen);
	//attr-value pair
	ParseState av  = new ParseState(0, 0);
	//item parser
	ParseState it  = new ParseState(0, 0);

	edl.separator  = (byte) ',';
	edl.spaceIsSep = false;
	av.separator   = (byte) ';';
	av.spaceIsSep  = false;
	it.separator   = (byte) '=';
	it.spaceIsSep = false;

	while ( HttpParser.nextItem(raw, edl) >= 0 ) {
	    av.ioff   = edl.start;
	    av.bufend = edl.end;
	    HttpExt ext = new HttpExt();
	    
	    while ( HttpParser.nextItem(raw, av) >= 0 ) {
		it.ioff   = av.start;
		it.bufend = av.end;
		// attr = value or "absoluteURI" or "field-name"
		boolean unquoted       = HttpParser.unquote(raw, it);
		if ( HttpParser.nextItem(raw, it) < 0 )
		    error("Invalid extension item ["+av.toString(raw)+"]");
		String itemNaturalCase = it.toString(raw);
		String item            = it.toString(raw, true);
		// if (item.charAt(0) == '"') {
		if (unquoted) {
		    // "absoluteURI" or "field-name"
		    ext.setName(itemNaturalCase);
		} else {
		    it.prepare();
		    HttpParser.unquote(raw, it);
		    if ( HttpParser.nextItem(raw, it) < 0 )
			    error("No value for attribute ["+item+"]");
		    if ( item.equals("ns") ) {
			ext.setNamespace(it.toString(raw));
		    } else {
			ext.addDeclExt(itemNaturalCase, it.toString(raw));
		    }
		}
		av.prepare();
	    }
	    edl.prepare();
	    httpexts.addElement(ext);
	}
    }

    protected void updateByteValue() {
	HttpBuffer buf = new HttpBuffer();
	//Dump all extensions declaration
	int len = httpexts.size();
	for (int i=0; i < len; i++) {
	    HttpExt ext = (HttpExt) httpexts.elementAt(i);
	    if (i != 0)
		buf.append(", ");
	    buf.appendQuoted(ext.getName());
	    if (ext.needsHeaders()) {
		buf.append(";");
		buf.append("ns",(byte)'=',ext.getNamespace());
	    }
	    Enumeration e = ext.getDeclExtNames();
	    while (e.hasMoreElements()) {
		String name = (String) e.nextElement();
		buf.append("; ");
		buf.append(name, (byte)'=', ext.getDeclExt(name));
	    }
	}
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    /**
     * Add an Http extension header.
     * @param ext an HttpExt.
     */
    public void addHttpExt(HttpExt ext) {
	validate();
	httpexts.addElement(ext);
    }

    /**
     * Get all Http extensions header.
     * @return an HttpExt array.
     */
    public HttpExt[] getHttpExts() {
	validate();
	HttpExt exts[] = new HttpExt[httpexts.size()];
	httpexts.copyInto(exts);
	return exts;
      }

    public int getLength() {
	validate();
	return httpexts.size();
    }

    /**
     * Get an Http extension header.
     * @param name The extension identifier (AbsoluteURI or field name)
     * @return an HttpExt or <strong>null</strong>.
     */
    public HttpExt getHttpExt(String name) {
	validate();
	for (int i=0; i < httpexts.size(); i++) {
	    HttpExt ext = (HttpExt) httpexts.elementAt(i);
	    if (ext.getName().equals(name))
		return ext;
	}
	return null;
    }

    public Object getValue() {
	return this;
    }

    /**
     * for user.
     * @param exts the HttpExt array.
     */
    public HttpExtList(HttpExt exts[]) {
	this.isValid  = true;
	int len       = exts.length;
	this.httpexts = new Vector(len);
	if (exts != null) {
	    for (int i=0; i < len; i++)
		httpexts.addElement(exts[i]);
	}
    }

    /**
     * Constructor, for User
     * @param old the old Http extension declaration list 
     * If you want to reply the same extensions, use this
     * contructor.
     */
    public HttpExtList(HttpExtList old) {
	this.isValid  = true;
	old.validate();
	int len       = old.httpexts.size();
	this.httpexts = new Vector(len);
	for (int i=0; i < len; i++) {   
	    HttpExt newext = new HttpExt((HttpExt)old.httpexts.elementAt(i));
	    this.httpexts.addElement(newext);
	}
	this.manopt = old.manopt;
    }

    /**
     * for parser only
     */
    protected HttpExtList() {
	this.isValid = false;
	this.httpexts = new Vector(2);
    }

}

// HttpTokenList.java
// $Id: HttpTokenList.java,v 1.1 2010/06/15 12:19:43 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.util.Vector;

/**
 * Parse a comma separated list of tokens.
 */

public class HttpTokenList extends BasicValue {
    /**
     * Convert tokens to lower case.
     */
    protected static final int CASE_LOWER = 0;
    /**
     * Don't touch cases of tokens.
     */
    protected static final int CASE_ASIS  = 1;
    /**
     * Convert case to upper case.
     */
    protected static final int CASE_UPPER = 2;

    protected String tokens[] = null ;
    protected int    casemode = 0;
    /**
     * Parse the byte buffer to build the token list.
     */

    protected void parse() {
	Vector     toks = new Vector(8);
	ParseState ps   = new ParseState();
	ParseState it   = new ParseState();
	
	ps.ioff       = roff;
	ps.bufend     = rlen;
	ps.spaceIsSep = false;
	while(HttpParser.nextItem(raw, ps) >= 0) {
	    it.ioff   = ps.start;
	    it.bufend = ps.end;
	    HttpParser.unquote(raw, it);
	    switch(casemode) {
	      case CASE_LOWER:
		  toks.addElement(it.toString(raw, true));
		  break;
	      case CASE_ASIS:
		  toks.addElement(it.toString(raw));
		  break;
	      case CASE_UPPER:
		  toks.addElement(it.toString(raw, false));
		  break;
	    }
	    ps.prepare();
	}
	tokens = new String[toks.size()];
	toks.copyInto(tokens);
    }

    protected void updateByteValue() {
	HttpBuffer buf = new HttpBuffer();
	if ( tokens != null ) {
	    for (int i = 0 ; i < tokens.length ; i++) {
		if ( i > 0 )
		    buf.append(',');
		buf.append(tokens[i]);
	    }
	}
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    /**
     * Get this token list value.
     * @return A list of tokens, encoded as a String array, or <strong>null
     *    </strong> if undefined.
     */

    public Object getValue() {
	validate();
	return tokens;
    }

    public void setValue(String tokens[]) {
	invalidateByteValue() ;
	this.tokens = tokens ;
	isValid = true ;
    }

    /**
     * Add a token to this token list.
     * @param token The token to add.
     * @param always Always add to the list, even if the token us already 
     * present in the list.
     */

    public void addToken(String token, boolean always) {
	validate();
	// Check if already set:
	if ( ! always ) {
	    if ( tokens != null ) {
		for (int i = 0 ; i < tokens.length ; i++)
		    if ( tokens[i].equals(token) )
			return;
	    }
	}
	// Add it to the token list:
	invalidateByteValue();
	if ( tokens == null ) {
	    tokens    = new String[1];
	    tokens[0] = token;
	} else {
	    String newtoks[] = new String[tokens.length+1];
	    System.arraycopy(tokens, 0, newtoks, 0, tokens.length);
	    newtoks[tokens.length] = token;
	    tokens = newtoks;
	}
    }

    /**
     * Does this token list includes that token ?
     * @param token The token to look for.
     * @return A boolean, <strong>true</strong> if found, 
     * <strong>false</strong> otherwise.
     */

    public boolean hasToken(String token, boolean caseSensitive) {
	validate();
	if ( tokens == null )
	    return false;
	if ( caseSensitive ) {
	    for (int i = 0 ; i < tokens.length ; i++)
		if ( tokens[i].equals(token) )
		    return true;
	} else {
	    for (int i = 0 ; i < tokens.length ; i++) 
		if ( tokens[i].equalsIgnoreCase(token) )
		    return true;
	}
	return false;
    }

    /**
     * Create a parsed token list, for emitting.
     */

    protected HttpTokenList(String tokens[]) {
	this.isValid = true;
	this.tokens  = tokens;
    }

    /**
     * Create a token list from a comma separated list of tokens.
     */

    protected HttpTokenList(String tokens) {
	this.isValid = false;
	this.raw     = new byte[tokens.length()];
	tokens.getBytes(0, raw.length, raw, 0);
	this.roff    = 0;
	this.rlen    = raw.length;
    }

    /**
     * Create an empty token list for parsing.
     */

    protected HttpTokenList() {
	this.isValid = false ;
    }

}

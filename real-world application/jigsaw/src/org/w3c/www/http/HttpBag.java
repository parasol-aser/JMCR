// HttpBag.java
// $Id: HttpBag.java,v 1.1 2010/06/15 12:19:55 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http ;

import java.util.Enumeration;

import org.w3c.util.ArrayDictionary;

/**
 * Internal representation of protocol headers conforming to the bag spec.
 * The <strong>bag</strong> specification is part of the Protocol Extension
 * Protocol defined by w3c, it can be found
 * <a href="http://www.w3.org/hypertext/WWW/TR/WD-http-pep.html>here</a>.
 */

public class HttpBag extends BasicValue {
    boolean         isToplevel = false;
    String          name       = null ;
    ArrayDictionary items      = null;

    protected final void updateByteValue() {
	HttpBuffer buf = new HttpBuffer();
	if ( isToplevel ) {
	    // Dump this bag as a list of bags:
	    Enumeration e = items.elements();
	    boolean     s = true;
	    while ( e.hasMoreElements() ) {
		HttpBag bag = (HttpBag) e.nextElement();
		// Append separator if needed:
		if ( s )
		    s = false;
		else
		    buf.append((byte)',');
		// Dump the bag now:
		bag.appendValue(buf);
	    }
	} else {
	    buf.append((byte) '{');
	    buf.append(name);
	    buf.append((byte) ' ');
	    // Append all items:
	    Enumeration e = items.keys();
	    boolean     s = true;
	    while (e.hasMoreElements()) {
		// Append separator after first item only:
		if ( s ) 
		    s = false;
		else
		    buf.append((byte) ' ');
		// Dump the item:
		String name  = (String) e.nextElement();
		Object value = items.get(name);
		if ( value instanceof Boolean ) {
		    buf.append(name);
		} else if ( value instanceof HttpBag ) {
		    ((HttpBag) value).appendValue(buf);
		} else {
		    String msg = "Invalid bag item value, key=\""+name+"\".";
		    throw new HttpInvalidValueException(msg);
		}
	    }
	    buf.append((byte) '}');
	}
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    /**
     * parse bag.
     * @exception HttpParserException if parsing failed.
     */
    protected HttpBag parseBag(ParseState ps)
	throws HttpParserException
    {
	StringBuffer sb = new StringBuffer();
	int i  = ps.ioff;
	byte b = raw[i];

	// skip spaces, check for opening bracket
	while ((i < ps.bufend) && ((b = raw[i]) <= 32))
	    i++;
	if ( i >= ps.bufend )
	    return null ;
	if (b != '{')
	    error("Invalid Bag format (no {).") ;
	// skip spaces, bag list separators, get the bag name
	for (++i; (i < ps.bufend) && ((b = raw[i]) <= 32) ; i++)
	    ;
	if ( i >= ps.bufend )
	    error("Invalid Bag format (no name).") ;
	while ((i < ps.bufend) && ((b = raw[i]) > 32) && (b != '}')) {
	    sb.append ((char) b);
	    i++;
	}
	HttpBag bag = new HttpBag(true, sb.toString()) ;
	// get items:
	while (i < ps.bufend) {
	    b = raw[i];
	    if ( b <= 32 ) {
		i++;
		continue ;
	    } else if ( b == '}' ) {
		ps.ooff = i+1;
		return bag ;
	    } else if ( b == '{' ) {
		ParseState inc = new ParseState(i, ps.bufend);
		HttpBag subbag = parseBag (inc) ;
		bag.items.put(subbag.name, subbag) ;
		i = inc.ooff;
	    } else if ( b == '\"') {
		// get quoted string (FIXME, escape chars !)
		sb.setLength(0);
		i++;
		while ((i < ps.bufend) && ((b = raw[i]) != '\"')) {
		    sb.append ((char) b) ;
		    i++;
		}
		bag.items.put(sb.toString(), Boolean.TRUE) ;
		i++;
	    } else {
		// get token (FIXME, see token spec and tspecials)
		sb.setLength(0) ;
		while ((i < ps.bufend) && ((b = raw[i]) > 32) && (b != '}')) {
		    sb.append ((char) b) ;
		    i++;
		}
		bag.addItem (sb.toString()) ;
	    }
	}
	return bag ;
    }

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected final void parse()
	throws HttpParserException
    {
	int i = roff;
	// Parses a list of bags:
	isToplevel  = true;
	HttpBag top = this;
	while ( i < rlen ) {
	    switch(raw[i]) {
	      case (byte) '{':
		  ParseState ps  = new ParseState(i, rlen);
		  HttpBag    bag = parseBag(ps);
		  top.items.put(bag.name, bag);
		  i = ps.ooff;
		  break;
	      case (byte)' ':
	      case (byte)'\t':
	      case (byte)',':
		  i++;
		  break;
	      default:
		  error("Unexpected separator \""+raw[i]+"\".");
	    }
	}
    }

    public Object getValue() {
	return this;
    }

    /**
     * Get this bag names
     * @return The name of this bag.
     */

    public String getName() {
	validate();
	return name ;
    }

    /**
     * Add a named bag into this bag.
     * @param bag The bag to add (in case item is a bag).
     */

    public void addBag (HttpBag subbag) {
	validate();
	items.put (subbag.getName(), subbag) ;
    }

    /**
     * Does this bag have a named sub-bag of the given name ?
     * @param name The name of the sub-bag to be tested for.
     * @return <strong>true</strong> if this sub-bag exists.
     */

    public boolean hasBag (String name) {
	validate();
	Object item = items.get (name) ;
	if ( (item != null) && (item instanceof HttpBag) )
	    return true ;
	return false ;
    }

    /**
     * Get a named sub-bag from this bag.
     * @param name The name of the sub-bag to get.
     * @return A bag instance, or <strong>null</strong> if none was found.
     */

    public HttpBag getBag (String name) {
	validate();
	if (hasBag (name))
	    return (HttpBag) items.get (name) ;
	return null ;
    }

    /**
     * Add an item into this bag.
     * @param name The new item name.
     */

    public void addItem (String name) {
	validate();
	items.put (name, Boolean.TRUE) ;
    }

    /**
     * Add a sub-bag to this bag.
     * @param subbag The sub-bag to add.
     */

    public void addItem(HttpBag subbag) {
	validate();
	items.put(subbag.getName(), subbag);
    }

    /**
     * Does this bag contains the given item, being a bag or a simple word.
     * @param name The name of the item to test.
     * @return <strong>true</strong> if the bag contains the given item, 
     *   <strong>false</strong> otherwise.
     */

    public boolean hasItem (String name) {
	validate();
	return items.get (name) != null ;
    }

    /**
     * Remove an item from that bag.
     * @param name The name of the item to remove.
     */

    public void removeItem(String name) {
	validate();
	items.remove(name);
    }

    /**
     * Get all named items from this bag. 
     * This include both named sub-bags and items by their own.
     */

    public Enumeration keys () {
	validate();
	return items.keys() ;
    }

    /**
     * Is that a top level bag or not ?
     * @return A boolean, <strong>true</strong> if the bag was a toplevel
     * bag.
     */

    public boolean isToplevelBag() {
	return isToplevel;
    }

    HttpBag() {
	isValid    = false;
	this.items = new ArrayDictionary(5, 5);
    }

    HttpBag(boolean isValid, String name) {
	this.isValid = isValid;
	this.name    = name ;
	this.items   = new ArrayDictionary(5, 5);
    }
}

// HeaderDescription.java
// $Id: HeaderDescription.java,v 1.2 2010/06/15 17:53:01 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

public class HeaderDescription {
    Class  cls     = null ;	// Class of parser to use.
    String name    = null ; 	// lower case name.
    byte   title[] = null;	// title for header emitting.
    int    offset  = -1;	// Offset of header in its message holder.

    /**
     * Get this header name, lower case (can be used as header id).
     * @return A String giving the header identifier.
     */

    public String getName() {
	return name;
    }

    /**
     * Get this header title, ready for emission.
     * @return The actual bytes to be emited for this header title.
     */

    public byte[] getTitle() {
	return title;
    }

    /**
     * Get this header parser, as an HeaderValue compatible instance.
     * @return An instance of HeaderValue, suitable for holding and parsing
     * the header value.
     */

    public HeaderValue getHolder() {
	try {
 	    return (HeaderValue) cls.newInstance();
	} catch (NoSuchMethodError er) {
	    throw new RuntimeException("Invalid class (method) for "+name);
	} catch (InstantiationError ex) {
	    throw new RuntimeException("Invalid class (method) for "+name);
	} catch (Exception ex) {
	    throw new RuntimeException("Invalid class for "+name);
	}
    }

    /**
     * Is this header description the one of that header.
     * @param h The header access token.
     */

    public boolean isHeader(int h) {
	return h == offset;
    }

    HeaderDescription(String title, String clsname, int offset) {
	try {
	    this.title  = new byte[title.length()];
	    title.getBytes(0, this.title.length, this.title, 0);
	    this.name   = title.toLowerCase();
	    this.offset = offset;
	    this.cls    = Class.forName(clsname);
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    // This is a nasty kludge due to a bug in jdk1.2 on NT
	    // this.cls = Class.forName(clsname, true,
	    //                          this.getClass().getClassLoader());
	    // works and not
	    // this.cls    = Class.forName(clsname);
	    // even if it is exactly the same thing :)
	    // and the creation of a new object is enough to make it run...
	    this.cls.newInstance();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException("Invalid header description "+name);
	}

    }

    HeaderDescription(String title, String clsname) {
	this(title, clsname, -1);
    }

}

// MimeHeaders.java
// $Id: MimeHeaders.java,v 1.1 2010/06/15 12:26:32 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mime;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The most stupid MIME header holder.
 * This class uses a hashtable mapping header names (as String), to header
 * values (as String). Header names are lowered before entering the hashtable.
 */

public class MimeHeaders implements MimeHeaderHolder {
    Hashtable headers = null;
    MimeParser parser = null;

    /**
     * A new header has been parsed.
     * @param name The name of the encountered header.
     * @param buf The byte buffer containing the value.
     * @param off Offset of the header value in the above buffer.
     * @param len Length of the value in the above header.
     * @exception MimeParserException if the parsing failed
     */

    public void notifyHeader(String name, byte buf[], int off, int len)
	throws MimeParserException
    {
	String lname  = name.toLowerCase();
	String oldval = null;
	if ( headers == null ) {
	    headers = new Hashtable(5);
	} else {
	    oldval = (String) headers.get(lname);
	}
	String newval = ((oldval != null) 
			 ? oldval + "," + new String(buf, 0, off, len)
			 : new String(buf, 0, off, len));
	headers.put(lname, newval);
    }

    /**
     * The parsing is now about to start, take any appropriate action.
     * This hook can return a <strong>true</strong> boolean value to enforce
     * the MIME parser into transparent mode (eg the parser will <em>not</em>
     * try to parse any headers.
     * <p>This hack is primarily defined for HTTP/0.9 support, it might
     * also be usefull for other hacks.
     * @param parser The Mime parser.
     * @return A boolean <strong>true</strong> if the MimeParser shouldn't
     * continue the parsing, <strong>false</strong> otherwise.
     * @exception IOException if an IO error occurs.
     */

    public boolean notifyBeginParsing(MimeParser parser)
	 throws IOException
    {
	return false;
    }

    /**
     * All the headers have been parsed, take any appropriate actions.
     * @param parser The Mime parser.
     * @exception IOException if an IO error occurs.
     */

    public void notifyEndParsing(MimeParser parser)
	 throws IOException
    {
	return;
    }

    /**
     * Set a header value.
     * @param name The header name.
     * @param value The header value.
     */

    public void setValue(String name, String value) {
	if ( headers == null )
	    headers = new Hashtable(5);
	headers.put(name.toLowerCase(), value);
    }

    /**
     * Retreive a header value.
     * @param name The name of the header.
     * @return The value for this header, or <strong>null</strong> if 
     * undefined.
     */

    public String getValue(String name) {
	return ((headers != null)
		? (String) headers.get(name.toLowerCase())
		: null);
    }

    /**
     * Enumerate the headers defined by the holder.
     * @return A enumeration of header names, or <strong>null</strong> if no
     * header is defined.
     */

    public Enumeration enumerateHeaders() {
	if ( headers == null )
	    return null;
	return headers.keys();
    }

    /**
     * Get the entity stream attached to these headers, if any.
     * @return An InputStream instance, or <strong>null</strong> if no
     * entity available.
     */

    public InputStream getInputStream() {
	return ((parser != null) ? parser.getInputStream() : null);
    }

    /**
     * Dump all headers to the given stream.
     * @param out The stream to dump to.
     */

    public void dump(PrintStream out) {
	Enumeration names = enumerateHeaders();
	if ( names != null ) {
	    while (names.hasMoreElements()) {
		String name = (String) names.nextElement();
		out.println(name+": "+headers.get(name));
	    }
	}
    }

    public MimeHeaders(MimeParser parser) {
	this.parser = parser;
    }

    public MimeHeaders() {
    }

}

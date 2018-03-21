// DAVParser.java
// $Id: DAVParser.java,v 1.1 2010/06/15 12:27:42 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav;

import java.io.InputStream;
import java.io.IOException;

import org.w3c.www.http.HttpInvalidValueException;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVParser {

    private static final boolean debug = false;

    /**
     * Emit an error.
     * @param mth The method trigerring the error.
     * @param msg An associated message.
     * @exception HttpInvalidValueException To indicate the error to caller.
     */

    protected static void error(String mth, String msg) 
	throws HttpInvalidValueException
    {
	throw new HttpInvalidValueException(mth+": "+msg);
    }

    /**
     * Skip leading LWS, <em>not</em> including CR LF.
     * Update the input offset, <em>after</em> any leading space.
     * @param buf The buffer to be parsed.
     * @param ptr The buffer pointer to be updated on return.
     * @return The potentially advanced buffer input offset.
     */

    public static final int skipSpaces(byte buf[], ParseState ps) {
	int len = (ps.bufend > 0) ? ps.bufend : buf.length;
	int off = ps.ioff;
	while (off < len) {
	    if ((buf[off] != (byte) ' ') 
		&& (buf[off] != (byte) '\t')
		&& (buf[off] != (byte) ps.separator)) {
		ps.ioff = off;
		return off;
	    }
	    off++;
	}
	return off;
    }

    public static final boolean startsWith(byte buf[], ParseState ps, char c) {
	return (buf[ps.start] == (byte) c);
    }

    /**
     * Parse list of items, taking care of quotes and optional LWS.
     * The output offset points to the <em>next</em> element of the list.
     * @return The starting location (i.e. <code>ps.start</code> value), or
     * <strong>-1</strong> if no item available (end of list).
     */

    public static final int nextItem(byte buf[], ParseState ps) {
	// Skip leading spaces, if needed:
	int off = -1;
	int len = -1;
	if ( ps.isSkipable ) 
	    ps.start = off = skipSpaces(buf, ps) ;
	else 
	    ps.start = off = ps.ioff ;
	len = (ps.bufend > 0) ? ps.bufend : buf.length;
	if ( debug )
	    System.out.println("parsing: ["+new String(buf, 0, off, len-off)+
			       "]");
	// Parse !
	if ( off >= len )
	    return -1;
	// Setup for parsing, and parse
	ps.start = off;
    loop:
	while (off < len) {
	    if ( buf[off] == (byte) '"' ) {
		// A quoted item, read as one chunk
		off++;
		while (off < len ) {
		    if (buf[off] == (byte) '\\') {
			off += 2;
		    } else if (buf[off] == (byte) '"') {
			off++;
			continue loop;
		    } else {
			off++;
		    }
		}
		if ( off == len )
		    error("nextItem", "Un-terminated quoted item.");
	    } else if ((buf[off] == ps.separator)
		       || (ps.spaceIsSep 
			   && ((buf[off] == ' ') || (buf[off] == '\t')))) {
		break loop;
	    }
	    off++;
	}
	ps.end = off;
	// Item start is set, we are right at the end of item
	if ( ps.isSkipable ) {
	    ps.ioff = off ;
	    ps.ooff = skipSpaces(buf, ps);
	}
	// Check for either the end of the list, or the separator:
	if (ps.ooff < ps.bufend) {
	    if (buf[ps.ooff] == (byte) ps.separator)
		ps.ooff++;
	}
	if ( debug ) 
	    System.out.println("nextItem = ["+new String(buf, 0, ps.start,
							 ps.end-ps.start)+"]");
	return (ps.end > ps.start) ? ps.start : -1;
    }

    /**
     * <URI> -> URI
     */
    public static final String decodeURL(String encoded) 
	throws HttpInvalidValueException
    {
	int idx1 = encoded.indexOf('<');
	int idx2 = encoded.indexOf('>');
	if (idx1 == -1 || idx2 == -1) {
	    throw new HttpInvalidValueException(encoded);
	}
	return encoded.substring(idx1+1, idx2);
    }

    /**
     * [ETag] -> ETag
     */
    public static final String decodeETag(String encoded) 
	throws HttpInvalidValueException
    {
	int idx1 = encoded.indexOf('[');
	int idx2 = encoded.indexOf(']');
	if (idx1 == -1 || idx2 == -1) {
	    throw new HttpInvalidValueException(encoded);
	}
	return encoded.substring(idx1+1, idx2);
    }

}

// MimeHeaderHolder.java
// $Id: MimeHeaderHolder.java,v 1.1 2010/06/15 12:26:30 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mime;

import java.io.IOException;

public interface MimeHeaderHolder {

    /**
     * A new header has been parsed.
     * @param name The name of the encountered header.
     * @param buf The byte buffer containing the value.
     * @param off Offset of the header value in the above buffer.
     * @param len Length of the value in the above header.
     * @exception MimeParserException if the parsing failed
     */

    public void notifyHeader(String name, byte buf[], int off, int len)
	throws MimeParserException;

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
     * @exception MimeParserException if the parsing failed
     * @exception IOException if an IO error occurs.
     */

    public boolean notifyBeginParsing(MimeParser parser)
	 throws MimeParserException, IOException;

    /**
     * All the headers have been parsed, take any appropriate actions.
     * @param parser The Mime parser.
     * @exception MimeParserException if the parsing failed
     * @exception IOException if an IO error occurs.
     */

    public void notifyEndParsing(MimeParser parser)
	 throws MimeParserException, IOException;

}

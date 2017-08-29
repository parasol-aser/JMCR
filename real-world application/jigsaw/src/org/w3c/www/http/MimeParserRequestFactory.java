// MimeParserRequestFactory.java
// $Id: MimeParserRequestFactory.java,v 1.1 2010/06/15 12:19:50 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import org.w3c.www.mime.MimeHeaderHolder;
import org.w3c.www.mime.MimeParser;
import org.w3c.www.mime.MimeParserFactory;

/**
 * The MIME parser factory for HTTP requests.
 */

public class MimeParserRequestFactory implements MimeParserFactory {

    /**
     * Create a new HTTP request to hold the parser's result.
     * @param parser The parser that has something to parse.
     * @return A MimeParserHandler compliant object.
     */

    public MimeHeaderHolder createHeaderHolder(MimeParser parser) {
	return new HttpRequestMessage(parser) ;
    }
}

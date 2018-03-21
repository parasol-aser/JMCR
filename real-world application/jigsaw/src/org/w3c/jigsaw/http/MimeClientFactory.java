// MimeClientFactory.java
// $Id: MimeClientFactory.java,v 1.1 2010/06/15 12:21:57 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigsaw.http;

import org.w3c.www.mime.MimeHeaderHolder;
import org.w3c.www.mime.MimeParser;
import org.w3c.www.mime.MimeParserException;
import org.w3c.www.mime.MimeParserFactory;

/**
 * The Mime factory for creating requests out of the client transport streams.
 * This factory creates instances of <code>org.w3c.jigsaw.http.Request</code>
 * a server-specific subclass of the generic <code>org.w3c.www.http.HttpRequest
 * </code> class.
 * @see org.w3c.www.http.HttpRequest
 */

class MimeClientFactory implements MimeParserFactory {
    Client client = null;

    public MimeHeaderHolder createHeaderHolder(MimeParser parser) {
	return new Request(client, parser);
    }

    MimeClientFactory(Client client) {
	this.client = client;
    }
}

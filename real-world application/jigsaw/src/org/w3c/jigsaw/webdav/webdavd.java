// webdavd.java
// $Id: webdavd.java,v 1.1 2010/06/15 12:29:41 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigsaw.webdav;

import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.http.Client;

import org.w3c.www.mime.MimeParserFactory;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class webdavd extends httpd {
    
    protected MimeParserFactory getMimeClientFactory(Client client) {
	return new DAVMimeClientFactory(client);
    }

}

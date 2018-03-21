// PICS.java
// $Id: PICS.java,v 1.1 2010/06/15 12:25:30 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pics ;

import org.w3c.www.http.HttpBag;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpMimeType;

import org.w3c.www.mime.MimeType;
import org.w3c.www.mime.MimeTypeFormatException;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class PICS {
    /**
     * The PICS protocol version that this filter handles.
     */
    public  static final String PICS_PROTOCOL_ID = "PICS-1.1" ;

    /**
     * The PICS mime type
     */
    public static HttpMimeType APPLICATION_PICSLABEL = null;

    /**
     * The bag describing the PICS extension:
     */
    public static HttpBag PICS_EXTENSION = null;

    private static boolean debug = false;

    static {
	try {
	    MimeType type = new MimeType("application/pics-label");
	    APPLICATION_PICSLABEL = HttpFactory.makeMimeType(type);
	} catch (MimeTypeFormatException ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}

	HttpBag headers = HttpFactory.makeBag("headers");
	headers.addItem("PICS-label");
	PICS_EXTENSION = HttpFactory.makeBag(PICS.PICS_PROTOCOL_ID);
	PICS_EXTENSION.addBag(headers);

    }

    public static boolean debug() {
	return debug;
    }

    public static void setDebug(boolean onoff) {
	debug = onoff;
    }

}

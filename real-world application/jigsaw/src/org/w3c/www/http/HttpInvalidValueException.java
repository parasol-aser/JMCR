// HttpInvalidValueException.java
// $Id: HttpInvalidValueException.java,v 1.1 2010/06/15 12:19:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

public class HttpInvalidValueException extends RuntimeException {

    public HttpInvalidValueException(String msg) {
	super(msg);
    }

}

// HttpExtException.java
// $Id: HttpExtException.java,v 1.1 2010/06/15 12:19:50 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class HttpExtException extends RuntimeException {

    protected HttpExtException(String msg) {
	super(msg);
    }

    protected HttpExtException() {
	super();
    }
}

// IllegalFieldValueException.java
// $Id: IllegalFieldValueException.java,v 1.1 2010/06/15 12:27:23 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.forms ;

/**
 * This exception is thrown when a field is set to an invalid value.
 */

public class IllegalFieldValueException extends Exception {
    IllegalFieldValueException (Object arg) {
	super("illegal value: "+arg) ;
    }
}

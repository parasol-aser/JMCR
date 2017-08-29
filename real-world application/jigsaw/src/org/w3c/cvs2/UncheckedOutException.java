// UncheckedOutException.java
// $Id: UncheckedOutException.java,v 1.1 2010/06/15 12:28:50 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs2 ;

/**
 * This exception is used whenever an abnormal situation in CVS processing
 * is encountered.
 */

public class UncheckedOutException extends CvsException {

    UncheckedOutException (String msg) {
	super (msg) ;
    }
}

   
	

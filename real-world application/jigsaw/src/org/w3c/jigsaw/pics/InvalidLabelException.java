// InvalidLabelException.java
// $Id: InvalidLabelException.java,v 1.1 2010/06/15 12:25:28 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pics ;

public class InvalidLabelException extends Exception {

    public InvalidLabelException (String msg) {
	super (msg) ;
    }

    public InvalidLabelException (int lineno, String msg) {
      this ("[" + lineno + "]" + ": " + msg) ;
    }
}

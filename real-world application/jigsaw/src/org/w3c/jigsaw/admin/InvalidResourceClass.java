// InvalidResourceClass.java 
// $Id: InvalidResourceClass.java,v 1.1 2010/06/15 12:24:27 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.admin;

/**
 * The exception for invalid classes.
 */

public class InvalidResourceClass extends Exception {

    public InvalidResourceClass(String msg) {
	super(msg);
    }

}

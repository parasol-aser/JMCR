// SSIException.java
// $Id: SSIException.java,v 1.1 2010/06/15 12:26:36 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi ;		

/**
 * Internal exceptions in the SSIFrame
 * @author Antonio Ramirez <anto@mit.edu>
 */ 
class SSIException extends Exception {
    public SSIException(String msg)
    {
	super(msg);
    }
}



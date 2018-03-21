// HTTPRuntimeException.java
// $Id: HTTPRuntimeException.java,v 1.1 2010/06/15 12:22:01 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http ;

/**
 * HTTP runtime exception.
 * These exeptions should be thrown whenever as a programmer you encounter
 * an abnormal situation. These exception are guaranted to be catched, and to
 * only kill the client (if this makes sense) that triggered it.
 */

public class HTTPRuntimeException extends RuntimeException {

    /**
     * Create a new HTTPRuntime exception. This is the right way to throw 
     * runtime exceptions from the http server, since it is the one that is 
     * likely to provide most usefull informations.
     * @param o The object were the error originated.
     * @param mth The method were the error originated.
     * @param msg An message explaining why this error occured.
     */

    public HTTPRuntimeException (Object o, String mth, String msg) {
	super (o.getClass().getName()
	       + "[" + mth + "]: "
	       + msg) ;
    }

}

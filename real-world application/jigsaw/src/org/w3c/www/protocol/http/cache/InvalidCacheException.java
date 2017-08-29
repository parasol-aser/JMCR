// InvalidCacheException.java
// $Id: InvalidCacheException.java,v 1.1 2010/06/15 12:25:11 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache;

public class InvalidCacheException extends Exception {

    public InvalidCacheException(String msg) {
	super(msg);
    }
}

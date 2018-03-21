// InvalidResourceException.java
// $Id: InvalidResourceException.java,v 1.1 2010/06/15 12:20:23 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources;

/**
 * The resource is no more a valide resource.
 */

public class InvalidResourceException extends Exception {

    public InvalidResourceException(String id, String msg) {
	super("["+id+"] loadResource failed: "+msg);
    }

    public InvalidResourceException(String parent, 
				    String child,
				    String msg) {
      super("["+parent+" , "+child+"] registerResource failed: "+msg);
    }
}



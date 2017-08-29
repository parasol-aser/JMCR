// MultipleLockException.java
// $Id: MultipleLockException.java,v 1.1 2010/06/15 12:20:22 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources;

/**
 * The resource store is unable to restore a resource.
 */

public class MultipleLockException extends Exception {

  int nb = 0;

  public int getNbLock() {
    return nb;
  }

  public MultipleLockException(int nb, Resource resource, String msg) {
    super(nb+" locks on "+resource.getIdentifier()+" "+msg);
    this.nb = nb;
  }

}



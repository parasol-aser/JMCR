// CommitHandler.java
// $Id: CommitHandler.java,v 1.1 2010/06/15 12:28:52 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs2;

abstract class CommitHandler {
    abstract void notifyEntry(String filename, int status);
}

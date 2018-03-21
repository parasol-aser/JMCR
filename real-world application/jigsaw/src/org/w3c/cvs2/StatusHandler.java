// StatusHandler.java
// $Id: StatusHandler.java,v 1.1 2010/06/15 12:28:48 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs2;

abstract class StatusHandler implements CVS {

    // for parser, notify that a new cvs entry is there
    // filename, revision and sticky options (for now)
    abstract void notifyEntry(String filename, String revision, String st_opt);

}

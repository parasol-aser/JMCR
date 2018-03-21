// CvsStatusHandler.java
// $Id: CvsStatusHandler.java,v 1.1 2010/06/15 12:28:49 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs2;

import java.util.Enumeration;
import java.util.Vector;

class CvsStatusHandler extends StatusHandler {

    class RevisionEntry {
        String file   = null;
        String rev    = null;
	String st_opt = null;
        RevisionEntry (String file, String rev, String st_opt) {
	    this.file   = file;
	    this.rev    = rev;
	    this.st_opt = st_opt;
	}
    }

    CvsDirectory cvs         = null;
    Vector       rentries     = null;

    void notifyEnd() {
	Enumeration renum = rentries.elements();
	while (renum.hasMoreElements()) {
	    RevisionEntry rentry = (RevisionEntry) renum.nextElement();
	    // Add an entry for the file:
	    CvsEntry entry = cvs.getFileEntry(rentry.file);
	    if ( entry != null ) {
		entry.setRevision(rentry.rev);
		entry.setStickyOptions(rentry.st_opt);
	    }
	}
    }

//    void notifyEntry(String filename, String revision) {
//	rentries.addElement( new RevisionEntry(filename, revision, null));
//    }

    void notifyEntry(String filename, String revision, String st_opt) {
	rentries.addElement( new RevisionEntry(filename, revision, st_opt));
    }

    CvsStatusHandler(CvsDirectory cvs) {
	this.cvs   = cvs;
	rentries   = new Vector(10);
    }
}

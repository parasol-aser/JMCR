// DirectoryUpdateHandler.java
// $Id: DirectoryUpdateHandler.java,v 1.1 2010/06/15 12:28:52 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs2;

import java.io.File;

class DirectoryUpdateHandler extends UpdateHandler implements CVS {
    CvsDirectory cvs     = null;
    long         stamp   = -1;

    void notifyEntry(String filename, int status) {
	// Look for the appropriate CVS manager:
	File         file  = new File(cvs.getDirectory(), filename);
	File         dir   = new File(file.getParent());
	String       name  = file.getName();
	CvsDirectory child = null;
	try {
	    child = CvsDirectory.getManager(cvs, dir);
	} catch (CvsException ex) {
	    return;
	}
	// Add an entry for the file:
	CvsEntry entry = child.getFileEntry(name);
	if ( entry == null ) 
	    child.createFileEntry(stamp, name, status);
	else
	    entry.setStatus(stamp, status);
    }

    DirectoryUpdateHandler(CvsDirectory cvs) {
	this.cvs   = cvs;
	this.stamp = System.currentTimeMillis();
    }
}

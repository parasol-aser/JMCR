// CvsUpdateHandler.java
// $Id: CvsUpdateHandler.java,v 1.1 2010/06/15 12:28:49 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs2;

class CvsUpdateHandler extends UpdateHandler implements CVS {
    CvsDirectory cvs     = null;
    long         stamp   = -1;

    void notifyEntry(String filename, int status) {
	// Add an entry for the file:
	CvsEntry entry = cvs.getFileEntry(filename);
	if ( entry == null ) 
	    cvs.createFileEntry(stamp, filename, status);
	else
	    entry.setStatus(stamp, status);
    }

    CvsUpdateHandler(CvsDirectory cvs) {
	this.cvs   = cvs;
	this.stamp = System.currentTimeMillis();
    }
}

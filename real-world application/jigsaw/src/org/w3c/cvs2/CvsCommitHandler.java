// CvsCommitHandler.java
// $Id: CvsCommitHandler.java,v 1.1 2010/06/15 12:28:48 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs2;

class CvsCommitHandler extends CommitHandler implements CVS {
    CvsDirectory cvs     = null;
    long         stamp   = -1;

    void notifyEntry(String filename, int status) {
	// Add an entry for the file:
	CvsEntry entry = cvs.getFileEntry(filename);
	//added there for testing (FIXME?)
	//stamp = System.currentTimeMillis();
	if ( entry == null ) 
	    cvs.createFileEntry(stamp, filename, status);
	else
	    entry.setStatus(stamp, status);
    }

    CvsCommitHandler(CvsDirectory cvs) {
	this.cvs   = cvs;
	this.stamp = System.currentTimeMillis();
    }
}

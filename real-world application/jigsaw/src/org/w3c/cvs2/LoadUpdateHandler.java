// LoadUpdateHandler.java
// $Id: LoadUpdateHandler.java,v 1.1 2010/06/15 12:28:51 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs2;

import java.io.File;
import java.io.FilenameFilter;

class LoadUpdateHandler extends UpdateHandler implements CVS {
    CvsDirectory cvs     = null;
    String       files[] = null;
    long         stamp   = -1;

    void notifyEnd() {
	// All remaining files in directory are in sync with rep:
	for (int i = 0 ; i < files.length ; i++) {
	    if ( files[i] != null ) 
		cvs.createFileEntry(stamp, files[i], FILE_OK);
	}
    }

    void notifyEntry(String filename, int status) {
	// We're only interested in knowing about files here:
	File file = new File(cvs.getDirectory(), filename);
	if ( file.isDirectory() )
	    return;
	// We are not really performing the update, so...
	if ( status == FILE_OK ) 
	    status = file.exists() ? FILE_U : FILE_NCO;
	// Add an entry for the file:
	cvs.createFileEntry(stamp, filename, status);
	// Remove the file from the directory listing (it's handled now)
	for (int i = 0 ; i < files.length ; i++) {
	    if ( files[i] == null )
		continue;
	    if ( files[i].equals(filename) ) {
		files[i] = null;
		return;
	    }
	}
    }

    LoadUpdateHandler(CvsDirectory cvs) {
	this.cvs   = cvs;
	this.files = cvs.getDirectory().list(new FileFilter());
	this.stamp = System.currentTimeMillis();
    }
}

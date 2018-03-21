// FileFilter.java
// $Id: FileFilter.java,v 1.1 2010/06/15 12:28:50 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs2;

import java.io.File;
import java.io.FilenameFilter;

class FileFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
	boolean reject = (name.startsWith(".")
			  || name.endsWith("~")
			  || (new File(dir, name)).isDirectory());
	return ! reject;
    }
}

// DirectoryFilter.java
// $Id: DirectoryFilter.java,v 1.1 2010/06/15 12:28:51 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs2;

import java.io.File;
import java.io.FilenameFilter;

class DirectoryFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
	return (( ! name.equals("CVS"))
		&& ( ! name.equals("Attic"))
		&& (new File(dir, name)).isDirectory());
    }

    DirectoryFilter() {
	super();
    }

}

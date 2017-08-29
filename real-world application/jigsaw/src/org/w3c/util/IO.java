// IO.java
// $Id: IO.java,v 1.1 2010/06/15 12:25:39 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class IO {

    /**
     * Copy source into dest.
     */
    public static void copy(File source, File dest) 
	throws IOException
    {
	BufferedInputStream in = 
	    new BufferedInputStream( new FileInputStream(source) );
	BufferedOutputStream out =
	    new BufferedOutputStream( new FileOutputStream(dest) );
	byte buffer[] = new byte[1024];
	int read = -1;
	while ((read = in.read(buffer, 0, 1024)) != -1)
	    out.write(buffer, 0, read);
	out.flush();
	out.close();
	in.close();
    }

    /**
     * Copy source into dest.
     */
    public static void copy(InputStream in, OutputStream out) 
	throws IOException
    {
	BufferedInputStream bin = 
	    new BufferedInputStream(in);
	BufferedOutputStream bout =
	    new BufferedOutputStream(out);
	byte buffer[] = new byte[1024];
	int read = -1;
	while ((read = bin.read(buffer, 0, 1024)) != -1)
	    bout.write(buffer, 0, read);
	bout.flush();
	bout.close();
	bin.close();
    } 

    /**
     * Delete recursivly
     * @param file the file (or directory) to delete.
     */
    public static boolean delete(File file) {
	if (file.exists()) {
	    if (file.isDirectory()) {
		if (clean(file)) {
		    return file.delete();
		} else {
		    return false;
		}
	    } else {
		return file.delete();
	    }
	}
	return true;
    }

    /**
     * Clean recursivly
     * @param file the directory to clean
     */
    public static boolean clean(File file) {
	if (file.isDirectory()) {
	    String filen[] = file.list();
	    for (int i = 0 ; i < filen.length ; i++) {
		File subfile = new File(file, filen[i]);
		if ((subfile.isDirectory()) && (! clean(subfile))) {
		    return false;
		} else if (! subfile.delete()) {
		    return false;
		}
	    }
	}
	return true;
    }

}

// PageCompileFile.java
// $Id: PageCompileFile.java,v 1.1 2010/06/15 12:29:29 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pagecompile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class PageCompileFile {

    private byte[] filedata = null;

    protected void readFileData(String filename) 
	throws IOException
    {
	File file = new File(filename);
	ByteArrayOutputStream out =
	    new ByteArrayOutputStream((int)file.length()) ;
	
	FileInputStream in = new FileInputStream(file);

	byte[] buf = new byte[4096] ;
	int len = 0;
	
	while( (len = in.read(buf)) != -1) 
	    out.write(buf,0,len);
	
	in.close();
	out.close();
	
	filedata = out.toByteArray() ;
    }

    /**
     * Write some bytes from this file in the given output stream.
     * @param start start position in the file
     * @param end end position in the file
     * @param out the destination output stream
     * @exception IOException if an IO error occurs
     */
    public void writeBytes(int start, int end, OutputStream out) 
	throws IOException
    {
	int len = end - start + 1;
	byte b[] = new byte[len];
	if (start+len > filedata.length)
	    len--;
	System.arraycopy(filedata, start, b, 0, len);
	out.write(b);
    }

    /**
     * Create a PageCompileFile.
     * @param filename the filename
     * @exception IOException if an IO error occurs.
     */
    public PageCompileFile (String filename) 
	throws IOException
    {
	readFileData(filename);
    }
}

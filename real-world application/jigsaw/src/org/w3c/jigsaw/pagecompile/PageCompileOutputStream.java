// PageCompileutStream.java
// $Id: PageCompileOutputStream.java,v 1.1 2010/06/15 12:29:30 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pagecompile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class PageCompileOutputStream extends ByteArrayOutputStream {

    public void writeBytes(String s) 
	throws IOException
    {
	write(s.getBytes());
    }

    public void print(int i)
	throws IOException
    {
	writeBytes(Integer.toString(i));
    }

    public void print(double i)
	throws IOException
    {
	writeBytes(Double.toString(i));
    }

    public void print(long l)
	throws IOException
    {
	writeBytes(Long.toString(l));
    }

    public void print(String s) 
	throws IOException 
    {
	writeBytes(s);
    }

    public void println() 
	throws IOException
    {
	writeBytes("\r\n");
    }

    public void println(int i)
	throws IOException
    {
	print(i); println();
    }

    public void println(double i)
	throws IOException
    {
	print(i); println();
    }

    public void println(long l) 
	throws IOException
    {
	print(l); println();
    }

    public void println(String s)
	throws IOException
    {
	print(s); println();
    }

    InputStream getInputStream() {
	return new ByteArrayInputStream(toByteArray(), 0 , size());
    }

}

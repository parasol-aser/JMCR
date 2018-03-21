// ZipInputStream.java
// $Id: ZipInputStream.java,v 1.1 2010/06/15 12:28:24 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigsaw.zip;

import java.io.IOException;
import java.io.InputStream;

import java.util.zip.ZipFile;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ZipInputStream extends InputStream {

    private InputStream in;
    private ZipFile     zip;

    public int available() 
	throws IOException
    {
	return in.available();
    }

    public void close() 
	throws IOException
    {
	try {
	    in.close();
	} finally {
	    zip.close();
	}
    }

    public synchronized void mark(int readlimit) {
	in.mark(readlimit);
    }

    public synchronized void reset() 
	throws IOException
    {
	in.reset();
    }

    public boolean markSupported() {
	return in.markSupported();
    }

    public int read() 
	throws IOException
    {
	return in.read();
    }

    public int read(byte b[]) 
	throws IOException
    {
	return in.read(b);
    }

    public int read(byte b[], int off, int len) 
	throws IOException
    {
	return in.read(b,off,len);
    }

    public long skip(long n) 
	throws IOException
    {
	return in.skip(n);
    }

    protected ZipInputStream(ZipFile zip, InputStream in) {
	this.zip = zip;
	this.in  = in;
    }
}

// JigsawServletInputStream.java
// $Id: JigsawServletInputStream.java,v 1.1 2010/06/15 12:24:10 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

/**
 *  @author Alexandre Rafalovitch <alex@access.com.au>
 *  @author Anselm Baird-Smith <abaird@w3.org>
 */

class JigsawServletInputStream extends ServletInputStream {
    InputStream in = null;

    public int read() 
	throws IOException
    {
	return in.read();
    }

    public int read(byte b[]) 
	throws IOException 
    {
	return in.read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len)
	throws IOException
    {
	return in.read(b, off, len);
    }

    public long skip(long n) 
	throws IOException 
    {
	return in.skip(n);
    }

    public int available()
	throws IOException 
    {
	return in.available();
    }

    public void close()
	throws IOException
    {
	in.close();
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

    public int readLine(byte b[], int off, int len)
	throws IOException
    {
	int got = 0;
	while (got < len) {
	    int ch = in.read();
	    switch(ch) {
	      case -1:
		  return -1;
// 	      case '\r':
	      case '\n':
		  b[off+got] = (byte) (ch&0xff);
		  got++;
// 		  in.mark(1);
// 		  if ((ch = in.read()) != '\n' )
// 		      in.reset();
		  return got;
	      default:
		  b[off+got] = (byte) (ch&0xff);
		  got++;
	    }
	}
	return got;
    }

    JigsawServletInputStream(InputStream in) {
	this.in = in;
    }

	 
}



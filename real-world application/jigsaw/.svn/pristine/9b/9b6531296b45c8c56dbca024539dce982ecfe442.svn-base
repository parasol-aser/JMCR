// ByteRangeOutputStream.java
// $Id: ByteRangeOutputStream.java,v 1.1 2010/06/15 12:19:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http ;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class ByteRangeOutputStream extends InputStream {
    int              firstp = -1;
    int              lastp  = -1;
    InputStream in = null;
//    RandomAccessFile in     = null;

    public int read()
	throws IOException
    {
	if ( firstp < lastp ) {
	    firstp++;
	    return in.read();
//	    return ((int) in.readByte()) & 0xff;
	}
	return -1;
    }

    public int read(byte b[]) 
	throws IOException
    {
	return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) 
	throws IOException
    {
	if (firstp < lastp) {
	    int send = Math.min(lastp-firstp, len);
	    send    = in.read(b, off, send);
	    firstp += send;
	    return send;
	}
	return -1;
    }

    public void close()
	throws IOException
    {
	in.close();
    }

    public int available() {
	return lastp-firstp;
    }

    public ByteRangeOutputStream(File file, int firstp, int lastp)
	throws IOException
    {
	this.firstp = firstp;
	this.lastp  = lastp;
	RandomAccessFile raf = new RandomAccessFile(file, "r");
	raf.seek((long) firstp);
	this.in = new FileInputStream(raf.getFD());
    }

    public ByteRangeOutputStream(InputStream in, int firstp, int lastp)
	throws IOException
    {
	this.firstp = firstp;
	this.lastp  = lastp;
	this.in = in;
	in.skip((long)firstp);
    }
}

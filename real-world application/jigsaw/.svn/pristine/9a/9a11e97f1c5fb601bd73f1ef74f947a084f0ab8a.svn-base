// CountInputStream.java
// $Id: CountInputStream.java,v 1.1 2010/06/15 12:25:40 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2001.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.util;

import java.io.InputStream;
import java.io.IOException;

/**
 * count the number of bytes read through the stream
 */
public class CountInputStream extends InputStream {
    long count = 0;
    long marked = -1;

    InputStream is;

    public int available()
	throws IOException
    {
	return is.available();
    }

    public boolean markSupported() {
	return is.markSupported();
    }

    public int read()
	throws IOException
    {
	int r = is.read();
	if (r > 0) {
	    count++;
	}
	return r;
    }
    
    public int read(byte[] b, int off, int len)
	throws IOException
    {
	int r = is.read(b, off, len);
	if (r > 0) {
	    count += r;
	}
	return r;
    }

    public long skip(long skipped) 
	throws IOException
    {
	long l = is.skip(skipped);
	if (l > 0) {
	    count += l;
	}
	return l;
    }
    
    public void mark(int readlimit) {
	is.mark(readlimit);
	marked = count;
    }

    public void reset()
	throws IOException
    {
	is.reset();
	count = marked;
    }

    public void close() 
	throws IOException
    {
	is.close();
    }

    /**
     * get the actual number of bytes read
     * @return a long, the number of bytes read
     */
    public long getBytesRead() {
	return count;
    }

    public CountInputStream(InputStream is) {
	this.is = is;
    }
}

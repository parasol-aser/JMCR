// SocketOutputBuffer.java
// $Id: SocketOutputBuffer.java,v 1.1 2010/06/15 12:26:09 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http.socket;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A very specific class to buffer output sent back to the client.
 * None of the Java base class are adequat, in particular, 
 * BufferedOutputStream has to inconvenient:
 * <ul>
 * <li>It enforces buffer reallocation on each new connection.
 * <li>It is not very smart with flushing the stream (it flushes it whenever
 * output exceeds buffer size, which triggers a reply header flush before
 * any data bytes can be added to the packet).
 */

class SocketOutputBuffer extends FilterOutputStream {
    protected byte buf[] = null;
    protected int  count = 0;

    public void close()
	throws IOException
    {
	try {
	    try { flush();     } catch (Exception ex) {}
	    try { out.close(); } catch (Exception ex) {}
	} finally {
	    out   = null;
	    count = 0;
	}
    }

    public void flush()
	throws IOException
    {
	if ( count > 0 ) {
	    out.write(buf, 0, count);
	    count = 0;
	}
    }

    public void write(byte b[])
	throws IOException
    {
	write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) 
	throws IOException
    {
	int avail = buf.length - count;
	if ( len < avail ) {
	    System.arraycopy(b, off, buf, count, len);
	    count += len;
	    return;
	} else if ((avail > 0) && (count > 0)) {
	    System.arraycopy(b, off, buf, count, avail);
	    count += avail;
	    flush();
	    out.write(b, off+avail, len-avail);
	} else {
	    flush();
	    out.write(b, off, len);
	}
    }

    public void write(int b)
	throws IOException
    {
	if ( count == buf.length )
	    flush();
	buf[count++] = (byte) b;
    }
	
    public void reuse(OutputStream out) {
	this.out   = out;
	this.count = 0;
    }

    public SocketOutputBuffer(OutputStream out) {
	this(out, 512);
    }

    public SocketOutputBuffer(OutputStream out, int size) {
	super(out);
	this.buf   = new byte[size];
	this.count = 0;
    }
}

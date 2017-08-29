// JigsawServletOutputStream.java
// $Id: JigsawServletOutputStream.java,v 1.1 2010/06/15 12:24:13 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

import org.w3c.jigsaw.http.Reply;

/**
 * A Bufferred ServletOutputStream.
 * @author Benoît Mahé <bmahe@w3.org>
 */

class JigsawServletOutputStream extends ServletOutputStream {

    DataOutputStream          out   = null;
    JigsawHttpServletResponse resp  = null;
    Reply                     reply = null;

    byte    buffer[]   = null;
    int     count      = 0;
    boolean committed  = false;
    boolean writerUsed = false;

    byte ln[] = { (byte)'\r' , (byte)'\n' };

    /**
     * Flush the internal buffer.
     */
    private void flushBuffer() 
	throws IOException
    {
	if (!committed) {
	    resp.notifyClient();
	}
	committed = true;
	if (out == null) {
	    if (reply != null)
		out = new DataOutputStream(reply.getOutputStream());
	}
	if (count > 0) {
	    out.write(buffer, 0, count);
	    count = 0;
	}
    }

    public void print(int i)
	throws IOException
    {
	write(i);
    }

    public void print(double i)
	throws IOException
    {
	print(Double.toString(i));
    }

    public void print(long l)
	throws IOException
    {
	print(Long.toString(l));
    }

    public void print(String s) 
	throws IOException 
    {
	write(s.getBytes());
    }

    public void println() 
	throws IOException
    {
	write(ln);
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

    public void write(int b) 
	throws IOException 
    {
	write((byte)b);
    }

    protected void write(byte b) 
	throws IOException 
    {
	if (count >= buffer.length) {
	    flushBuffer();
	}
	buffer[count++] = b;
    }

    public void write(byte b[]) 
	throws IOException 
    {
	write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) 
	throws IOException 
    {
	if (len >= buffer.length) {
	    flushBuffer();
	    out.write(b, off, len);
	    return;
	}
	if (len > buffer.length - count) {
	    flushBuffer();
	}
	System.arraycopy(b, off, buffer, count, len);
	count += len;
    }

    public void flush() 
	throws IOException 
    {
	if (! writerUsed) {
	    flushBuffer();
	    out.flush();
	}
    }

    public void realFlush() 
	throws IOException 
    {
	flushBuffer();
	out.flush();
    }

    public void close() 
	throws IOException 
    {
	flushBuffer();
	out.close();
    }

    public void reset() 
	throws IllegalStateException
    {
	if (committed) {
	    throw new IllegalStateException("Response already committed");
	}
	// empty the buffer
	count = 0;
    }

    public boolean isCommitted() {
	return committed;
    }

    /**
     * Creates a new buffered JigsawServletOutputStream.
     * @param resp the Response
     * @param out The underlying output stream
     * @param bufsize the buffer size
     * @exception IllegalArgumentException if bufsize <= 0.
     */
    JigsawServletOutputStream(JigsawHttpServletResponse resp,
			      DataOutputStream out,
			      int bufsize,
			      boolean writerUsed)
    {
	this.out        = out;
	this.resp       = resp;
	this.writerUsed = writerUsed;
	if (bufsize <= 0) {
	    throw new IllegalArgumentException("Buffer size <= 0");
	}
	this.buffer    = new byte[bufsize];
	this.count     = 0;
	this.committed = false;
    }

    /**
     * Creates a new buffered JigsawServletOutputStream.
     * @param resp the Response
     * @param reply the Jigsaw reply
     * @param bufsize the buffer size
     * @exception IllegalArgumentException if bufsize <= 0.
     */
    JigsawServletOutputStream(JigsawHttpServletResponse resp, 
			      Reply reply,
			      int bufsize,
			      boolean writerUsed)
    {
	this.resp       = resp;
	this.reply      = reply;
	this.writerUsed = writerUsed;
	if (bufsize <= 0) {
	    throw new IllegalArgumentException("Buffer size <= 0");
	}
	this.buffer    = new byte[bufsize];
	this.count     = 0;
	this.committed = false;
    }

}



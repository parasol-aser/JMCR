// ChunkedInputStream.java
// $Id: ChunkedInputStream.java,v 1.1 2010/06/15 12:19:48 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * A Stream that parses and present chunk encoded data.
 * This stream should only be used on top of a buffered input stream, it might
 * be very inefficient otherwise.
 * Chunk encoding is defined in version 1.1 of the HTTP specification.
 */

public class ChunkedInputStream extends InputStream {
    protected InputStream in  = null;
    protected HttpBuffer  buf = null;
    protected ParseState  ps  = null;
    protected boolean     inited = false;

    protected int clen = -1;	// Remaining bytes to read for current chunk
    protected int ahead = -1;   // Any look ahead byte
    protected boolean isahead = false;

    protected boolean eof = false;

    protected HttpStreamObserver observer = null;

    private final void checkInit() 
	throws IOException
    {
	if ( ! inited ) {
	    clen   = nextChunk(false);
	    inited = true;
	}
    }

    /**
     * Read in next chunk description.
     * Sets the eof flag to <strong>true</strong> when reached.
     * @return The length of next incomming chunk of data.
     */

    protected int nextChunk(boolean skipCRLF) 
	throws IOException
    {
	if ( eof )
	    return 0;
	int ch = -1;
	buf.reset();
	if ( skipCRLF ) {
	    ch = in.read();	// '\r'
	    ch = in.read();	// '\n'
	}
    loop:
	while (true) {
	    ch = in.read();
	    switch(ch) {
	      case -1:
		  throw new IOException("Premature end of chunked stream.");
	      case '\r':
		  if ((ch = in.read()) != '\n') {
		      ahead = ch ;
		      isahead = true;
		  }
		  break loop;
	      case '\n':
		  break loop;
	      default:
		  buf.append(ch);
	    }
	}
	// Parse the buffer content as an hexa number:
	ps.ioff   = 0;
	ps.bufend = buf.length();
	int len = HttpParser.parseInt(buf.getBytes(), 16, ps);
	eof = (len == 0);
	return len;
    }

    public void close() 
	throws IOException 
    {
	checkInit();
	if ( observer != null ) {
	    observer.notifyClose(this);
	    observer = null;
	}
    }

    public int read() 
	throws IOException
    {
	checkInit();
	if (clen == 0) {
	    if (eof || ((clen = nextChunk(true)) == 0)) {
		if ( observer != null ) {
		    observer.notifyEOF(this);
		    observer = null;
		}
		return -1;
	    }
	    if ( isahead ) {
		clen--;
		isahead = false;
		return ahead;
	    }
	} 
	clen--;
	return in.read();
    }

    public int read(byte b[], int off, int len) 
	throws IOException
    {
	checkInit();
	if ( eof ) {
	    if ( observer != null ) {
		observer.notifyEOF(this);
		observer = null;
	    }
	    return -1;
	}
	if ( clen > len ) {
	    // More data available then requested:
	    int cnt = in.read(b, off, len);
	    if(cnt == -1) {
		observer.notifyFailure(this);
		observer = null;
		throw new IOException("Chunked stream read aborted ("+clen
				      +" remaining in chunk)");
	    }
	    clen -= cnt;
	    return cnt;
	} else {
	    int copied = 0;
	    while ( len > 0 ) {
		// Check for available data:
		if ( clen == 0 ) {
		    // End of data ?
		    if (eof || ((clen = nextChunk(true)) == 0)) {
			if ( observer != null ) {
			    observer.notifyEOF(this);
			    observer = null;
			}
			return (copied == 0) ? -1 : copied;
		    } else if (isahead) {
			b[off++] = (byte) (ahead & 0xff);
			len--;
			clen--;
			isahead = false;
		    }
		}
		// Read in available data:
		int got = in.read(b, off, Math.min(clen, len));
		if(got == -1) {
		    observer.notifyFailure(this);
		    observer = null;
		    throw new IOException("Chunked stream read aborted"); 
		}
		copied += got;
		len  -= got;
		clen -= got;
		off  += got;
	    }
	    return copied;
	}
    }

    public int available() 
	throws IOException
    {
	checkInit();
	return eof ? -1 : 1;
    }

    public long skip(long n) 
	throws IOException
    {
	checkInit();
	byte buf[] = new byte[512];
	int  cnt   = Math.min(buf.length, (int) n);
	while ((cnt = read(buf, 0, cnt)) > 0) {
	    n -= cnt;
	    cnt = Math.min(buf.length, (int) n);
	}
	return n;
    }

    /**
     * Make sure the stream is ultimately closed !
     */

    public void finalize() {
	try {
	    close();
	} catch (IOException ex) {
	}
    }

    public ChunkedInputStream(InputStream in) 
	throws IOException
    {
	this(null, in);
    }

    public ChunkedInputStream(HttpStreamObserver observer, InputStream in)
	throws IOException
    {
	this.observer = observer;
	this.buf  = new HttpBuffer();
	this.ps   = new ParseState();
	this.in   = in;
    }
}

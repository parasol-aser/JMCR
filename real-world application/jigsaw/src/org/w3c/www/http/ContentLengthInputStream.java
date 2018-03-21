// ContentLengthInputStream.java
// $Id: ContentLengthInputStream.java,v 1.1 2010/06/15 12:19:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * The content length input stream is used internally to return entity bodies.
 */

public class ContentLengthInputStream extends InputStream {
    /**
     * The original input stream.
     */
    protected InputStream in = null;
    /**
     * The stream observer, if any.
     */
    protected HttpStreamObserver observer = null;

    /**
     * The number of bytes readable from the above stream.
     */
    protected int length = 0 ;
    /**
     * The place of a single pending mark.
     */
    protected int marklength = 0;

    public void mark(int readlimit) {
	in.mark(readlimit);
	marklength = length;
    }

    public void reset() 
	throws IOException
    {
	in.reset();
	length = marklength;
    }

    public synchronized void close() 
	throws IOException 
    {
	if ( observer != null ) {
	    observer.notifyClose(this);
	    observer = null;
	} 
    }

    public int read()
	throws IOException 
    {
	if ( length > 0 ) {
	    length--;
	    // notify if the is nothing more to read
	    // FIXME is it the right place for this?
	    if ( length <= 0 ) {
		if ( observer != null )
		    observer.notifyEOF(this);
		observer = null;
	    }
	    int r = in.read();
	    if (r == -1) {
		// mark end of stream in error
		length = -1;
		if ( observer != null ) {
		    observer.notifyFailure(this);
		}
		observer = null;
		throw new IOException("Size not matching: "+length+" left");
	    }
	    return r;
	}
	if ( observer != null ) {
	    observer.notifyEOF(this);
	    observer = null;
	}
	return -1 ;
    }

    public int read (byte b[], int off, int len)
	throws IOException 
    {
	if ( length <= 0 ) {
	    if ( observer != null )
		observer.notifyEOF(this);
	    observer = null;
	    return -1 ;
	}
	if ( len > length ) 
	    len = length ;
	len     = in.read (b, off, len) ;
	if ( len == -1 ) { // error code, dont let length grow
	    length = -1; // a bad thing happened anyway.
	    if ( observer != null )
		observer.notifyFailure(this);
	    observer = null;
	    throw new IOException("Size not matching");
	}
	length -= len ;
	if ( length <= 0 ) { // nothing to read, notify the observer (if any)
	    if ( observer != null )
		observer.notifyEOF(this);
	    observer = null;
	}
	return len ;
    }

    public long skip (long n) 
	throws IOException 
    {
	int howmany = (int) n ;
	if (howmany > length)
	    howmany = length ;
	howmany = (int) in.skip (howmany) ;
	length -= howmany ;
	return (long) howmany ;
    }

    public int available ()
	throws IOException
    {
	int _av = in.available();
	if (_av > length) {
	    return length;
	}
	return _av;
	//return length ;
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

    /**
     * do we support mark() ?
     * it depends on the underlying InputStream
     * @return a boolean, true if mark is supported
     */
    public boolean markSupported() {
	if( in == null )
	    return false;
	else
	    return in.markSupported();
    }

    /**
     * Builds a new content-length input stream.
     * This stream acts as a normal stream except that it will return 
     * an end of file, after <em>count</em> bytes have been delivered.
     */

    public ContentLengthInputStream (InputStream in, int length) {
	this(null, in, length);
    }

    public ContentLengthInputStream(HttpStreamObserver observer
				    , InputStream in
				    , int length) {
	this.observer = observer;
	this.in       = in;
	this.length   = length;
    }
	    
}

// SSIStream.java
// $Id: SSIStream.java,v 1.1 2010/06/15 12:26:36 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi ;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;

import org.w3c.jigsaw.http.ClientException;
import org.w3c.jigsaw.http.Reply;

import org.w3c.jigsaw.ssi.commands.ControlCommandException;

/**
 * This stream concatenates the output streams of each of the segments.
 * (It absorbs IOExceptions, so that a failure of a segment doesn't
 * stop the emission).
 * @author Antonio Ramirez <anto@mit.edu>
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

class SSIStream extends FilterInputStream {

    private Segment[] segments ;
    private Reply[] replies ;
    private RandomAccessFile file ;

    private int curSeg = 0 ;
    protected boolean cacheValid = true;

    private boolean nextSegment()
    {
	try {
	    if(in != null) in.close() ;
	} catch(IOException ex) {
	    // nil
	}
	while(curSeg < segments.length) { //-1
	    Segment seg = segments[curSeg] ;
	    if(seg.isControl()) { 
		if(SSIFrame.debug)
		    System.out.println("@@@@ feeding control segment "+
				       curSeg+": "+
				       segments[curSeg]) ;
		try {
		    curSeg = segments[curSeg].jumpTo(); 
		} catch (ControlCommandException ex) {
		    System.out.println(ex.getMessage());
		    ex.printStackTrace();
		    return false;
		}
	    } else  if(!seg.isUnparsed()) { 
		if (! cacheValid) {
		    if(SSIFrame.debug)
			System.out.println("@@@@ revalidate cache segment "+
					   curSeg+": "+
					   segments[curSeg]) ;
		    replies[curSeg] = segments[curSeg].get();
		} else if (segments[curSeg].needsRevalidate()) {
		    replies[curSeg] = segments[curSeg].get();
		}
		if(replies[curSeg] != null) {
		    in = replies[curSeg].openStream() ;
		    if(in != null) {
			if(SSIFrame.debug)
			    System.out.println("@@@@ feeding command segment "+
					       curSeg+": "+
					       segments[curSeg]) ;
			curSeg++;
			return true ;
		    } else if (SSIFrame.debug) {
			System.out.println("@@@@ not feeding command segment "+
					   curSeg+": "+
					   segments[curSeg]) ;
		    }
		}
		curSeg++; 
	    } else {
		if(SSIFrame.debug)
		    System.out.println("@@@@ feeding Unparsed segment "+
				       curSeg+": "+
				       segments[curSeg]) ;
		try {
		    in = new SegmentInputStream(file,
						seg.start,
						seg.end - seg.start) ;
		    curSeg++;
		    return true ;
		} catch(IOException ex) {
		    // nil
		}
	    }
	}

	in = null ;
	if(SSIFrame.debug)
	    System.out.println("@@@@ no more segments") ;
	return false ;
    }

    public SSIStream(boolean cacheValid,
		     Segment[] segments,
		     Reply[] replies,
		     RandomAccessFile file)
	throws IOException, ClientException
    {
	super((InputStream) null) ;

	this.segments = segments ;
	this.replies = replies ;
	this.file = file ;
	this.cacheValid = cacheValid ;

	nextSegment() ;
    }

    public int read()
	throws IOException
    {
	int data = -1 ;
	try {
	    data = in.read() ;
	} catch(IOException ex) {
	    if(SSIFrame.debug)	
		System.out.println("@@@@ absorbed exception: "+
				   ex.getMessage()) ;
	    data = -1 ;
	} finally {
	    if(data != -1) return data ;
	    else {
		if(!nextSegment()) return -1 ;
		else return read() ;
	    }
	} 
    }

    public int read(byte b[],int off, int len)
	throws IOException
    {
	int result = -1 ;
	try {
	    result = in.read(b,off,len) ;
	} catch(IOException ex) {
	    if(SSIFrame.debug)
		System.out.println("@@@@ absorbed exception: "+
				   ex.getMessage()) ;
	    result = -1 ;
	} finally {
	    if(result != -1) return result ;
	    else {
		if(!nextSegment()) return -1 ;
		else return read(b,off,len) ;
	    }
	}
    }

    public long skip(long n)
	throws IOException
    {
	return in.skip(n) ;
    }

    public int available()
	throws IOException
    {
	return in.available() ;
    }

    public void close()
	throws IOException
    {
	if(in != null) in.close() ;
	file.close();
    }

    public synchronized void mark()
    {
	// nil
    }

    public synchronized void reset()
	throws IOException
    {
	throw new IOException("mark not supported") ;
    }

    public boolean markSupported()
    {
	return false ;
    }
}

/**
 * Provides an unparsed segment in the input file as an InputStream
 */
class SegmentInputStream extends InputStream {

    private RandomAccessFile file ;

    private long bytesLeft ;

    SegmentInputStream(RandomAccessFile file, long start, long length)
	throws IOException
    {
	this.file = file ;
	file.seek(start) ;
	bytesLeft = length ;
    }

    public final void close()
	throws IOException
    {
	// nil
    }

    public final int read()
	throws IOException
    {
	if(bytesLeft>0) {
	    bytesLeft--;
	    return file.read() ;
	} else return -1 ;
    }

    public final int read(byte b[], int off, int len)
	throws IOException
    {
	if(bytesLeft==0) return -1 ;
	if(len > bytesLeft)
	    len = (int) bytesLeft ;
	file.readFully(b,off,len) ;
	bytesLeft -= len ;
	return len ;
    }

    public final int read(byte b[])
	throws IOException
    {
	return this.read(b,0,b.length) ;
    }

    public final void reset()
	throws IOException
    {
	throw new IOException("mark not supported") ;
    }

    public final void mark(int readlimit)
    {
	// nil
    }

    public final boolean markSupported()
    {
	return false ;
    }

    public final long skip(long n)
	throws IOException
    {
	if(n>bytesLeft) n = bytesLeft ;
	return file.skipBytes((int)n) ; // hmm...
    }

    public final int available() {
	return (int) bytesLeft;
    }

}



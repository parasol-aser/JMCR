// DelayedInputStream.java
// $Id: DelayedInputStream.java,v 1.1 2010/06/15 12:26:37 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi ;

import java.io.IOException;
import java.io.InputStream;

/**
 * Used to delay the (perhaps expensive) creation of a real stream
 * until the first access. 
 * @author Antonio Ramirez <anto@mit.edu>
 */

public abstract class DelayedInputStream extends InputStream
{

    /**
     * The InputStream that data will be really read from.
     */
    protected InputStream in = null ;

    /**
     * This method is called on the first access to the stream.
     * (<em>Not</em> at construction time.) Should initialize
     * <code>in</code> as a valid stream. Must <em>not</em> make it
     * <strong>null</strong>.
     */
    protected abstract void init() ;

    public final void close()
	throws IOException
    {
	if(in!=null) in.close() ;
    }

    public final int read()
	throws IOException 
    {
	if(in == null) init() ;
	return in.read() ;
    }

    public final int read(byte b[], int off, int len)
	throws IOException 
    {
	if(in == null) init() ;
	return in.read(b,off,len) ;
    }

    public final int read(byte b[])
	throws IOException
    {
	if(in == null) init() ;
	return in.read(b) ;
    }

    public final void reset()
	throws IOException
    {
	if(in == null) init() ;
	in.reset() ;
    }

    public final void mark(int readlimit)
    {
	if(in == null) init() ;
	in.mark(readlimit) ;
    }

    public final boolean markSupported()
    {
	if(in == null) init() ;
	return in.markSupported() ;
    }

    public final long skip(long n)
	throws IOException
    {
	if(in == null) init() ;
	return in.skip(n) ;
    }

    public final int available()
	throws IOException
    {
	if (in == null) init();
	return in.available();
    }

}    

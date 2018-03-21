// MSFileInputStream.java
// $Id: MSFileInputStream.java,v 1.1 2010/06/15 12:25:39 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FilterInputStream;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class MSFileInputStream extends FilterInputStream {

    private File file = null;

    protected int readlimit = -1;

    protected int count   = 0;
    protected int markpos = 0;

    /**
     * Tests if this input stream supports the <code>mark</code> 
     * and <code>reset</code> methods. The <code>markSupported</code> 
     * method of <code>FilterInputStream</code> calls the 
     * <code>markSupported</code> method of its underlying input stream 
     * and returns whatever value that method returns. 
     *
     * @return  always true.
     * @since   JDK1.0
     */
    public boolean markSupported() {
	return true;
    }

    /**
     * Marks the current position in this input stream. A subsequent 
     * call to the <code>reset</code> method repositions this stream at 
     * the last marked position so that subsequent reads re-read the same 
     * bytes. 
     * <p>
     * The <code>readlimit</code> arguments tells this input stream to 
     * allow that many bytes to be read before the mark position gets 
     * invalidated. 
     * <p>
     *
     * @param   readlimit   the maximum limit of bytes that can be read before
     *                      the mark position becomes invalid.
     * @see     java.io.InputStream#reset()
     * @since   JDK1.0
     */
    public synchronized void mark(int readlimit) {
	this.readlimit  = readlimit;
	this.markpos    = count;
    }

    /**
     * Repositions this stream to the position at the time the 
     * <code>mark</code> method was last called on this input stream. 
     * <p>
     * Stream marks are intended to be used in
     * situations where you need to read ahead a little to see what's in
     * the stream. Often this is most easily done by invoking some
     * general parser. If the stream is of the type handled by the
     * parser, it just chugs along happily. If the stream is not of
     * that type, the parser should toss an exception when it fails,
     * which, if it happens within readlimit bytes, allows the outer
     * code to reset the stream and try another parser.
     *
     * @exception  IOException  if this stream has not been marked or if the
     *               mark has been invalidated.
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.IOException
     * @since   JDK1.0
     */
    public synchronized void reset() 
	throws IOException 
    {
	if (markpos < 0) {
	    throw new IOException("Resetting to invalid mark");
	}
	if (count-markpos > readlimit) {
	    throw new IOException("Read limit reached, invalid mark");
	}
	in.close();
	in = new FileInputStream(file);
	if (markpos > 0) {
	    in.skip(markpos);
	}
	markpos = 0;
	count   = 0;
    }

    /**
     * Reads the next byte of data from this input stream. The value 
     * byte is returned as an <code>int</code> in the range 
     * <code>0</code> to <code>255</code>. If no byte is available 
     * because the end of the stream has been reached, the value 
     * <code>-1</code> is returned. This method blocks until input data 
     * is available, the end of the stream is detected, or an exception 
     * is thrown. 
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     * @since      JDK1.0
     */
    public int read() 
	throws IOException 
    {
	int read = in.read();
	if (read != -1) {
	    count++;
	}
	return read;
    }

    /**
     * Reads up to <code>byte.length</code> bytes of data from this 
     * input stream into an array of bytes. This method blocks until some 
     * input is available. 
     *
     * @param      b   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#read(byte[], int, int)
     * @since      JDK1.0
     */
    public int read(byte b[]) 
	throws IOException 
    {
	int read = in.read(b, 0, b.length);
	if (read != -1) {
	    count += read;
	}
	return read;
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input stream 
     * into an array of bytes. This method blocks until some input is 
     * available. 
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     * @since      JDK1.0
     */
    public int read(byte b[], int off, int len) 
	throws IOException 
    {
	int read = in.read(b, off, len);
	if (read != -1) {
	    count += read;
	}
	return read;
    }


    /**
     * Creates an input file stream to read from the specified file descriptor.
     *
     * @param      fdObj   the file descriptor to be opened for reading.
     * @exception  SecurityException  if a security manager exists, its
     *               <code>checkRead</code> method is called with the file
     *               descriptor to see if the application is allowed to read
     *               from the specified file descriptor.
     * @see        java.lang.SecurityManager#checkRead(java.io.FileDescriptor)
     * @since      JDK1.0
     */
    public MSFileInputStream(File file) 
	throws FileNotFoundException
    {
	super(new FileInputStream(file));
	this.file = file;
    }

    /**
     * Creates an input file stream to read from a file with the 
     * specified name. 
     *
     * @param      name   the system-dependent file name.
     * @exception  FileNotFoundException  if the file is not found.
     * @exception  SecurityException      if a security manager exists, its
     *               <code>checkRead</code> method is called with the name
     *               argument to see if the application is allowed read access
     *               to the file.
     * @see        java.lang.SecurityManager#checkRead(java.lang.String)
     * @since      JDK1.0
     */
    public MSFileInputStream(String name) 
	throws FileNotFoundException
    {
	super(new FileInputStream(name));
	this.file = new File(name);
    }

}

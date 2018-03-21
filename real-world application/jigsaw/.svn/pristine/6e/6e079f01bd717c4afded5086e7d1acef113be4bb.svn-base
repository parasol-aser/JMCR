// ChunkedOutputStream.java
// $Id: ChunkedOutputStream.java,v 1.1 2010/06/15 12:19:48 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ChunkedOutputStream extends DataOutputStream {

    private final static byte hexaTable[] = { 
	(byte) '0', (byte) '1', (byte) '2', (byte) '3',
	(byte) '4', (byte) '5', (byte) '6', (byte) '7',	 
	(byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
	(byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F' };
    private static final byte crlf[]      = { (byte) 13, (byte) 10 };
    private static final byte zeroChunk[] = { (byte) 48, (byte) 13, (byte) 10};
    private static final int DEFAULT_CHUNK_SIZE = 512;

    /**
     * The chunking buffer.
     */
    protected byte buffer[] = null;
    /**
     * Where to put next piece of data (current chunk size).
     */
    protected int bufptr = 0;
    /**
     * The chunk size to use (defaults to buffer size).
     */
    protected int chunksize = -1;
    /**
     * Internal buffer to hold chunk size.
     */
    protected byte bheader[] = new byte[32];

    /**
     * Send the close chunk.
     * @Exception IOException If writing fails.
     */

    protected void sendClose()
	throws IOException
    {
	out.write(zeroChunk, 0, 3) ;
	out.write(crlf, 0, 2) ;
	out.flush() ;
    }

    /**
     * Send given buffer as a full chunk.
     * @param b The buffer that contains the data to emit.
     * @param off Offset of chunk in above buffer.
     * @param len Length of chunk.
     * @exception IOException If writing fails.
     */

    protected void sendChunk(byte b[], int off, int len)
	throws IOException
    {
	// Anything to send ?
	if ( len == 0 ) 
	    return;
	// Send one chunk:
	int size = len;
	int blen = 3;
	// we dump the hexa size of the header backward
	bheader[30] = ((byte) 13); // \r
	bheader[31] = ((byte) 10); // \n
	while (size > 15) {
	    bheader[32-blen] = hexaTable[size % 16];
	    size >>= 4;
	    blen++;
	}
	bheader[32-blen] = hexaTable[size];
	out.write(bheader, 32-blen, blen) ;
	out.write(b, off, len) ;
	out.write(crlf, 0, 2) ;
	out.flush() ;
    }

    /**
     * Send current chunk of data.
     * @exception IOException If writing fails.
     */

    protected void sendChunk() 
	throws IOException
    {
	if ( bufptr == 0 )
	    return;
	sendChunk(buffer, 0, bufptr);
	bufptr = 0;
    }

    /** 
     * Append one byte to pending chunk.
     * @param v The byte to append.
     * @exception IOException If writing fails.
     */

    protected final void append(int v) 
	throws IOException
    {
	if ( bufptr + 1 >= chunksize )
	    sendChunk();
	buffer[bufptr++] = (byte) (v & 0xff);
    }

    /**
     * Append a bunch of bytes to current pending chunk.
     * @param b The chunk of bytes to add.
     * @param off Offset of chunk within above buffer.
     * @param len Length of chunk.
     * @exception IOException If writing fails.
     */

    protected final void append(byte b[], int off, int len) 
	throws IOException
    {
	if ( bufptr + len >= chunksize )
	    sendChunk();
	if ( len < buffer.length ) {
	    System.arraycopy(b, off, buffer, bufptr, len);
	    bufptr += len;
	} else {
	    sendChunk(b, off, len);
	}
    }

    /**
     * Close that encoding stream.
     * @exception IOException If writing fails.
     */

    public void close() 
	throws IOException
    {
	close(true);
    }

    /**
     * Close that encoding stream.
     * @exception IOException If writing fails.
     */
    public void close(boolean really)
	throws IOException
    {
	if (really) {
	    sendChunk();
	    sendClose();
	    super.close();
	} else {
	    sendChunk();
	    sendClose();
	}
    }
    
    /**
     * Flush pending output.
     * @exception IOException If writing fails.
     */

    public void flush()
	throws IOException
    {
	sendChunk();
	super.flush();
    }

    /**
     * Write one byte of output.
     * @param v The byte to write.
     * @exception IOException If writing fails.
     */

    public void write(int b) 
	throws IOException
    {
	append(b);
    }

    /**
     * Write an array of bytes.
     * @param b The data to write.
     * @param off Offfset within above buffer.
     * @param len Length of data to write.
     * @exception IOException If writing fails.
     */

    public void write(byte b[], int off, int len)
	throws IOException
    {
	append(b, off, len);
    }

    /**
     * Create a chunk encoder, using the provided buffer.
     * @param buffer The buffer to use (determines the default chunk size).
     * @param put The DataOutputStream to write encoded data to.
     */

    public ChunkedOutputStream(byte buffer[], DataOutputStream out) {
	super(out);
	this.buffer    = buffer;
	this.chunksize = buffer.length;
    }

    /**
     * Create a chunk encoder.
     * @param out The DataOutputStream to write to.
     */

    public ChunkedOutputStream(DataOutputStream out) {
	this(new byte[DEFAULT_CHUNK_SIZE], out);
    }

}

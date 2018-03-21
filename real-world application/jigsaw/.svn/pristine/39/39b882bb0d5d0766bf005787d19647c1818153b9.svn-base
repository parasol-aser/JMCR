// JpegCOmmentWriter.java
// $Id: JpegCommentWriter.java,v 1.1 2010/06/15 12:29:18 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.jpeg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Allow you to write text comments to jpeg stream
 * Some code has been adapted from wrjpgcom.c from The Independent JPEG Group
 */
public class JpegCommentWriter extends Writer {

    // usual debug stuff
    private static final boolean debug = true;

    InputStream inJpegData;
    OutputStream outJpegData;
    ByteArrayOutputStream byteStream;
    OutputStreamWriter osw;
    boolean init;
    int lastMarker;

    /**
     * Create a JpegCommentWriter, using an Input stream as the jpeg binary
     * source, and writing in the output stream
     * @param out, the output stream where the image will be written
     * @param in, the input stream of the jpeg file, it MUST point to the
     * beginning of the jpeg to avoid problems
     */
    public JpegCommentWriter(OutputStream out, InputStream in) {
	super(out);
	inJpegData = in;
	outJpegData = out;
	byteStream = new ByteArrayOutputStream(65536);
	osw = new OutputStreamWriter(byteStream);
	init = false;
    }

    /**
     * Create a JpegCommentWriter, using an Input stream as the jpeg binary
     * source, and writing in the output stream
     * @param out, the output stream where the image will be written
     * @param in, the input stream of the jpeg file, it MUST point to the
     * beginning of the jpeg to avoid problems
     * @param enc, the encoding name used when you write comments
     */
    public JpegCommentWriter(OutputStream out, InputStream in, String enc)
	throws UnsupportedEncodingException
    { 
	super(out);
	inJpegData = in;
	outJpegData = out;
	byteStream = new ByteArrayOutputStream(65536);
	osw = new OutputStreamWriter(byteStream, enc);
	init = false;
    } 

    /**
     * gets the encoding used by the comment writer
     */
    public String getEncoding() {
	return osw.getEncoding();
    }

    /**
     * write one character
     */
    public void write(int ch) 
	throws IOException
    {
	osw.write(ch);
    }

    /**
     * write an array of characters
     */
    public void write(char[] buffer)
	throws IOException
    {
	osw.write(buffer);
    }

    /**
     * write a portion of an array of characters
     */
    public void write(char[] buffer, int off, int len)
	throws IOException
    {
	osw.write(buffer, off, len);
    }

    /**
     * Write a String
     */
    public void write(String s)
	throws IOException
    {
	osw.write(s);
    }

     /**
     * Write a portion of a String
     */
    public void write(String s, int off, int len)
	throws IOException
    {
	osw.write(s, off, len);
    }

    public void flush()
	throws IOException
    {
	if (!init) {
	    dupFirstHeaders();
	    init = true;
	}
	osw.flush();
	byte[] buffer;
	synchronized(byteStream) {
	    buffer = byteStream.toByteArray();
	    byteStream.reset();
	}
	int buflen = buffer.length;
	int curlen;
	int curpos = 0;
	// write the comments, using chunks if necessary
	while (buflen > 0) {
	    writeMarker(Jpeg.M_COM);
	    curlen = Math.min(Jpeg.M_MAX_COM_LENGTH, buflen);
	    writeMarkerLength(curlen+2);
	    outJpegData.write(buffer, curpos, curlen);
	    curpos += curlen;
	    buflen -= curlen;
	};
    }

    public void close()
	throws IOException
    {
	flush();
	byte[] buf = new byte[1024];
	int got;
	// first dump the last marker
	writeMarker(lastMarker);
	// then the end of the file
	do {
	    got = inJpegData.read(buf);
	    if (got < 0) 
		break;
	    outJpegData.write(buf, 0, got);
	} while (got >= 0);
    }

    /**
     * copy the marker and return it
     */
    protected int dupFirstMarker()
	throws IOException, JpegException
    {
	int c1, c2;
	c1 = inJpegData.read();
	c2 = inJpegData.read();
	if (c1 != 0xFF || c2 != Jpeg.M_SOI)
	    throw new JpegException("Not a JPEG file");
	outJpegData.write(c1);
	outJpegData.write(c2);
	return c2;
    }

    /**
     * read 2 bytes and create an integer out of it
     */
    protected int read2bytes() 
	throws IOException, JpegException
    {
	int c1, c2;
	c1 = inJpegData.read();
	if (c1 == -1)
	    throw new JpegException("Premature EOF in JPEG file");
	c2 = inJpegData.read();
	if (c2 == -1)
	    throw new JpegException("Premature EOF in JPEG file");
	return (((int) c1) << 8) + ((int) c2);
    }

    /**
     * get the next marker, and eat extra bytes
     */
    protected int nextMarker() 
	throws IOException
    {
	int discarded_bytes = 0;
	int c;

	/* Find 0xFF byte; count and skip any non-FFs. */
	c = inJpegData.read();
	while (c != 0xFF)
	    c = inJpegData.read();

	/* Get marker code byte, swallowing any duplicate FF bytes.  Extra FFs
	 * are legal as pad bytes, so don't count them in discarded_bytes.
	 */
	do {
	    c = inJpegData.read();
	} while (c == 0xFF);

	return c;
    }

    /**
     * skip the body after a marker
     */
    protected void skipVariable() 
	throws IOException, JpegException
    {
	long len = (long)read2bytes() - 2;
	if (len < 0 )
	    throw new JpegException("Erroneous JPEG marker length");
	while (len > 0) {
	    long saved = inJpegData.skip(len);
	    if (saved < 0)
		throw new IOException("Error while reading jpeg stream");
	    len -= saved;
	}
    }

    /**
     * dup the marker and the body
     */
    protected void dupHeader(int marker) 
	throws IOException, JpegException
    {
	int len = read2bytes();
	if (len < 2 )
	    throw new JpegException("Erroneous JPEG marker length");
	// dump the marker
	writeMarker(marker);
	// write the length
	writeMarkerLength(len);
	// and now copy the bytes
	byte[] buf = new byte[1024];
	int got;
	len -= 2;
	while (len > 0) {
	    got = inJpegData.read(buf, 0, Math.min(buf.length, len));
	    if (got < 0) 
		throw new IOException("Error while reading jpeg stream (EOF)");
	    outJpegData.write(buf, 0, got);
	    len -= got;
	}
    }    

    protected void writeMarker(int marker) 
	throws IOException
    {
	outJpegData.write(0xFF);
	outJpegData.write(marker);
    }

    protected void writeMarkerLength(int len)
	throws IOException
    {
	outJpegData.write((len >> 8) & 0xFF);
	outJpegData.write(len & 0xFF);
    }	

    /**
     * the the first headers until a SOF parker is found
     */
    protected void dupFirstHeaders() 
	throws IOException
    {
	int marker;
	// first duplicate the header and make sure it is valid
	try {
	    dupFirstMarker();
	} catch (JpegException ex) {
	    if (debug) {
		ex.printStackTrace();
	    }
	    throw new IOException(ex.getMessage());
	}
	// now dump the headers
	
	while (true) {
	    marker = nextMarker();
	    switch (marker) {
	    case Jpeg.M_SOF0:	 /* Baseline */
	    case Jpeg.M_SOF1:	 /* Extended sequential, Huffman */
	    case Jpeg.M_SOF2:	 /* Progressive, Huffman */
	    case Jpeg.M_SOF3:	 /* Lossless, Huffman */
	    case Jpeg.M_SOF5:	 /* Differential sequential, Huffman */
	    case Jpeg.M_SOF6:	 /* Differential progressive, Huffman */
	    case Jpeg.M_SOF7:	 /* Differential lossless, Huffman */
	    case Jpeg.M_SOF9:	 /* Extended sequential, arithmetic */
	    case Jpeg.M_SOF10:	 /* Progressive, arithmetic */
	    case Jpeg.M_SOF11:	 /* Lossless, arithmetic */
	    case Jpeg.M_SOF13:	 /* Differential sequential, arithmetic */
	    case Jpeg.M_SOF14:	 /* Differential progressive, arithmetic */
	    case Jpeg.M_SOF15:	 /* Differential lossless, arithmetic */   
		// bye!
		lastMarker = marker;
		return;
	    case Jpeg.M_SOS:	
		break;
	    case Jpeg.M_EOI:
		lastMarker = marker;
		return;
	    case Jpeg.M_APP12:
	    case Jpeg.M_COM:   // remove previous comments
		try {
		    skipVariable();
		} catch (JpegException jex) {
		    if (debug)
			jex.printStackTrace();
		    throw new IOException(jex.getMessage());
		}
		break;
	    default:	
		try {
		    dupHeader(marker);
		} catch (JpegException jex) {
		    if (debug)
			jex.printStackTrace();
		    throw new IOException(jex.getMessage());
		}
		break;
	    }	    
	}
    }

    /**
     * The usual debugging tool
     */
    public static void main(String args[]) {
	try {
	    File jpegFile = new File(args[0]);
	    JpegCommentWriter jcw = new JpegCommentWriter(System.out,
					       new FileInputStream(jpegFile));
	    jcw.write(args[1]);
	    jcw.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }    
}

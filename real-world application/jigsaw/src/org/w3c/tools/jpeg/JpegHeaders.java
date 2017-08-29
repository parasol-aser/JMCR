// JpegHeaders.java
// $Id: JpegHeaders.java,v 1.1 2010/06/15 12:29:18 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
//
// Modified by Norman Walsh (ndw@nwalsh.com) to support extraction of EXIF
// camera data.

package org.w3c.tools.jpeg;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import java.util.Vector;
import java.util.Hashtable;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 * jpeg reading code adapted from rdjpgcom from The Independent JPEG Group 
 */
public class JpegHeaders implements Jpeg {

    protected File        jpegfile = null;
    protected InputStream in       = null;

    protected Vector vcom          = null;
    protected Vector vacom[]       = new Vector[16];

    protected String comments[]    = null;
    protected byte appcomments[][] = null;

    // Additional EXIF data
    protected Exif exif = null;
    protected int compression = -1;
    protected int bitsPerPixel = -1;
    protected int height = -1;
    protected int width = -1;
    protected int numComponents = -1;

    /**
     * Get the comments extracted from the jpeg stream
     * @return an array of Strings
     */
    public String[] getComments() {
	if (comments == null) {
	    comments = new String[vcom.size()];
	    vcom.copyInto(comments);
	}
	return comments;
    }

    /**
     * Get the application specific values extracted from the jpeg stream
     * @return an array of Strings
     */
    public String[] getStringAPPComments(int marker) {
	// out of bound, no comment
	if ((marker < M_APP0) || (marker > M_APP15)) {
	    return null;
	}
	int idx = marker - M_APP0;
	int asize = vacom[idx].size();
	if (appcomments == null) {
	    appcomments = new byte[asize][];
	    vacom[idx].copyInto(appcomments);
	}
	String strappcomments[] = new String[asize];
	for (int i=0; i< asize; i++) {
	    try {
		strappcomments[i] = new String(appcomments[i], "ISO-8859-1");
	    } catch (UnsupportedEncodingException ex) {};
	}
	return strappcomments;
    }

    /**
     * An old default, it gets only the M_APP12
     */
    public String[] getStringAppComments() {
	return getStringAPPComments(M_APP12);
    }

    /**
     * A get XMP in APP1
     */
    public String getXMP() 
	throws IOException, JpegException

    {
	String magicstr = "W5M0MpCehiHzreSzNTczkc9d"; 
	char magic[] = magicstr.toCharArray();
	String magicendstr = "<?xpacket";
	char magicend[] = magicendstr.toCharArray();
	char c ;
	int length;
	int h=0,i = 0, j = 0 , k;
	char buf[] = new char [256] ;

	/* get the APP1 marker */
	String app1markers[] = getStringAPPComments(M_APP1);
	String app1marker = new String() ; 
	boolean found = false ;
	for (h=0; h < app1markers.length ; h++) {
	    if (found == false && app1markers[h].indexOf(magicstr) != -1 ){
		found = true ;
//		System.out.println("magic found");
		app1marker = app1marker.concat(app1markers[h]);
	    }
	    else if (found == true) {
		app1marker = app1marker.concat(app1markers[h]);
	    }
	}

	StringReader app1reader = new StringReader(app1marker);
	StringBuffer sbuf = new StringBuffer();

	/* Get the marker parameter length count */
	length = read2bytes(app1reader);
	/* Length includes itself, so must be at least 2 */
	if (length < 2)
	    throw new JpegException("Erroneous JPEG marker length");
	length -= 2;

	/* initialize a new reader to start from the beginning */
	app1reader = new StringReader(app1marker);
	/* Read until end of block or until magic string found */
	while (length > 0 && j < magic.length) {
	    buf[i] = (char)(app1reader.read());
	    if (buf[i] == -1)
		throw new JpegException("Premature EOF in JPEG file");
	    if (buf[i] == magic[j])  {
		j++; 
	    }
	    else {
		j = 0;
	    }
	    i = (i + 1) % 100;
	    length--;
	 }


	 if ( j == magic.length) {
         /* Copy from buffer everything since beginning of the PI */
	    k = i; 
	    do {
		i = (i + 100 - 1) % 100; 
	    }
	    while (buf[i] != '<' || buf[(i+1)%100] != '?' ||
		   buf[(i+2)%100] != 'x' || buf[(i+3)%100] != 'p'); 

	    for (  ; i != k; i = (i + 1) % 100) 
		sbuf.append(buf[i]);

	/* Continue copying until end of XMP packet */ 
	    j = 0; 
	    while (length > 0 &&  j < magicend.length) { 
		c = (char)(app1reader.read());
		if (c == -1)
		    throw new JpegException("Premature EOF in JPEG file");
		if (c == magicend[j]) 
		    j++; 
		else j = 0; 
		sbuf.append(c); 
		length--; 
	    } 
	/* Copy until end of PI */ 
	    while (length > 0) { 
		c = (char)(app1reader.read());
		if (c == -1)
		    throw new JpegException("Premature EOF in JPEG file"); 
		sbuf.append(c); 
		length--; 
		if (c == '>') break; 
	    } 
	} 
	/* Skip rest, if any */ 
	while (length > 0) { 
	    app1reader.read(); 
	    length--; 
	}
    return (sbuf.toString());
    }


    public byte[][] getByteArrayAPPComment() {
	return null;
    }

    /**
     * The old way of extracting comments in M_APP12 markers
     * @deprecated use getStringAppComments instead
     */
    public String[] getAppComments() {
	return getStringAppComments();
    }

    protected int scanHeaders()
	throws IOException, JpegException
    {
	int marker;
	vcom  = new Vector(1);
	vacom = new Vector[16];
	for (int i=0; i< 16; i++) {
	    vacom[i] = new Vector(1);
	}

	if (firstMarker() != M_SOI)
	    throw new JpegException("Expected SOI marker first");

	while (true) {
	    marker = nextMarker();
	    switch (marker) {
	    case M_SOF0:	 /* Baseline */
	    case M_SOF1:	 /* Extended sequential, Huffman */
	    case M_SOF2:	 /* Progressive, Huffman */
	    case M_SOF3:	 /* Lossless, Huffman */
	    case M_SOF5:	 /* Differential sequential, Huffman */
	    case M_SOF6:	 /* Differential progressive, Huffman */
	    case M_SOF7:	 /* Differential lossless, Huffman */
	    case M_SOF9:	 /* Extended sequential, arithmetic */
	    case M_SOF10:	 /* Progressive, arithmetic */
	    case M_SOF11:	 /* Lossless, arithmetic */
	    case M_SOF13:	 /* Differential sequential, arithmetic */
	    case M_SOF14:	 /* Differential progressive, arithmetic */
	    case M_SOF15:	 /* Differential lossless, arithmetic */
	        // Remember the kind of compression we saw
	        compression = marker;
		// Get the intrinsic properties fo the image
		readImageInfo();
		break;
	    case M_SOS:	 /* stop before hitting compressed data */
		skipVariable();
		// Update the EXIF
		updateExif();
		return marker;
	    case M_EOI:	 /* in case it's a tables-only JPEG stream */
		// Update the EXIF
		updateExif();
		return marker;
	    case M_COM:
	        // Always ISO-8859-1? Is this a bug or is there something about
	        // the comment field that I don't understand...
		vcom.addElement(new String(processComment(), "ISO-8859-1"));
		break;
	    case M_APP0:
	    case M_APP1:
	    case M_APP2:
	    case M_APP3:
	    case M_APP4:
	    case M_APP5:
	    case M_APP6:
	    case M_APP7:
	    case M_APP8:
	    case M_APP9:
	    case M_APP10:
	    case M_APP11:
	    case M_APP12:
	    case M_APP13:
	    case M_APP14:
	    case M_APP15:
		// Some digital camera makers put useful textual
		// information into APP1 andAPP12 markers, so we print
		// those out too when in -verbose mode.

	        byte data[] = processComment();
	        vacom[marker-M_APP0].addElement(data);

	        // This is where the EXIF data is stored, grab it and parse it!
		if (marker == M_APP1) { // APP1 == EXIF
		  if (exif != null) {
		    exif.parseExif(data);
		  }
		}

	      break;
	    default:	          // Anything else just gets skipped
		skipVariable();  // we assume it has a parameter count...
		break;
	    }
	}
    }

  /** Update the EXIF to include the intrinsic values */
  protected void updateExif() {
    if (exif == null) {
      return;
    }

    if (compression >= 0) {
      switch (compression) {
      case -1:
	// nop;
	break;
      case M_SOF0:
	exif.setCompression("Baseline");
	break;
      case M_SOF1:
	exif.setCompression("Extended sequential");
	break;
      case M_SOF2:
	exif.setCompression("Progressive");
	break;
      case M_SOF3:
	exif.setCompression("Lossless");
	break;
      case M_SOF5:
	exif.setCompression("Differential sequential");
	break;
      case M_SOF6:
	exif.setCompression("Differential progressive");
	break;
      case M_SOF7:
	exif.setCompression("Differential lossless");
	break;
      case M_SOF9:
	exif.setCompression("Extended sequential, arithmetic coding");
	break;
      case M_SOF10:
	exif.setCompression("Progressive, arithmetic coding");
	break;
      case M_SOF11:
	exif.setCompression("Lossless, arithmetic coding");
	break;
      case M_SOF13:
	exif.setCompression("Differential sequential, arithmetic coding");
	break;
      case M_SOF14:
	exif.setCompression("Differential progressive, arithmetic coding");
	break;
      case M_SOF15:
	exif.setCompression("Differential lossless, arithmetic coding");
	break;
      default:
	exif.setCompression("Unknown" + compression);
      }
    }

    if (bitsPerPixel >= 0) {
      exif.setBPP(bitsPerPixel);
    }

    if (height >= 0) {
      exif.setHeight(height);
    }

    if (width >= 0) {
      exif.setWidth(width);
    }

    if (numComponents >= 0) {
      exif.setNumCC(numComponents);
    }
  }

    protected byte[] processComment() 
	throws IOException, JpegException
    {
	int length;

	/* Get the marker parameter length count */
	length = read2bytes();
	/* Length includes itself, so must be at least 2 */
	if (length < 2)
	    throw new JpegException("Erroneous JPEG marker length");
	length -= 2;

	StringBuffer buffer = new StringBuffer(length);
	byte comment[] = new byte[length];
	int got, pos;
	pos = 0;
	while (length > 0) {
	    got = in.read(comment, pos, length);
	    if (got < 0)
		throw new JpegException("EOF while reading jpeg comment");
	    pos += got;
	    length -= got;
	}
	return comment;
    }

    protected int read2bytes() 
	throws IOException, JpegException
    {
	int c1, c2;
	c1 = in.read();
	if (c1 == -1)
	    throw new JpegException("Premature EOF in JPEG file");
	c2 = in.read();
	if (c2 == -1)
	    throw new JpegException("Premature EOF in JPEG file");
	return (((int) c1) << 8) + ((int) c2);
    }

    protected int read2bytes(StringReader sr) 
	throws IOException, JpegException
    {
	int c1, c2;
	c1 = sr.read();
	if (c1 == -1)
	    throw new JpegException("Premature EOF in JPEG file");
	c2 = sr.read();
	if (c2 == -1)
	    throw new JpegException("Premature EOF in JPEG file");
	return (((int) c1) << 8) + ((int) c2);
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
	    long saved = in.skip(len);
	    if (saved < 0)
		throw new IOException("Error while reading jpeg stream");
	    len -= saved;
	}
    }

    /**
     * read the image info then the section
     */
    protected void readImageInfo() 
	throws IOException, JpegException
    {
	long len = (long)read2bytes() - 2;

	if (len < 0 )
	    throw new JpegException("Erroneous JPEG marker length");

	bitsPerPixel = in.read(); len--;
	height = read2bytes(); len -= 2;
	width = read2bytes(); len -= 2;
	numComponents = in.read(); len--;

	while (len > 0) {
	    long saved = in.skip(len);
	    if (saved < 0)
		throw new IOException("Error while reading jpeg stream");
	    len -= saved;
	}
    }

    protected int firstMarker()
	throws IOException, JpegException
    {
	int c1, c2;
	c1 = in.read();
	c2 = in.read();
	if (c1 != 0xFF || c2 != M_SOI)
	    throw new JpegException("Not a JPEG file");
	return c2;
    }

    protected int nextMarker() 
	throws IOException
    {
	int discarded_bytes = 0;
	int c;

	/* Find 0xFF byte; count and skip any non-FFs. */
	c = in.read();
	while (c != 0xFF)
	    c = in.read();

	/* Get marker code byte, swallowing any duplicate FF bytes.  Extra FFs
	 * are legal as pad bytes, so don't count them in discarded_bytes.
	 */
	do {
	    c = in.read();
	} while (c == 0xFF);

	return c;
    }

    /**
     * get the headers out of a file, ignoring EXIF
     */
    public JpegHeaders(File jpegfile)
      throws FileNotFoundException, JpegException, IOException {
      parseJpeg(jpegfile, null);
    }

    /**
     * get the headers out of a file, including EXIF
     */
    public JpegHeaders(File jpegfile, Exif exif)
      throws FileNotFoundException, JpegException, IOException {
      parseJpeg(jpegfile, exif);
    }

    /**
     * get the headers out of a stream, ignoring EXIF
     */
    public JpegHeaders(InputStream in) 
	throws JpegException, IOException
    {
	this.in = in; 
	scanHeaders();
    } 

    /**
     * get the headers out of a stream, including EXIF
     */
    public JpegHeaders(InputStream in, Exif exif) 
	throws JpegException, IOException
    {
        this.exif = exif;
	this.in = in; 
	scanHeaders();
    } 

    protected void parseJpeg(File jpegfile, Exif exif)
      throws FileNotFoundException, JpegException, IOException
    {
        this.exif = exif;
	this.jpegfile = jpegfile;
	this.in       = 
	    new BufferedInputStream( new FileInputStream(jpegfile));
	try {
	    scanHeaders();
	} finally {
	    try {
		in.close();
	    } catch (Exception ex) {};
	}
    }

    public static void main(String args[]) {
	try {
	    JpegHeaders headers = new JpegHeaders(new File(args[0]));
	    String comments[] = headers.getComments();
	    if (comments != null) {
		for (int i = 0 ; i < comments.length ; i++) 
		    System.out.println(comments[i]);
	    }
	System.out.println(headers.getXMP());
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

}

// Exif.java
// $Id: Exif.java,v 1.2 2010/06/15 17:53:10 smhuang Exp $
// Copyright (c) 2003 Norman Walsh
// Please first read the full copyright statement in file COPYRIGHT

package org.w3c.tools.jpeg;

import java.util.Hashtable;

/**
 * An API for accessing EXIF encoded information in a JPEG file.
 *
 * <p>JPEG images are stored in a tagged format reminiscent of TIFF files.
 * Each component of the image is identified with a tag number and a size.
 * This allows applications that read JPEG files to skip over information
 * that they don't understand.</p>
 *
 * <p>Additional resources:</p>
 * <ul>
 * <li>Official standards, http://www.exif.org/specifications.html</li>
 * <li>TsuruZoh Tachibanaya's excellent description,
 * http://www.ba.wakwak.com/~tsuruzoh/Computer/Digicams/exif-e.html
 * </li>
 * <li>Matthias Wandel's JHead, http://www.sentex.net/~mwandel/jhead
 * </ul>
 *
 * <p>This class treats a byte array as a JPEG image and parses the tags
 * searching for EXIF data. EXIF data is also tagged. Based on information
 * provided by the caller, this class builds a hash of EXIF data and
 * makes it available to the caller.</p>
 *
 * <p>Most simple EXIF values are tagged with both their identity and their
 * format. For example, ExposureTime (0x829A) is a rational number and
 * this class can extract that value. However, some fields are of "unknown"
 * format. If you decode one of these, you can add a special purpose decode
 * for that field by associating the name of the decoder class with the
 * field name. For example, my Nikon CoolPix 950 includes a MakerNote (0x927C)
 * field that's tagged "unknown" format. Using information from TsuruZoh's
 * page, I've built a decoder for that field and added it as an example.</p>
 *
 * <p>In addition to the tagged data, JPEG images have five intrinisic
 * properties: height, width, the compression algorithm used, the number of 
 * bits used to store each pixel value, and the number of color components.
 * This class allows the caller to unify those intrinsic components with the
 * tagged data.</p>
 *
 * <p>In an effort to be flexible without requiring users to change the
 * source, a fair bit of setup is needed to use this class.</p>
 *
 * <p>The caller must:</p>
 *
 * <ol>
 * <li>Construct an Exif object, <code>exif = new Exif()</code>.</li>
 * <li>Associate names with each of the tags of interest,
 *     <code>exif.addTag(<em>nnn</em>, "<em>name</em>")</code>.</li>
 * <li>Associate names with the intrinsic values:
 *   <ul>
 *     <li><code>exif.addHeight("<em>name</em>")</code>
 *     <li><code>exif.addWidth("<em>name</em>")</code>
 *     <li><code>exif.addCompression("<em>name</em>")</code>
 *     <li><code>exif.addNumberOfColorComponents("<em>name</em>")</code>
 *     <li><code>exif.addBitsPerPixel("<em>name</em>")</code>
 *   </ul>
 * </li>
 * <li>Finally, the caller may associate a decoder with specific fields:
 * <code>exif.addDecoder("<em>name</em>", "<em>java.class.name</em>")</code>.
 * </li>
 * </ol>
 *
 * <p>Having setup the exif object, it can be passed to JpegHeaders to be 
 * filled in when the JPEG file is parsed.</p>
 *
 * <p>The caller must also explicitly set the intrinsic values since they do
 * not come from the EXIF data.</p>
 *
 * <p>After parsing the JPEG, call <code>exif.getTags()</code> to get back the
 * has of name/value pairs.</p>
 *
 * @version $Revision: 1.2 $
 * @author  Norman Walsh
 * @see ExifData
 * @see TagDecoder
 * @see JpegHeaders
 */
public class Exif {
    private static final int TAG_EXIF_OFFSET = 0x8769;
    private static final int TAG_INTEROP_OFFSET = 0xa005;

    private Hashtable tags = new Hashtable();
    private Hashtable exif = new Hashtable();
    private Hashtable decoder = new Hashtable();
    private ExifData data = null;
    private boolean intelOrder = false;

    private String tagHeight = null;
    private String tagWidth = null;
    private String tagComp = null;
    private String tagBPP = null;
    private String tagNumCC = null;

    public void parseExif(byte[] exifData) {
	data = new ExifData(exifData);
	if (!data.isExifData()) {
	    return;
	}

	int firstOffset = data.get32u(10);
	processExifDir(6+firstOffset, 6);
    }

    public void setHeight(int height) {
	if (tagHeight != null) {
	    exif.put(tagHeight, ""+height);
	}
    }

    public void setWidth(int width) {
	if (tagWidth != null) {
	    exif.put(tagWidth, ""+width);
	}
    }

    public void setCompression(String comp) {
	if (tagComp != null) {
	    exif.put(tagComp, comp);
	}
    }

    public void setBPP(int bitsPP) {
	if (tagBPP != null) {
	    exif.put(tagBPP, ""+bitsPP);
	}
    }

    public void setNumCC(int numCC) {
	if (tagNumCC != null) {
	    exif.put(tagNumCC, ""+numCC);
	}
    }

    public void addHeight(String name) {
	tagHeight = name;
    }

    public void addWidth(String name) {
	tagWidth = name;
    }

    public void addCompression(String name) {
	tagComp = name;
    }

    public void addBitsPerPixel(String name) {
	tagBPP = name;
    }
    public void addNumberOfColorComponents(String name) {
	tagNumCC = name;
    }

    public void addTag(int tag, String tagName) {
	tags.put(new Integer(tag), tagName);
    }

    public void addDecoder(String name, String className) {
	decoder.put(name, className);
    }

    public Hashtable getTags() {
	return exif;
    }

    protected void processExifDir(int dirStart,
				  int offsetBase) {

	int numEntries = data.get16u(dirStart);
	//System.err.println("EXIF: numEntries: " + numEntries);

	for (int de = 0; de < numEntries; de++) {
	    int dirOffset = dirStart + 2 + (12 * de);

	    int tag = data.get16u(dirOffset);
	    int format = data.get16u(dirOffset+2);
	    int components = data.get32u(dirOffset+4);

	    //System.err.println("EXIF: entry: 0x" + Integer.toHexString(tag)
	    //		 + " " + format
	    //		 + " " + components);

	    if (format < 0 || format > data.NUM_FORMATS) {
		System.err.println("Bad number of formats in EXIF dir: " +
				   format);
		return;
	    }

	    int byteCount = components * ExifData.bytesPerFormat[format];
	    int valueOffset = dirOffset + 8;

	    if (byteCount > 4) {
		int offsetVal = data.get32u(dirOffset+8);
		valueOffset = offsetBase + offsetVal;
	    }

	    //System.err.println("valueOffset: " + valueOffset + 
	    //                                     " byteCount: " + byteCount);

	    Integer iTag = new Integer(tag);

	    if (tag == TAG_EXIF_OFFSET || tag == TAG_INTEROP_OFFSET) {
		int subdirOffset = data.get32u(valueOffset);

		//System.err.println("offset: " + subdirOffset+
		//                                ":"+offsetBase+subdirOffset);

		processExifDir(offsetBase+subdirOffset, offsetBase);
	    } else {
		String tagName = "BugBugBug";
		boolean usedTag = false;

		if (tags.containsKey(iTag)) {
		    tagName = (String) tags.get(iTag);
		    usedTag = true;
		} else {
		    tagName = ":unknown0x" + 
			                  Integer.toHexString(iTag.intValue());
		}

		if (decoder.containsKey(tagName)) {
		    String className = (String) decoder.get(tagName);
		    TagDecoder decoder = null;

		    try {
			decoder = (TagDecoder) 
			                Class.forName(className).newInstance();
		    //Added by Jeff Huang
		    //TODO: FIXIT
		    } catch (ClassNotFoundException cnfe) {
			System.err.println("Class not found: " + className);
		    } catch (InstantiationException cnfe) {
			System.err.println("Failed to instantiate " +
					   className);
		    } catch (IllegalAccessException cnfe) {
			System.err.println("Illegal access instantiating " +
					   className);
		    } catch (ClassCastException cnfe) {
			System.err.println("Class " + className + 
					   " is not a TagDecoder");
		    }

		    if (decoder != null) {
			decoder.decode(exif, data, format, valueOffset, 
				       byteCount);
		    }
		} else {
		    switch (format) {
		    case ExifData.FMT_UNDEFINED:
			assignUndefined(tagName, valueOffset, byteCount);
			break;
		    case ExifData.FMT_STRING:
			assignString(tagName, valueOffset, byteCount);
			break;
		    case ExifData.FMT_SBYTE:
			assignSByte(tagName, valueOffset);
			break;
		    case ExifData.FMT_BYTE:
			assignByte(tagName, valueOffset);
			break;
		    case ExifData.FMT_USHORT:
			assignUShort(tagName, valueOffset);
			break;
		    case ExifData.FMT_SSHORT:
			assignSShort(tagName, valueOffset);
			break;
		    case ExifData.FMT_ULONG:
			assignULong(tagName, valueOffset);
			break;
		    case ExifData.FMT_SLONG:
			assignSLong(tagName, valueOffset);
			break;
		    case ExifData.FMT_URATIONAL:
		    case ExifData.FMT_SRATIONAL:
			assignRational(tagName, valueOffset);
			break;
		    default:
			//System.err.println("Unknown format " + format + 
			//                   " for " + tagName);
		    }
		}
	    }
	}
    }

    protected void assignUndefined(String tagName, int offset, int length) {
	String result = data.getUndefined(offset, length);
	if (!"".equals(result)) {
	    exif.put(tagName, result);
	}
    }

    protected void assignString(String tagName, int offset, int length) {
	String result = data.getString(offset, length);
	if (!"".equals(result)) {
	    exif.put(tagName, result);
	}
    }

    protected void assignSByte(String tagName, int offset) {
	int result = (int) data.convertAnyValue(ExifData.FMT_SBYTE, offset);
	//System.err.println("\t" + tagName + ": " + result);
	exif.put(tagName, ""+result);
    }

    protected void assignByte(String tagName, int offset) {
	int result = (int) data.convertAnyValue(ExifData.FMT_BYTE, offset);
	//System.err.println("\t" + tagName + ": " + result);
	exif.put(tagName, ""+result);
    }

    protected void assignUShort(String tagName, int offset) {
	int result = (int) data.convertAnyValue(ExifData.FMT_USHORT, offset);
	//System.err.println("\t" + tagName + ": " + result);
	exif.put(tagName, ""+result);
    }

    protected void assignSShort(String tagName, int offset) {
	int result = (int) data.convertAnyValue(ExifData.FMT_SSHORT, offset);
	//System.err.println("\t" + tagName + ": " + result);
	exif.put(tagName, ""+result);
    }

    protected void assignULong(String tagName, int offset) {
	int result = (int) data.convertAnyValue(ExifData.FMT_ULONG, offset);
	//System.err.println("\t" + tagName + ": " + result);
	exif.put(tagName, ""+result);
    }

    protected void assignSLong(String tagName, int offset) {
	int result = (int) data.convertAnyValue(ExifData.FMT_SLONG, offset);
	//System.err.println("\t" + tagName + ": " + result);
	exif.put(tagName, ""+result);
    }

    protected void assignRational(String tagName, int offset) {
	int num = data.get32s(offset);
	int den = data.get32s(offset+4);
	String result = "";

	// This is a bit silly, I really ought to find a real GCD algorithm
	if (num % 10 == 0 && den % 10 == 0) {
	    num = num / 10;
	    den = den / 10;
	}

	if (num % 5 == 0 && den % 5 == 0) {
	    num = num / 5;
	    den = den / 5;
	}

	if (num % 3 == 0 && den % 3 == 0) {
	    num = num / 3;
	    den = den / 3;
	}

	if (num % 2 == 0 && den % 2 == 0) {
	    num = num / 2;
	    den = den / 2;
	}

	if (den == 0) {
	    result = "0";
	} else if (den == 1) {
	    result = "" + num; // "" + int sure looks ugly...
	} else {
	    result = "" + num + "/" + den;
	}

	//System.err.println("\t" + tagName + ": " + result);
	exif.put(tagName, ""+result);
    }
}

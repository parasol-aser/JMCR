// Exif.java
// $Id: ExifData.java,v 1.1 2010/06/15 12:29:18 smhuang Exp $
// Copyright (c) 2003 Norman Walsh
// Please first read the full copyright statement in file COPYRIGHT

package org.w3c.tools.jpeg;

import java.util.Hashtable;

/**
 * An internal API for decoding EXIF data.
 *
 * <p>This class identifies a byte array as valid EXIF and provides methods
 * for converting the internal representation of types to equivalent Java 
 * types.
 * </p>
 *
 * <p>This is a separate class in order to facilitate writing decoders for
 * unknown fields.</p>
 *
 * <p>Special note: string values are %-encoded to protect the caller from 
 * values that might not be legitimate UTF-8 strings.</p>
 *
 * <p><b>Bugs:</b></p>
 * <p>The EXIF format includes unsigned integers which aren't directly
 * available in Java. Unless I'm mistaken, they'd have to be turned into
 * longs. But that would
 * be inconvenient in other APIs. For the moment, I'm just treating them all as
 * signed. I've never seen an EXIF unsigned value too large to represent
 * in a Java int.</p>
 *
 * @version $Revision: 1.1 $
 * @author  Norman Walsh
 * @see Exif
 */
public class ExifData {
    public static final int bytesPerFormat[] = {0,1,1,2,4,8,1,1,2,4,8,4,8};
    public static final int NUM_FORMATS   = 12;
    public static final int FMT_BYTE      =  1;
    public static final int FMT_STRING    =  2;
    public static final int FMT_USHORT    =  3;
    public static final int FMT_ULONG     =  4;
    public static final int FMT_URATIONAL =  5;
    public static final int FMT_SBYTE     =  6;
    public static final int FMT_UNDEFINED =  7;
    public static final int FMT_SSHORT    =  8;
    public static final int FMT_SLONG     =  9;
    public static final int FMT_SRATIONAL = 10;
    public static final int FMT_SINGLE    = 11;
    public static final int FMT_DOUBLE    = 12;

    private byte[] data = null;
    private boolean intelOrder = false;

    public ExifData(byte[] exifData) {
	String dataStr = new String(exifData);
	if (exifData.length <= 4 || !"Exif".equals(dataStr.substring(0, 4))) {
	    // Not really EXIF data
	    return;
	}

	String byteOrderMarker = dataStr.substring(6, 8);
	if ("II".equals(byteOrderMarker)) {
	    intelOrder = true;
	} else if ("MM".equals(byteOrderMarker)) {
	    intelOrder = false;
	} else {
	    // bogus!
	    System.err.println("Bogus byte order in EXIF data.");
	    return;
	}

	data = exifData;

	int checkValue = get16u(8);
	if (checkValue != 0x2a) {
	    data = null;
	    System.err.println("Check value fails: 0x" + 
			       Integer.toHexString(checkValue));
	    return;
	}
    }

    public boolean isExifData() {
	return (data != null);
    }

    public int get16s(int offset) {
	if (data == null) {
	    return 0;
	}

	int hi, lo;

	if (intelOrder) {
	    hi = data[offset+1];
	    lo = data[offset];
	} else {
	    hi = data[offset];
	    lo = data[offset+1];
	}

	lo = lo & 0xFF;
	hi = hi & 0xFF;

	return (hi << 8) + lo;
    }

    public int get16u(int offset) {
	if (data == null) {
	    return 0;
	}

	int value = get16s(offset);
	value = value & 0xFFFF;
	return value;
    }

    public int get32s(int offset) {
	if (data == null) {
	    return 0;
	}

	int n1, n2, n3, n4;

	if (intelOrder) {
	    n1 = data[offset+3] & 0xFF;
	    n2 = data[offset+2] & 0xFF;
	    n3 = data[offset+1] & 0xFF;
	    n4 = data[offset] & 0xFF;
	} else {
	    n1 = data[offset] & 0xFF;
	    n2 = data[offset+1] & 0xFF;
	    n3 = data[offset+2] & 0xFF;
	    n4 = data[offset+3] & 0xFF;
	}

	int value = (n1 << 24) + (n2 << 16) + (n3 << 8) + n4;

	return value;
    }

    public int get32u(int offset) {
	if (data == null) {
	    return 0;
	}

	// I don't know how to represent an unsigned in Java!
	return get32s(offset);
    }

    public byte[] getBytes(int offset, int length) {
	if (data == null || length == 0) {
	    return null;
	}

	byte[] raw = new byte[length];
	for (int count = offset; length > 0; count++, length--) {
	    raw[count-offset] = data[count];
	}

	return raw;
    }

    public String getString(int offset, int length) {
	return getString(offset, length, true);
    }

    public String getUndefined(int offset, int length) {
	return getString(offset, length, false);
    }

    protected String getString(int offset, int length, 
			       boolean nullTerminated) {
	if (data == null) {
	    return "";
	}

	String result = "";

	for (int count = offset;
	     (length > 0) && (!nullTerminated || data[count] != 0);
	     count++, length--) {
	    short ub = data[count];
	    ub = (short) (ub & 0xFF);

	    String ch = "" + (char) ub;
	    if ((ub == '%') || (ub < ' ') || (ub > '~')) {
		ch = Integer.toHexString((char) ub);
		if (ch.length() < 2) {
		    ch = "0" + ch;
		}
		ch = "%" + ch;
	    }
	    result += ch;
	}

	return result;
    }

    public double convertAnyValue(int format, int offset) {
	if (data == null) {
	    return 0.0;
	}

	double value = 0.0;

	switch(format){
	case FMT_SBYTE:
	    value = data[offset];
	    break;
	case FMT_BYTE:
	    int iValue = data[offset];
	    iValue = iValue & 0xFF;
	    value = iValue;
	    break;
	case FMT_USHORT:
	    value = get16u(offset);
	    break;
	case FMT_ULONG:
	    value = get32u(offset);
	    break;
	case FMT_URATIONAL:
	case FMT_SRATIONAL:
	    int num = get32s(offset);
	    int den = get32s(offset+4);

	    if (den == 0) {
		value = 0;
	    } else {
		value = (double) num / (double) den;
	    }
	    break;
	case FMT_SSHORT:
	    value = get16s(offset);
	    break;
	case FMT_SLONG:
	    value = get32s(offset);
	    break;
	default:
	    System.err.println("Unexpected number format: " + format);
	}

	return value;
    }
}

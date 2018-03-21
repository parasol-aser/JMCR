// StringUtils.java
// $Id: StringUtils.java,v 1.1 2010/06/15 12:25:37 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.util;

public class StringUtils {
    /**
     * to hex converter
     */
    private static final char[] toHex = { '0', '1', '2', '3', '4', '5', '6',
					  '7', '8', '9', 'a', 'b', 'c', 'd',
					  'e', 'f' };

    /**
     * convert an array of bytes to an hexadecimal string
     * @param an array of bytes
     * @return a string
     */

    public static String toHexString(byte b[]) {
	int pos = 0;
	char[] c = new char[b.length*2];
	for (int i=0; i< b.length; i++) {
	    c[pos++] = toHex[(b[i] >> 4) & 0x0F];
	    c[pos++] = toHex[b[i] & 0x0f];
	}
	return new String(c);
    }
}

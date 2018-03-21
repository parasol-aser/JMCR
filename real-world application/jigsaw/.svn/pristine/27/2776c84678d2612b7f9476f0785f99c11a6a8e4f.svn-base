// MimeTypeArrayAttribute.java
// $Id: MimeTypeArrayAttribute.java,v 1.1 2010/06/15 12:24:15 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.frames;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.www.mime.MimeType;
import org.w3c.www.mime.MimeTypeFormatException;
/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class MimeTypeArrayAttribute extends StringArrayAttribute {

    public static String[] toStringArray(Object array) {
	if (array == null)
	    return null;
	if (array instanceof String[])
	    return (String[]) array;
	else if (array instanceof MimeType[]) {
	    MimeType mimes[] = (MimeType[]) array;
	    String strArray [] = new String[mimes.length];
	    for (int i = 0 ; i < mimes.length ; i++)
		strArray[i] = mimes[i].toString();
	    return strArray;
	} else
	    return null;
    }

    /**
     * Is the given object a valid MimeTypeArrayAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */

    public boolean checkValue(Object obj) {
	return ((obj instanceof MimeType[]) || (obj instanceof String[]));
    }

    public String[] pickle(Object array) {
	return toStringArray(array);
    }

    public Object unpickle (String array[]) {
	int    cnt       = array.length ;
	if (cnt < 1)
	    return null;
	MimeType mimes[] = new MimeType[cnt] ;
	for (int i = 0 ; i < cnt ; i++) {
	    try {
		mimes[i] = new MimeType(array[i]) ;
	    } catch (MimeTypeFormatException ex) {
		mimes[i] = null;
	    }
	}
	return mimes ;
    }

    public MimeTypeArrayAttribute(String name, MimeType def[], int flags) {
	super(name, toStringArray(def), flags);
	this.type = "[Lorg.w3c.www.mime.MimeType;".intern();
    }

    public MimeTypeArrayAttribute() {
	super();
	this.type = "[Lorg.w3c.www.mime.MimeType;".intern();
    }

}

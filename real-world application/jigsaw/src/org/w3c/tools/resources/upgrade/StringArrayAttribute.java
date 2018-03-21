// StringArrayAttribute.java
// $Id: StringArrayAttribute.java,v 1.1 2010/06/15 12:22:51 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade ;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The generic description of an StringArrayAttribute.
 */

public class StringArrayAttribute extends Attribute {

    /**
     * Turn a StringArray attribute into a String.
     * We use the <em>normal</em> property convention, which is to separate
     * each item with a <strong>|</strong>.
     * @return A String based encoding for that value.
     */

    public String stringify(Object value) {
	if ((value == null) || ( ! (value instanceof String[])) )
	    return null;
	String       as[] = (String[]) value;
	StringBuffer sb   = new StringBuffer();
	for (int i = 0 ; i < as.length ; i++) {
	    if ( i > 0 )
		sb.append('|');
	    sb.append(as[i]);
	}
	return sb.toString();
    }

    /**
     * Is the given object a valid StringArrayAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */

    public boolean checkValue(Object obj) {
	return (obj instanceof String[]) ;
    }

    /**
     * Get the number of bytes required to save that attribute value.
     * @param The value about to be pickled.
     * @return The number of bytes needed to pickle that value.
     */

    public int getPickleLength(Object value) {
	String strs[] = (String[]) value;
	int    sz     = 4;
	for (int n = 0 ; n < strs.length ; n++) {
	    String str    = strs[n];
	    int    strlen = str.length() ;
	    int    utflen = 0 ;

	    for (int i = 0 ; i < strlen ; i++) {
		int c = str.charAt(i);
		if ((c >= 0x0001) && (c <= 0x007F)) {
		    utflen++;
		} else if (c > 0x07FF) {
		    utflen += 3;
		} else {
		    utflen += 2;
		}
	    }
	    sz += (utflen+2);
	}
	return sz;
    }

    /**
     * Pickle a String array to the given output stream.
     * @param out The output stream to pickle to.
     * @param obj The object to pickle.
     * @exception IOException If some IO error occured.
     */

    public void pickle(DataOutputStream out, Object sa) 
	throws IOException
    {
	String strs[] = (String[]) sa ;
	out.writeInt(strs.length) ;
	for (int i = 0 ; i < strs.length ; i++)
	    out.writeUTF(strs[i]) ;
    }

    /**
     * Unpickle an String array from the given input stream.
     * @param in The input stream to unpickle from.
     * @return An instance of String[].
     * @exception IOException If some IO error occured.
     */

    public Object unpickle (DataInputStream in) 
	throws IOException
    {
	int    cnt    = in.readInt() ;
	String strs[] = new String[cnt] ;
	for (int i = 0 ; i < cnt ; i++)
	    strs[i] = in.readUTF() ;
	return strs ;
    }

    public StringArrayAttribute(String name, String def[], Integer flags) {
	super(name, def, flags) ;
	this.type = "java.lang.String[]";
    }

}

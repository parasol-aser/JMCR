// PropertiesAttribute.java
// $Id: PropertiesAttribute.java,v 1.1 2010/06/15 12:22:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade ;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.Enumeration;

import org.w3c.util.ArrayDictionary;

/**
 * The generic description of an PropertiesAttribute.
 * A PropertiesAttribute instance holds a String to String mapping, it
 * should be used only with care, since people may act on a reference to
 * it.
 */

public class PropertiesAttribute extends Attribute {

    /**
     * Get the pickled length of that string.
     */

    private int getPickleLength(String str) {
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
	return utflen + 2 ;
    }

    /**
     * Is the given object a valid PropertiesAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if value is valid.
     */

    public boolean checkValue(Object obj) {
	return (obj == null) || (obj instanceof ArrayDictionary);
    }

    /**
     * Get the number of bytes required to save that attribute value.
     * @param The value about to be pickled.
     * @return The number of bytes needed to pickle that value.
     */

    public final int getPickleLength(Object value) {
	ArrayDictionary a = (ArrayDictionary) value;
	Enumeration     e = a.keys();
	int             s = 4;
	while ( e.hasMoreElements() ) {
	    String key = (String) e.nextElement();
	    s += getPickleLength(key);
	    s += getPickleLength((String) a.get(key));
	}
	return s;
    }

    /**
     * Pickle a property list to the given output stream.
     * @param out The output stream to pickle to.
     * @param obj The object to pickle.
     * @exception IOException If some IO error occured.
     */

    public void pickle(DataOutputStream out, Object o) 
	throws IOException
    {
	ArrayDictionary a = (ArrayDictionary) o;
	Enumeration     e = a.keys();
	int             s = a.size();
	out.writeInt(s);
	while (--s >= 0) {
	    String key = (String) e.nextElement();
	    out.writeUTF(key);
	    out.writeUTF((String) a.get(key));
	}
    }

    /**
     * Unpickle an string from the given input stream.
     * @param in The input stream to unpickle from.
     * @return An instance of String.
     * @exception IOException If some IO error occured.
     */

    public Object unpickle (DataInputStream in) 
	throws IOException
    {
	int             s = in.readInt();
	ArrayDictionary a = new ArrayDictionary(s, 5);
	while (--s >= 0) {
	    String k = in.readUTF();
	    String v = in.readUTF();
	    a.put(k, v);
	}
	return a;
    }

    /**
     * Create a description for a generic property list attribute.
     * @param name The attribute name.
     * @param def The default value for these attributes.
     * @param flags The associated flags.
     */

    public PropertiesAttribute(String name, String def, Integer flags) {
	super(name, def, flags) ;
	this.type = "java.lang.String";
    }

}



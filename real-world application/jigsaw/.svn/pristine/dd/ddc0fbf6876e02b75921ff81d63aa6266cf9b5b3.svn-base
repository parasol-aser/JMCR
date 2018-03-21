// PropertiesAttribute.java
// $Id: PropertiesAttribute.java,v 1.1 2010/06/15 12:20:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.util.Enumeration;

import org.w3c.util.ArrayDictionary;

/**
 * The generic description of an PropertiesAttribute.
 * A PropertiesAttribute instance holds a String to String mapping, it
 * should be used only with care, since people may act on a reference to
 * it.
 */

public class PropertiesAttribute extends ArrayAttribute {

    /**
     * Is the given object a valid PropertiesAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if value is valid.
     */

    public boolean checkValue(Object obj) {
	return (obj == null) || (obj instanceof ArrayDictionary);
    }

    /**
     * Unpickle an attribute array from a string array.
     * @param array the String array
     * @return a Object array
     */
    public Object unpickle(String array[]) {
	if (array.length < 1)
	    return null;
	ArrayDictionary a = new ArrayDictionary(array.length, 5);
	for (int i = 0 ; i < array.length ; i++) {
	    String encoded = array[i];
	    int    idx     = encoded.indexOf('=');
	    if (idx != -1) {
		String key     = encoded.substring(0, idx);
		String value   = encoded.substring(idx+1);
		a.put(key, value);
	    }
	}
	return a;
    }

    /**
     * Pickle an attribute array into a String array.
     * @param array the attribute array
     * @return a String array
     */
    public String[] pickle(Object o) {
	ArrayDictionary a       = (ArrayDictionary) o;
	Enumeration     e       = a.keys();
	int             len     = a.size();
	String          array[] = new String[len];

	for (int i = 0 ; i < len ; i++ ) {
	    String key =  (String) e.nextElement();
	    array[i]   = key+"="+(String) a.get(key);
	}
	return array;
    }

    /**
     * Create a description for a generic property list attribute.
     * @param name The attribute name.
     * @param def The default value for these attributes.
     * @param flags The associated flags.
     */

    public PropertiesAttribute(String name, String def, int flags) {
	super(name, def, flags) ;
	this.type = "java.lang.String".intern();
    }

    public PropertiesAttribute() {
	super();
	this.type = "java.lang.String".intern();
    }

}



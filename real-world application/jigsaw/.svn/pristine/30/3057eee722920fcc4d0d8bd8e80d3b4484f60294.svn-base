// ArrayAttribute.java
// $Id: ArrayAttribute.java,v 1.1 2010/06/15 12:20:19 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
abstract public class ArrayAttribute extends Attribute {

    /**
     * Unpickle an attribute array from a string array.
     * @param array the String array
     * @return a Object array
     */
    public abstract Object unpickle(String array[]);

    /**
     * Pickle an attribute array into a String array.
     * @param array the attribute array
     * @return a String array
     */
    public abstract String[] pickle(Object array);

    public String stringify(Object value) {
	String array[] = pickle(value);
	String string  = ""; 
	for (int i = 0 ; i < array.length ; i++) {
	    if (i != 0)
		string += " | "+array[i];
	    else
		string = array[i];
	}
	return string;
    }
    public ArrayAttribute(String name, Object def, int flags) {
	super(name, def, flags);
    }

    public ArrayAttribute() {
	super();
    }

}

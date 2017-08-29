// BooleanAttribute.java
// $Id: BooleanAttribute.java,v 1.1 2010/06/15 12:20:25 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources;

/**
 * The generic description of an BooleanAttribute.
 */

public class BooleanAttribute extends SimpleAttribute {

    /**
     * Is the given object a valid BooleanAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */

    public boolean checkValue(Object obj) {
	return (obj instanceof Boolean);
    }

    /**
     * Pickle an integer to the given output stream.
     * @param obj The object to pickle.
     */

    public String pickle(Object obj) {
	return ((Boolean) obj).toString();
    }

    /**
     * Unpickle an integer from the given input stream.
     * @param value the string representation of this integer
     * @return An instance of Integer.
     */

    public Object unpickle (String value) {
	return Boolean.valueOf(value);
    }

    public BooleanAttribute(String name, Boolean def, int flags) {
	super(name, def, flags) ;
	this.type = "java.lang.Boolean".intern();
    }

    public BooleanAttribute() {
	super() ;
    }

}

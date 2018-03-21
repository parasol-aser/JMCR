// DoubleAttribute.java
// $Id: DoubleAttribute.java,v 1.1 2010/06/15 12:20:13 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

/**
 * The generic description of an DoubleAttribute.
 */

public class DoubleAttribute extends SimpleAttribute {

    /**
     * Is the given object a valid DoubleAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     * @exception IllegalAttributeAccess If the provided value doesn't pass the
     *    test.
     */

    public boolean checkValue(Object obj) {
	return (obj instanceof Double) ;
    }

    /**
     * Pickle an integer to the given output stream.
     * @param obj The object to pickle.
     */

    public String pickle(Object obj) {
	return ((Double) obj).toString();
    }

    /**
     * Unpickle an integer from the given input stream.
     * @param value the string representation of this integer
     * @return An instance of Integer.
     */

    public Object unpickle (String value) {
	return Double.valueOf(value);
    }
    /**
     * Create a description for a generic Double attribute.
     * @param name The attribute name.
     * @param def The default value for these attributes.
     * @param flags The associated flags.
     */

    public DoubleAttribute(String name, Double def, int flags) {
	super(name, def, flags) ;
	this.type = "java.lang.Double".intern();
    }

    public DoubleAttribute() {
	super();
    }

}



// ClassAttribute.java
// $Id: ClassAttribute.java,v 1.2 2010/06/15 17:52:59 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

/**
 * The generic description of an ClassAttribute.
 */

public class ClassAttribute extends SimpleAttribute {

    /**
     * Make a String out of a ClassAttribute value.
     * The default <code>toString</code> method on classes doesn't work
     * for that purpose, since it will preceed the class name with
     * a <strong>class</strong> keyword.
     * @return The String name of the class.
     */

    public String stringify(Object value) {
	return ((value instanceof Class) 
		? ((Class) value).getName()
		: "<unknown-class>");
    }

    /**
     * Is the given object a valid ClassAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */

    public boolean checkValue(Object obj) {
	return (obj instanceof Class);
    }

    /**
     * Pickle an integer to the given output stream.
     * @param obj The object to pickle.
     */

    public String pickle(Object obj) {
	return ((Class) obj).getName();
    }

    /**
     * Unpickle an integer from the given input stream.
     * @param value the string representation of this integer
     * @return An instance of Integer.
     */

    public Object unpickle (String value) {
	try {
	    return Class.forName(value);
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    return null;
	}
    }

    public ClassAttribute(String name, Class def, int flags) {
	super(name, def, flags) ;
	this.type = "java.lang.Class".intern();
    }

    public ClassAttribute() {
	super() ;
    }

}

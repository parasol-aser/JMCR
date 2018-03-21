// ObjectAttribute.java
// $Id: ObjectAttribute.java,v 1.2 2010/06/15 17:53:00 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources;

/**
 * A generic Object attribute.
 * This attribute is usefull for attributes that are:
 * <ul>
 * <li>Have Object values.
 * <li>Need not be saved (have the DONTSAVE bit set).
 * </ul>
 */

public class ObjectAttribute extends Attribute {
    /**
     * The class for values of this attribute.
     */
    protected Class cls = null ;

    /**
     * Check that a value is allowed for this attribute.
     * @param value The value to check.
     * @return A boolean <strong>true</strong> if value is allowed.
     */

    public boolean checkValue(Object value) {
	return true;
    }

    /**
     * Pickle an integer to the given output stream.
     * @param obj The object to pickle.
     */

    public String pickle(Object obj) {
	throw new RuntimeException("Can't pickle ObjectAttribute");
    }

    /**
     * Unpickle an integer from the given input stream.
     * @param value the string representation of this integer
     * @return An instance of Integer.
     */

    public Object unpickle (String value) {
	throw new RuntimeException("Can't pickle ObjectAttribute");
    }

    public String stringify(Object value) {
	throw new RuntimeException("Can't pickle ObjectAttribute");
    }

    /**
     * Create a new ObjectAttribute instance.
     * @param name The name of the attribute.
     * @param cls The class for this attribute values.
     * @param def The default value for this attribute.
     * @param flags The attribute flags.
     */

    public ObjectAttribute(String name, Class cls, Object def, int flags) {
	super(name, def, flags) ;
	// Check consistency
	if ( ! checkFlag(DONTSAVE) ) {
	    String error = "ObjectAttribute can't pickle themselves." ;
	    throw new RuntimeException (error) ;
	}
	this.cls = cls ;
    }

    /**
     * Create a new ObjectAttribute instance.
     * @param name The name of the attribute.
     * @param cname The name class for this attribute values.
     * @param def The default value for this attribute.
     * @param flags The attribute flags.
     * @exception RuntimeException If we couldn't resolve the class name.
     */

    public ObjectAttribute(String name, String cname, Object def, int flags) {
	super(name, def, flags) ;
	// Check consistency:
	if ( ! checkFlag(DONTSAVE) ) {
	    String error = "ObjectAttribute can't pickle themselves." ;
	    throw new RuntimeException (error) ;
	}
	// Resolve the class:
	try {
	    this.cls = Class.forName(cname) ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    throw new RuntimeException("unable to resolve class "+cname) ;
	}
	this.type = "java.lang.Object".intern();
    }

}

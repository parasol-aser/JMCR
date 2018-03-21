// ObjectAttribute.java
// $Id: ObjectAttribute.java,v 1.2 2010/06/15 17:53:10 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade;

import java.io.DataInputStream;
import java.io.DataOutputStream;

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
     * Get the number of bytes required to save that attribute value.
     * @param The value about to be pickled.
     * @return The number of bytes needed to pickle that value.
     * @exception RuntimeException Always throw since ObjectAttribute 
     * can't be pickled.
     */

    public final int getPickleLength(Object value) {
	throw new RuntimeException("Can't pickle ObjectAttribute");
    }

    /**
     * The ObjectAttribute values can't be pickled.
     */

    public void pickle(DataOutputStream out, Object obj) {
	throw new RuntimeException ("Can't pickle ObjectAttribute.");
    }

    /**
     * The ObjectAttribute values can't be unpickled.
     */

    public Object unpickle(DataInputStream in) {
	throw new RuntimeException("Can't unpickle ObjectAttribute.");
    }

    /**
     * Create a new ObjectAttribute instance.
     * @param name The name of the attribute.
     * @param cls The class for this attribute values.
     * @param def The default value for this attribute.
     * @param flags The attribute flags.
     */

    public ObjectAttribute(String name, Class cls, Object def,
			   Integer  flags) {
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

    public ObjectAttribute(String name, String cname, Object def,
			   Integer flags) {
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
	this.type = "java.lang.Object";
    }

}

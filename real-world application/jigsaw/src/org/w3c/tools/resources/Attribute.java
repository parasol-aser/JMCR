// Attribute.java
// $Id: Attribute.java,v 1.1 2010/06/15 12:20:18 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.io.Serializable;

/**
 * Instances of this class describe an attribute of a resource.
 */

abstract public class Attribute implements Serializable {
    /**
     * Flags value - This attribute is computed from the resource state.
     */
    public static final int COMPUTED = (1<<0) ;
    /**
     * Flag value - This attribute is editable.
     */
    public static final int EDITABLE = (1<<1) ;
    /**
     * Flag value - This attribute is mandatory.
     */
    public static final int MANDATORY = (1<<2) ;
    /**
     * Flag value - This attribute shouldn't be saved.
     */
    public static final int DONTSAVE = (1<<3) ;
    /**
     * The attribute name.
     */
    protected String name = null ;
    /**
     * The attribute's value type, as the name of its class.
     */
    protected String type = null ;
    /**
     * The attribute's default value.
     */
    private transient Object defvalue = null ;
    /**
     * The associated flags (see the predefined flags).
     */
    protected int flags = 0 ;

    public String getFlag() {
	return String.valueOf(flags);
    }

    public void setFlag(String flag) {
	try {
	    flags = Integer.parseInt(flag);
	} catch (Exception ex) {
	    flags = 0;
	}
    }

    /**
     * Get this attribute name.
     * @return A String giving the attribute name.
     */

    public String getName() {
	return name ;
    }

    /**
     * set the attribute name.
     * @param name the attribute name.
     */
    public void setName(String name) {
	this.name = name.intern();
    }

    /**
     * Get this attribute type.
     */

    public String getType() {
	return type ;
    }

    /**
     * Check some flag on this attribute description.
     */

    public boolean checkFlag(int tst) {
	return (flags & tst) == tst ;
    }

    /**
     * Get this attribute default value.
     * @return A default value for this attribute (may be
     *    <strong>null</strong>).
     */

    public Object getDefault() {
	return defvalue ;
    }

    /**
     * Is the provided object a suitable value for this attribute ?
     * If so, store it into the given store.
     * @param value The value to check.
     * @param store The array to store the value to if succeed.
     * @param idx The location in the above array.
     * @return A boolean <strong>true</strong> if this object can be used
     *    as a value for this attribute.
     * @exception IllegalAttributeAccess If the provided value doesn't match
     *    the expected type.
     */

    public abstract boolean checkValue(Object value) ;

    public abstract String stringify(Object value) ;

    /**
     * Private constructore to create a new resource attribute description.
     * @param name The name of the attribute.
     * @param def Its default value.
     * @param flags Its associated flags.
     */

    public Attribute(String name, Object def, int flags) {
	this.name     = name.intern() ;
	this.defvalue = def ;
	this.flags    = flags ;
    }

    /**
     * Empty contructor, (cls.newInstance())
     */
    public Attribute() {
	this.defvalue = null ;
	this.flags    = COMPUTED;
    }
}

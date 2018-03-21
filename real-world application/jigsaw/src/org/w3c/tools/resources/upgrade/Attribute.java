// Attribute.java
// $Id: Attribute.java,v 1.1 2010/06/15 12:22:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade ;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    /**
     * Get this attribute name.
     * @return A String giving the attribute name.
     */

    public String getName() {
	return name ;
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
     * Stringify a value of this kind.
     * @param obj The value to stringify.
     */

    public String stringify (Object value) {
	return value.toString() ;
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

    abstract
    public boolean checkValue(Object value) ;

    /**
     * Get number of bytes needed to pickle that attribute.
     * This method is always called before pickling an attribute, to
     * get the length of that attribute value, and record it before saving
     * the actual bytes. This allows, for example, to skip attribute whose
     * definition was removed from a class. 
     * <p>In an ASCII format, this plays a role similar to emitting
     * a newline.
     * @param value The value that is about to be pickled.
     * @return The number of bytes needed to pickle that value.
     */

    abstract 
    public int getPickleLength(Object value);

    /**
     * Pickle an attribute of this type to the given stream.
     * This method is used to make attribute values persistent, the pickle
     * method should dump the provided value in whatever format, provided
     * its unpickle method is able to restore it.
     * @param out The DataOutputStream to dump the object to.
     * @param obj The object to pickle.
     * @exception IOException If some IO error occured while dump the
     *    attribute.
     */

    abstract
    public void pickle(DataOutputStream out, Object obj) 
	throws IOException;

    /**
     * Unpickle an attribute of this type from the given stream.
     * This method is used to restore a pickled attribute value from the given
     * stream. It should read in the format it used at pickle time, and
     * consume the same number of bytes from the stream.
     * @param in The DataInputStream to read from.
     * @return The object value.
     * @exception IOException If some IOError occured while reading the stream.
     */

    abstract 
    public Object unpickle(DataInputStream in)
	throws IOException ;

    /**
     * Private constructore to create a new resource attribute description.
     * @param name The name of the attribute.
     * @param type Its type (as a Java class).
     * @param def Its default value.
     * @param flags Its associated flags.
     */

    public Attribute(String name, Object def, Integer flags) {
	this.name     = name ;
	this.defvalue = def ;
	this.flags    = flags.intValue() ;
    }
}

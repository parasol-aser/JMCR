// AttributeHolder.java
// $Id: AttributeHolder.java,v 1.1 2010/06/15 12:20:23 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.util.Hashtable;

import java.io.PrintStream;

/**
 * An attribute holder is an object that holds a list of attributes.
 * Each of the attributes maintained by a holder are described by an 
 * Attribute object, wich is able to pickle/unpickle its value, provides
 * the status of the attribute (eg mandatory, editable, etc), etc.
 * <p>Given this, an attribute holder is able to pickle its state (made of
 * its attribute values), and unpickle it to any DataOutputStream (resp.
 * DataInputStream).
 * @see Attribute
 * @see Resource
 */

public class AttributeHolder implements Cloneable {
    /**
     * Pointer to this class registered list of attributes.
     */
    protected Attribute attributes[] = null ;
    /**
     * Attribute values.
     */
    protected Object values[] = null ;

    /**
     * Clone this attribute holder, and init it with the given attributes.
     * @param values Attribute values to overide in the clone.
     * @return A clone of this resource.
     */

    public Object getClone(Object values[]) {
	try {
	    // Continue with normal cloning:
	    AttributeHolder cl   = (AttributeHolder) getClass().newInstance();
	    cl.initialize(values);
	    return cl;
	} catch (Exception ex) {
	    String msg = ("Unable to create an instance of "+getClass());
	    throw new HolderInitException(msg);
	}
    }

    /**
     * Clone this attribute holder.
     * The resulting clone will <em>share</em> the attribute values of 
     * the cloned attribute holder. 
     * @return An attribute holder sharing its ancestor attribute values.
     */

    public synchronized Object getClone() {
	Object vs[] = new Object[attributes.length];
	System.arraycopy(values, 0, vs, 0, vs.length);
	return getClone(vs);
    }

    /**
     * Clone this AttributeHolder instance, and initialize it with defaults.
     * This method first clones the receiving attribute holder, and then
     * uses the defaults provided to finish the initialization.
     * @param defs The attribute values, in a Hashtable.
     * @return The clone.
     */

    public synchronized Object getClone(Hashtable defs) {
	try {
	    Object          vs[] = new Object[attributes.length];
	    System.arraycopy(values, 0, vs, 0, vs.length);
	    // Merge the provided attribute values:
	    for (int i = 0 ; i < attributes.length ; i++) {
		Object value = defs.get(attributes[i].getName());
		if ( value != null )
		    vs[i] = value;
	    }
	    return getClone(vs);
	} catch (Exception ex) {
	  String msg = ("Unable to create an instance of "+getClass());
	  throw new HolderInitException(msg);
	}
    }

    /**
     * Get this attribute holders attributes description.
     * The attribute list is guaranteed to be returned always in the same
     * order, wich is fixed at compilation time. This allows for fast access
     * to resource by their position rather than by name.
     * @return An array of Attribute objects, each one containing the
     *    description of one single attribute of the resource.
     * @see org.w3c.tools.resources.Attribute
     */

    public Attribute[] getAttributes() {
	return attributes ;
    }

    /**
     * Lookup up the index of an attribute in our attribute description.
     * @param name The name of the attribute to look for.
     * @return An integer, positive if found, negative otherwise.
     */

    public int lookupAttribute(String name) {
	for (int i = 0 ; i < attributes.length ; i++) {
	    if ( name == attributes[i].getName()) {
		return i;
	    }
	}
	return -1 ;
    }

    /**
     * Set an attribute value.
     * This method sets the value of some attribute of the resource. It marks
     * the resource as being modified, and alert its resource store (so
     * that it knows it will have to save the object at some time in the
     * future).
     * @param idx The attribute index, in the list of attributes advertized by 
     *    the resource.
     * @param value The new value for this attribute.
     * @exception IllegalAttributeAccess if the provided value doesn't 
     * match the attribute expected type.
     */

    synchronized public void setValue(int idx, Object value) {
	// Check the index value:
	if ((idx < 0) || (idx >= attributes.length))
	    throw new IllegalAttributeAccess(this, idx) ;
	// Check the requested attribute's type
	Attribute attr = attributes[idx] ;
	if ( attr.checkValue(value) ) {
	    values[idx] = value ;
	} else {
	    throw new IllegalAttributeAccess(this, attr, value) ;
	}
    }

    /**
     * Set an attribute value.
     * This method sets the value of an attribute, referenced by its name.
     * @param name The attribute name.
     * @param value The new value for the attribute.
     * @exception IllegalAttributeAccess if the provided value doesn't match
     *    the attribute expected type.
     */

    public void setValue(String name, Object value) {
	setValue(lookupAttribute(name), value) ;
    }

    public void setBoolean(int idx, boolean b) {
	setValue(idx, (b ? Boolean.TRUE : Boolean.FALSE)) ;
    }

    public void setChar(int idx, char ch) 
	throws IllegalAttributeAccess
    {
	setValue(idx, new Character(ch)) ;
    }

    public void setDouble(int idx, double d) 
	throws IllegalAttributeAccess
    {
	setValue(idx, new Double(d)) ;
    }

    public void setFloat(int idx, float f) 
	throws IllegalAttributeAccess
    {
	setValue(idx, new Float(f)) ;
    }

    public void setInt(int idx, int i) 
	throws IllegalAttributeAccess
    {
	setValue(idx, new Integer(i)) ;
    }

    public void setLong(int idx, long l) 
	throws IllegalAttributeAccess
    {
	setValue(idx, new Long(l)) ;
    }

    public void setString(int idx, String s) 
	throws IllegalAttributeAccess
    {
	setValue(idx, (Object) s) ;
    }

    /**
     * Generic get of an attribute value.
     * Retreive an attribute value from its index in the resource's attribute
     * list.
     * @param idx The index of the attribute whose value is queried.
     * @param def The default value (if the attribute isn't defined).
     * @return An object, giving the attribute value, or the provided
     *    default if this attribute isn't currently define for the resource.
     * @exception IllegalAttributeAccess if the given index doesn't match any
     *    of the resource's attributes.
     */

    synchronized public Object getValue (int idx, Object def) 
	throws IllegalAttributeAccess
    {
	// Check the provided index:
	if ((idx < 0) || (idx >= attributes.length))
	    throw new IllegalAttributeAccess(this, idx) ;
	Object value = values[idx] ;
	if ( value == null ) 
	    return (def == null) ? attributes[idx].getDefault() : def ;
	else
	    return value ;
    }

    /**
     * Generic get of an attribute value.
     * Retreive an attribute value from its index in the resource's attribute
     * list.
     * THIS VERSION IS NOT SYNCHRONIZED AND THEREFORE SHOULD BE USED
     * ONLY WHEN YOU ARE SURE YOU SHOULD USE THIS, WHICH MEANS ALMOST
     * NEVER!
     * @param idx The index of the attribute whose value is queried.
     * @param def The default value (if the attribute isn't defined).
     * @return An object, giving the attribute value, or the provided
     *    default if this attribute isn't currently define for the resource.
     * @exception IllegalAttributeAccess if the given index doesn't match any
     *    of the resource's attributes.
     */

    public Object unsafeGetValue (int idx, Object def) 
	throws IllegalAttributeAccess
    {
	// Check the provided index:
	if ((idx < 0) || (idx >= attributes.length))
	    throw new IllegalAttributeAccess(this, idx) ;
	Object value = values[idx] ;
	if ( value == null ) 
	    return (def == null) ? attributes[idx].getDefault() : def ;
	else
	    return value ;
    }

    /**
     * Generic get of an attribute value.
     * Get the method of an attribute, by name.
     * @param name The name of the queried attribute.
     * @param def The default value.
     * @exception IllegalAttributeAccess if the given name doesn't match any
     *    of the attribute's name.
     */

    public Object getValue(String name, Object def)
	throws IllegalAttributeAccess
    {
	return getValue(lookupAttribute(name), def) ;
    }

    public boolean getBoolean(int idx, boolean def) {
	Object value = getValue(idx, null) ;
	if ( value == null ) {
	    return def;
	} else if ( value instanceof Boolean ) {
	    return ((Boolean) value).booleanValue() ;
	} else {
	    throw new IllegalAttributeAccess(this
					     , attributes[idx]
					     , "getBoolean") ;
	}
    }

    public char getChar(int idx, char def) {
	Object value = getValue(idx, null) ;
	if ( value == null ) {
	    return def ;
	} else if ( value instanceof Character ) {
	    return ((Character) value).charValue() ;
	} else {
	    throw new IllegalAttributeAccess(this
					     , attributes[idx]
					     , "getChar");
	}
    }

    public double getDouble(int idx, double def) {
	Object value = getValue(idx, null) ;
	if ( value == null ) {
	    return def ;
	} else if ( value instanceof Double ) {
	    return ((Double) value).doubleValue() ;
	} else {
	    throw new IllegalAttributeAccess(this
					     , attributes[idx]
					     , "getDouble") ;
	}
    }
    
    public double unsafeGetDouble(int idx, double def) {
	Object value = unsafeGetValue(idx, null) ;
	if ( value == null ) {
	    return def ;
	} else if ( value instanceof Double ) {
	    return ((Double) value).doubleValue() ;
	} else {
	    throw new IllegalAttributeAccess(this
					     , attributes[idx]
					     , "getDouble") ;
	}
    }

    public float getFloat(int idx, float def) {
	Object value = getValue(idx, null) ;
	if ( value == null ) {
	    return def ;
	} else if ( value instanceof Float ) {
	    return ((Float) value).floatValue() ;
	} else {
	    throw new IllegalAttributeAccess(this
					     , attributes[idx]
					     , "getFloat") ;
	}
    }

    public int getInt(int idx, int def) {
	Object value = getValue(idx, null) ;
	if ( value == null ) {
	    return def ;
	} else if ( value instanceof Integer ) {
	    return ((Integer) value).intValue() ;
	} else {
	    throw new IllegalAttributeAccess(this
					     , attributes[idx]
					     , "getInt") ;
	}
    }

    public long getLong(int idx, long def) {
	Object value = getValue(idx, null) ;
	if ( value == null ) {
	    return def ;
	} else if ( value instanceof Long ) {
	    return ((Long) value).longValue() ;
	} else {
	    throw new IllegalAttributeAccess (this
					      , attributes[idx]
					      , "getLong") ;
	}
    }

    public String getString(int idx, String def) {
	Object value = getValue(idx, null) ;
	if ( value == null ) {
	    return def ;
	} else if ( value instanceof String ) {
	    return (String) value ;
	} else {
	    throw new IllegalAttributeAccess (this
					      , attributes[idx]
					      , "getString") ;
	}
    }

    public String unsafeGetString(int idx, String def) {
	Object value = unsafeGetValue(idx, null) ;
	if ( value == null ) {
	    return def ;
	} else if ( value instanceof String ) {
	    return (String) value ;
	} else {
	    throw new IllegalAttributeAccess (this
					      , attributes[idx]
					      , "getString") ;
	}
    }

    /**
     * Does this resource has defined a value for the given attribute.
     * @param idx The index of the attribute to check.
     * @return A boolean <strong>true</strong> if the resource has a value
     *    for this attribute, <strong>false</strong> otherwise.
     */

    public boolean definesAttribute(int idx) 
	throws IllegalAttributeAccess
    {
	return (getValue(idx, null) != null) ;
    }

    /**
     * Does this resource has defined a value for the given attribute.
     * @param idx The index of the attribute to check.
     * @return A boolean <strong>true</strong> if the resource has a value
     *    for this attribute, <strong>false</strong> otherwise.
     */

    public boolean unsafeDefinesAttribute(int idx) 
	throws IllegalAttributeAccess
    {
	// Check the provided index:
	if ((idx < 0) || (idx >= attributes.length))
	    throw new IllegalAttributeAccess(this, idx) ;
	Object value = values[idx] ;
	return ( value != null ) ;
    }

    /**
     * Does this resource has defined a value for the given attribute.
     * @param name The name of the attribute to check.
     * @return A boolean <strong>true</strong> if the resource has a value
     *    for this attribute, <strong>false</strong> otherwise.
     */

    public boolean definesAttribute(String name) 
	throws IllegalAttributeAccess
    {
	int idx = lookupAttribute(name);
	return (idx >= 0) ? (getValue(idx, null) != null) : false;
    }

    /**
     * Set the values. (MUST be called before initialize).
     * @param defs The Hashtable containing the values.
     */
    public synchronized void pickleValues(Hashtable defs) {
	Object nvalues[] = new Object[attributes.length];
	for (int i = 0 ; i < nvalues.length ; i++) {
	    String attrname = attributes[i].getName() ;
	    nvalues[i]      = defs.get(attrname) ;
	}
	this.values = nvalues ;
    }

    /**
     * Initialization method for attribute holders.
     * Each time an attribute holder get restored, its <code>initialize</code>
     * method gets called. The holder should initialize itself with the set
     * of provided values and perform any additional startup code.
     * @param values The attribute values the holder should initialize from.
     */

    public synchronized void initialize(Object nvalues[]) {
	if (this.values != null) {
	    for (int i = 0 ; i < nvalues.length ; i++) {
		if ( this.values[i] == null ) {
		    this.values[i] = nvalues[i];
		}
	    }
	} else {
	    this.values = nvalues ;
	}
    }

    /**
     * Initialization method for attribute holders.
     * This method allows to initialize an attribute holder by providing
     * its attributes values through a Hashtable mapping attribute names
     * to attribute values.
     * @param defs The Hashtable containing the default values.
     */

    public synchronized void initialize(Hashtable defs) {
	Object values[] = ((this.values == null)
			   ? new Object[attributes.length] 
			   : this.values);
	for (int i = 0 ; i < values.length ; i++) {
	    String attrname = attributes[i].getName() ;
	    Object def      = defs.get(attrname) ;
	    if ( values[i] == null ) {
		values[i] = def ;
	    }
	}
	initialize(values) ;
    }

    /**
     * Debugging purposes only, print this attribute holder.
     * @param out The print stream to print to.
     */

    public void print(PrintStream out) {
	for (int i = 0 ; i < attributes.length ; i++) {
	    if ( values[i] != null ) 
		System.out.println(attributes[i].getName()+"="+values[i]);
	}
    }

    /**
     * Create an attribute holder.
     */

    public AttributeHolder() {
	this.attributes = AttributeRegistry.getClassAttributes(getClass()) ;
	if ((attributes != null) && (attributes.length > 0))
	    this.values = new Object[attributes.length] ;
    }

}

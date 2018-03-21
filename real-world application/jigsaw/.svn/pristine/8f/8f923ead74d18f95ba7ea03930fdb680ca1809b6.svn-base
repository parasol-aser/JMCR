// AttributeRegistry.java
// $Id: AttributeRegistry.java,v 1.2 2010/06/15 17:53:10 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade ;

import java.util.Hashtable;
import java.util.Vector;

class ClassAttributes {
    Attribute fixed[] = null ;
    Vector    attrs   = null ;
    int       nextid  = 0 ;

    /**
     * Fix the description of the attributes for this record.
     */

    void fix() {
	if ( fixed == null ) {
	    fixed = new Attribute[attrs.size()] ;
	    attrs.copyInto(fixed) ;
	    attrs = null;
	}
    }

    /**
     * Add a new attribute description into this record.
     * @param attr The new attribute description.
     * @return The attribute index.
     */

    int add (Attribute attr) {
	if ( attrs == null ) 
	    throw new RuntimeException ("add in a fixed record.");
	attrs.addElement(attr) ;
	return nextid++ ;
    }

    ClassAttributes(int idx) {
	this.fixed  = null ;
	this.attrs  = new Vector() ;
	this.nextid = idx ;
    }

    ClassAttributes(ClassAttributes sup) {
	this.fixed  = null ;
	this.attrs  = new Vector(sup.fixed.length) ;
	this.nextid = sup.nextid ;
	for (int i = 0 ; i < sup.fixed.length ; i++) 
	    attrs.addElement(sup.fixed[i]) ;
    }
}

public class AttributeRegistry {
    private static Hashtable registery = new Hashtable() ;
    private static Class top = null ;

    static {
	try {
	    top = Class.forName("org.w3c.tools.resources.AttributeHolder") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
    }

    /**
     * Register a new attribute for a given class.
     * This method create the approrpriate attribute description record if
     * required, and return the index of this attribute in the corresponding
     * holder instances.
     * @param cls The class that defines this attribute.
     * @param attr The attribute to declare.
     * @return The attribute index.
     */

    public static synchronized 
    int registerAttribute(Class cls, Attribute attr) {
	// Do we have a description record for this class ?
	ClassAttributes record = (ClassAttributes) registery.get(cls) ;
	if ( record == null ) {
	    // Create and register the record:
	    if ( cls == top ) {
		record = new ClassAttributes(0) ;
	    } else {
		// Get our super class record:
		for (Class clsptr = cls.getSuperclass()
			 ; (record == null)
			 ; clsptr = clsptr.getSuperclass()) {
		    record = (ClassAttributes) registery.get(clsptr) ;
		    if ( clsptr == top ) {
			if ( record == null )
			    record = new ClassAttributes(0);
			break ;
		    }
		}
		if ( record == null )
		    throw new RuntimeException ("inconsistent state.");
		record.fix() ;
		record = new ClassAttributes(record) ;
	    }
	    registery.put(cls, record) ;
	}
	return record.add(attr) ;
    }

    /**
     * Get this class declared attributes.
     * @param cls The class we are querying.
     * @return An array of Attribute instances, describing each of the 
     *    attributes of all instances of the class, or <strong>null</strong>
     *    if the class hasn't defined any attributes.
     */

    public static synchronized 
    Attribute[] getClassAttributes(Class cls) {
	Object result = registery.get(cls) ;
	while ((cls != top) && (result == null)) {
	    cls    = cls.getSuperclass() ;
	    result = registery.get(cls) ;
	}
	// Fix the resulting record before returning it:
	if ( result == null )
	    return null ;
	ClassAttributes record = (ClassAttributes) result ;
	record.fix() ;
	return record.fixed ;
    }

    /**
     * Get the name of the class that has declared this attribute.
     * @param cls The class that makes the starting point of lookup.
     * @param attr The attribute we are looking for.
     * @return The name of the class that defined that attribute, or <strong>
     * null</strong>.
     */

    public static Class getAttributeClass(Class cls, String attrname) {
	Class lookup = cls;
	// We lookup until we find the class that doesn't have this attribute:
	while (true) {
	    boolean   found = false;
	    Attribute a[]   = getClassAttributes(cls);
	    // Lookup the attribute in that set:
	    if ( a != null ) {
		for (int i = 0 ; i < a.length ; i++) {
		    if ((found = a[i].getName().equals(attrname)) )
			break;
		}
	    }
	    if ( found ) {
		lookup = cls;
		cls    = cls.getSuperclass();
	    } else {
		return (lookup == cls) ? null : lookup;
	    }
	}
	
    }

}

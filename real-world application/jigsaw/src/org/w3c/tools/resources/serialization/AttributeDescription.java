// AttributeDescription.java
// $Id: AttributeDescription.java,v 1.2 2010/06/15 17:53:06 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources.serialization;

import org.w3c.tools.resources.Attribute;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class AttributeDescription {

    Attribute attribute = null;
    String    classname = null;
    String    name      = null;
    Object    value     = null;

    /**
     * Get the attribute class name
     * @return a String
     */
    public String getClassName() {
	return classname;
    }

    /**
     * Get the attribute name.
     * @return a String
     */
    public String getName() {
	return name;
    }

    /**
     * Get the attribute value
     * @return an Object
     */
    public Object getValue() {
	return value;
    }

    /**
     * Set the attribute value.
     * @param the new value
     */
    public void setValue(Object value) {
	this.value = value;
    }

    /**
     * Get the attribute itself.
     * @return an Attribute instance
     * @see org.w3c.tools.resources.Attribute
     */
    public Attribute getAttribute() {
	return attribute;
    }

    /**
     * Constructor.
     * @param classname The attribute class name
     * @param name the attribute name
     * @param value the attribute value
     */
    private AttributeDescription(String classname, String name, Object value) 
    {
	this.classname = classname;
	this.name      = name;
	this.value     = value;
	try {
	    Class cls = Class.forName(classname);
	    this.attribute = (Attribute)cls.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    this.attribute = null;
	}
    }

    /**
     * Constructor.
     * @param attribute the attribute itself
     * @param value the attribute value.
     */
    public AttributeDescription(Attribute attribute, Object value) {
	this.name      = attribute.getName();
	this.value     = value;
	this.classname = attribute.getClass().getName();
	this.attribute = attribute;
    }

}

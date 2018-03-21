// ResourceDescription.java
// $Id: ResourceDescription.java,v 1.1 2010/06/15 12:28:13 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources.serialization;

import java.util.Vector;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFrame;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ResourceDescription {

    String               classname    = null;
    String               classes[]    = null;
    String               interfaces[] = null;
    String               identifier   = null;
    AttributeDescription attributes[] = null;
    String               children[]   = null;

    /**
     * Get a clone of this resource description but with only the
     * given list of attribute descriptions.
     * @param attrs the new attribute descriptions
     * @return a ResourceDescription;
     */
    public ResourceDescription getClone(AttributeDescription attrs[]) {
	ResourceDescription descr = new ResourceDescription(classname);
	descr.identifier          = identifier;
	descr.children            = children;
	descr.attributes          = attrs;
	descr.classes             = classes;
	descr.interfaces          = interfaces;
	return descr;
    }

    /**
     * Get this resource class hierarchy.
     * @return a String array
     */
    public String[] getClassHierarchy() {
	return classes;
    }

    /**
     * Get this resource interfaces
     * @return a String array
     */
    public String[] getInterfaces() {
	return interfaces;
    }

    public String[] getClassesAndInterfaces() {
	String all[] = new String[interfaces.length+classes.length];
	System.arraycopy(classes, 0, all, 0, classes.length);
	System.arraycopy(interfaces, 0, all, classes.length, 
			 interfaces.length);
	return all;
    }

    /**
     * Get the resource Class name.
     * @return a String
     */
    public String getClassName() {
	return classname;
    }

    /**
     * Get the resource identifier.
     * @return a String instance
     */
    public String getIdentifier() {
	if ((identifier == null) && (attributes != null)) {
	    for (int i = 0 ; i < attributes.length ; i ++) {
		if (attributes[i].getName().equals("identifier"))
		    identifier = (String)attributes[i].getValue();
	    }
	}
	return identifier;
    }

    /**
     * get the children identifiers
     * @return a String array
     */
    public String[] getChildren() {
	return children;
    }

    /**
     * Set the children names.
     * @param a String array
     */
    public void setChildren(String children[]) {
	this.children = children;
    }

    /**
     * Get the attributes description.
     * @return an AttributeDescription array
     * @see AttributeDescription
     */
    public AttributeDescription[] getAttributeDescriptions() {
	return attributes;
    }

    /**
     * Get the description of the frames associated to this resource.
     * @return a ResourceDescription array.
     */
    public ResourceDescription[] getFrameDescriptions() {
	for (int i = 0 ; i < attributes.length ; i ++) {
	    Object value = attributes[i].getValue();
	    if (value instanceof ResourceDescription[])
		return (ResourceDescription[])value;
	}
	return new ResourceDescription[0];
    }

    /**
     * Constructor.
     * @param resource the resource to describe.
     */
    public ResourceDescription(Resource resource) {
	this.classname = resource.getClass().getName();
	//build class hierarchy
	Vector vclasses    = new Vector(8);
	Vector vinterfaces = new Vector(8);
	Class ints[] = resource.getClass().getInterfaces();
	if (ints != null)
	    for (int i = 0 ; i < ints.length ; i++)
		vinterfaces.addElement(ints[i]);
	for (Class c = resource.getClass().getSuperclass(); 
	     c != null; 
	     c = c.getSuperclass()) {
	    vclasses.addElement(c.getName());
	    ints = c.getInterfaces();
	    if (ints != null)
		for (int i = 0 ; i < ints.length ; i++)
		    vinterfaces.addElement(ints[i]);
	}
	this.classes = new String[vclasses.size()];
	vclasses.copyInto(this.classes);
	this.interfaces = new String[vinterfaces.size()];
	vinterfaces.copyInto(this.interfaces);
	//build attributes description
	Attribute attrs  [] = resource.getAttributes();
	Vector vattrs = new Vector(10);
	for (int j = 0 ; j < attrs.length ; j++) {
	    Object value = resource.getValue(j, null);
	    if (value instanceof ResourceFrame[]) {
		ResourceFrame frames[] = (ResourceFrame[])value;
		int len = frames.length;
		ResourceDescription descr[] = new ResourceDescription[len];
		for (int i = 0 ; i < len ; i++)
		    descr[i] = new ResourceDescription(frames[i]);
		vattrs.addElement(new AttributeDescription(attrs[j],
							   descr));
	    } else {
		vattrs.addElement(new AttributeDescription(attrs[j], 
							   value));
	    }
	}
	this.attributes = new AttributeDescription[vattrs.size()];
	vattrs.copyInto(attributes);
    }

    /**
     * Set the attributes description of the resource
     * @param a Vector of AttributeDescription instances
     */
    public void setAttributeDescriptions(Vector attrs) {
	attributes = new AttributeDescription[attrs.size()];
	attrs.copyInto(attributes);
    }

    /**
     * Set the resource class hierarchy.
     * @param a String array
     */
    public void setClassHierarchy(String classes[]) {
	this.classes = classes;
    }

    /**
     * Set the resource class hierarchy.
     * @param a String array
     */
    public void setInterfaces(String interfaces[]) {
	this.interfaces = interfaces;
    }

    /**
     * Set the resource class hierarchy.
     * @param a Vector of String instances
     */
    public void setClassHierarchy(Vector vclasses) {
	this.classes = new String[vclasses.size()];
	vclasses.copyInto(this.classes);
    }

    /**
     * Set the resource class hierarchy.
     * @param a Vector of String instances
     */
    public void setInterfaces(Vector vinterfaces) {
	this.interfaces = new String[vinterfaces.size()];
	vinterfaces.copyInto(this.interfaces);
    }
    /**
     * Constructor.
     * @param classname the resource class name
     */
    public ResourceDescription(String classname) {
	this.classname  = classname;
	this.interfaces = new String[0];
	this.classes    = new String[0];
    }

}

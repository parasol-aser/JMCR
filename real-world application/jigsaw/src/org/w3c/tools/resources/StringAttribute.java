// StringAttribute.java
// $Id: StringAttribute.java,v 1.1 2010/06/15 12:20:20 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

/**
 * The generic description of an StringAttribute.
 */

public class StringAttribute extends SimpleAttribute {

    /**
     * Is the given object a valid StringAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if value is valid.
     */

    public boolean checkValue(Object obj) {
	return (obj instanceof String) || (obj == null) ;
    }

    public String pickle(Object obj) {
	return (String)obj;
    }

    public Object unpickle (String value) {
	return value;
    }

    /**
     * Create a description for a generic String attribute.
     * @param name The attribute name.
     * @param def The default value for these attributes.
     * @param flags The associated flags.
     */

    public StringAttribute(String name, String def, int flags) {
	super(name, def, flags) ;
	this.type = "java.lang.String".intern();
    }

    public StringAttribute() {
	super();
    }
}



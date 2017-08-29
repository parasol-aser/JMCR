// SimpleAttribute.java
// $Id: SimpleAttribute.java,v 1.1 2010/06/15 12:20:15 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
abstract public class SimpleAttribute extends Attribute {

    /**
     * Unpickle an attribute from a string.
     * @param array the String.
     * @return a Object.
     */
    public abstract Object unpickle(String value);

    /**
     * Pickle an attribute into a String.
     * @param array the attribute
     * @return a String.
     */
    public abstract String pickle(Object obj);

    public String stringify(Object value) {
	return pickle(value);
    }

    public SimpleAttribute(String name, Object def, int flags) {
	super(name, def, flags);
    }

    public SimpleAttribute() {
	super();
    }

}

// LongAttribute.java
// $Id: LongAttribute.java,v 1.1 2010/06/15 12:20:19 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

/**
 * The generic description of an LongAttribute.
 */

public class LongAttribute extends SimpleAttribute {

    /**
     * Is the given object a valid LongAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */

    public boolean checkValue(Object obj) {
	return (obj instanceof Long) || (obj == null) ;
    }

    /**
     * Pickle an integer to the given output stream.
     * @param obj The object to pickle.
     */

    public String pickle(Object obj) {
	return ((Long) obj).toString();
    }

    /**
     * Unpickle an integer from the given input stream.
     * @param value the string representation of this integer
     * @return An instance of Integer.
     */

    public Object unpickle (String value) {
	return Long.valueOf(value);
    }

    public LongAttribute(String name, Long def, int flags) {
	super(name, def, flags) ;
	this.type = "java.lang.Long".intern();
    }

    public LongAttribute() {
	super() ;
    }

}

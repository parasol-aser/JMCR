// IntegerArrayAttribute.java
// $Id: IntegerArrayAttribute.java,v 1.1 2010/06/15 12:20:25 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

/**
 * The generic description of an IntegerArrayAttribute.
 */

public class IntegerArrayAttribute extends ArrayAttribute {

    /**
     * Is the given object a valid IntegerArrayAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */

    public boolean checkValue(Object obj) {
	return (obj instanceof int[]) ;
    }

    public String[] pickle(Object array) {
	if (array == null)
	    return null;
	int ints[] = (int[]) array;
	int len = ints.length;
	String strings[] = new String[len];
	for (int i = 0 ; i < len ; i++) {
	    strings[i] = String.valueOf(ints[i]);
	}
	return strings;
    }

    public Object unpickle (String array[]) {
	if (array.length < 1)
	    return null;
	int len = array.length;
	int ints[] = new int[len];
	for (int i = 0 ; i < len ; i++) {
	    ints[i] = Integer.parseInt(array[i]);
	}
	return ints;
    }

    public IntegerArrayAttribute(String name, int def[], int flags) {
	super(name, def, flags) ;
	this.type = "int[]".intern();
    }

    public IntegerArrayAttribute() {
	super();
    }

}

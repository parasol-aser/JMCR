// BooleanField.java
// $Id: BooleanField.java,v 1.1 2010/06/15 12:27:23 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.forms ;

public class BooleanField extends OptionField {
    private static String bools[] = { "true", "false" } ;
    /**
     * Our current value.
     */
    Boolean value = Boolean.TRUE ;

    /**
     * Do we accept the given change ?
     */

    public boolean acceptChange(int idx) {
	try {
	    setValue((idx == 0) ? Boolean.TRUE : Boolean.FALSE, true, false) ;
	} catch (IllegalFieldValueException ex) {
	    throw new RuntimeException ("implementation bug.");
	}
	return true ;
    }

    /**
     * Set this field's boolean value.
     * @param value A Boolean object.
     * @param udate Should we update the editor's view ?
     * @exception IllegalFieldValueException If the field rejected the value.
     */

    public void setValue(Object value, boolean notify, boolean update)
	throws IllegalFieldValueException
    {
	if ( ! (value instanceof Boolean) )
	    throw new IllegalFieldValueException (value) ;
	this.value = (Boolean) value ;
	super.setValue((this.value.booleanValue() ? 0 : 1), notify, update) ;
    }

    /**
     * Get this field's value according to its native type.
     */

    public Object getValue() {
	return value ;
    }

    /**
     * Get this field's value as a boolean.
     */

    public Boolean getBooleanValue() {
	return value ;
    }

    /**
     * Create a boolean field.
     * @exception IllegalFieldValueException If the field rejected the value.
     */

    public BooleanField(FormManager manager
			, String name, String title
			, boolean value) 
	throws IllegalFieldValueException
    {
	super(manager, name, title, bools, (value ? 0 : 1)) ;
	this.value = (value ? Boolean.TRUE : Boolean.FALSE) ;
    }

}

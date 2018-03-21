// FormField.java
// $Id: FormField.java,v 1.1 2010/06/15 12:27:23 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.forms ;

import java.awt.Component;

abstract public class FormField {
    /**
     * The field's name.
     */
    protected String name = null ;
    /**
     * The field's title.
     */
    protected String title = null ;
    /**
     * The associated form manager.
     */
    protected FormManager manager = null ;

    /**
     * Get the field's name.
     */

    public String getName() {
	return name ;
    }

    /**
     * Get the field's title.
     */

    public String getTitle() {
	return title ;
    }

    /**
     * Our editor is telling us that it got the focus, propagate to manager.
     */

    protected void gotFocus() {
	manager.gotFocus(this) ;
    }

    /**
     * Get this field value's in its native type.
     */

    abstract public Object getValue() ;

    /**
     * Set this field value.
     * @param value This field's new value.
     * @param notify Should we notify the manager for this change ?
     * @param update Update the editor view, if <strong>true</strong>.
     * @exception IllegalFieldValueException If the field rejected the 
     *    value.
     */

    abstract public void setValue(Object value, boolean notify, boolean update)
	throws IllegalFieldValueException ;

    /**
     * Set this field's value, notifying the manager.
     * @param value The new field value.
     * @param update Should we update the editor's view ?
     * @exception IllegalFieldValueException If the field rejected the value.
     */

    public void setValue(Object value, boolean update) 
	throws IllegalFieldValueException
    {
	setValue(value, true, update) ;
    }
	
    /**
     * Get this field graphical editor.
     */

    abstract Component getEditor() ;

    /**
     * Form field basic constructor.
     */

    public FormField(FormManager manager, String name, String title) {
	this.manager = manager ;
	this.name    = name ;
	this.title   = title ;
    }

}

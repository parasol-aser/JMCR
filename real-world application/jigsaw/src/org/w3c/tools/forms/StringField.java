// StringField.java
// $Id: StringField.java,v 1.1 2010/06/15 12:27:21 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.forms ;

import java.awt.Component;
import java.awt.Event;
import java.awt.TextComponent;
import java.awt.TextField;

class StringFieldEditor extends TextField {
    StringField field = null ;

    /**
     * Handle the action: the field edition is finished.
     */

    public boolean action (Event evt, Object arg) {
	if ( ! field.acceptChange(getText()) ) {
	    String oldtxt = field.getStringValue() ;
	    setText((oldtxt != null) ? oldtxt : "") ;
	}
	return true ;
    }

    /**
     * Set the editor's value.
     */

    public void setValue(String value) {
	setText((value == null) ? "" : value) ;
    }

    /**
     * This editor got the focus, notify the form manager.
     */

    public boolean gotFocus(Event evt, Object what) {
	field.gotFocus() ;
	return false ;
    }

    /**
     * Handle event: manage fields walking.
     * @param evt The event to handle.
     */

    public boolean keyDown(Event evt, int key) {
	switch (key) {
	  case 9:
	  case 10:
	      action(evt, evt.arg) ;
	      field.manager.nextField() ;
	      return true ;
	  default:
	      return super.keyDown(evt, key) ;
	}
    }

    StringFieldEditor(StringField field, String defvalue) {
	super((defvalue == null) ? "" : defvalue, 32) ;
	this.field = field ;
    }

}

/**
 * An editor for string fields.
 */

public class StringField extends FormField {
    /**
     * Our current value.
     */
    String value = null ;
    /**
     * Our GUI editor.
     */
    StringFieldEditor editor = null ;

    /**
     * Do we want to accept this value as our new value.
     */

    public boolean acceptChange(String value) {
	try {
	    setValue(value, true, false) ;
	} catch (IllegalFieldValueException ex) {
	    throw new RuntimeException ("implementation bug.") ;
	}
	return true ;
    }

    /**
     * Get this field's value according to its native type.
     */

    public Object getValue() {
	return value ;
    }

    /**
     * Get this field value as a String.
     */

    public String getStringValue() {
	return value ;
    }

    /**
     * Set this field value.
     * @exception IllegalFieldValueException if the value isn't accepted
     */

    public void setValue(Object value, boolean notify, boolean update)
	throws IllegalFieldValueException
    {
	if ( ! (value instanceof String) )
	    throw new IllegalFieldValueException (value) ;
	setValue((String) value, notify, update) ;
    }

    /**
     * Set this field value.
     * @exception IllegalFieldValueException if the value isn't accepted
     */
    public void setValue(String value, boolean notify, boolean update) 
	throws IllegalFieldValueException
    {
	this.value = value;
	if ( update && (editor != null) )
	    editor.setValue(value) ;
	if ( notify )
	    manager.notifyChange(this) ;
    }

    /**
     * FormField implementation - Get the editor for the field.
     */

    public Component getEditor() {
	if ( editor == null ) 
	    editor = new StringFieldEditor(this, value) ;
	return editor ;
    }

    /**
     * Create a new field for string edition.
     * @param manager The form manager.
     * @param value The initial value for the field.
     */

    public StringField (FormManager manager
			, String name, String title
			, String value) {
	super(manager, name, title) ;
	this.value  = value ;
    }

    /**
     * Create a new field for string edition, with no initial value.
     * @param manager The form manager.
     * @param name The field's name.
     * @param title The field's title.
     */

    public StringField (FormManager manager, String name, String title) {
	this(manager, name, title, null) ;
    }

}

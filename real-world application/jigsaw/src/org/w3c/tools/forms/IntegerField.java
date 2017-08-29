// IntegerField.java
// $Id: IntegerField.java,v 1.1 2010/06/15 12:27:23 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.forms ;

import java.awt.Component;
import java.awt.Event;
import java.awt.TextComponent;
import java.awt.TextField;

class IntegerFieldEditor extends TextField {
    IntegerField field = null ;

    public boolean action (Event evt, Object arg) {
	try {
	    Integer ival = new Integer(Integer.parseInt(getText()));
	    if ( ! field.acceptChange(ival) )
		setText(field.getValue().toString()) ;
	    return true ;
	} catch (NumberFormatException ex) {
	    // This should never happen!
	    throw new RuntimeException ("implementation bug !");
	}
    }

    public void setValue(Integer ival) {
	setText(ival.toString()) ;
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
	  case '0': case '1': case '2': case '3': case '4':
	  case '5': case '6': case '7': case '8': case '9':
	  case Event.LEFT: case Event.RIGHT: case 96: case 127:
	      return super.keyDown(evt, key) ;
	  default:
	      // Not allowed here
	      return true ;
	}
    }

    IntegerFieldEditor(IntegerField field, Integer ival) {
	super(ival.toString()) ;
	this.field = field ;
    }
}

/**
 * An editor for integer field.
 */

public class IntegerField extends FormField {
    /**
     * Our current value.
     */
    Integer   ival   = new Integer(0) ;
    /**
     * Our GUI editor.
     */
    IntegerFieldEditor editor = null ;

    /**
     * Get an editor to edit this form field.
     */

    public Component getEditor() {
	if ( editor == null )
	    editor = new IntegerFieldEditor(this, ival) ;
	return editor ;
    }

    /**
     * Get this field integer value.
     * @return The current field's value.
     */

    public int getIntValue() {
	return ival.intValue() ;
    }

    /**
     * Do we accept this new value as our setting ?
     */

    public boolean acceptChange(Integer ival) {
	try {
	    setValue(ival, true, false) ;
	} catch (IllegalFieldValueException ex) {
	    throw new RuntimeException ("implementation bug.") ;
	}
	return true ;
    }

    /**
     * Get our value, in its native type.
     */

    public Object getValue() {
	return ival ;
    }

    /**
     * Set our value.
     * @param value The proposed value.
     * @exception IllegalFieldValueException if the value isn't accepted
     */

    public void setValue (Integer ival, boolean notify, boolean update) 
	throws IllegalFieldValueException
    {
	this.ival = ival ;
	if ( update && (editor != null))
	    editor.setValue(ival) ;
	if ( notify )
	    manager.notifyChange(this) ;
    }

    /**
     * Set our value.
     * @param value The proposed value.
     * @exception IllegalFieldValueException if the value isn't accepted
     */
    public void setValue(Object value, boolean notify, boolean update)
	throws IllegalFieldValueException
    {
	if ( ! (value instanceof Integer) )
	    throw new IllegalFieldValueException (value) ;
	setValue((Integer) value, update) ;
    }

    public IntegerField (FormManager manager, String name, String title
			, Integer ival){
	super(manager, name, title) ;
	this.ival    = ival ;
    }

    public IntegerField (FormManager manager, String name, String title
			 , int value) {
	this(manager, name, title, new Integer(value)) ;
    }

}

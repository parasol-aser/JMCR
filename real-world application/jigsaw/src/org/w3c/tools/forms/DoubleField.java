// DoubleField.java
// $Id: DoubleField.java,v 1.1 2010/06/15 12:27:22 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.forms ;

import java.awt.Component;
import java.awt.Event;
import java.awt.TextComponent;
import java.awt.TextField;

class DoubleFieldEditor extends TextField {
    DoubleField field  = null ;
    boolean     donext = false ;

    public boolean action (Event evt, Object arg) {
	try {
	    Double dval = Double.valueOf(getText()) ;
	    if ( field.acceptChange(dval) ) {
		donext = true ;
		return true ;
	    }
	} catch (NumberFormatException ex) {
	}
	// The new proposed value was rejected, retreiev the old value:
	Double oldval = (Double) field.getValue() ;
	if ( oldval != null )
	    setText(oldval.toString()) ;
	else
	    setText("") ;
	return true ;
    }

    /**
     * Notify the form manager that we did get the focus.
     */

    public boolean gotFocus(Event evt, Object what) {
	field.gotFocus() ;
	return false ;
    }
	
    public void setValue(Double dval) {
	setText(dval.toString()) ;
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
	      if ( donext ) {
		  donext = false ;
		  field.manager.nextField() ;
	      }
	      return true ;
	  default:
	      return super.keyDown(evt, key) ;
	}
    }

    DoubleFieldEditor(DoubleField field, Double dval) {
	super((dval != null) ? dval.toString() : "") ;
	this.field = field ;
    }

}

public class DoubleField extends FormField {
    /**
     * Our editor for the field value.
     */
    DoubleFieldEditor editor = null ;
    /**
     * Our current value.
     */
    Double value = null ;

    /**
     * Do we want to accept this new value ?
     * @return A boolean, <strong>true</strong> if we accept this new
     *    value.
     */

    public boolean acceptChange (Double dval) {
	try {
	    setValue(dval, true, false) ;
	} catch (IllegalFieldValueException ex) {
	    throw new RuntimeException ("implementation bug.") ;
	}
	return true ;
    }

    /**
     * Get this field's value in its native format (ie Double).
     * @return An instance of Double.
     */

    public Object getValue() {
	return value ;
    }

    /**
     * Get this field's value as a Double.
     * @return An instance of Double.
     */

    public Double getDoubleValue () {
	return value ;
    }

    /**
     * Set this field's value to thegiven object.
     * @param value The new value for the field.
     * @param update Should the editor updates its view ?
     * @exception IllegalFieldValueException If the provided value is not
     *    a Double object.
     */

    public void setValue(Object value, boolean notify, boolean update) 
	throws IllegalFieldValueException
    {
	if ( ! (value instanceof Double) )
	    throw new IllegalFieldValueException(value) ;
	setValue((Double) value, notify, update) ;
    }

    /**
     * Set this field's value to the given Double value.
     * @param value The double value to set the field to.
     * @param update Should the editor updates its view ?
     * @exception IllegalFieldValueException If the value couldn't be set.
     */

    public void setValue(Double value, boolean notify, boolean update)
	throws IllegalFieldValueException
    {
	this.value = value ;
	if ( update && (editor != null) )
	    editor.setValue(value) ;
	if ( notify )
	    manager.notifyChange(this) ;
    }

    /**
     * Get an editor for this field.
     */

    public Component getEditor() {
	if ( editor == null )
	    editor = new DoubleFieldEditor(this, value) ;
	return editor ;
    }

    /**
     * Create a new double field.
     * @param manager The associated form manager.
     * @param name The name for this field.
     * @param title This field's title.
     * @param value The initial value for the field.
     * @exception IllegalFieldValueException If the default value isn't 
     *    accepted by the field.
     */

    public DoubleField (FormManager manager
			, String name, String title
			, Double value) 
	throws IllegalFieldValueException 
    {
	super(manager, name, title) ;
	setValue(value, false) ;
    }

    /**
     * Create a new uninitialized double field.
     * @param manager The asociated form manager.
     * @param name The field's name.
     * @param title The field's title.
     */

    public DoubleField (FormManager manager, String name, String title) {
	super(manager, name, title) ;
	this.value = null ;
    }
			
}

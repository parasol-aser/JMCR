// OptionField.java
// $Id: OptionField.java,v 1.1 2010/06/15 12:27:22 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.forms ;

import java.awt.Choice;
import java.awt.Component;
import java.awt.Event;

class OptionFieldEditor extends Choice {
    OptionField field = null ;

    public boolean action(Event evt, Object arg) {
	if ( ! field.acceptChange(getSelectedIndex()) ) 
	    select(field.getIntValue()) ;
	return true ;
    }

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

    public void setValue(int idx) {
	select(idx) ;
    }

    OptionFieldEditor(OptionField field, String options[], int cursor) {
	super() ;
	this.field = field ;
	for (int i = 0 ; i < options.length ; i++)
	    addItem(options[i]) ;
	select(cursor) ;
    }
}

public class OptionField extends FormField {
    /**
     * List of allwed options.
     */
    String options[] = null ;
    /**
     * Our value.
     */
    int cursor = 0 ;
    /**
     * Our editor.
     */
    OptionFieldEditor editor = null ;

    /**
     * Do we accept to change to the given (valid) index ?
     */

    public boolean acceptChange(int idx) {
	try {
	    setValue(idx, true, false) ;
	} catch (IllegalFieldValueException ex) {
	    throw new RuntimeException ("implementation bug.") ;
	}
	return true ;
    }

    /**
     * Get this field's value in its native type.
     * @return The currently selected option as a String.
     */

    public Object getValue() {
	return options[cursor] ;
    }

    /**
     * Get the selected option as its index in our array of options.
     */

    public int getIntValue() {
	return cursor ;
    }

    /**
     * Get the selected option as a String.
     */

    public String getStringValue() {
	return options[cursor];
    }

    /**
     * Set this option's value.
     * @param value The new value.
     * @param update Should we update the editor view.
     * @exception IllegalFieldValueException if the value isn't accepted
     */

    public void setValue (Object value, boolean notify, boolean update)
	throws IllegalFieldValueException
    {
	if ( ! (value instanceof String) )
	    throw new IllegalFieldValueException (value) ;
	setValue((String) value, notify, update) ;
    }

    /**
     * Set this option's value.
     * @param idx The index of the option to set.
     * @param update Should we update our editor's view.
     * @exception IllegalFieldValueException if the value isn't accepted
     */

    public void setValue(int idx, boolean notify, boolean update) 
	throws IllegalFieldValueException
    {
	if ((idx < 0) || (idx >= options.length))
	    throw new IllegalFieldValueException (new Integer(idx));
	this.cursor = idx ;
	if (update && (editor != null))
	    editor.setValue(cursor) ;
	if ( notify )
	    manager.notifyChange(this) ;
    }

    /**
     * Set this option's value.
     * @exception IllegalFieldValueException if the value isn't accepted
     */

    public void setValue(String value, boolean notify, boolean update) 
	throws IllegalFieldValueException
    {
	for (int i = 0 ; i < options.length ; i++) {
	    if ( options[i].equals(value) )
		setValue(i, notify, update);
	}
	throw new IllegalFieldValueException (value) ;
    }

    /**
     * Get an editor to edit this option's value.
     */

    public Component getEditor() {
	if ( editor == null ) 
	    editor = new OptionFieldEditor(this, options, cursor) ;
	return editor ;
    }

    /**
     * Create an option field.
     * @exception IllegalFieldValueException if the value isn't accepted
     */

    public OptionField (FormManager manager
			, String name, String title
			, String options[], int value)
	throws IllegalFieldValueException
    {
	super(manager, name, title) ;
	this.options = options ;
	setValue(value, false, false) ;
    }

}

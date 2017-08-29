// FormManager.java
// $Id: FormManager.java,v 1.1 2010/06/15 12:27:22 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.forms ;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Window;

import java.util.Vector;

public class FormManager {
    /**
     * Our list of field, at description time.
     */
    protected Vector vfields = null ;
    /**
     * Our list of fields, at runtime.
     */
    protected FormField fields[] = null ;
    /**
     * The current field being edited, as an index in our fields.
     */
    protected int    cursor = 0 ;
    /**
     * Is this form description completed ?
     */
    protected boolean finished = false ;
    /**
     * The form's title.
     */
    protected String title = null ;
    /**
     * The form grphical UI.
     */
    protected FormPanel panel = null ;

    /**
     * Callback for field value's change.
     * @param field The field that changed.
     */

    public void notifyChange(FormField field) {
	return ;
    }

    /**
     * Construct the Panel to edit the form.
     * @return A Panel instance, layed out for this form edition.
     */

    protected FormPanel createPanel() {
	panel = new FormPanel(this) ;
	for (int i = 0 ; i < fields.length ; i++) 
	    panel.addField (fields[i].getTitle(), fields[i].getEditor()) ;
	return panel ;
    }

    /**
     * Move to the field whose index is given.
     * @param n The field to move to.
     */

    public void gotoField (int idx) {
	if ((idx < 0) || (idx >= fields.length)) 
	    throw new RuntimeException ("invalid form cursor:"+cursor) ;
	cursor = idx ;
	fields[cursor].getEditor().requestFocus() ;
    }

    /**
     * Move the focus to the next editable field.
     */

    public void nextField() {
	gotoField((cursor+1) % fields.length) ;
    }

    /**
     * Some of our field got the focus, update our cursor.
     * @param field The field that now has the focus.
     */

    protected void gotFocus (FormField field) {
	for (int i = 0 ; i < fields.length ; i++) {
	    if ( fields[i] == field ) {
		cursor = i ;
		break ;
	    }
	}
    }
		
    /**
     * Add a field to the form.
     * @param name The field name (the key by wich this field will be 
     *    accessible.)
     * @param field The field to be created.
     */

    public void addField(FormField field) {
	if ( finished )
	    throw new RuntimeException ("This form has been finished.");
	vfields.addElement (field) ;
    }

    /**
     * Mark the description of the form as completed.
     * Once this method is called, no more fields can be added to the form.
     * This method will perform any required compilation of the form.
     */

    public void finish() {
	if ( finished )
	    return ;
	finished = true ;
	// Compile the vfields into fields, for fast access and no casts:
	fields = new FormField[vfields.size()] ;
	vfields.copyInto(fields) ;
	vfields = null ;
    }

    /**
     * Get the graphical object for editing the form.
     */

    public Panel getPanel() {
	if ( ! finished )
	    finish() ;
	if ( panel == null )
	    panel = createPanel() ;
	return panel ;
    }

    /**
     * Create a new, empty form.
     * @param title The form's title.
     */

    public FormManager(String title) {
	this.title   = title ;
	this.vfields = new Vector() ;
    }

    /**
     * Test.
     * @exception IllegalFieldValueException test
     */

    public static void main (String args[]) 
	throws IllegalFieldValueException
    {
	FormManager manager = new FormManager ("test") ;
	FormField field   = null ;
	// Create the first field:
	field = new StringField(manager, "field-1", "title-1", "value-1");
	manager.addField(field) ;
	// Create the second field:
	field = new StringField(manager, "field-2", "title-2", "value-2") ;
	manager.addField(field) ;
	// Create an Integer field;
	field = new IntegerField(manager, "field-3", "title-3", 10);
	manager.addField(field) ;
	// Create an option field:
	String opts[] = { "option-1", "option-2", "foo", "bar", "etc" };
	field = new OptionField(manager, "field-4", "title-4", opts, 0) ;
	manager.addField(field) ;
	// Create a boolean field.
	field = new BooleanField(manager, "field-5", "title-5", true) ;
	manager.addField(field) ;
	// Create a RangedInteger
	field = new RangedIntegerField(manager, "field-6", "title-6"
				       , 0, 10000, 5000) ;
	manager.addField(field);
	// Display the resulting GUI:
	Panel p = manager.getPanel() ;
	Frame toplevel = new Frame("form test") ;
	toplevel.add ("Center", p) ;
	toplevel.pack() ;
	toplevel.resize(toplevel.preferredSize()) ;
	toplevel.show() ;

    }

}

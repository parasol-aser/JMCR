// RangedIntegerField.java
// $Id: RangedIntegerField.java,v 1.1 2010/06/15 12:27:21 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.forms ;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Event;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextComponent;
import java.awt.TextField;

class RangedIntegerFieldEditor extends Panel {
    RangedIntegerField field = null ;
    TextField text   = null ;
    Scrollbar slider = null ;
    int       lo     = Integer.MIN_VALUE ;
    int       hi     = Integer.MAX_VALUE ;

    public boolean action (Event evt, Object arg) {
	try {
	    Integer ival = new Integer(Integer.parseInt(text.getText()));
	    if ( ! field.acceptChange(ival) )
		text.setText(field.getValue().toString()) ;
	    return true ;
	} catch (NumberFormatException ex) {
	    // This should never happen!
	    throw new RuntimeException ("implementation bug !");
	}
    }

    public boolean handleEvent (Event evt) {
	if ( evt.target instanceof Scrollbar ) {
	    text.setText(String.valueOf(slider.getValue()));
	    return true ;
	} else {
	    return super.handleEvent(evt) ;
	}
    }

    public boolean keyDown (Event evt, int key) {
	if ( evt.target instanceof TextField ) {
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
		  return true ;
	    }
	} else {
	    return super.keyDown(evt, key) ;
	}
    }

    public Insets insets() {
	return new Insets(5, 5, 5, 8) ;
    }

    RangedIntegerFieldEditor(RangedIntegerField field, int lo, int hi, int v) {
	super() ;
	this.field = field ;
	BorderLayout bl = new BorderLayout() ;
	setLayout(bl) ;
	// Add the text:
	text = new TextField("0") ;
	add ("North", text) ;
	// Add the slider:
	slider = new Scrollbar(Scrollbar.HORIZONTAL, 0, 100, lo, hi) ;
	add ("South", slider) ;
    }
}

public class RangedIntegerField extends IntegerField {
    int hi = Integer.MAX_VALUE ;
    int lo = Integer.MIN_VALUE ;
    RangedIntegerFieldEditor editor = null ;

    /**
     * Get an editor for this field.
     */

    public Component getEditor() {
	if ( editor == null )
	    editor = new RangedIntegerFieldEditor(this, lo, hi, getIntValue());
	return editor ;
    }

    public RangedIntegerField(FormManager manager
			      , String name, String title
			      , int lo, int hi, int val)
	throws IllegalFieldValueException
    {
	super(manager, name, title, val) ;
	this.lo = lo ;
	this.hi = hi ;
    }
}

// IntegerAttributeEditor.java
// $Id: IntegerAttributeEditor.java,v 1.1 2010/06/15 12:20:44 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigadmin.attributes;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;
import javax.swing.BorderFactory;

import java.util.Properties;

import org.w3c.jigadmin.widgets.IntegerTextField;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigadm.editors.AttributeEditor;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.IntegerAttribute;

public class IntegerAttributeEditor extends AttributeEditor {

    private String origs;
    JTextField widget;

    /**
     * Tells if the edited value has changed
     * @return true if the value changed.
     */

    public boolean hasChanged() {
	return !origs.equals(widget.getText());
    }

    /**
     * set the current value to be the original value, ie: changed
     * must return <strong>false</strong> after a reset.
     */

    public void clearChanged() {
	origs = widget.getText();
    }

    /**
     * reset the changes (if any)
     */

    public void resetChanges() {
	widget.setText(origs);
    }

    /**
     * Get the current value of the edited value
     * @return an object or <strong>null</strong> if the object was not
     * initialized
     */

    public Object getValue() {
	try {
	    return new Integer(Integer.parseInt(widget.getText()));
	} catch (NumberFormatException ex) {
	    // as the widget only accept numeric values, it means
	    // we want to get rid of this, so...
	    return null;
	}
    }

    /**
     * Set the value of the edited value
     * @param o the new value.
     */

    public void setValue(Object o) {
	widget.setText(o.toString());
    }

    /**
     * get the Component created by the editor.
     * @return a Component
     */

    public Component getComponent() {
	return widget;
    }

    /**
     * Initialize the editor
     * @param w the ResourceWrapper father of the attribute
     * @param a the Attribute we are editing
     * @param o the value of the above attribute
     * @param p some Properties, used to fine-tune the editor
     * @exception RemoteAccessException if a remote access error occurs.
     */

    public void initialize(RemoteResourceWrapper w, Attribute a,  Object o,
			   Properties p)
	throws RemoteAccessException
    {
	RemoteResource r = w.getResource();
	if(o == null) {
	    String v = (String) r.getValue(a.getName());
	    if(v == null)
		if(a.getDefault() != null)
		    v = a.getDefault().toString();
	    if ( v != null ) {
		origs = v;
		widget.setText(origs);
	    } 
	} else {
	    origs = o.toString();
	}
	widget.setText(origs);
    }

    public IntegerAttributeEditor() {
	widget = new IntegerTextField();
	widget.setBorder(BorderFactory.createLoweredBevelBorder());
	origs = "";
    }
}

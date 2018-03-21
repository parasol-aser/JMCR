// BooleanAttributeEditor.java
// $Id: BooleanAttributeEditor.java,v 1.1 2010/06/15 12:20:43 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.attributes ;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.ImageIcon;

import java.util.Properties;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigadm.editors.AttributeEditor;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.BooleanAttribute;

/**
 * Editor for BooleanAttribute.
 */ 
public class BooleanAttributeEditor extends AttributeEditor {

    class MyJCheckBox extends JCheckBox {
	
	protected static final String SELECTED_STRING   = "True";
	protected static final String UNSELECTED_STRING = "False";

	protected void setSelectedText(boolean onoff) {
	    if (onoff)
		setText(SELECTED_STRING);
	    else
		setText(UNSELECTED_STRING);
	}

	protected void fireStateChanged() {
	    setSelectedText(isSelected());
	    super.fireStateChanged();
	}

	MyJCheckBox(boolean onoff) {
	    setSelected(onoff);
	    setSelectedText(onoff);
	}
    }

    private boolean origb;
    MyJCheckBox widget;

    /**
     * Tells if the edited value has changed
     * @return true if the value changed.
     */

    public boolean hasChanged() {
	return !(origb == widget.isSelected());
    }

    /**
     * set the current value to be the original value, ie: changed
     * must return <strong>false</strong> after a reset.
     */

    public void clearChanged() {
	origb = widget.isSelected();
    }

    /**
     * reset the changes (if any)
     */

    public void resetChanges() {
	widget.setSelected(origb);
    }

    /**
     * Get the current value of the edited value
     * @return an object or <strong>null</strong> if the object was not
     * initialized
     */

    public Object getValue() {
	return new Boolean(widget.isSelected());
    }

    /**
     * Set the value of the edited value
     * @param o the new value.
     */

    public void setValue(Object o) {
	if(o instanceof Boolean) {
	    origb = ((Boolean)o).booleanValue();
	    widget.setSelected(origb);
	}
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

    public void initialize(RemoteResourceWrapper w, Attribute a, Object o, 
			   Properties p)
	throws RemoteAccessException
    {
	RemoteResource r = w.getResource();
	if(o == null) {
	    if(a instanceof BooleanAttribute) {
		Object oo = r.getValue(a.getName());
		if(oo != null) {
		    origb = ((Boolean)oo).booleanValue();
		} else {
		    if(a.getDefault() != null) {
			origb = ((Boolean)a.getDefault()).booleanValue();
		    }
		}
		widget.setSelected(origb);
	    }
	} else {
	    if(o instanceof Boolean) {
		origb = (((Boolean)o).booleanValue());
	    }
	}
	widget.setSelected(origb);
    }

    public BooleanAttributeEditor() {
	widget = new MyJCheckBox(false);
	origb = false;
    }
}

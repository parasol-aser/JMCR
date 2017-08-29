// ClassAttributeEditor.java
// $Id: ClassAttributeEditor.java,v 1.2 2010/06/15 17:52:56 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.awt.Component;
import java.awt.TextComponent;
import java.awt.TextField;

import java.util.Properties;

import org.w3c.tools.resources.Attribute;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.tools.widgets.MessagePopup;

/**
 * ClassAttributeEditor :
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class ClassAttributeEditor extends AttributeEditor {

    private Class origs;
    TextField widget;

    /**
     * Tells if the edited value has changed
     * @return true if the value changed.
     */

    public boolean hasChanged() {
	return ! ((origs.getName()).equals(widget.getText()));
    }

    /**
     * set the current value to be the original value, ie: changed
     * must return <strong>false</strong> after a reset.
     */

    public void clearChanged() {
	origs = getClassAttribute();
    }

    /**
     * reset the changes (if any)
     */

    public void resetChanges() {
	setClassAttribute(origs);
    }

    protected void setClassAttribute(Class c) {
	if (c != null)
	    widget.setText(c.getName());
	else 
	    widget.setText("");
    }

    protected Class getClassAttribute() {
	String classname = widget.getText();
	if (classname != null)
	    if (classname.length() > 0) {
		try {
		    Class c = Class.forName(classname);
		    //Added by Jeff Huang
		    //TODO: FIXIT
		    return c;
		} catch (ClassNotFoundException ex) {
		    (new MessagePopup("Error","Class not found : "+
				      classname)).show();
		    return null;
		}
	    }
	return null;
    }

    /**
     * Get the current value of the edited value
     * @return an object or <strong>null</strong> if the object was not
     * initialized
     */

    public Object getValue() {
	return getClassAttribute();
    }

    /**
     * Set the value of the edited value
     * @param o the new value.
     */

    public void setValue(Object o) {
	setClassAttribute((Class)o);
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

    public void initialize(RemoteResourceWrapper w,
			   Attribute a,
			   Object o,
			   Properties p)
	throws RemoteAccessException
    {
	RemoteResource r = w.getResource();
	if(o == null) {
	    Class v = null;
	    // FIXME
	    v = (Class) r.getValue(a.getName());
	   
	    if(v == null)
		if(a.getDefault() != null)
		    v = (Class)a.getDefault();
	    if ( v != null ) {
		origs = v;
		setClassAttribute(origs);
	    } 
	} else {
	    origs = (Class)o;
	}
	setClassAttribute(origs);
    }

    public ClassAttributeEditor() {
	widget = new TextField();
    }

}

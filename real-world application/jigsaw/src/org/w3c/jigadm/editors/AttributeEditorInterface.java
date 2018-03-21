// AttributeEditorInterface.java
// $Id: AttributeEditorInterface.java,v 1.1 2010/06/15 12:22:46 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.util.Properties;
import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigadm.events.AttributeListener;

import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.tools.resources.Attribute;

public interface AttributeEditorInterface {

    /**
     * Tells if the edited value has changed
     * @return true if the value changed.
     */

    public boolean hasChanged();

    /**
     * set the current value to be the original value, ie: changed
     * must return <strong>false</strong> after a reset.
     */

    public void clearChanged();

    /**
     * reset the changes (if any)
     */

    public void resetChanges();

    /**
     * Get the current value of the edited value
     * @return an object or <strong>null</strong> if the object was not
     * initialized
     */

    public Object getValue();

    /**
     * Set the value of the edited value
     * @param o the new value.
     */

    public void setValue(Object o);

    /**
     * Add a Listener to this editor.
     * @param el a listener
     */

    public void addAttributeListener(AttributeListener el);

    /**
     * Remove the listener from this editor.
     * @param el the listener to be removed.
     */

    public void removeAttributeListener(AttributeListener el);

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
	throws RemoteAccessException;

}

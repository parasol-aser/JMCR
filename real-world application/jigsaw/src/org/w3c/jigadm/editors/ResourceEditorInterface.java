// ResourceEditorInterface.java
// $Id: ResourceEditorInterface.java,v 1.1 2010/06/15 12:22:44 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.util.Properties;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigadm.events.ResourceListener;

import org.w3c.tools.resources.Attribute;

public interface ResourceEditorInterface {

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
     * commit the changes (if any)
     * @exception RemoteAccessException if a remote access error occurs.
     */

    public void commitChanges()
	throws RemoteAccessException;

    /**
     * Get the current value of the edited value
     * @return a RemoteResource or <strong>null</strong> if the object was not
     * initialized
     */

    public RemoteResource getValue();

    /**
     * Add a Listener to this helper.
     * @param el a listener
     */

    public void addResourceListener(ResourceListener el);

    /**
     * Remove the listener from this helper.
     * @param el the listener to be removed.
     */

    public void removeResourceListener(ResourceListener el);

    /**
     * initialize the helper
     * @param rw the ResourceWrapper of the Resource edited with this helper
     * @param p some Properties, used to fine-tune the helper
     * @exception RemoteAccessException if a remote access error occurs.
     */

    public void initialize(RemoteResourceWrapper rw, Properties p)
	throws RemoteAccessException;
}

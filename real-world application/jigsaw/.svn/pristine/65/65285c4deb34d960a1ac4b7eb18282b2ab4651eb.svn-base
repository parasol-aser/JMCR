// ResourceHelperInterface.java
// $Id: ResourceHelperInterface.java,v 1.1 2010/06/15 12:22:41 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.util.Properties;

import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigadm.events.ResourceListener;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.tools.resources.Attribute;

public interface ResourceHelperInterface {

    /**
     * tells if the edited resource in the helper has changed
     * @return <strong>true</strong> if the values changed.
     * to get more informations about what has changed, you can use the 
     * three methods below.
     */

    public boolean hasChanged();

    /**
     * set the current resource to be the original resource (ie: the
     * hasChanged() method must return <strong>false</false> now.
     * to do a "fine tuned" reset, use one of the three following method.
     */

    public void clearChanged();

    /**
     * get the Resource  edited with this helper
     * @return a  RemoteResource
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
     * commit the changes (if any)
     * @exception RemoteAccessException if a remote access error occurs.
     */

    public void commitChanges()
	throws RemoteAccessException;

    /**
     * undo the not-yet-commited changes
     */

    public void resetChanges();

    /**
     * initialize the helper
     * @param r the ResourceWrapper containing the Resource edited with 
     * this helper
     * @param p some Properties, used to fine-tune the helper
     * @exception RemoteAccessException if a remote access error occurs.
     */

    public void initialize(RemoteResourceWrapper rw, Properties p)
	throws RemoteAccessException;

}

// ResourceActionSource.java
// $Id: ResourceActionSource.java,v 1.1 2010/06/15 12:29:28 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.events;

/**
 * Object that implements this interface cand fire ResourceActionEvent.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface ResourceActionSource {

    /**
     * Add a ResourceActionListener.
     * @param listener the ResourceActionListener to add
     */
    public void addResourceActionListener(ResourceActionListener listener);

    /**
     * Remove a ResourceActionListener.
     * @param listener the ResourceActionListener to remove
     */
    public void removeResourceActionListener(ResourceActionListener listener);

}

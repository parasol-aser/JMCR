// ResourceActionListener.java
// $Id: ResourceActionListener.java,v 1.1 2010/06/15 12:29:28 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigadmin.events; 

/**
 * The ResourceAction listener class.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface ResourceActionListener {

    /**
     * A resource action occured.
     * @param e the ResourceActionEvent
     */
    public void resourceActionPerformed(ResourceActionEvent e);

}

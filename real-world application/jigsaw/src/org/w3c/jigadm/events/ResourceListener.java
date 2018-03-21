// ResourceListener.java
// $Id: ResourceListener.java,v 1.1 2010/06/15 12:29:21 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.events ;

import java.util.EventListener;

public interface ResourceListener extends EventListener {

    /**
     * Invoked when the value of the Attribute has changed
     */

    public void resourceChanged(ResourceChangeEvent e);
}

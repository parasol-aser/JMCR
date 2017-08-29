// ResourceEditorListener.java
// $Id: ResourceEditorListener.java,v 1.1 2010/06/15 12:27:17 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.gui ;

import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigadm.events.ResourceChangeEvent;
import org.w3c.jigadm.events.ResourceListener;

public class ResourceEditorListener implements ResourceListener {

    ServerBrowser sb = null;

    public void resourceChanged(ResourceChangeEvent e) {
	if(e.getNewValue() == null) { // deleted ?
	    if(e.getOldValue() != null) {
		sb.removeNode((RemoteResourceWrapper)e.getOldValue());
	    }
	}
    }

    public ResourceEditorListener(ServerBrowser sb) {
	this.sb = sb;
    }
}

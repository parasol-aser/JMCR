// FramesHelperListener.java
// $Id: FramesHelperListener.java,v 1.1 2010/06/15 12:22:46 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.util.EventObject;

import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigadm.events.ResourceChangeEvent;
import org.w3c.jigadm.events.ResourceListener;

public class FramesHelperListener implements ResourceListener {

    FrameBrowser fb = null;

    public void resourceChanged(ResourceChangeEvent e) {
	if(e.getNewValue() == null) { // deleted ?
	  if(e.getOldValue() != null) {
	    fb.removeNode((RemoteResourceWrapper)e.getOldValue());
	  }
	} else { //added ?
	  if ((e.getSource() != null) && (e.getNewValue() != null)) {
	    if (e.getPropertyName().equals("added")) {
	      fb.insertNode((RemoteResourceWrapper)e.getSource(),
			    (RemoteResourceWrapper)e.getNewValue());
	    } else if (e.getPropertyName().equals("identifier")) {
	      fb.renameNode((RemoteResourceWrapper)e.getSource(),
			    (String)e.getNewValue());
	    } 
	  }
	}
    }

    public FramesHelperListener(FrameBrowser fb) {
	this.fb = fb;
    }
}

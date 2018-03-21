// TreeListener.java
// $Id: TreeListener.java,v 1.1 2010/06/15 12:27:16 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.gui ;

import java.util.EventListener;

import java.awt.Container;
import java.awt.Panel;

import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigadm.editors.ResourceEditor;

public class TreeListener implements EventListener {
    Panel target = null;
    RemoteResourceWrapper lastr = null;

        class Initializer extends Thread {
	    ResourceEditor re;

	    public void run() {
		try {
		    re.initialize(lastr, null);
		} catch (RemoteAccessException ex) {
		    // FIXME
		}      
	    }

    	    Initializer(ResourceEditor re) {
		this.re = re;
	    }
	}

        public void editedChanged(ServerBrowser tb,
				  RemoteResourceWrapper resourcew) { 
	lastr = resourcew;
	if(target != null) {
	    ResourceEditor re = new ResourceEditor(target);
	    re.addResourceListener(new ResourceEditorListener(tb));
	    (new Initializer(re)).start();
//  try {
	    //	re.initialize(resourcew, null);
	    //} catch (RemoteAccessException ex) {
		// FIXME
	    //}
	}
    }

    public void focusChanged(RemoteResourceWrapper rw) { //FIXME 
	if(rw == null) {
	    if(lastr != null) {
		target.removeAll();
	    }
	} else {
	    if(!rw.equals(lastr)) {
		target.removeAll();
	    }
	}
	lastr = rw;
    }

    public void nodeRemoved(RemoteResourceWrapper rw) {
	if(rw.equals(lastr)) {
	    lastr = null;
	    target.removeAll();
	}
    }

    public TreeListener(Panel target) {
	this.target = target;
    }
}

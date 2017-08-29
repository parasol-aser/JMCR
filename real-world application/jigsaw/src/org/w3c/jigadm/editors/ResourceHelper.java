// ResourceHelper.java
// $Id: ResourceHelper.java,v 1.1 2010/06/15 12:22:48 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.awt.Component;

import java.util.EventObject;
import java.util.Properties;
import java.util.Vector;

import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.tools.resources.Attribute;

import org.w3c.tools.widgets.MessagePopup;

import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigadm.events.ResourceChangeEvent;
import org.w3c.jigadm.events.ResourceListener;

abstract public class ResourceHelper implements ResourceHelperInterface {

    protected Vector rls = null;

    abstract public String getTitle();

    abstract public Component getComponent();

    protected void errorPopup(String name, Exception ex) {
      (new MessagePopup(name+" : "+ex.getMessage())).show();
    }

    protected void msgPopup(String name) {
      (new MessagePopup(name)).show();
    }

    public synchronized void addResourceListener(ResourceListener rl) {
	if (rls == null)
	    rls = new Vector(2);
	rls.addElement(rl);
    }

    public RemoteResource getValue() {
	return null;
    }

    public synchronized void removeResourceListener(ResourceListener rl) {
	if ( rls != null ) 
	    rls.removeElement(rl);
    }

    protected void processEvent(EventObject eo) {
	Vector rls = null;
	ResourceListener rl;
	synchronized(this) {
	    if((this.rls != null) && (eo instanceof ResourceChangeEvent )) {
		rls = (Vector) this.rls.clone();
	    } else {
		return;
	    }
	}
	for(int i=0; i<rls.size(); i++) {
	    rl = (ResourceListener) rls.elementAt(i);
	    rl.resourceChanged((ResourceChangeEvent)eo);
	}
    }
}

// AttributeEditor.java
// $Id: AttributeEditor.java,v 1.1 2010/06/15 12:22:40 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.awt.Component;

import java.util.EventObject;
import java.util.Properties;
import java.util.Vector;

import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.tools.resources.Attribute;

import org.w3c.jigadm.events.AttributeChangeEvent;
import org.w3c.jigadm.events.AttributeListener;

    /*
     * The purpose of this class is to specialize the AttributeEditorInterface
     * for graphical purposes.
     */

abstract public class AttributeEditor implements AttributeEditorInterface {

    protected Vector als = null;

    public synchronized void addAttributeListener(AttributeListener al) {
	if (als == null)
	    als = new Vector(2);
	als.addElement(al);
    }

    public synchronized void removeAttributeListener(AttributeListener al) {
	if ( als != null ) 
	    als.removeElement(al);
    }

    protected void processEvent(EventObject eo) {
	Vector als = null;
	AttributeListener al;
	synchronized(this) {
	    if((this.als != null) && (eo instanceof AttributeChangeEvent )) {
		als = (Vector) this.als.clone();
	    } else {
		return;
	    }
	}
	for(int i=0; i<als.size(); i++) {
	    al = (AttributeListener) als.elementAt(i);
	    al.attributeChanged((AttributeChangeEvent)eo);
	}
    }

    /**
     * get the Component created by the editor.
     * @return a Component
     */

    abstract public Component getComponent();
}

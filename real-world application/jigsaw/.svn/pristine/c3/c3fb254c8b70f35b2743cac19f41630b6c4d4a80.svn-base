// ResourceEventMulticaster.java
// $Id: ResourceEventMulticaster.java,v 1.1 2010/06/15 12:26:40 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.event;

import java.util.EventListener;

/**
 * Event dispatching suport class.
 */

public class ResourceEventMulticaster implements StructureChangedListener,
                                                 FrameEventListener,
                                                 AttributeChangedListener
{
    protected EventListener a = null;
    protected EventListener b = null;

    protected ResourceEventMulticaster(EventListener a, EventListener b) {
	this.a = a;
	this.b = b;
    }

    protected EventListener remove(EventListener oldl) {
	if ( oldl == a )
	    return b;
	if ( oldl == b)
	    return a;
	EventListener a2 = removeInternal(a, oldl);
	EventListener b2 = removeInternal(b, oldl);
	if (a2 == a && b2 == b)
	    return this;
	return addInternal(a2, b2);
    }

    protected static EventListener removeInternal(EventListener l, 
						  EventListener oldl) {
	if ( l == oldl || l == null ) {
	    return null;
	} else if ( l instanceof ResourceEventMulticaster ) {
	    return ((ResourceEventMulticaster) l).remove(oldl);
	} else {
	    return l;
	}
    }

    protected static EventListener addInternal(EventListener a, 
					       EventListener b) {
	if ( a == null )
	    return b;
	if ( b == null )
	    return a;
	return new ResourceEventMulticaster(a, b);
    }

    /**
     * Add an attribute change listener.
     */

    public static AttributeChangedListener add(AttributeChangedListener a,
					       AttributeChangedListener b) {
	return (AttributeChangedListener) addInternal(a, b);
    }

    /**
     * Remove an attribute change listener.
     */

    public static AttributeChangedListener remove(AttributeChangedListener l,
						  AttributeChangedListener ol)
    {
	return (AttributeChangedListener) removeInternal(l, ol);
    }

    /**
     * Propagate AttributeChanged events.
     */

    public void attributeChanged(AttributeChangedEvent evt) {
	((AttributeChangedListener) a).attributeChanged(evt);
	((AttributeChangedListener) b).attributeChanged(evt);
    }

    /**
     * Add an Frame event listener.
     */

    public static FrameEventListener add(FrameEventListener a,
					 FrameEventListener b) {
	return (FrameEventListener) addInternal(a, b);
    }

    /**
     * Remove a frame event listener.
     */

    public static FrameEventListener remove(FrameEventListener l,
					    FrameEventListener oldl) {
	return (FrameEventListener) removeInternal(l, oldl);
    }

    public void frameAdded(FrameEvent evt) {
	((FrameEventListener)a).frameAdded(evt);
	((FrameEventListener)b).frameAdded(evt);
    }

    public void frameModified(FrameEvent evt) {
	((FrameEventListener)a).frameModified(evt);
	((FrameEventListener)b).frameModified(evt);
    }

    public void frameRemoved(FrameEvent evt) {
	((FrameEventListener)a).frameRemoved(evt);
	((FrameEventListener)b).frameRemoved(evt);
    }

    /**
     * Add a structure changed listener.
     */

    public static StructureChangedListener add(StructureChangedListener a,
					       StructureChangedListener b) {
	return (StructureChangedListener) addInternal(a, b);
    }

    /**
     * Remove a structure changed listener.
     */

    public static StructureChangedListener remove(StructureChangedListener l,
						  StructureChangedListener ol)
    {
	return (StructureChangedListener) removeInternal(l, ol);
    }

    public void resourceModified(StructureChangedEvent evt) {
	((StructureChangedListener) a).resourceModified(evt);
	((StructureChangedListener) b).resourceModified(evt);
    }

    public void resourceCreated(StructureChangedEvent evt) {
	((StructureChangedListener) a).resourceCreated(evt);
	((StructureChangedListener) b).resourceCreated(evt);
    }

    public void resourceRemoved(StructureChangedEvent evt) {
	((StructureChangedListener) a).resourceRemoved(evt);
	((StructureChangedListener) b).resourceRemoved(evt);
    }

    public void resourceUnloaded(StructureChangedEvent evt){ 
	((StructureChangedListener) a).resourceUnloaded(evt);
	((StructureChangedListener) b).resourceUnloaded(evt);
    }
}

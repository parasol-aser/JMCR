// StructureChangedEvent.java
// $Id: StructureChangedEvent.java,v 1.1 2010/06/15 12:26:41 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.event;

import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceReference;

public class StructureChangedEvent extends ResourceEvent {

    public String toString() {
	ResourceReference rr = (ResourceReference) getSource();
	String ssource = null;
	try {
	    Resource resource = rr.lock();
	    ssource = resource.getURLPath();
	} catch (InvalidResourceException ex) {
	    ssource = "invalid";
	} catch (Exception ex) {
	    ssource = "invalid";	
	} finally {
	    rr.unlock();
	}
	String stype = null;
	switch (id) {
	case Events.RESOURCE_CREATED:
	    stype = "RESOURCE_CREATED";
	    break;
	case Events.RESOURCE_MODIFIED:
	    stype = "RESOURCE_MODIFIED";
	    break;
	case Events.RESOURCE_REMOVED:
	    stype = "RESOURCE_REMOVED";
	    break;
	case Events.RESOURCE_UNLOADED:
	    stype = "RESOURCE_UNLOADED";
	    break;
	default:
	    stype = "UNKNOWN";
	}
	return "StructureChangedEvent : ["+ssource+" : "+stype+"]";
    }

    /**
     * Create a structure change event.
     * @param source The resource emitting the event.
     * @param type The kind of event being emitted.
     */

    public StructureChangedEvent(ResourceReference ref,
				 int type) 
    {
	super(ref, type);
    }
}

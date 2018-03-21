// AttributeChangedEvent.java
// $Id: AttributeChangedEvent.java,v 1.1 2010/06/15 12:26:40 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.event;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceReference;

public class AttributeChangedEvent extends ResourceEvent {

    /**
     * The index of the attribut modified.
     */
    protected Attribute attr;

    /**
     * The attribut new value.
     */
    protected Object newvalue = null;

 
    public Attribute getAttribute() {
	return attr;
    }

    public Object getNewValue() {
	return newvalue;
    }

    public String toString() {
	ResourceReference rr = (ResourceReference) getSource();
	String ssource = null;
	String id = null;
	try {
	    Resource resource = rr.lock();
	    ssource = resource.getURLPath();
	    id = resource.getIdentifier();
	} catch (InvalidResourceException ex) {
	    ssource = "invalid";
	} catch (Exception ex) {
	    ssource = "invalid";	
	} finally {
	    rr.unlock();
	}
	return ("AttributeChangedEvent : ["+ssource+
		" ("+id+")"+
		" : "+attr.getName()+
		" <- "+newvalue+"]");
    }

    /**
     * Create an attribute change event.
     * @param source The resource whose attribute has changed.
     * @param idx The index of the attribute that has changed.
     * @param newvalue The new attribuyte value.
     */
    public AttributeChangedEvent(ResourceReference ref, 
				 Attribute attr, 
				 Object newvalue) 
    {
	super(ref, Events.ATTRIBUTE_EVENT);
	this.attr = attr;
	this.newvalue = newvalue;
    }

}

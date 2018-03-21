// ResourceStateFilter.java
// $Id: ResourceStateFilter.java,v 1.1 2010/06/15 12:20:25 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.io.PrintStream;

import org.w3c.tools.resources.event.AttributeChangedEvent;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ResourceStateFilter extends ResourceFilter {

    protected void showState() {
	Resource resource = getTargetResource();
	System.out.println("-----------------------------------------");
	System.out.println("Resource state :");
	if (resource == null)
	    System.out.println("NULL resource");
	else {
	    System.out.println("Identifier : "+resource.getIdentifier());
	    System.out.println("Modified   : "+resource.getLastModified());
	    System.out.println("Parent     : "+resource.getParent());
	    System.out.println(resource.getContext());
	    System.out.println("URLPath    : "+resource.getURLPath());
	    System.out.println("Space      : "+resource.getSpace());
	    System.out.println("SpaceEntry : "+resource.getSpaceEntry());
	    System.out.println("Reference  : "+
			       resource.getResourceReference());
	}
	System.out.println("-----------------------------------------");
    }

    /**
     * Lookup the target resource.
     * @param ls The current lookup state
     * @param lr The result
     * @exception ProtocolException if a protocol error occurs.
     */
    public boolean lookup(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	showState();
	return false;
    }

    public void attributeChanged(AttributeChangedEvent evt) {
	super.attributeChanged(evt);
	System.out.println("-----------------------------------------");
	System.out.println("Resource State event:");
	System.out.println(evt);
	System.out.println("-----------------------------------------");
    }

    public void registerResource(FramedResource resource) {
	super.registerResource(resource);
	FramedResource fr = (FramedResource)getTargetResource();
	if (fr != null) 
	    fr.addAttributeChangedListener(this);
    }

}

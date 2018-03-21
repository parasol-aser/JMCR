// ExternalContainer.java
// $Id: ExternalContainer.java,v 1.1 2010/06/15 12:20:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.File;

/**
 * A Container which manage an external store, outside the space.
 */
public abstract class ExternalContainer extends ContainerResource {

    /**
     * Our transientFlag, is true that container must not be saved.
     */ 
    protected boolean transientFlag = false;

    /**
     * Our external repository.
     */
    protected File    repository    = null;

    public  ResourceReference createDefaultResource(String name) {
	throw new RuntimeException("not extensible");
    }

    /**
     * Mark this resource as having been modified.
     */
    public void markModified() {
	if (transientFlag) {
	    setValue(ATTR_LAST_MODIFIED, new Long(System.currentTimeMillis()));
	} else {
	    super.markModified();
	}
    }

    /**
     * acquire children and notify space if we will be saved.
     */
    protected synchronized void acquireChildren() {
	if (!acquired) {
	    ResourceSpace space = getSpace();
	    if (repository != null) {
		space.acquireChildren( getChildrenSpaceEntry() , 
				       repository, 
				       transientFlag );
	    } else {
		// if we have been saved one time yet.
		space.acquireChildren( getChildrenSpaceEntry() );
	    }
	    acquired = true;
	}
    }

    /**
     * Delete this Resource instance , and remove it from its store.
     * This method will erase definitely this resource, for ever, by removing
     * it from its resource store (when doable).
     * @exception MultipleLockException if someone has locked this resource.
     */

    public synchronized void delete() 
	throws MultipleLockException
    {
	if (transientFlag) {
	    // transient, so don't try to delete myself.
	    ResourceSpace space = getSpace();
	    if (space != null) {
		acquireChildren();
		// check for lock on children
		Enumeration       e        = enumerateResourceIdentifiers();
		ResourceReference rr       = null;
		Resource          resource = null;
		while (e.hasMoreElements()) {
		    rr = lookup((String) e.nextElement());
		    if (rr != null) {
			try {
			    synchronized (rr) {
				resource = rr.lock();
				resource.delete();
			    }	  
			} catch (InvalidResourceException ex) {
			    // nothing, remove invalid resource.
			} finally {
			    rr.unlock();
			}
		    }
		}
		space.deleteChildren(getChildrenSpaceEntry());
	    }
	} else {
	    super.delete();
	}
    }

    /**
     * Get The repository for this external container.
     * Warning: called in the constructor!
     * @param context The container context.
     * @return A File instance
     */
    abstract public File getRepository(ResourceContext context);

    public void initialize(Object values[]) {
	super.initialize(values);
	if (repository == null)
	    repository = getRepository(getContext());
    }

    /**
     * @param id The identifier.
     * @param context The default context.
     * @param transientFlag The transient flag.
     */

    public ExternalContainer (String identifier, 
			      ResourceContext context,
			      boolean transientFlag) 
    {
	Hashtable h = new Hashtable(3);
	h.put(id, identifier);
	h.put(co, context);
	initialize(h);
	this.acquired      = false; 
	this.transientFlag = transientFlag;
	if (transientFlag)
	    context.setResourceReference( new DummyResourceReference(this));
    }

    public ExternalContainer () {
	super();
	this.acquired      = false;
	this.transientFlag = false;
	this.repository    = null;
    }
}

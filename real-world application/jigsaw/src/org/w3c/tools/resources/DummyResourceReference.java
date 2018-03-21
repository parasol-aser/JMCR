// DummyResourceIndexer.java
// $Id: DummyResourceReference.java,v 1.1 2010/06/15 12:20:15 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources;

/**
 * This class implements the ResourceReference interface. For external
 * use only. The ResourceStoreManager has its own Reference class.
 */

public class DummyResourceReference implements ResourceReference {

    /**
     * The lock count associated to the reference.
     */
    protected int lockCount = 0;

    private Resource resource = null;
    private String identifier = null;

    /**
     * update the cached context of that reference.
     * @param ctxt the new ResourceContext.
     */
    public void updateContext(ResourceContext ctxt) {
    }

    /**
     * Lock the refered resource in memory.
     * @return A real pointer to the resource.
     * @exception InvalidResourceException is thrown if the resource is
     * invalid (has been deleted or everything else).
     */
    public Resource lock()
	throws InvalidResourceException
    {
	if (resource == null)
	    throw new InvalidResourceException(identifier,
					"This reference has been invalidated");
	lockCount++;
	return resource;
    }

    /**
     * Lock the refered resource in memory.
     * @return A real pointer to the resource.
     * @exception InvalidResourceException is thrown if the resource is
     * invalid (has been deleted or everything else).
     */
    public Resource unsafeLock()
	throws InvalidResourceException
    {
	return lock();
    }
    /**
     * How many locks?
     * @return an int.
     */
    public int nbLock() {
	return lockCount;
    }

    /**
     * Unlock that resource from memory.
     */
    public void unlock() {
	lockCount--;
    }

    /**
     * Is that resource reference locked ?
     */
    public boolean isLocked() {
	return lockCount != 0;
    }

    /**
     * Set this reference as invalid.
     */
    public void invalidate() {
	resource = null;
    }

    /**
     * @param resource The resource to reference.
     */
    public DummyResourceReference(Resource resource) {
	this.resource = resource;
	this.identifier = resource.getIdentifier();
    }

}

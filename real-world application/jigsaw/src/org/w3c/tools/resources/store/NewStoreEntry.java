// NewStoreEntry.java
// $Id: NewStoreEntry.java,v 1.2 2010/06/15 17:52:58 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.store ;

import java.io.File;
import java.io.PrintStream;

import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.util.LRUAble;


class NewReference implements ResourceReference {

    public static boolean debug = false;

    // Our entry
    NewStoreEntry entry = null;

    // The resource identifier
    String identifier = null;

    // The default attributs
    Hashtable defs = null;

    public void updateContext(ResourceContext ctxt) {
	if (defs != null)
	    defs.put("context", ctxt);
    }

    /**
     * The lock count associated to the reference.
     */
    protected int lockCount = 0;

    public int nbLock() {
	synchronized (identifier) {
	    return lockCount;
	}
    }

    protected void invalidate() {
	entry = null;
    }

    protected boolean isValid() {
	return ( entry != null ) ;
    }

    /**
     * Lock that reference in memory.
     * @return A pointer to the underlying resource, after lock succeed.
     */
    public synchronized Resource lock() 
	throws InvalidResourceException
    {
	synchronized (identifier) {
	    lockCount++;
	}
	if (entry == null)
	    throw new InvalidResourceException(identifier,
					"This reference has been invalidated");
	ResourceStore store = entry.getStore();
	Resource resource = store.lookupResource(identifier);
	if (debug) {
	    if (defs.get("context") == null) {
		System.out.println("**** Context null for : "+identifier);
	    } else if (((ResourceContext)(defs.get("context"))).getServer() 
		       == null) {
		System.out.println("**** Server null for "+
				   identifier+"'s context");
	    }
	}
	if (resource == null) {
	    resource = store.loadResource(identifier, defs);
	}
	if (debug) {
	    System.out.println("[LOCK] locking ["+lockCount+"]: "+identifier);
	}
	return resource;
    }

    /**
     * Lock that reference in memory.
     * @return A pointer to the underlying resource, after lock succeed.
     */
    public Resource unsafeLock() 
	throws InvalidResourceException
    {
	synchronized (identifier) {	
	    lockCount++;
	}
	if (entry == null) {
	    throw new InvalidResourceException(identifier,
					"This reference has been invalidated");
	}
	ResourceStore store = entry.getStore();
	Resource resource = store.lookupResource(identifier);
	if (debug) {
	    if (defs.get("context") == null) {
		System.out.println("**** Context null for : "+identifier);
	    } else if (((ResourceContext)(defs.get("context"))).getServer() 
		       == null) {
		System.out.println("**** Server null for "+
				   identifier+"'s context");
	    }
	}
	if (resource == null) {
	    resource = store.loadResource(identifier, defs);
	}
	if (debug) {
	    System.out.println("[LOCK] locking ["+lockCount+"]: "+identifier);
	}
	return resource;
    }   

    /**
     * Unlock that resource reference.
     */
    public void unlock() {
	synchronized (identifier) {
	    lockCount--;
	}
	if (debug)
	    System.out.println("[LOCK] unlocking ["+lockCount+"]: "+
			       identifier);
    }

    /**
     * Is that resource locked ?
     * @return A boolean, <strong>true</strong> if the resource is locked.
     */
    public boolean isLocked() {
	return lockCount != 0;
    }

    NewReference (NewStoreEntry entry, String identifier, Hashtable defs) {
	this.entry      = entry;
	this.identifier = identifier;
	this.defs       = defs;
    }
}

public class NewStoreEntry extends AttributeHolder implements LRUAble {

    protected static int ATTR_REPOSITORY = -1;

    protected static int ATTR_KEY = -1;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	// Get a pointer to our own class:
	try {
	    cls = Class.forName(
           "org.w3c.tools.resources.store.NewStoreEntry");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}

	// The repository attribute:
	a = new StringAttribute("repository", null, Attribute.MANDATORY);
	ATTR_REPOSITORY = AttributeRegistry.registerAttribute(cls, a);
	//The key attribute
	a = new IntegerAttribute("key", null, Attribute.MANDATORY);
	ATTR_KEY = AttributeRegistry.registerAttribute(cls, a);
    }

    protected Integer getKey() {
	return (Integer)getValue(ATTR_KEY, null);
    }

    protected File getRepository() {
	String repository = getString(ATTR_REPOSITORY, null);
	if (rep == null)
	    rep = new File(manager.storedir, repository);
	return rep;
    }

    boolean istransient = false;

    // LRU management infos:
    LRUAble next = null ;
    LRUAble prev = null ;

    // ResourceStore infos:
    ResourceStore store      = null ;

    // References
    Hashtable references = null;

    //The manager
    ResourceStoreManager manager = null;

    //The repository
    File rep = null;

    public LRUAble getNext() {
	return next;
    }

    public LRUAble getPrev() {
	return prev;
    }

    public void setNext(LRUAble next) {
	this.next = next;
    }

    public void setPrev(LRUAble prev) {
	this.prev = prev;
    }

    public void setTransient(boolean onoff) {
	istransient = onoff;
    }

    public boolean isTransient() {
	return istransient;
    }

    /**
     * Load the store of this entry.
     */
    ResourceStore getStore() {
	if ( store == null ) {
	    synchronized (this) {
		if ( store == null ) {
		    store = new ResourceStoreImpl() ;
		    store.initialize(manager, this, getRepository(), 
				     manager.serializer) ;
		    manager.incrLoadedStore();
		}
	    }
	}
	return store;
    }

    /**
     * Delete the store of this entry
     */
    synchronized void deleteStore() {
	Enumeration e = references.elements();
	NewReference rr = null;
	while (e.hasMoreElements()) {
	    rr = (NewReference) e.nextElement();
	    rr.invalidate();
	}
	getRepository().delete();
	if (store != null) {
	    store = null;
	    manager.decrLoadedStore();
	}
	references = null;
    }

    /**
     * Lookup this resource.
     * @param identifier The resource identifier.
     * @return A Resource instance, or <strong>null</strong> if either the
     *    resource doesn't exist, or it isn't loaded yet.
     */
    public ResourceReference lookupResource (String identifier) {
	return (ResourceReference)references.get(identifier);
    }

    /**
     * Load a resource, or get one from the cache.
     * @param identifier The resource identifier.
     * @return A Resource instance, or <strong>null</strong> if the resource
     * doesn't exist in that storeEntry.
     * @exception InvalidResourceException If the resource couldn't be 
     * restored from its pickled format.
     */
    synchronized ResourceReference loadResource(String name, Hashtable defs) {
	ResourceReference rr = lookupResource(name);
	if (rr != null)
	    return rr;
	rr = new NewReference(this, name, defs);
	try {
	    Resource res = rr.lock();
	    if (res == null)
		return null;
	} catch (InvalidResourceException ex) {
	    return null;
	} finally {
	    rr.unlock();
	}
	references.put(name, rr);
	return rr;
    }

    /**
     * Save a given resource.
     * @param resource The resource to be save right now.
     */
    synchronized void saveResource(Resource resource) {
	getStore();
	store.saveResource(resource);
    }

    /**
     * Add a new resource to the resource store.
     * @param resource The resource to add.
     */
    ResourceReference addResource(Resource resource, 
				  Hashtable defs) {
	ResourceReference rr = null;
	synchronized (resource) {
	    synchronized (this) {
		getStore();
		store.addResource(resource);
		String name = resource.getIdentifier();
		rr = new NewReference(this, name , defs);    
		references.put(name, rr);
	    }
	}
	return rr;
    }

    /**
     * FIXME doc
     */
    public void markModified(Resource resource) {
	getStore();
	store.markModified(resource);
    }

    /**
     * Rename a resource in this resource store.
     * @param identifier The identifier of the resource to be renamed.
     */
    public synchronized void renameResource(String oldid, String newid) {
	getStore();
	store.renameResource(oldid, newid);
	NewReference rr = (NewReference)lookupResource(oldid);
	if (rr != null) {
	    rr.identifier = newid;
	    references.remove(oldid);
	    references.put(newid, rr);
	}
    }

    public synchronized Enumeration enumerateResourceIdentifiers() {
	getStore();
	return store.enumerateResourceIdentifiers();
    }

    /**
     * Remove a resource from this resource store.
     * @param identifier The identifier of the resource to be removed.
     */
    public synchronized void removeResource(String identifier) {
	getStore();
	NewReference rr = (NewReference)references.get(identifier);
	if (rr != null) {
	    references.remove(identifier);
	    manager.getEventQueue().removeSourceEvents(rr);
	    rr.invalidate();
	}
	store.removeResource(identifier);
    }

    /**
     * Try unloading the space for this entry.
     */
    synchronized boolean unloadStore() {
	if ( store != null ) {
	    Enumeration e = references.elements();
	    ResourceReference rr = null;
	    while (e.hasMoreElements()) {
		rr = (ResourceReference)e.nextElement();
		if (rr.isLocked())
		    return false;
	    }
	    // Will the store unload itself ?
	    if ( ! store.acceptUnload())
		return false;
	    // Great, proceed:
	    shutdownStore();
	}
	return true;
    }

    /**
     * Shutdown the store.
     */
    synchronized void shutdownStore() {
	if ( store != null ) {
	    store.shutdown() ;
	    store = null ;
	    references = new Hashtable(3);
	    manager.decrLoadedStore();
	}
    }

    /**
     * Try stabilizing the store.
     */
    synchronized void saveStore() {
	if ( store != null ) {
	    // Save the resource store:
	    store.save();
	}
    }

    void initialize(ResourceStoreManager manager) {
	this.manager    = manager;
	this.references = new Hashtable(3);
    }

    NewStoreEntry(ResourceStoreManager manager, 
		  String repository, 
		  Integer key) 
    {
	this.manager    = manager;
	this.store      = null ;
	this.references = new Hashtable(3);
	setValue(ATTR_REPOSITORY, repository);
	setValue(ATTR_KEY, key);
    }

    NewStoreEntry(ResourceStoreManager manager, 
		  File repository, 
		  Integer key) 
    {
	this.manager    = manager;
	this.store      = null ;
	this.rep        = repository ;
	this.references = new Hashtable(3);
	setValue(ATTR_KEY, key);
    }

    public NewStoreEntry() {}

}



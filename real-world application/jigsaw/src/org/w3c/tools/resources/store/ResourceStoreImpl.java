// ResourceStoreImpl.java
// $Id: ResourceStoreImpl.java,v 1.1 2010/06/15 12:25:25 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.store ;

import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.Resource;

import org.w3c.tools.resources.serialization.Serializer;
import org.w3c.tools.resources.serialization.SerializationException;
import org.w3c.util.EmptyEnumeration;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;

/**
 * A generic resource store that keeps resource in a file using
 * the Serializer interface.
 */

public class ResourceStoreImpl implements ResourceStore {
    
    static final int writerSize = 65536;

    class ResourceIndex {
	
	boolean  modified    = false;
	String   identifier  = null;
	Resource resource    = null;
	boolean  initialized = false;

	synchronized void markModified() {
	    modified = true;
	}

	synchronized boolean isModified() {
	    return modified;
	}

	synchronized Resource loadResource(Hashtable defs) {
	    if (initialized) {
		return resource;
	    } else {
		resource.initialize(defs);
		initialized = true;
		return resource;
	    }
	}

	synchronized Resource getResource() {
	    return resource;
	}

	void unloadResource() {
	    // notify the resource of unload:
	    resource.notifyUnload() ;
	    resource = null;
	}

	synchronized String getIdentifier() {
	    return identifier;
	}

	synchronized void setIdentifier(String identifier) {
	    this.identifier = identifier;
	}

	ResourceIndex(Resource resource, boolean initialized) {
	    this.resource    = resource;
	    this.identifier  = resource.unsafeGetIdentifier();
	    this.modified    = false;
	    this.initialized = initialized;
	}
	
    }

    /**
     * The store format version number.
     */
    private static final int VERSION = 2;
    /**
     * Our Resource Serializer.
     */
    protected Serializer serializer = null;
    /**
     * Our underlying associated file.
     */
    File repository = null ;
    /**
     * Our resource store manager.
     */
    protected ResourceStoreManager manager = null ;
    /**
     * Our token within the resource store manager.
     */
    Object token = null;
    /**
     * The resources we know about: maps identifier to resource objects.
     */
    Hashtable resources = null ;
    /**
     * Has this repository been modified.
     */
    boolean modified = false ;

    /**
     * Mark the store as having been used recently.
     */

    protected final void markUsed() {
	if ( manager != null )
	    manager.markUsed(token) ;
    }

    // be smart here
    // FIXME removed the synchronized to avoid a deadlock
    protected void markModified() {
	if (!modified) {
	    synchronized (this) {
		modified = true;
	    }
	}
    }

    /**
     * Get the version of that resource store.
     * Version numbers are used to distinguish between pickling format. 
     * A resource store implementator has the duty of bumping the returned
     * number whenever it's archiving format changes.
     * Resource stores that relies on some external archiving mechanisms
     * (such as a database), may return a constant.
     * @return An integer version number.
     */

    public int getVersion() {
	return VERSION;
    }

    /**
     * Get the identifier for that store.
     * @return A uniq store identifier, as a String.
     */

    public String getIdentifier() {
	return repository.getAbsolutePath();
    }

    /**
     * Emit the given string as a warning, to whoever it is appropriate.
     * @param msg The warning message.
     */

    protected void warning(String msg) {
	System.out.println("[" + getClass().getName()+
			   "@" + repository+
			   "]: " + msg) ;
    }

    /**
     * Restore the resource whose name is given.
     * This method doesn't assume that the resource will actually be restored,
     * it can be kept in a cache by the ResourceStore object, and the cached 
     * version of the resource may be returned.
     * @param identifier The identifier of the resource to restore.
     * @param defs Default attribute values. If the resource needs to be
     *     restored from its pickled version, this Hashtable provides
     *     a set of default values for some of the attributes.
     * @return A Resource instance, or <strong>null</strong>.
     * @exception InvalidResourceException If the resource could not
     * be restored from the store.
     */

    public Resource loadResource(String identifier, Hashtable defs)
	throws InvalidResourceException
    {
	loadResources();
	markUsed();
	ResourceIndex index = (ResourceIndex) resources.get(identifier) ;
	if ( index == null )
	    return null;
	if ( defs == null )
	    defs = new Hashtable(3);
	defs.put("store-entry", index);
	return index.loadResource(defs);
    }

    /**
     * Get this resource, but only if already loaded.
     * The resource store may (recommended) maintain a cache of the resource
     * it loads from its store. If the resource having this identifier 
     * has already been loaded, return it, otherwise, return
     * <strong>null</strong>.
     * @param identifier The resource identifier.
     * @return A Resource instance, or <strong>null</strong>.
     */

    public Resource lookupResource(String identifier) {
	loadResources();
	markUsed();
	ResourceIndex index = (ResourceIndex) resources.get(identifier);
	return (((index == null) || (! index.initialized)) 
		? null : index.getResource() );
    }

    /**
     * Stabilize the given resource.
     * @param resource The resource to save.
     */

    public void saveResource(Resource resource) {
	loadResources();
	ResourceIndex index = (ResourceIndex) resource.getStoreEntry();
	if ( index == null )
	    throw new UnknownResourceException(resource);
	if (index.isModified())
	    save() ;
	markUsed() ;
    }

    /**
     * Add this resource to this resource store.
     * @param resource The resource to be added.
     */

    public synchronized void addResource(Resource resource) {
	loadResources();
	ResourceIndex index = new ResourceIndex(resource, true);
	index.markModified();
	resource.setValue("store-entry", index);
	resources.put(index.getIdentifier(), index);
	markModified();
	markUsed();
    }

    /**
     * Remove this resource from the repository.
     * @param identifier The identifier of the resource to be removed.
     */

    public synchronized void removeResource(String identifier) {
	ResourceIndex index = (ResourceIndex) resources.get(identifier);
	if ( index != null ) {
	    index.unloadResource();
	    resources.remove(identifier);
	    markModified();
	    markUsed();
	}
    }

    /**
     * Rename a given resource.
     * @param oldid The olde resource identifier.
     * @param newid The new resource identifier.
     */

    public synchronized void renameResource(String oldid, String newid) {
	ResourceIndex index = (ResourceIndex) resources.get(oldid);
	if (index != null) {
	    resources.remove(oldid);
	    index.setIdentifier(newid);
	    resources.put(newid, index);
	    index.markModified();
	    markModified();
	}
    }

    /**
     * Mark this resource as modified.
     * @param resource The resource that has changed (and will have to be
     * pickled some time latter).
     */

    public void markModified(Resource resource) {
	ResourceIndex index = (ResourceIndex) resource.getStoreEntry();
	if ( index != null ) {
	    index.markModified();
	    markModified() ;
	    markUsed();
	}
    }

    /**
     * Can this resource store be unloaded now ?
     * This method gets called by the ResourceStoreManager before calling
     * the <code>shutdown</code> method, when possible. An implementation
     * of that method is responsible for checking the <code>acceptUnload
     * </code> method of all its loaded resource before returning 
     * <strong>true</strong>, meaning that the resource store can be unloaded.
     * @return A boolean <strong>true</strong> if the resource store can be
     * unloaded.
     */

    public synchronized boolean acceptUnload() {
	if (resources == null) {
	    return true;
	}
	boolean accept = true;
	if ((manager != null) &&
	    (manager.getStoreSizeLimit() > 0) &&
	    resources.size() > manager.getStoreSizeLimit()) {
	    accept = false;
	} else {
	    Enumeration e      = resources.elements();
	    while ( e.hasMoreElements() ) {
		ResourceIndex entry    = (ResourceIndex) e.nextElement();
		Resource      resource = entry.getResource();
		synchronized (entry) {
		    if (! resource.acceptUnload() )
			accept = false;
		}
	    }
	}
	if ( ! accept ) {
	    try {
		if ( modified ) 
		    internalSave(false);
	    } catch (IOException ex) {
		ex.printStackTrace();
		warning("internalSave failed at acceptUnload");
	    }
	}
	return accept;
    }

    /**
     * Internal save: save the repository back to disk.
     * @param unload Should we unload any existing resources ?
     */

    protected synchronized void internalSave(boolean unload) 
	throws IOException
    {
	//nothing to save
	if (resources == null) {
	    return;
	}
	//1st, build the resource array
	Enumeration e    = resources.elements();
	Vector      vres = new Vector(11);
	while (e.hasMoreElements()) {
	    Resource res = ((ResourceIndex)e.nextElement()).getResource();
	    vres.addElement(res);
	}
	Resource resourcearray[] = new Resource[vres.size()];
	vres.copyInto(resourcearray);

	//try to save in a temporary file
	File tmp = new File(repository.getParent(),
			    repository.getName()+".tmp") ;
	FileOutputStream fos = new FileOutputStream(tmp);
	OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
	Writer writer = new BufferedWriter( osw, writerSize);

	serializer.writeResources(resourcearray, writer);

	//success!!
	String name = repository.getName() ;
	String dir  = repository.getParent() ;
	File   bak  = new File(dir, name+".bak");
	File   tild = new File(dir, name+".bak~");
	// 1st move: delete the ~ file if any:
	if ( tild.exists() )
	    tild.delete() ;
	// 2nd move rename bak to ~ (bak no longer exists)
	if ( bak.exists() ) {
	    bak.renameTo(tild);
	    bak.delete() ;
	}
	// 3nd move: rename the current repository to bak
	if ( repository.exists() ) { 
	    if ( ! repository.renameTo(bak) ) {
		warning("unable to rename "+repository+" to "+bak) ;
		tild.renameTo(bak) ;
	    }
	    repository.delete();
	}
	// 4th move: rename the tmp file to the repository
	if ( ! tmp.renameTo(repository) ) {
	    bak.renameTo(repository) ;
	    tild.renameTo(bak);
	    warning("unable to rename "+tmp+" to "+repository) ;
	}
	// cleanup (erase the ~ file)
	tild.delete() ;
	modified = false;
	//unload if needed
	if (unload) {
	    for (int i = 0 ; i < resourcearray.length ; i++)
		resourcearray[i].notifyUnload();
	    resources = null;
	}
    }

    /**
     * Shutdown this store.
     */

    public synchronized void shutdown() {
	if (modified) {
	    try {
		internalSave(true) ;
	    } catch (IOException ex) {
		ex.printStackTrace();
		warning("internalSave failed at shutdown.") ;
	    }
	} else {
	    // Just notify the unload of loaded resources:
	    if (resources != null) {
		Enumeration entries = resources.elements() ;
		while ( entries.hasMoreElements() ) {
		    ResourceIndex index = (ResourceIndex)entries.nextElement();
		    index.unloadResource() ;
		}
	    }
	}
	// Clean-up all references we have to external objects:
	resources  = null ;
	manager    = null ;
    }

    /**
     * Save this store.
     */

    public void save() {
	if ( modified ) {
	    try {
		internalSave(false) ;
	    } catch (IOException ex) {
		warning("Save failed (IO) ["+ex.getMessage()+"]");
	    } catch (Exception oex) {
		warning("Save failed ["+oex.getMessage()+"]");
	    }
	}
    }

    /**
     * Enumerate all the resources saved in this store.
     * @return An enumeration of Strings, giving the identifier for all 
     *     the resources that this store knows about.
     */

    public Enumeration enumerateResourceIdentifiers() {
	markUsed();
	loadResources();
	return resources.keys();
    }

    /**
     * Check for the existence of a resource in this store.
     * @param identifier The identifier of the resource to check.
     * @return A boolean <strong>true</strong> if the resource exists
     *    in this store, <strong>false</strong> otherwise.
     */

    public boolean hasResource(String identifier) {
	markUsed();
	loadResources();
	return resources.get(identifier) != null ;
    }

    protected synchronized void loadResources() {
	int i = 0;
	if (resources == null) {
	    try {
		resources = new Hashtable(11);
		if (repository.exists()) {
		    Reader reader = 
			new BufferedReader(new FileReader(repository));
		    Resource resourceArray[] = 
			serializer.readResources(reader);
		    for (i = 0 ; i < resourceArray.length ; i++) {
			ResourceIndex entry = 
			    new ResourceIndex(resourceArray[i], false);
			if (entry != null && 
			    entry.getIdentifier() != null) {
			    resources.put(entry.getIdentifier(),
					  entry);
			}
		    }
		}
	    } catch (IOException ioex) {
		ioex.printStackTrace();
		warning("Unable to load resources");
	    } catch (SerializationException sex) {
		warning(sex.getMessage());
		sex.printStackTrace();
	    } catch (Exception ex) {
		ex.printStackTrace();
		String err = "Error in " + repository.getName() +
		    " in dir " + repository.getParent() + ": [" + i +
		    "] " + ex.getMessage();
		warning(err);
	    }
	}
    }

    /**
     * This resource store is being built, initialize it with the given arg.
     * @param manager The ResourceStoreManager instance that asks yourself
     * to initialize.
     * @param token The resource store manager key to that resource store, 
     * this token should be used when calling methods from the manager that
     * are to act on yourself.
     * @param repository A file, giving the location of the associated 
     *    repository.
     */
    public void initialize(ResourceStoreManager manager,
			   Object token,
			   File repository,
			   Serializer serializer) 
    {
	this.manager    = manager;
	this.token      = token;
	this.repository = repository;
	this.serializer = serializer;
	this.resources  = null;
	markUsed();
    }

}

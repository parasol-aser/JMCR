// Resourcestoremanager.java
// $Id: ResourceStoreManager.java,v 1.3 2010/06/15 18:00:15 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.store ;

import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ResourceSpace;
import org.w3c.tools.resources.ServerInterface;
import org.w3c.tools.resources.SpaceEntry;

import org.w3c.tools.resources.serialization.Serializer;

import org.w3c.tools.resources.event.ResourceEventQueue;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.util.AsyncLRUList;
import org.w3c.util.LRUAble;
import org.w3c.util.LRUList;
import org.w3c.util.Status;

class Reference implements ResourceReference {

    public static boolean debug = false;

    // Our entry
    StoreEntry entry = null;

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
	return lockCount;
    }

    protected void invalidate() {
	entry = null;
    }

    protected boolean isValid() {
	return ( entry != null ) ;
    }

    public Resource unsafeLock() 
	throws InvalidResourceException
    {
	return lock();
    }

    /**
     * Lock that reference in memory.
     * @return A pointer to the underlying resource, after lock succeed.
     */
    public synchronized Resource lock() 
	throws InvalidResourceException
    {
	lockCount++;
	if (entry == null)
	    throw new InvalidResourceException(identifier,
					      "This reference was invalidate");
	ResourceStore store = entry.getStore();
	Resource resource = store.lookupResource(identifier);
	if (debug) {
	    if (defs.get("context") == null)
		System.out.println("**** Context null for : "+identifier);
	    else if (((ResourceContext)(defs.get("context"))).getServer() 
		     == null)
		System.out.println("**** Server null for "+
				   identifier+"'s context");
	}
	if (resource == null) {
	    resource = store.loadResource(identifier, defs);
	}
	if (debug)
	    System.out.println("[LOCK] locking ["+lockCount+"]: "+identifier);
	return resource;
    }

    /**
     * Unlock that resource reference.
     */
    public void unlock() {
	lockCount--;
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

    Reference (StoreEntry entry, String identifier, Hashtable defs) {
	this.entry      = entry;
	this.identifier = identifier;
	this.defs       = defs;
    }

}

class StoreEntry implements LRUAble, Serializable {

    boolean istransient = false;

    // our key in the cache.
    Integer key = null;

    // LRU management infos:
    transient LRUAble next = null ;
    transient LRUAble prev = null ;

    // ResourceStore infos:
    transient ResourceStore store      = null ;

    // References
    transient Hashtable references = null;

    //The manager
    transient ResourceStoreManager manager = null;

    String repository = null ;

    transient File rep = null;

    public File getRepository() {
	if (rep == null)
	    rep = new File(manager.storedir, repository);
	return rep;
    }

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
    synchronized ResourceStore getStore() {
	if ( store == null ) {
	    store = new ResourceStoreImpl() ;
	    store.initialize(manager, this, getRepository(), 
			     manager.serializer) ;
	    manager.incrLoadedStore();
	}
	return store;
    }

    /**
     * Delete the store of this entry
     */
    synchronized void deleteStore() {
	Enumeration e = references.elements();
	Reference rr = null;
	while (e.hasMoreElements()) {
	    rr = (Reference) e.nextElement();
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
	rr = new Reference(this, name, defs);
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
    synchronized ResourceReference addResource(Resource resource, 
					       Hashtable defs) {
	getStore();
	store.addResource(resource);
	String name = resource.getIdentifier();
	ResourceReference rr = new Reference(this, name , defs);    
	references.put(name, rr);
	return rr;
    }

    /**
     * FIXME doc
     */
    public synchronized void markModified(Resource resource) {
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
	Reference rr = (Reference)lookupResource(oldid);
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
	Reference rr = (Reference)references.get(identifier);
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
		rr = (ResourceReference) e.nextElement();
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
	    references = new Hashtable(); // clear also the references
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
	this.references = new Hashtable();
    }

    StoreEntry(ResourceStoreManager manager, String repository, Integer key) {
	this.manager    = manager;
	this.store      = null ;
	this.repository = repository ;
	this.key        = key;
	this.references = new Hashtable();
    }

    StoreEntry(ResourceStoreManager manager, File repository, Integer key) {
	this.manager    = manager;
	this.store      = null ;
	this.rep        = repository ;
	this.key        = key;
	this.references = new Hashtable();
    }

}

class StoreManagerSweeper extends Thread {
    ResourceStoreManager manager = null ;
    boolean              killed  = false ;

    protected synchronized void waitEvent() {
	boolean done = false ;

	// Wait for an event to come by:
	while ( ! done ) {
	    try {
		wait() ;
		done = true ;
	    } catch (InterruptedException ex) {
	    }
	}
    }

    protected synchronized void sweep() {
	notifyAll() ;
    }

    protected synchronized void shutdown() {
	killed = true ;
	notifyAll() ;
    }

    public void run() {
	while ( true ) {
	    waitEvent() ;
	    // The whole trick is to run the collect method of the store 
	    // manager, without having the lock on the sweeper itself, so
	    // that clients can still trigger it later
	    if ( killed ) {
		break ;
	    } else {
		try {
		    manager.collect() ;
		} catch (Exception ex) {
		    // We really don't want this thread to die
		    ex.printStackTrace() ;
		}
	    }
	}
    }

    StoreManagerSweeper(ResourceStoreManager manager) {
	this.manager = manager ;
	this.setName("StoreSweeper") ;
	this.start() ;
    }

}

public class ResourceStoreManager implements ResourceSpace {

    public static boolean debug = false;

    /**
     * Number of sub-levels file system directories in the store directory.
     */
    public final static int SUBDIRS = 128;

    public static final String ROOT_REP = "root.xml";

    public static final String STATE_F = "state.xml";

    public boolean debugMemory = false;

    protected Serializer serializer = null;

    /**
     * The loaded resource stores entries.
     */
    Hashtable entries = new Hashtable(256); // <sentryKey - StoreEntry>
    /**
     * The limit of "BIG" stores, over this limits stores will be kept
     * in memory for performance purposes
     * If set to -1, this optimization won't happen
     */
    private int storeSizeLimit = -1;
    /**
     * The max number of loaded stores
     */ 
    private int maxLoadedStore = -1;
    /**
     * The number of loaded stores
     */ 
    private int loadedStore = 0;
    /**
     * Is this store shutdown ?
     */
    protected boolean closed = false ;
    /**
     * Our store directory.
     */
    protected File storedir = null;
    /**
     * Our index file.
     */
    protected File index = null;
    /**
     * server name;
     */
    protected String server_name = null;

    /**
     * Our root repository.
     */
    protected File root_repository = null;
    /**
     * The store entries least recetenly used list.
     */
    protected LRUList lru = null;
    // FIXME doc
    protected ResourceStoreState state = null;

    /**
     * Our sweeper thread:
     */
    protected StoreManagerSweeper sweeper = null ;

    protected ResourceEventQueue eventQueue = null;

    public ResourceEventQueue getEventQueue() {
	if (eventQueue == null)
	    eventQueue = new ResourceEventQueue();
	return eventQueue;
    }

    protected final int getMaxLoadedStore() {
	return maxLoadedStore ;
    }

    protected final int getStoreSizeLimit() {
	return storeSizeLimit;
    }

    protected synchronized void incrLoadedStore() {
	loadedStore++;
	checkMaxLoadedStore();
    }

    protected synchronized void decrLoadedStore() {
	loadedStore--;
    }

    /**
     * Check that this resource store manager isn't closed.
     * @exception RuntimeException If the store manager was closed.
     */
    protected final synchronized void checkClosed() {
	if ( closed )
	    throw new RuntimeException("Invalid store manager access.") ;
    }

    /**
     * Pick the least recently used entry, and remove all links to it.
     * After this method as run, the least recently used entry for some store
     * will be returned. The store manager will have discarded all its link to 
     * it, and the entry shutdown will have to be performed by the caller.
     * @return An StoreEntry instance, to be cleaned up.
     */
    protected NewStoreEntry pickLRUEntry() {
	NewStoreEntry entry;
	synchronized (lru) {
	    entry = (NewStoreEntry) lru.removeTail();
	}
	return entry;
    }

    /**
     * Collect enough entries to go back into fixed limits.
     */
    public void collect() {
	if (debugMemory)
	    System.out.println("[MEMORY] start sweeper <- "+
			       loadedStore+" stores loaded.");
	NewStoreEntry watchdog = null;
	NewStoreEntry entry    = null;
	int maxload = getMaxLoadedStore();
	while ( loadedStore > maxload ) {
	    entry = pickLRUEntry();
	    if (( entry != null ) && (entry != watchdog)) {
		synchronized(entry) {
		    if (! entry.unloadStore()) {
			synchronized (lru) {
			    lru.toHead(entry);
			    if (watchdog == null)
				watchdog = entry;
			}
		    } 
		}
	    } else {
		break;
	    }
	}
	if (debugMemory)
	    System.out.println("[MEMORY] sweeper done  -> "+
			       loadedStore+" stores still loaded.");
    }

    /**
     * Create a resource store repository name.
     * This method will return a new resource store repository key. When
     * used in conjunction with the <code>loadResourceStore</code> method
     * that takes a key as a parameter, this allows to caller to abstract
     * itself from the physical location of the repository.
     * @return A fresh resource store key, guaranteed to be uniq.
     */
    public synchronized String createResourceStoreRepository() {
	int key = state.getNextKey();
	return (key%SUBDIRS)+"/st-" + key;
    }

    public int getCurrentStoreIdentifier() {
	return state.getCurrentKey();
    }

    protected boolean checkSubDirs() {
	for (int i = 0 ; i < SUBDIRS ; i++) {
	    File subd = new File(storedir, Integer.toString(i));
	    if ((! subd.exists()) && ! subd.mkdirs())
		return false;
	}
	return true;
    }

    /**
     * Get the index file.
     * @return A File instance.
     */
    protected File getIndexFile() {
	if (index == null)
	    index = new File(storedir,server_name+"-index.xml");
	return index;
    }

    /**
     * Get the index file.
     * @return A File instance.
     */
    protected File getOldIndexFile() {
	if (index == null)
	    index = new File(storedir,server_name+"-index");
	return index;
    }

    /**
     * Get the root repository.
     * @return A File instance.
     */
    protected File getRootRepository() {
	if (root_repository == null)
	    root_repository = new File(storedir,ROOT_REP);
	return root_repository;
    }

    /**
     * Get The root key.
     * @return A String instance
     */
    protected Integer getRootKey() {
	return new Integer((new String("root")).hashCode());
    }

    /**
     * Emit the given string as a warning, to whoever it is appropriate.
     * @param msg The warning message.
     */
    protected void warning(String msg) {
	System.out.println("********* WARNING *********");
	System.out.println("[" + getClass().getName()+"]:\n" + msg) ;
	System.out.println("***************************");
    }

    protected synchronized void saveNewEntriesIndex() {
	File   indexFile = getIndexFile();
	String dir       = indexFile.getParent();
	String name      = indexFile.getName();
	File   tmp       = new File(dir, name+".tmp");
	File   bak       = new File(dir, name+".bak");
	File   tild      = new File(dir, name+".bak~");

	Enumeration elements = entries.elements();
	Vector v = new Vector(10);
	while (elements.hasMoreElements()) {
	    NewStoreEntry entry = (NewStoreEntry) elements.nextElement();
	    if (! entry.isTransient())
		v.addElement(entry);
	}
	NewStoreEntry se[] = new NewStoreEntry[v.size()];
	v.copyInto(se);

	try {
	    FileOutputStream fos = new FileOutputStream(tmp);
	    
	    Writer writer = new BufferedWriter(
		new OutputStreamWriter(fos,"UTF-8"));
	    serializer.writeResources(se, writer);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException("Unable to save entries index");	    
	}
	// 1st move: delete the ~ file if any:
	if ( tild.exists() )
	    tild.delete() ;
	// 2nd move rename bak to ~ (bak no longer exists)
	if ( bak.exists() ) {
	    bak.renameTo(tild);
	    bak.delete();
	}
	// 3nd move: rename the current index to bak
	if ( indexFile.exists() ) {
	    if ( !indexFile.renameTo(bak) ) {
		warning("unable to rename "+indexFile+" to "+bak);
		tild.renameTo(bak);
	    }
	    indexFile.delete();
	}
	// 4th move: rename the tmp file to index
	if ( !tmp.renameTo(indexFile) ) {
	    bak.renameTo(indexFile) ;
	    tild.renameTo(bak);
	    warning("unable to rename "+tmp+" to "+indexFile);
	}
	// cleanup (erase the ~ file)
	tild.delete() ;
    }

    protected void loadNewEntriesIndex() {
	File indexFile = getIndexFile();
	if (! indexFile.exists()) {
	    File bak = new File(indexFile.getParent(), 
				indexFile.getName()+".bak");
	    if (bak.exists()) {
		warning(indexFile+" not found! using bak file :"+bak);
		if ( !bak.renameTo(indexFile) ) {
		    warning("unable to rename "+bak+" to "+indexFile+
		    "\n Try by yourself or all your resources will be lost!");
		    System.exit(-1);
		}
	    } else {
		entries = new Hashtable(256);
		return;
	    }
	}
	entries = new Hashtable(256);
	try {
	    Reader reader = new BufferedReader(new FileReader(indexFile));
	    AttributeHolder holders[] = 
		serializer.readAttributeHolders(reader);
	    for (int i = 0 ; i < holders.length ; i++) {
		NewStoreEntry entry = (NewStoreEntry) holders[i];
		entries.put(entry.getKey(), entry);
		synchronized (lru) {
		    lru.toHead((LRUAble) entry);
		}
		entry.initialize(this);
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException("Unable to load entries index");
	}
    }

    /**
     * update the old index file. Load it and save it with the serializer.
     * @param the index file (serialized hashtable of StoreEntry)
     */
    public static synchronized 
	void updateEntriesIndex(File oldIndexFile,
				File newIndexFile,
				Serializer serializer) 
	throws IOException
    {
	Hashtable entries = new Hashtable();
	if (! oldIndexFile.exists()) {
	    File bak = new File(oldIndexFile.getParent(), 
				oldIndexFile.getName()+".bak");
	    if (bak.exists()) {
		System.out.println(oldIndexFile.getAbsolutePath()+
				   " not found! using bak file :"+bak);
		if ( !bak.renameTo(oldIndexFile) ) {
		    System.out.println("unable to rename "+bak+" to "+
				       oldIndexFile+
		    "\n Try by yourself or all your resources will be lost!");
		    System.exit(-1);
		}
	    } else {
		return;
	    }
	}

	ObjectInputStream oi = null;
	try {
	    oi = new ObjectInputStream(new BufferedInputStream
				       (new FileInputStream
					(oldIndexFile)));
	    entries = (Hashtable) oi.readObject();
	} catch (ClassNotFoundException ex) {
	    ex.printStackTrace();
	    throw new RuntimeException("Unable to load entries index");
	} catch (IOException ex) {
	    ex.printStackTrace();
	    throw new RuntimeException("Unable to load entries index");
	} finally {
	    if ( oi != null ) {
		try { oi.close() ; } catch (Exception ex) {}
	    }
	}
	
	Enumeration e   = entries.elements();
	int         len = 0;
	while (e.hasMoreElements()) {
	    StoreEntry entry = (StoreEntry)e.nextElement();
	    if (entry.isTransient()) 
		entries.remove(entry.key);
	    else {
		len++;
		String rep = entry.repository;
		if (rep != null) {
		    if (rep.equals("root.idx")) {
			entry.repository = "root.xml";
		    } else {
			// st-xxx
			int number = Integer.parseInt(rep.substring(3));
			entry.repository = (number%SUBDIRS)+"/"+rep;
		    }
		}
	    }
	}
	//StoreEntry => NewStoreEntry
	e = entries.elements();
	NewStoreEntry nentries[] = new NewStoreEntry[len];
	int i = 0;
	while (e.hasMoreElements()) {
	    StoreEntry    entry  = (StoreEntry)e.nextElement();
	    NewStoreEntry nentry = new NewStoreEntry(null,
						     entry.repository,
						     entry.key);
	    nentries[i++] = nentry;
	}
	FileOutputStream fos = new FileOutputStream(newIndexFile);
	OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
	Writer writer = new BufferedWriter(osw);
	serializer.writeResources(nentries, writer);
    }

    /**
     * Shutdown this resource store manager.
     * Go through all entries, and shut them down.
     */
    public synchronized void shutdown() {
	saveNewEntriesIndex();
	// Kill the sweeper thread:
	sweeper.shutdown() ;
	// Clenup all pending resource stores:
	Enumeration e = entries.elements() ;
	while ( e.hasMoreElements() ) {
	    NewStoreEntry entry = (NewStoreEntry) e.nextElement() ;
	    entry.shutdownStore() ;
	    entries.remove(entry.getKey()) ;
	}
	closed = true ;
	// Now save our state:
	File rmstate = new File(storedir, STATE_F);
	try {
	    FileOutputStream fos = new FileOutputStream(rmstate);
	    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
	    Writer writer = new BufferedWriter ( osw );
	    AttributeHolder statearray[] = { state };
	    serializer.writeResources(statearray, writer);
	} catch (Exception ex) {
	    // FIXME !!
	    System.out.println("ResourceStoreManager: unable to save state !");
	    ex.printStackTrace();
	}
    }

    /**
     * Checkpoint all modified resource stores, by saving them to disk.
     */
    public void checkpoint() {
	try {
	    saveNewEntriesIndex();
	} catch (Exception ex) {
	    System.out.println("*** Error during checkpoint");
	}
	// Checkpoint all loaded resource stores:
	Enumeration e = entries.elements();
	while ( e.hasMoreElements() ) {
	    NewStoreEntry entry = (NewStoreEntry) e.nextElement();
	    try {
		entry.saveStore();
	    } catch (Exception ex) {
		if (entry == null) {
		    System.out.println("*** Error, saving null entry!");
		} else {
		    System.out.println("*** Error while saving store " 
				       + entry.getKey());
		}
	    }
	}
	// Then save our state:
	File rmstate = new File(storedir, STATE_F);
	try {
	    FileOutputStream fos = new FileOutputStream(rmstate);
	    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
	    Writer writer = new BufferedWriter ( osw );
	    AttributeHolder statearray[] = { state };
	    serializer.writeResources(statearray, writer);
	} catch (Exception ex) {
	    // FIXME !!
	    System.out.println("ResourceStoreManager: unable to save state !");
	    ex.printStackTrace();
	}
    }

    /**
     * Mark the given store as having been used recently.
     * @param token The token the resource store manager provided you when
     * it initialized the store.
     */
    public void markUsed(Object token) {
	NewStoreEntry entry = (NewStoreEntry) token;
	if ( entry != null ) {
	    synchronized (lru) {
	    	lru.toHead(entry);
	    }
        }
    }

    protected boolean used(String rep) {
	Enumeration e = entries.elements();
	NewStoreEntry entry = null;
	while (e.hasMoreElements()) {
	    entry = (NewStoreEntry)e.nextElement();
	    System.out.println(storedir+":"+entry.getRepository());
	    try {
		if (entry.getRepository().getName().equals(rep))
		    return true;
	    } catch (Exception ex) {
		//continue
	    }
	}
	return false;
    }

    public void salvage() {
	String stores[] = storedir.list();
	if ( stores != null ) {
	    for (int i = 0 ; i < stores.length; i++) {
		if ((stores[i].length() <= 3) || ! stores[i].startsWith("st-"))
		    continue;
		if ( stores[i].endsWith(".bak") )
		    continue;
		//Is there a storeEntry using this repository?
		if (! used(stores[i])) {
		    // For testing now
		    System.err.println("*** "+stores[i]+" not used! deleted");
		    File notUsed = new File(storedir, stores[i]);
		    notUsed.delete();
		}
	    }
	}
    }

    public void displayIndex() {
	Enumeration e = entries.elements();
	NewStoreEntry entry = null;
	System.out.println("Index : ");
	while (e.hasMoreElements()) {
	    entry = (NewStoreEntry)e.nextElement();
	    System.out.println(entry.getKey()+" : "+entry.getRepository());
	}
    }

   
    /**
     * Try to salvage the resource store manager state.
     * That's pretty easy and robust, it should really work well if no
     * one hacked the store directory.
     * @return A ResourceStoreState instance.
     */
    public ResourceStoreState salvageState() {
	System.err.println("*** salvaging resource manager state...");
	File state = new File(storedir, STATE_F);
	int  maxid = -1;
	FilenameFilter filter = new FilenameFilter() {
	    public boolean accept(File dir, String name) {
		return (name.startsWith("st-") && (! name.endsWith(".bak")));
	    }
	};
	for (int i = 0 ; i < SUBDIRS ; i++) {
	    File subdir = new File(storedir, Integer.toString(i));
	    if (subdir.exists()) {
		String stores[] = subdir.list(filter);
		for (int j = 0 ; j < stores.length; j++) {
		    int id = Integer.parseInt(stores[j].substring(3));
		    maxid  = Math.max(maxid, id);
		}
	    }
	}
	++maxid;
	System.err.println("*** resource store state salvaged, using: "+maxid);
	return new ResourceStoreState(maxid);
    }

    /**
     * Restore the resource whose name is given from the root NewStoreEntry.
     * @param identifier The identifier of the resource to restore.
     * @param defs Default attribute values.
     */ 
    public ResourceReference loadRootResource(String identifier,
					      Hashtable defs)
    {
	NewStoreEntry entry = (NewStoreEntry) entries.get(getRootKey());
	if (entry == null) {
	    synchronized (this) {
		entry = (NewStoreEntry) entries.get(getRootKey());
		if (entry == null) {
		    entry = new NewStoreEntry(this, ROOT_REP, getRootKey());
		    entries.put(getRootKey(), entry);
		}
	    }
	}
	return entry.loadResource(identifier, defs);
    }

    /**
     * Check that the key is not already in the list of references 
     * @return a boolean, true if the key is not there, false if it is
     */
    public boolean checkKey(Integer key) {
	if (entries != null) {
	    return !entries.containsKey(key);
	}
	return true;
    }

    /**
     * Lookup this resource.
     * @param sentry The resource space entry.
     * @param identifier The resource identifier.
     * @return A Resource instance, or <strong>null</strong> if either the
     *    resource doesn't exist, or it isn't loaded yet.
     */
    public ResourceReference lookupResource(SpaceEntry sentry, 
					    String identifier)
    {
	NewStoreEntry entry = 
	    (NewStoreEntry) entries.get(sentry.getEntryKey());
	if (entry == null)
	    throw new RuntimeException("Unable to lookup resource ("+
				       identifier+
				       "), no StoreEntry for its space entry");
	return entry.lookupResource(identifier);
    }

    /**
     * Restore the resource whose name is given.
     * @param sentry The resource space entry.
     * @param identifier The identifier of the resource to restore.
     * @param defs Default attribute values.
     */
    public ResourceReference loadResource(SpaceEntry sentry, 
					  String identifier,
					  Hashtable defs)
    {
	if (debug) {
	    System.out.println("[RSM] loading ["+identifier+"] from ["+
			       sentry+']');
	}
	NewStoreEntry entry = 
	    (NewStoreEntry) entries.get(sentry.getEntryKey());
	if (entry == null) 
	    throw new RuntimeException(identifier+": Space Entry not valid");
	return entry.loadResource(identifier, defs);
    }

    /**
     * Add this resource to the StoreEntry of the space entry.
     * @param sentry The resource space entry.
     * @param resource The resource to add.
     * @param defs Default attribute values.
     */
    public ResourceReference addResource(SpaceEntry sentry,
					 Resource resource,
					 Hashtable defs)
    {
	NewStoreEntry entry = 
	    (NewStoreEntry) entries.get(sentry.getEntryKey());
	if (entry == null)
	    throw new RuntimeException("Unable to add resource ("+
				       resource.getIdentifier()+
				       "), no StoreEntry for its space entry");
	return entry.addResource(resource, defs);
    }

    /**
     * Save this resource to the StoreEntry of the space entry.
     * @param sentry The resource space entry
     * @param resource The resource to save.
     */
    public void saveResource(SpaceEntry sentry,
			     Resource resource) 
    {
	NewStoreEntry entry = 
	    (NewStoreEntry) entries.get(sentry.getEntryKey());
	if (entry == null)
	    throw new RuntimeException("Unable to save resource ("+
				       resource.getIdentifier()+
				       "), no StoreEntry for its space entry");
	entry.saveResource(resource);
    }

    /**
     * Mark the given resource as being modified.
     * @param sentry The resource space entry.
     * @param resource The resource to mark as modified.
     */
    public void markModified(SpaceEntry sentry,
			     Resource resource) 
    {
	NewStoreEntry entry = 
	    (NewStoreEntry) entries.get(sentry.getEntryKey());
	if (entry == null)
	    throw new RuntimeException("Unable to mark resource ("+
				       resource.getIdentifier()+
				       "), no StoreEntry for its space entry");
	synchronized (resource) {
	    entry.markModified(resource);
	}
    }

    /**
     * Rename a resource in this resource space.
     * @param sentry The resource space entry.
     * @param oldid The old resorce identifier.
     * @param newid The new resorce identifier.
     */
    public void renameResource(SpaceEntry sentry,
			       String oldid,
			       String newid) 
    {
	NewStoreEntry entry = 
	    (NewStoreEntry) entries.get(sentry.getEntryKey());
	if (entry == null)
	    throw new RuntimeException("Unable to rename resource ("+
				       oldid+" to "+newid+
				       "), no StoreEntry for its space entry");
	entry.renameResource(oldid, newid);
    }

    /**
     * delete this resource from the StoreEntry (and the repository).
     * @param sentry The resource space entry
     * @param resource The resource to delete.
     */
    public void deleteResource(SpaceEntry sentry,
			       Resource resource) 
    {
	NewStoreEntry entry = 
	    (NewStoreEntry) entries.get(sentry.getEntryKey());
	if (entry == null)
	    throw new RuntimeException("Unable to delete resource ("+
				       resource.getIdentifier()+
				       "), no StoreEntry for its space entry");
	entry.removeResource(resource.getIdentifier());
    }

    /**
     * Delete all the children of resource indentified by its
     * space entry.
     * @param sentry The resource space entry
     */
    public void deleteChildren(SpaceEntry sentry) {
	NewStoreEntry entry = 
	    (NewStoreEntry) entries.get(sentry.getEntryKey());
	if (entry == null)
	    throw new RuntimeException(
				    "Unable to delete children, no StoreEntry"+
				    " for its space entry");
	entry.deleteStore();
	entries.remove(sentry.getEntryKey());
    }

    /**
     * Save all the children of the resource indentified by its
     * spaec entry.
     * @param sentry The resource space entry
     */  
    public void saveChildren(SpaceEntry sentry) {
	NewStoreEntry entry = 
	    (NewStoreEntry) entries.get(sentry.getEntryKey());
	if (entry == null)
	    throw new RuntimeException(
				      "Unable to save children, no StoreEntry"+
				      " for its space entry");
	entry.saveStore();
    }

    protected void checkMaxLoadedStore() {
	// Check to see if we have exceeded our quota:
	if (loadedStore > getMaxLoadedStore()) {
	    sweeper.sweep() ;
	}
    }

    /**
     * acquire children from an external file.
     * @param sentry The resource space entry.
     * @param repository The file used to store children.
     */
    public void acquireChildren(SpaceEntry sentry,
				File repository,
				boolean transientFlag) 
    {
	NewStoreEntry entry = 
	    (NewStoreEntry) entries.get(sentry.getEntryKey());
	if (entry == null) {
	    //create it.
	    checkMaxLoadedStore();
	    entry = new NewStoreEntry(this, repository, sentry.getEntryKey());
	    entry.setTransient(transientFlag);
	    entries.put(sentry.getEntryKey(), entry);
	    // FIXME : unload index hashtable ?
	} else {
	    entry.rep = repository;
	}
    }

    /**
     * Acquire the StoreEntry of the space entry.
     * @param sentry The resource space entry.
     */
    public void acquireChildren(SpaceEntry sentry) {
	// 
	NewStoreEntry entry = 
	    (NewStoreEntry) entries.get(sentry.getEntryKey());
	if (entry == null) {
	    //create it.
	    checkMaxLoadedStore();
	    String rpath = createResourceStoreRepository();
	    File repository = new File(storedir, rpath);
	    if (repository.exists()) {
		String fpath = new String(rpath);
		String lpath = null;
		do {
		    lpath = new String(rpath);
		    rpath = createResourceStoreRepository();
		    repository = new File(storedir, rpath);
		} while (repository.exists());
		if (fpath.equals(lpath))
		    warning("repository "+fpath+" exists! using "+rpath);
		else
		    warning("repositories "+fpath+" to "+
			    lpath+" exists! using "+rpath);
	    }
	    entry = new NewStoreEntry(this, rpath, sentry.getEntryKey());
	    entries.put(sentry.getEntryKey(), entry);
	    // FIXME : unload index hashtable ?
	}
    }

    /**
     * Implementation of Status interface
     * Display statistics about usage of the resource store manager
     * @returns a String containing a fragment of HTML
     */
    public String getHTMLStatus() {
	Enumeration elements = entries.elements();
	int nb_entries = 0;
	int entries_nn = 0;
	int nb_entries_store = 0;
	int stores_nn = 0;
	int oversized = 0;
	while (elements.hasMoreElements()) {
	    int nb;
	    NewStoreEntry entry = (NewStoreEntry) elements.nextElement();
	    if (entry.references != null) {
		nb = entry.references.size();
		if (nb > 0) {
		    nb_entries += nb;
		    entries_nn++;
		}
	    }
	    if (entry.store != null) {
		ResourceStoreImpl st = (ResourceStoreImpl) entry.store;
		if (st.resources != null) {
		    nb = st.resources.size();
		    if (nb > 0) {
			int ssize = st.resources.size();
			nb_entries_store += ssize;
			stores_nn++;
			if ((storeSizeLimit > 0) && (ssize > storeSizeLimit)) {
			    oversized++;
			}
		    }
		}
	    }
	}
	StringBuffer sb = new StringBuffer();
	sb.append("<table border=\"1\" class=\"store\">\n"
		  + "<caption>Resource Store usage</caption>\n"
		  + "<tr><th>Loaded Stores</th>"
		  + "<th>Loaded Entries</th></tr>\n");
	sb.append("<tr><td>");
	sb.append(loadedStore);
	sb.append(" (");
	sb.append(entries.size());
	sb.append(") / ");
	sb.append(maxLoadedStore);
	if (storeSizeLimit >= 0) {
	    sb.append(" [");
	    sb.append(oversized);
	    sb.append(" > ");
	    sb.append(storeSizeLimit);
	    sb.append(" ]");
	}
	sb.append("</td><td>");
	sb.append(nb_entries);
	sb.append('(');
	sb.append(entries_nn);
	sb.append(')');
	sb.append(" / ");
	sb.append(nb_entries_store);
	sb.append('(');
	sb.append(stores_nn);
	sb.append(")</td>\n</table>");
	return sb.toString();
    }


    /**
     * Enumerate the name (ie identifiers) of the space entry children.
     * @param sentry The space entry.
     * @param all Should all resources be listed.
     * @return An enumeration, providing one element per child, which is
     * the name of the child, as a String.
     */
    public Enumeration enumerateResourceIdentifiers(SpaceEntry sentry) {
	NewStoreEntry entry = 
	    (NewStoreEntry) entries.get(sentry.getEntryKey());
	if (entry == null)
	    throw new RuntimeException(
				      "Unable to list children, no StoreEntry"+
				      " for its space entry");
	return entry.enumerateResourceIdentifiers();
    }

    /**
     * Create a new resource store manager for given store directory.
     * The resource store manager will manage the collection of stores 
     * contained in the directory, and keep track of the stores state.
     * @param storedir The store directory to manage.
     */
    public ResourceStoreManager (String server_name,
				 File storedir, 
				 String default_root_class,
				 String default_root_name,
				 String serializer_class,
				 int max_loaded_store,
				 Hashtable defs)
    {
	this(server_name, storedir, default_root_class, default_root_name,
		    serializer_class, max_loaded_store, -1, defs);
    }

    /**
     * Create a new resource store manager for given store directory.
     * The resource store manager will manage the collection of stores 
     * contained in the directory, and keep track of the stores state.
     * @param storedir The store directory to manage.
     */
    public ResourceStoreManager (String server_name,
				 File storedir, 
				 String default_root_class,
				 String default_root_name,
				 String serializer_class,
				 int max_loaded_store,
				 int store_size_limit,
				 Hashtable defs)
    {
	// Initialize the instance variables:
	this.server_name    = server_name;
	this.storedir       = storedir;
	this.entries        = new Hashtable() ;
	this.sweeper        = new StoreManagerSweeper(this) ;
	this.lru            = new AsyncLRUList();
	this.maxLoadedStore = (max_loaded_store < 10) ? 10 : max_loaded_store;
	this.storeSizeLimit = (store_size_limit < 100) ? -1 : store_size_limit;
	this.loadedStore = 0;
	
	try {
	    Class ser_class = Class.forName(serializer_class);
	    this.serializer = (Serializer) ser_class.newInstance();
	    serializer = new org.w3c.tools.resources.serialization.xml.XMLSerializer();
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException("Invalid serializer class : "+
				       serializer_class);
	}

	loadNewEntriesIndex();

	if (! checkSubDirs())
	    throw new RuntimeException("Unable to create store "+
				       "subdirectories!");

	// If not already available, create the root resource, and its
	// repository.
	getRootRepository();
	if (! root_repository.exists()) {
	    try {
		Class root_class = Class.forName(default_root_class);
		Resource root = (Resource) root_class.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
		if (defs == null)
		    defs = new Hashtable(4);
		defs.put("identifier".intern(), default_root_name);
		defs.put("key".intern(), getRootKey());
		root.initialize(defs);
		
		NewStoreEntry entry = new NewStoreEntry(this,
							ROOT_REP,
							getRootKey());
		ResourceReference rr = entry.addResource(root, defs);
		ResourceContext context = (ResourceContext) 
		    defs.get("context");
		context.setResourceReference(rr);
		entry.saveResource(root);
		entries.put(getRootKey(), entry);
		saveNewEntriesIndex();
	    } catch (InstantiationException ex) {
	    } catch (IllegalAccessException ex) {
	    } catch (ClassNotFoundException ex) {
		System.out.println(ex.getMessage());
		ex.printStackTrace();
	    }
	}
	
	// If not already available, create the resource store state object:
	File rsmstate = new File(storedir, STATE_F);
	// Restore it:
	Reader reader = null;
	try {
	    reader = new BufferedReader( new FileReader(rsmstate));
	    AttributeHolder states[] = serializer.readAttributeHolders(reader);
	    this.state = (ResourceStoreState) states[0];
	} catch (Exception ex) {
	    // Let's try to fix this:
	    this.state = salvageState();
	}
	if ( reader != null ) {
	    try { reader.close(); } catch (IOException ex) {}
	}
	//    salvage();
	//    displayIndex();
    }

}



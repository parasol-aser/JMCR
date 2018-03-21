// CacheGeneration.java
// $Id: CacheGeneration.java,v 1.1 2010/06/15 12:25:09 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import java.io.File;
import java.io.PrintStream;

import org.w3c.util.LRUAble;
import org.w3c.util.LRUList;
import org.w3c.util.LookupTable;
import org.w3c.util.SyncLRUList;

public class CacheGeneration implements LRUAble {
    // the usual debug flag
    private static final boolean debug = true;

    // hashtable of resources
    private Hashtable lookupTable = null;
    // the LRUList of resources
    private SyncLRUList lruList = null;
    // the occupation of this generation
    private long bytecount = 0;
    // the capacity of this generation
    private long bytelimit = 0;
    // stored size
    private long bytestored = 0;
    // serialization flag
    private boolean saved = false;
    // serialization flag
    private boolean loaded = false;
    // resource count
    private int cr_count = 0;
    // the ID of this generation
    private int id = 0;

    // a Vector or resource to be removed
    private Vector toDel = null;

    // our father
    private CacheStore store = null;

    protected File generationFile = null;

    /**
     * set the file where the generation is stored
     * @param generationFile the file
     */
    public void setGenerationFile(File generationFile) {
	this.generationFile = generationFile;
    }

    /**
     * get the generation file
     * @return a File
     */
    public File getGenerationFile() {
	return generationFile;
    }

    /**
     * Is the generation loaded?
     * @return a boolean
     */
    public boolean isLoaded() {
	return loaded;
    }

    /**
     * Set the generation as loaded or unloaded
     * @param loaded the new loaded flag
     */
    protected void setLoaded(boolean loaded) {
	this.loaded = loaded;
    }

    /**
     * Is the generation saved?
     * @return a boolean
     */
    public boolean isSaved() {
	return saved;
    }

    /**
     * Set the generation as saved or not.
     * @param saved a boolean
     */
    protected void setSaved(boolean saved) {
	this.saved = saved;
    }

    /**
     * LRU management - previous entry.
     */
    protected LRUAble prev = null;
    /**
     * LRU management - next entry.
     */
    protected LRUAble next = null;

    /**
     * LRU management - Get next node.
     * @return A CacheGeneration instance.
     */
    public LRUAble getNext() {
	return next;
    }

    /**
     * LRU management - Get previous node.
     * @return A CacheGeneration instance.
     */
    public LRUAble getPrev() {
	return prev;
    }

    /**
     * LRU management - Set next node.
     */
    public synchronized void setNext(LRUAble next) {
	this.next = next;
    }

    /**
     * LRU management - Set previous node.
     */
    public synchronized void setPrev(LRUAble prev) {
	this.prev = prev;
    }

    /**
     * Get the ID of this generation
     * @return an int, the generation number
     */
    public int getId() {
	return id;
    }

    /**
     * Set the ID of this generation
     * Useful to reuse generation
     * @param an integer, the new generation number
     */
    public synchronized void setId(int id) {
	this.id = id;
    }

    /**
     * Give the acual occupation level of this generation
     * @return a long, the number of bytes of this generation
     */
    public long getCachedByteCount() {
	return bytecount;
    }

    /**
     * Give the fill ratio for the cached resources
     * @return a float between 0 and 1
     */
    public float getFillRatio() {
	return ((float) bytecount / (float) bytelimit);
    }

    /**
     * Give the acual storeage occupation level of this generation
     * @return a long, the number of bytes of this generation
     */
    public long getStoredByteCount() {
	return bytestored;
    }

    /**
     * Get the bytecount limit for this generation
     * @return a long, the maximum number of bytes
     */
    public long getByteLimit() {
	return bytelimit;
    }

    /**
     * Set the new bytecount limit, not that it may perform a cleanup
     * if necessary.
     * @param long, the new maximum number of bytes
     */
    public synchronized void setByteLimit(long newlimit) {
	bytelimit = newlimit;
	if (newlimit >= bytecount) {
	    return;
	}
	// try to get some space 
	long to_save = newlimit - bytecount;
	// be nice
	to_save -= collectSpace(newlimit - bytecount, true);
	// then get the space we want ;)
	to_save -= collectSpace(to_save, false);
    }

    /**
     * Deletes a resource from the "to be deleted" vector
     * it updates also the number of bye stored in this generation
     * @return the number of bytes saved.
     */
    public long deleteStored(CachedResource cr) {
	if (! loaded)
	    throw new UnloadedGenerationException("generation "+id);
	if (debug) {
	    System.out.println("Deleting "+cr.getIdentifier()+
			       "from generation: "+id);
	}
	toDel.removeElement(cr);
	long saved = cr.delete();
	synchronized(this) {
	    bytestored -= saved;
	}
	store.getState().notifyResourceDeleted(cr);
	return saved;
    }

    /**
     * Check if a resource has been cached in this generation
     * @param url the resource url
     * @return a boolean
     */
    public synchronized boolean containsResource(String url) {
	return (lookupTable.get(url) != null);
    }

    /**
     * Get all the files handled by this generation
     * @return an enumeration of File
     */
    public synchronized Enumeration getFiles() {
	Vector files = new Vector();
	if (loaded) {
	    Enumeration fenum = lookupTable.elements();
	    while (fenum.hasMoreElements()) {
		CachedResource cr = (CachedResource) fenum.nextElement();
		File file = cr.getFile();
		if (file != null) {
		    files.addElement(file);
		}
	    }
	} else {
	    Enumeration fenum = lookupTable.elements();
	    while (fenum.hasMoreElements()) {
		String file = (String) fenum.nextElement();
		if (! file.equals("")) {
		    files.addElement(new File(file));
		}
	    }
	}
	return files.elements();
    }

    /**
     * Get the CachedResource relative to the given URL.
     * @param url the URL of the CachedResource to find
     * @return a CachedResource or null.
     */
    public synchronized CachedResource lookupResource(String url) {
	if (! loaded)
	    throw new UnloadedGenerationException("generation "+id);
	return (CachedResource)lookupTable.get(url);
    }

    /**
     * can this resource be stored?
     * If the resource is in the generation, only the delta will be taken
     * into account
     * @param CachedResource cr, the candidate.
     * @param long size, the size of the candidate.
     * @return a boolean, if this generation can cache it or not
     */
    private boolean canStore(CachedResource cr, long size) {
	if (! loaded)
	    throw new UnloadedGenerationException("generation "+id);
	CachedResource old_cr = null;
	old_cr = (CachedResource) lookupTable.get(cr.getIdentifier());
	if (old_cr != null) {
	    long delta = size - old_cr.getCurrentLength();
	    if ( (bytecount + delta) > bytelimit) {
		return false;
	    }
	}
	if ((size + bytecount) > bytelimit) {
	    return false;
	}
	return true;
    }

    /**
     * Adds this resource, if possible
     * @param cr, the candidate.
     * @param size, the size of the candidate.
     * @return a boolean, true if this resource has been cached
     */
    public synchronized boolean addResource(CachedResource cr, 
					    long size,
					    long oldsize)
    {
	if (! loaded)
	    throw new UnloadedGenerationException("generation "+id);
	if (canStore(cr, size)) {
	    CachedResource old_cr = null;
	    old_cr = (CachedResource) lookupTable.get(cr.getIdentifier());
	    // do we already have this resource?
	    if (old_cr != null) {
		// this is the real oldsize
		oldsize = old_cr.getCurrentLength();
		long delta   = size - oldsize;
		if  ((bytecount + delta) > bytelimit) {
		    return false;
		} 
		lookupTable.remove(cr.getIdentifier());
		lruList.remove(cr);
		bytecount -= oldsize;
		toDel.addElement(old_cr);
		store.getState().notifyResourceReplaced(cr, oldsize);
	    } else {
		store.getState().notifyResourceAdded(cr, oldsize);
	    }
	    lookupTable.put(cr.getIdentifier(), cr);
	    cr.generation = this;
	    lruList.toHead(cr);
	    bytestored += size;
	    bytecount  += size;
	    saved       = false;
	    cr_count++;
	    return true;
	}
	return false;
    }

    /**
     * Load a CachedResource in this generation. (to be used only at
     * generation loading). This method load only valid cachedresource.
     * Check if the associated file exists and has the right size.
     * @param CachedResource the CachedResource to load.
     */
    protected void loadCachedResource(CachedResource cr) {
	File file = cr.getFile();
	if ((file != null) && 
	    ((! file.exists()) || (file.length() != cr.getCurrentLength()))) {
	    //don't load invalid cachedresource
	    return;
	}
	lookupTable.put(cr.getIdentifier(), cr);
	cr.generation = this;
	lruList.toHead(cr);
	long size   = cr.getCurrentLength();
	bytestored += size;
	bytecount  += size;
	cr_count++;
    }

    /**
     * Remove the resource from the generation (but don't delete it).
     * @param cr the CachedResource to remove.
     * @return the number of byte saved
     * @exception NoSuchResourceException if this resource was not in this 
     * generation     
     */
    public synchronized long removeResource(CachedResource cr) 
	throws NoSuchResourceException
    {
	if (! loaded)
	    throw new UnloadedGenerationException("generation "+id);
	return removeResource(cr.getIdentifier());
    }

    /**
     * Remove the resource from the generation (but don't delete it).
     * @param cr the CachedResource to remove.
     * @return the number of byte saved
     * @exception NoSuchResourceException if this resource was not in this 
     * generation     
     */
    public synchronized long removeResource(String url) 
	throws NoSuchResourceException
    {
	if (! loaded)
	    throw new UnloadedGenerationException("generation "+id);
	CachedResource old_cr = _removeResource(url);
	return old_cr.getCurrentLength();
    }

    /**
     * Remove the resource from the generation and update the bytecount 
     * variable.
     * @param url the CachedResource URL
     * @return the CachedResource removed.
     * @exception NoSuchResourceException if this resource was not in this 
     * generation
     */
    private CachedResource _removeResource(String url) 
	throws NoSuchResourceException
    {
	if (debug) {
	    System.err.println("*** removing from generation "+id+": " + url);
	}
	CachedResource old_cr = (CachedResource) lookupTable.get(url);
	if (old_cr == null) {
	    String msg = url + " not found in generation " + id;
	    throw new NoSuchResourceException(msg);
	}
	lookupTable.remove(url);
	lruList.remove(old_cr);
	long b_saved = old_cr.getCurrentLength();
	if (debug) {
	    System.err.println("*** removed... saved " + b_saved + " bytes");
	}
	bytecount  -= b_saved;
	bytestored -= b_saved;
	saved = false;
	cr_count--;
	return old_cr;
    }

    /**
     * will garbage collect up to "size" bytes in this generation.
     * WARNING: this is not synchronized, use with caution!
     * @param long the number of bytes to be collected
     * @param check, a boolean, used to validate or not the resource before
     * deleting them (ie: delete only invalid resources)
     * @return a long, the number of bytes saved
     * from disk afterward using delete.
     */

    public long collectSpace(long size, boolean check) {
	if (! loaded) {
	    // load me, and unload another generation is necessary.
	    try {
		store.loadGeneration(this);
	    } catch (InvalidCacheException ex) {
		// oups! cache corrupted?
		if (debug) {
		    System.err.println("*** Collecting "+
				       "Unable to load generation ["+
				       getId()+"]");
		    System.err.println(ex.getMessage());
		}
		return 0;
	    }
	}
	Vector res_vect = new Vector();
	long collected = 0;
	CachedResource cr, ncr;
	CacheValidator validator;
	
	if (debug) {
	    System.err.println("*** Collecting " + size + " bytes " + 
			       ((check) ? "with" : "without") + " checking");
	}
	// dumb check
	if (size <= 0)
	    return 0;
	if ((size > bytecount) && !check) {
	    return emptyGeneration();
	}
	validator = store.getValidator();
	// start with the oldest ones
	cr = (CachedResource) lruList.getTail();
	while (cr != null) {
	    // check if we can delete the resource or not
	    if (check) {
		if (validator.checkStaleness(cr)) {
		    cr = (CachedResource) lruList.getPrev(cr);
		    continue;
		}
	    }
	    ncr = (CachedResource) lruList.getPrev(cr);
	    synchronized(this) {
		lookupTable.remove(cr.getIdentifier());
		lruList.remove(cr);
		saved = false;
		cr_count--;
		store.getState().notifyResourceToBeDeleted(cr);
	    }
	    collected += cr.getCurrentLength();
	    toDel.addElement(cr);
	    if (collected >= size) {
		break;
	    }
	    cr = ncr;
	}
	synchronized (this) {
	    bytecount -= collected;
	}
	if (debug) {
	    System.err.println("*** Collected " + collected + 
			       " bytes from generation "+id);
	}
	return collected;
    }

    /**
     * empty this generation
     * @return a long, the number of bytes saved
     */
    protected long emptyGeneration() {
	if (! loaded)
	    throw new UnloadedGenerationException("generation "+id);
	Hashtable saved_table;
	long collected;
	if (debug) {
	    System.err.println("*** Deleting Generation " + id + " ("
			       + bytecount + ")");
	}
	synchronized (this) {
	    collected = bytecount;
	    saved_table = lookupTable;
	    lookupTable = new Hashtable();
	    lruList = new SyncLRUList();
	    bytecount = 0;
	    cr_count = 0;
	}
	Enumeration e = saved_table.elements();
	while (e.hasMoreElements()) {
	    CachedResource cr = (CachedResource) e.nextElement();
	    toDel.addElement(cr);
	    store.getState().notifyResourceToBeDeleted(cr);
	}
	generationFile.delete();
	saved = false;
	return collected;
    }

    /**
     * Get the CachedResource of this generation (except the "to be
     * deleted" resources)
     * @return an Enumeration of CachedResource
     */
    public Enumeration getCachedResources() {
	if (! loaded)
	    throw new UnloadedGenerationException("generation "+id);
	return lookupTable.elements();
    }

    /**
     * get the deleted but still stored resource
     * @returns an enumeration of CachedResources
     */
    public Enumeration getDeletedResources() {
	return toDel.elements();
    }

    /**
     * Set this Generation as a description (update the saved and loaded 
     * status)
     * @param tables the LookupTables containing attribute descriptions
     */
    protected void setDescription(LookupTable tables[]) {
	clean();
	saved      = true;
	bytestored = 0;
	bytecount  = 0;
	cr_count   = 0;
	for (int i = 0 ; i < tables.length ; i++) {
	    LookupTable table = tables[i];
	    try {
		String stored = 
		    (String)table.get(CachedResource.NAME_CURRENT_LENGTH);
		String id = 
		    (String)table.get(CachedResource.NAME_IDENTIFIER);
		String file =
		    (String)table.get(CachedResource.NAME_FILE);
		file = (file == null) ? "" : file;
		bytestored += Integer.parseInt(stored);
		lookupTable.put(id, file);
	    } catch (Exception ex) {
		if (debug) {
		    System.err.println("Unable to load description in ["+
				       getId()+"] "+ex.getMessage());
		}
	    }
	}
    }

    /**
     * Unload the generation, transform CachedResources to descriptions.
     */
    public void unload() {
	// bytestored unchanged
	clean();
	saved             = true;
	bytecount         = 0;
	cr_count          = 0;
	Enumeration kenum = lookupTable.keys();
	while(kenum.hasMoreElements()) {
	    lookupTable.put(kenum.nextElement(), Boolean.TRUE);
	}
    }

    /**
     * delete the serialized resource file from the disk
     */
    protected void deleteGenerationFile() {
	generationFile.delete();
    }

    /**
     * Get the current number of resource loaded.
     * @return an long
     */
    public int getCRCount() {
	return cr_count;
    }

    /**
     * Clean this generation.
     */
    public void clean() {
	this.lookupTable = new Hashtable();
	this.lruList     = new SyncLRUList();
	this.loaded      = false;
    }

    /**
     * copy the content of the generation here
     * @parameter a CacheGeneration, the one we want to dump here
     */
    protected synchronized void copyInto(CacheGeneration gen) {
	// add everything at the "right" place
	LRUList ngenLruList = gen.lruList;
	CachedResource cr = (CachedResource) ngenLruList.getHead();
	while (cr != null) {
	    CachedResource next_cr = (CachedResource) ngenLruList.getNext(cr);
	    cr.generation = this;
	    lruList.toTail(cr);
	    lookupTable.put(cr.getIdentifier(), cr);
	    cr = next_cr;
	}
	gen.setSaved(false);
	// now dump the toDel vector
	Enumeration e = gen.toDel.elements();
	while (e.hasMoreElements()) {
	    toDel.add(e.nextElement());
	}
	// update the values
	cr_count += gen.cr_count;
	bytecount += gen.bytecount;
	bytestored += gen.bytestored;
	// finally state that we have been modified
	saved = false;
    }

    public CacheGeneration(CacheStore store, long maxsize) {
	this.store       = store;
	this.toDel       = new Vector();
	this.lookupTable = new Hashtable();
	this.bytelimit   = maxsize;
	this.lruList     = new SyncLRUList();
    }
}



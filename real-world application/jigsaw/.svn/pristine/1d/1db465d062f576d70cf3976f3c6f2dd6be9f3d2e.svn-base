// CacheStore.java
// $Id: CacheStore.java,v 1.1 2010/06/15 12:25:08 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;

import org.w3c.util.LRUAble;
import org.w3c.util.ObservableProperties;
import org.w3c.util.PropertyMonitoring;
import org.w3c.util.SyncLRUList;

import org.w3c.tools.sorter.Sorter;

public class CacheStore implements PropertyMonitoring {

    /**
     * Name of the property giving the cache's directory.
     * This property defaults to the current directory.
     */
    public static final
    String CACHE_DIRECTORY_P = "org.w3c.www.protocol.http.cache.directory";

    /**
     * The name of the properties indicating the storesize of the cache
     * (in bytes).
     * This property will give the value of the disk-based cache size. This
     * value only takes into account the size of the entities saved, not
     * the size of the associated headers, and not the physical size on the
     * disc.
     * <p>This property defaults to <strong>20</strong> Mbytes.
     */
    public static final 
    String STORE_SIZE_P = "org.w3c.www.protocol.http.cache.storesize";

    /**
     * Name of the property used to knkow the percentage of bytes to be
     * kept after a garbage collection
     * It defaults to 0.80 (80% of the cache size)
     */
    public static final String GARBAGE_COLLECTION_THRESHOLD_P =
    "org.w3c.www.protocol.http.cache.gc_threshold";

   /**
     * Name of the property indicating the max size for files to be cached.
     * This property gives the ratio (relative to the cache size) of
     * the number of bytes a single entry is able to occupy.
     * <p>The ratio should be given as a floating point value between 
     * <strong>0</strong> and <strong>1</strong>. If set to <strong>0.1
     * </strong> and the cache size is <strong>5000000</strong>, files larger
     * then <strong>500000</strong> will not be cached (except if garbage
     * collection is disbabled).
     * <p>This property defaults to <strong>0.1</strong>.
     * Note that the generation size will be taken from this threshold
     */
    public static final 
    String FILE_SIZE_RATIO_P = "org.w3c.www.protocol.http.cache.fileSizeRatio";

    /**
     * Name of the property enabling garbage collection of the cache.
     * This property defaults to <strong>true</strong>.
     */
    public static final String GARBAGE_COLLECTION_ENABLED_P
        = "org.w3c.www.protocol.http.cache.garbageCollectionEnabled";

    /**
     * Name of the property indicating the amount of time in second between
     * two synchronization of the database (aka dump on disk)
     * Milliseconds
     */
    public static final String SYNCHRONIZATION_DELAY_P
        = "org.w3c.www.protocol.http.cache.SynchronizationDelay";

    /**
     * Name of the property indicating the amount of time in second between
     * two attempts to compact generations. In milliseconds
     */
    public static final String GENERATION_COMPACT_DELAY_P
        = "org.w3c.www.protocol.http.cache.GenerationCompactDelay";

    /**
     * Name of the property indicating the maximal number of resources
     * the cache can load in memory (not the content of the resources)
     */
    public static final String MAX_CACHED_RESOURCES_P
        = "org.w3c.www.protocol.http.cache.MaxCachedResources";

    /**
     * Name of the property indicating the maximal number of generations
     * in this cache store
     */
    public static final String MAX_GENERATIONS_P
        = "org.w3c.www.protocol.http.cache.MaxGenerations";

    //Generation file name.
    public static final String GENERATION_FILENAME = "gen-";

    // the store state
    private StoreState state = null;

    // the state file
    private File statefile = null;

    private SyncLRUList generations = null;

    // the capacity of this cache
    private long bytelimit = 0;

    // the store capacity of this cache
    private long storelimit = 0;

    // the usual length of a generation
    private long generationlimit = 0;

    // the maximum number of CachedResource in memory
    private int cr_limit = 0;

    // the file ratio (threshold)
    private double threshold = 0.1;

    // the ratio kept after a garbage collection
    private double gc_kept_ratio = 0.80;

    // the number of directories used (default 128)
    private int nb_dir = 128;

    // The cache directory
    private File cache_dir = null;

    // the directories where bodies are stored
    private File dirs[] = null;

    // the garbage collection flag
    private boolean garbageCollectionEnabled = true;

    // our father
    private CacheFilter filter = null;

    // the traditional debug flag
    private boolean debug = false;

    // the maximal number of generations
    private int max_generation = 10;

    // the delay between two store sync
    private long sync_delay = 60000;

    // the delay between two attempts to compact the database
    private long gencomp_delay = 60000;

    /**
     * The properties we initialized ourself from.
     */
    protected ObservableProperties props = null;

    /**
     * Property monitoring for the CacheStore.
     * The CacheStore allows you to dynamically (typically through the property
     * setter) change the class of the sweeper, the validator, the size...
     * @param name The name of the property that has changed.
     * @return A boolean, <strong>true</strong> if the change was made, 
     *    <strong>false</strong> otherwise.
     */
    public boolean propertyChanged (String name) {
	double dval;

	if ( name.equals(CacheFilter.CACHE_SIZE_P) ) {
	    bytelimit = props.getLong(name, bytelimit);
	    return true;
	} else if (name.equals(STORE_SIZE_P)) {
	    storelimit = props.getLong(name, storelimit);
	    return true;
	} else if ( name.equals(CacheFilter.DEBUG_P) ) {
	    debug = props.getBoolean(name, debug);
	    return true;
	} else if ( name.equals(GARBAGE_COLLECTION_ENABLED_P) ) {
	    garbageCollectionEnabled = props.getBoolean(name, true);
	    return true;
	} else if ( name.equals(FILE_SIZE_RATIO_P) ) {
	    dval = props.getDouble(name, threshold);
	    if ((dval <= (double) 0.00001) || (dval >= (double) 1.0))
		return false;
	    threshold = dval;
	    return true;
	} else if ( name.equals(GARBAGE_COLLECTION_THRESHOLD_P) ) {
	    dval = props.getDouble(name, gc_kept_ratio);
	    if ((dval <= (double) 0.00001) || (dval >= (double) 1.0))
		return false;
	    gc_kept_ratio = dval;
	    return true;
	} else if ( name.equals(MAX_GENERATIONS_P) ) {
	    int new_nb = props.getInteger(name, max_generation);
	    if (new_nb <= 0 ) {
		return false;
	    }
	    // not that we won't do too much if it is reduced
	    // the user will have to clean the cache...
	    max_generation = new_nb;
	    return true;
	} else if ( name.equals(MAX_CACHED_RESOURCES_P) ) {
	    int new_nb = props.getInteger(name, cr_limit);
	    if (new_nb <= 0 ) {
		return false;
	    }
	    // not that we won't do too much if it is reduced
	    // the user will have to clean the cache...
	    cr_limit = new_nb;
	    return true;
	} else if ( name.equals(SYNCHRONIZATION_DELAY_P) ) {
	    long new_nb = props.getLong(name, sync_delay);
	    if (new_nb <= 0 ) {
		return false;
	    }
	    // not that we won't do too much if it is reduced
	    // the user will have to clean the cache...
	    sync_delay = new_nb;
	    return true;
	} else if ( name.equals(GENERATION_COMPACT_DELAY_P) ) {
	    long new_nb = props.getLong(name, gencomp_delay);
	    if (new_nb <= 0 ) {
		return false;
	    }
	    // not that we won't do too much if it is reduced
	    // the user will have to clean the cache...
	    gencomp_delay = new_nb;
	    return true;
	}
	// nothing changed, everything is ok!
	return true;
    }

    public StoreState getState() {
	return state;
    }

    /**
     * return the cache sweeper used by the cache
     * @return an instance of CacheSweeper
     */
    public CacheSweeper getSweeper() {
	return filter.sweeper;
    }

    /**
     * return the serializer used by the cache
     * @return an instance of Serializer
     */
    public CacheSerializer getSerializer() {
	return filter.serializer;
    }

    /**
     * return the cache validator used by the cache
     * @return an instance of CacheValidator
     */
    public CacheValidator getValidator() {
        return filter.validator;
    }

    /**
     * Get the next generation (in LRU order).
     * @param gen the current generation
     * @return a generation
     */
    public CacheGeneration getNextGeneration(CacheGeneration gen) {
	if (generations != null) {
	    return (CacheGeneration) generations.getNext(gen);
	}
	return null;
    }

    /**
     * Get the previous generation (in LRU order).
     * @param gen the current generation
     * @return a generation
     */
    public CacheGeneration getPrevGeneration(CacheGeneration gen) {
	if (generations != null) {
	    return (CacheGeneration) generations.getPrev(gen);
	}
	return null;
    }

    /**
     * Get the last generation, ie the Most recently used generation
     * @return a CacheGeneration, the most recently used one
     */
    public CacheGeneration getMRUGeneration() {
	return (CacheGeneration) generations.getHead();
    }

    /**
     * Get the fill ratio of the last generation (the most recently used)
     */
    public float getMRUGenerationRatio() {
	CacheGeneration last = (CacheGeneration)generations.getHead();
	return (last == null) ? (float)0.0 : last.getFillRatio();
    }

    /**
     * Get the LRU generation, ie the Least recently used generation
     * @return a CacheGeneration, the least recently used one
     */
    public CacheGeneration getLRUGeneration() {
	return (CacheGeneration) generations.getTail();
    }

    /**
     * Get the oldest loaded generation
     * @return a generation
     */
    public CacheGeneration getLRULoadedGeneration() {
	CacheGeneration gen = (CacheGeneration) generations.getTail();
	while ((gen != null) && (! gen.isLoaded())) {
	    gen = (CacheGeneration) generations.getPrev(gen);
	}
	return gen;
    }

    /**
     * Get the synchronization delay between to sync, in milliseconds
     * @return a long, the number of milliseconds
     */
    public long getSyncDelay() {
	return sync_delay;
    }

    /**
     * Get the delay between two attempts to compact the generations.
     * @return a long, the number of milliseconds
     */
    public long getCompactGenerationDelay() {
	return gencomp_delay;
    }

    /**
     * Create and add a new Generation. WARNING, this method is not
     * synchronized.
     * @return the newly created generation or null if the max number
     * of generations is reached or it the current size of the cache
     * (in memory) is too big to accept a new generation.
     * @exception InvalidCacheException if the cache is corrupted
     */
    protected CacheGeneration addNewGeneration() 
	throws InvalidCacheException
    {
	long byteleft = bytelimit - state.getByteCount();
	if ((state.getNbGeneration() < max_generation) && 
	    (byteleft > generationlimit)) {
	    CacheGeneration newgen = 
		new CacheGeneration(this, generationlimit);
	    setGenerationFile(newgen);
	    state.notifyGenerationCreated(newgen);
	    generations.toHead(newgen);
	    return newgen;
	} else {
	    return null;
	}
    }

    /**
     * Load the given generation and unload the LRU loaded
     * generation if necessary.
     * @param cg the generation to load
     * @exception InvalidCacheException if the cache is corrupted
     */
    protected CacheGeneration loadGeneration(CacheGeneration cg) 
	throws InvalidCacheException
    {
	// load this generation and unload the LRU loaded
	// generation
	CacheGeneration cglru = getLRULoadedGeneration();
	synchronized(this) {
	    // FIRST, unload the LRU loaded generation
	    if (getCachedByteFree() < generationlimit) {
		if (cglru != null)
		    unloadGeneration(cglru);
	    }
	    return _loadGeneration(cg);
	}
    }

    /**
     * Load a "unloaded" generation
     * @return the loaded generation
     * @exception InvalidCacheException if the cache is corrupted
     */
    private CacheGeneration _loadGeneration(CacheGeneration cg) 
	throws InvalidCacheException
    {
	try {
	    Reader reader = 
		new BufferedReader(new FileReader(cg.getGenerationFile()));
	    cg = getSerializer().readGeneration(cg, reader);
	    state.notifyGenerationLoaded(cg);
	    return cg;
	} catch (FileNotFoundException ex) {
	    String msg = "Generation file does not exists: "+
		cg.getGenerationFile().getAbsolutePath();
	    throw new InvalidCacheException(msg);
	} catch (IOException ex) {
	    String msg = "IOError reading "+
		cg.getGenerationFile().getAbsolutePath();
	    throw new InvalidCacheException(msg);
	}
    }

    /**
     * UnLoad a "loaded" generation. WARNING: not synchronized.
     * @param the loaded generation
     * @exception InvalidCacheException if the cache is corrupted
     */
    protected void unloadGeneration(CacheGeneration cg) 
	throws InvalidCacheException
    {
	try {
	    Writer writer = 
		new BufferedWriter(new FileWriter(cg.getGenerationFile()));
	    getSerializer().writeGeneration(cg, writer);
	    state.notifyGenerationUnloaded(cg);
	} catch (FileNotFoundException ex) {
	    String msg = "Generation file does not exists: "+
		cg.getGenerationFile().getAbsolutePath();
	    throw new InvalidCacheException(msg);
	} catch (IOException ex) {
	    String msg = "IOError writing on "+
		cg.getGenerationFile().getAbsolutePath();
	    throw new InvalidCacheException(msg);
	}
    }

    /**
     * Save a generation.
     * @param the generation to be saved
     * @exception InvalidCacheException if the cache is corrupted
     */
    protected void saveGeneration(CacheGeneration cg)
	throws InvalidCacheException
    {
	if (cg.isLoaded() && (! cg.isSaved())) {
	    try {
		Writer writer = 
		    new BufferedWriter(new FileWriter(cg.getGenerationFile()));
		getSerializer().writeGeneration(cg, writer);
		cg.setSaved(true);
	    } catch (FileNotFoundException ex) {
		String msg = "Generation file does not exists: "+
		    cg.getGenerationFile().getAbsolutePath();
		throw new InvalidCacheException(msg);
	    } catch (IOException ex) {
		String msg = "IOError writing on "+
		    cg.getGenerationFile().getAbsolutePath();
		throw new InvalidCacheException(msg);
	    }
	}
    }

    /**
     * Compute the generation identifier from filename. Filenames
     * looks like gen-001
     * @param file the generation file
     * @return the generation number
     */
    private int getGenerationId(File file) {
	String name = file.getName();
	int    idx  = name.indexOf('-') + 1;
	String num  = name.substring(idx);
	try {
	    return Integer.parseInt(num);
	} catch (NumberFormatException ex) {
	    return -1;
	}
    }

    /**
     * Load the generations. Only a subset of the generations will 
     * actually be loaded, some generations will only have a description
     * (URLs, size) in memory.
     * This method load some generations (until cr_limit is reached) and the
     * remaining generations (if any) will only be a set of 
     * CachedResourceDescription.
     */
    protected synchronized void loadGenerations() 
	throws InvalidCacheException
    {
	generations        = new SyncLRUList();
	File genfiles[]    = getGenerationFiles();
	if (genfiles == null) {
	    throw new InvalidCacheException("No generation files!");
	}
	int  len           = genfiles.length;
	Vector sorted      = new Vector(len);

	//sort generation files
	for (int i = 0 ; i < len ; i++) {
	    Sorter.orderedFileInsert(genfiles[i], sorted);
	}
	sorted.copyInto(genfiles);
	//load generations
	for (int i = 0 ; i < len ; i++) {
	    int id = -1;
	    try {
		File genfile = genfiles[i];
		id = getGenerationId(genfile);
		if (id != -1) { //valid generation filename
		    CacheGeneration gen = 
			new CacheGeneration(this, generationlimit);
		    Reader reader = 
			new BufferedReader(new FileReader(genfile));
		    if (state.getCrCount() < cr_limit) {
			getSerializer().readGeneration(gen, reader);
		    } else {
			getSerializer().readDescription(gen, reader);
		    }
		    gen.setId(id);
		    gen.setGenerationFile(genfile);
		    generations.toHead(gen);
		} else if (debug) {
		    System.err.println("Invalid generation filename : "+
				       genfiles[i].getName());
		}
	    } catch (FileNotFoundException ex) {
		if (debug) {
		    String msg = "File not found generation["+id+"]";
		    System.err.println(msg+" "+ex.getMessage());
		}
	    } catch (IOException ioex) {
		if (debug) {
		    String msg = "Error loading generation ["+id+"]";
		    System.err.println(msg+" "+ioex.getMessage());
		}
	    }
	}
    }

    /**
     * Load the store state
     */
    protected void loadState() {
	try {
	    Reader reader = new BufferedReader(new FileReader(statefile));
	    state = (StoreState) getSerializer().read(reader);
	} catch (IOException ex) {
	    if (debug) {
		System.err.println("Can't load StoreState : "+ex.getMessage());
	    }
	    state = new StoreState();
	}
    }

    /**
     * Save the current state
     */
    protected void saveState() {
	state.sync();
	try {
	    Writer writer = new BufferedWriter(new FileWriter(statefile));
	    getSerializer().write(state, writer);
	} catch (IOException ex) {
	    if (debug) {
		System.err.println("Can't load StoreState : "+ex.getMessage());
	    }
	}
    }

    /**
     * get the number of bytes the garbage collector needs to collect
     * to keep the cache in good state, it will only move the resource
     * to the delete list, another check has to be done to save physical space
     * @return a long, the number of bytes to save
     */
    public long getRequiredByteNumber() {
	return (long)(state.getByteCount() - (bytelimit * gc_kept_ratio));
    }

    /**
     * get the cached size of this cache
     * @return a long, the number of bytes cached
     */
    public synchronized long getCachedByteCount() {
	return state.getByteCount();
    }

    /**
     * get the number of bytes used phisycally by this cache
     * @return a long, the number of bytes cached
     */    
    public synchronized long getStoredByteCount() {
	return state.getStoreCount();
    }

    /**
     * get the number of bytes available for the cache memory
     * @return a long, the number of bytes free
     */    
    public synchronized long getCachedByteFree() {
	return (bytelimit - state.getByteCount());
    }

    /**
     * synchronize the internal database with the storage
     */
    public synchronized void sync() {
	CacheGeneration cg = (CacheGeneration) generations.getHead();
	if (cg == null) {
	    // nothing to save
	    return;
	}
	do {
	    try {
		saveGeneration(cg);
	    } catch (InvalidCacheException ex) {
		if (debug) {
		    System.err.println("Unable to save generation ["+
				       cg.getId()+"] "+ex.getMessage());
		}
	    }
	    cg = getNextGeneration(cg);
	} while (cg != null);
	saveState();
    }

    /**
     * Remove the given CachedResource from the given CacheGeneration.
     * WARNING: not synchronized
     * @exception NoSuchResourceException if this resource was not in this 
     * generation
     */
    protected void removeResource(CacheGeneration cg, CachedResource cr)
	throws NoSuchResourceException
    {
	cg.removeResource(cr);
	state.notifyResourceRemoved(cr);
    }

    /**
     * Get a cached resource relative to the given URL. WARNING: the
     * CachedResource returned is no more in the CacheStore.
     * @param url the URL of the CachedResource
     * @return a CachedResource or null
     * @see #storeCachedResource
     * @exception InvalidCacheException if the cache is corrupted
     */
    public CachedResource getCachedResource(String url) 
	throws InvalidCacheException
    {
	CacheGeneration cg = (CacheGeneration)generations.getHead();
	if (cg == null)
	    return null;
	do {
	    CachedResource cr = null;
	    if (cg.isLoaded()) {
		// The generation is already loaded
		cr = cg.lookupResource(url);
	    } else if (cg.containsResource(url)) {
		// load this generation and unload the LRU loaded
		// generation if necessary
		loadGeneration(cg);
		cr = cg.lookupResource(url);
	    }
	    // found something?
	    if (cr != null) {
		try {
		    synchronized(this) {
			removeResource(cg, cr);
		    }
		} catch (NoSuchResourceException ex) {
		    //should not happen
		}
		return cr;
	    }
	    // try with next generation
	    cg = getNextGeneration(cg);
	} while (cg != null);
	return null;
    }

    /**
     * extract a cached resource from the store
     * @param the cached resource to be extracted
     * @return the extracted cached resource
     * @exception InvalidCacheException if the cache is corrupted
     */
    public CachedResource getCachedResource(CachedResource cr) 
	throws InvalidCacheException
    {
	CacheGeneration cg = cr.generation;
	if (cg != null) {
	    try {
		synchronized(this) {
		    removeResource(cg, cr);
		}
	    } catch (NoSuchResourceException ex) {
		//should not happen
	    }
	    return cr;
	}
	// FIXME do the lookup, but we shouldn't end up here anyway
	return cr;
    }

    /**
     * Resize the generation in order to be able to store the given 
     * Resource.
     * @param cg the generation to resize
     * @param cr the CachedResource to store.
     */
    protected synchronized void resizeGeneration(CacheGeneration cg, 
						 CachedResource cr) 
    {
	if (debug) {
	    System.out.println("Resizing generation "+cg.getId());
	}
	long real_size = Math.max(cr.getContentLength(), 
				  cr.getCurrentLength());
	if (real_size > generationlimit) {
	    cg.setByteLimit(real_size);
	    generationlimit = 
		(bytelimit - real_size) / (max_generation - 1);
	} else if (debug) {
	    System.out.println("Asked for a not necessary resize!");
	}
    }

    /**
     * Get a cached resource relative to the given URL. WARNING: the
     * CachedResource returned is still in the cache store!
     * @param url the URL of the CachedResource
     * @return a CachedResource or null
     * @see #storeCachedResource
     * @exception InvalidCacheException if the cache is corrupted
     */
    public CachedResource getCachedResourceReference(String url) 
	throws InvalidCacheException
    {
	CacheGeneration cg = (CacheGeneration)generations.getHead();
	if (cg == null)
	    return null;
	do {
	    CachedResource cr = null;
	    if (cg.isLoaded()) {
		// The generation is already loaded
		cr = cg.lookupResource(url);
	    } else if (cg.containsResource(url)) {
		// load this generation and unload the LRU loaded
		// generation if necessary
		loadGeneration(cg);
		cr = cg.lookupResource(url);
	    }
	    // found something?
	    if (cr != null) {
		return cr;
	    }
	    // try with next generation
	    cg = getNextGeneration(cg);
	} while (cg != null);
	return null;
    }

    /**
     * update this cached resource from generation x ot the latest
     * @param the cached resource to be updated
     * @return the updated
     * @exception InvalidCacheException if the cache is corrupted
     */
    public CachedResource updateResourceGeneration(CachedResource cr)
	throws InvalidCacheException
    {
	CacheGeneration cg = (CacheGeneration)generations.getHead();
	if (cg != cr.generation) {
	    try {
		synchronized(this) {
		    removeResource(cr.generation, cr);
		}
	    } catch (NoSuchResourceException ex) {
		//should not happen
	    }
	    storeCachedResource(cr, cr.getCurrentLength());
	}
	return cr;
    }
	
    /**
     * Store a newly created (or updated) CachedResource.
     * @param cr the CachedResource to store
     * @exception InvalidCacheException if the cache is corrupted
     * @return <code>true</code> if the resource has been cached
     */
    public boolean storeCachedResource(CachedResource cr) 
	throws InvalidCacheException
    {
	return storeCachedResource(cr, 0);
    }

    /**
     * Store a newly created (or updated) CachedResource.
     * @param cr the CachedResource to store
     * @return <code>true</code> if the resource has been cached
     * @exception InvalidCacheException if the cache is corrupted
     */
    public boolean storeCachedResource(CachedResource cr, long oldsize) 
	throws InvalidCacheException
    {
	CacheGeneration cg = (CacheGeneration)generations.getHead();
	long size = cr.getCurrentLength();
	long maxsize = (long)(((double) bytelimit) * threshold);
	// size > threshold, can't cache it...
	if (size > maxsize) {
	    return false;
	}
	if (cg != null) {
	    // firt try to add in last generation
	    if (cg.addResource(cr, size, oldsize)) {
		// success
		return true;
	    }
	    // create a new generation
	    CacheGeneration new_cg = addNewGeneration();
	    if (new_cg != null) {
		// generation created
		if (! new_cg.addResource(cr, size, oldsize)) {
		    // resize generation
		    resizeGeneration(new_cg, cr);
		    // try again
		    if (! new_cg.addResource(cr, size, oldsize)) {
			String msg = "Unable to add a cachedResource in a "+
			    "resized generation!!";
			throw new InvalidCacheException(msg);
		    }
		}
	    } else {
		// failed! unload a generation on disk and create a new
		// generation if the storelimit and the max number of 
		// generations have not been reached.
		if ((state.getStoreCount() < (storelimit - generationlimit)) 
		    && (state.getNbGeneration() < max_generation)) {
		    // unload the oldest loaded generation until
		    // we can create a new generation
		    while (new_cg == null) {
			CacheGeneration older_cg = getLRULoadedGeneration();
			if (older_cg == null) {
			    String msg = 
				"No Generation Loaded but store limit reached";
			    throw new InvalidCacheException(msg);
			}
			synchronized(this) {
			    unloadGeneration(older_cg);
			    // Create a new generation
			    new_cg = addNewGeneration();
			}
		    }
		} else {
		    // we can't create a new generation so empty the oldest
		    // generation
		    CacheGeneration older_cg = null;
		    synchronized(this) {
			older_cg = getLRUGeneration();
			if (older_cg == null) {
			    String msg = "No Generation Loaded"+
				" but store limit reached";
			    throw new InvalidCacheException(msg);
			}
			older_cg.emptyGeneration();
			long cg_limit = older_cg.getByteLimit();
			generationlimit = 
			    ((generationlimit * (max_generation - 1)) 
			     + cg_limit) / max_generation;
			generations.toHead(older_cg);
			setGenerationFile(older_cg);
		    }
		    getSweeper().collectStored(older_cg);
		    new_cg = older_cg;
		}
		// finally add the resource in the new generation
		if (! new_cg.addResource(cr, size, oldsize)) {
		    // resize generation
		    resizeGeneration(new_cg, cr);
		    // try again
		    if (! new_cg.addResource(cr, size, oldsize)) {
			String msg = "Unable to add a cachedResource in a "+
			    "resized generation!!";
			throw new InvalidCacheException(msg);
		    }
		    return true;
		}
	    }
	} else {
	    CacheGeneration new_cg = null;
	    synchronized (this) {
		new_cg = addNewGeneration();
	    }
	    if (new_cg == null) {
		String msg = "Unable create the first generation!!";
		throw new InvalidCacheException(msg);
	    }
	    if (! new_cg.addResource(cr, size, oldsize)) {
		// resize generation
		resizeGeneration(new_cg, cr);
		// try again
		if (! new_cg.addResource(cr, size, oldsize)) {
		    String msg = "Unable to add a cachedResource in a "+
			"resized generation!!";
		    throw new InvalidCacheException(msg);
		}
		return true;
	    }
	}
	return false;
    }

    /**
     * Get a beautiful file number, eg :
     * <ul>
     * <li>2 => 002 if size == 3
     * <li>50 => 0050 if size == 4
     * <li>5000 => 5000 if size < 4
     * @param nb the file number
     * @param size the 'size' of the number
     * @return a String
     */
    private String getFileNumber(int nb, int size) {
	//compute the number of '0' to add
	int cpt = 0;
	int nb2 = nb;
	if (nb2 == 0) {
	    cpt = 1;
	} else {
	    while (nb2 > 0) {
		nb2 = nb2/10;
		cpt++;
	    }
	}
	//number of '0' to add = size - cpt;
	int zero2add = size - cpt;
	StringBuffer buffer = new StringBuffer();
	for (int i = 0 ; i < zero2add ; i++) {
	    buffer.append("0");
	}
	buffer.append(Integer.toString(nb));
	return buffer.toString();
	
    }

    private File[] getGenerationFiles() 
	throws InvalidCacheException
    {
	FilenameFilter filter = new FilenameFilter() {
	    /**
	     * Accept file like gen-xxxx (xxxx is a number)
	     */
	    public boolean accept(File dir, String name) {
		return (name.startsWith(GENERATION_FILENAME));
	    }
	};
	if (cache_dir == null) {
	    throw new InvalidCacheException("No Cache Directory!!");
	} else if (! cache_dir.exists()) {
	    cache_dir.mkdirs();
	}
	return cache_dir.listFiles(filter);
    }

    /**
     * allocate a new name for the next generation file.
     * @return a File, used to dump the generation
     * @exception InvalidCacheException if the cache is corrupted
     */
    private synchronized void setGenerationFile(CacheGeneration gen) 
	throws InvalidCacheException
    {
	int current_generation = state.incrCurrentGeneration();
	File file = new File(cache_dir, GENERATION_FILENAME+
			getFileNumber(current_generation, 4));
	if (debug) {
	    System.err.println(file);
	}
	gen.setGenerationFile(file);
	gen.setId(current_generation);
    }

    /**
     * allocate a new name for the next cached resource. This method
     * create some directories if needed.
     * @return a File, used to dump the entry
     */
    protected File getNewEntryFile() {
	int curnum;
	synchronized (this) {
	    curnum = state.incrEntryNum();
	}
	int filenum = curnum / nb_dir;
	int dirnum  = curnum % nb_dir;
	File dir = dirs[dirnum];
	return new File(dir, getFileNumber(filenum, 4));
    }

    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append(">>> CacheStore [")
	    .append(cache_dir.getAbsolutePath()).append("] <<<");
	buffer.append("\n  Store limit          : ").append(storelimit);
	buffer.append("\n  Byte limit           : ").append(bytelimit);
	buffer.append("\n  CR limit             : ").append(cr_limit);
	buffer.append(state);
	return buffer.toString();
    }

    /**
     * Check the subdirectories. Create them if necessary.
     */
    protected void checkDirs() {
	dirs = new File[nb_dir];
	for (int i = 0 ; i < nb_dir ; i++) {
	    dirs[i] = new File(cache_dir, getFileNumber(i, 3));
	    if (! dirs[i].exists()) {
		dirs[i].mkdirs();
	    }
	}
    }

    /**
     * Clean the Cache directory, remove unused files.
     * @return the number of files deleted.
     */
    protected int cleanCacheDir() {
	// first, store all the known files
	Hashtable       entryfiles = new Hashtable();
	CacheGeneration cg         = (CacheGeneration) generations.getHead();
	while (cg != null) {
	    Enumeration fenum = cg.getFiles();
	    while (fenum.hasMoreElements()) {
		entryfiles.put(fenum.nextElement(), Boolean.TRUE);
	    }
	    cg = getNextGeneration(cg);
	}
	// check all files found in dirs
	int cpt = 0;
	for (int i = 0 ; i < dirs.length ; i++) {
	    File files[] = dirs[i].listFiles();
	    if (files != null) {
		for (int j = 0 ; j < files.length ; j++) {
		    if (entryfiles.get(files[j]) == null) {
			if (files[j].delete()) {
			    cpt++;
			    if (debug) {
				System.out.println(files[j]+
						   " not used, removed");
			    }
			}
		    }
		}
	    }
	}
	return cpt;
    }

    /**
     * update the mode of the sweeper according to the state of the 
     * cache store, 
     */
    protected void updateSweeperPriority() {
	long byteCount         = state.getByteCount();
	long storeCount        = state.getStoreCount();
	long crCount           = state.getCrCount();
	CacheSweeper sweeper   = getSweeper();
	// over the limit of cached resource usage, stop and clean
	if (byteCount > bytelimit) {
	    sweeper.setState(CacheSweeper.STATE_FORCE_CLEAN_GENERATIONS);
	} else if (byteCount > (long)((float) bytelimit * gc_kept_ratio)) {
	    // near the limit, start loosely to remove resources
	    sweeper.setState(CacheSweeper.STATE_CLEAN_GENERATIONS);
	} else if (storeCount > storelimit) {
	    // to many on the disk, remove then asap
	    sweeper.setState(CacheSweeper.STATE_FORCE_CLEAN_STORED);
	} else {
	    // normal operation, remove in a smooth way the deleted resources
	    sweeper.setState(CacheSweeper.STATE_CLEAN_STORED);
	}
    }

    /**
     * Compact our generations
     * The algorithm is the following,
     * If the number of generation is the maximum number allowed, 
     * then a check is done from the the generation after the MRU one
     * and if the sum of two generation can fit into one, it is done, and
     * the generation is removed from the list
     */
    protected void compactGenerations() {
	if (debug)
	    System.out.println("*** trying to compact generations");
	// limit not reached? exit ASAP
	if (state.getNbGeneration() < max_generation)
	    return;
	if (debug)
	    System.out.println("*** compact: Max reached, compacting...");
	CacheGeneration gen = getMRUGeneration();
	CacheGeneration nextGen;
	gen = getNextGeneration(gen);
	while (gen != null) {
	    if (debug) {
		System.out.println("*** compact: working on generation " + 
				   gen.getId());
	    }
	    nextGen = getNextGeneration(gen);
	    // last one, exit
	    if (nextGen == null) {
		break;
	    }
	    if ((gen.getCachedByteCount() + nextGen.getCachedByteCount()) <
		gen.getByteLimit()) {
		// do the dirty work now...
		synchronized (generations) {
		    if (debug) {
			System.out.println("*** compact: merging ("+
					   gen.getId() + ") and ("+
					   nextGen.getId()+")");
		    }
		    generations.remove(nextGen);
		    gen.copyInto(nextGen);
		    nextGen.deleteGenerationFile();
		    state.decrGenerationNum();
		}
	    }
	    gen = getNextGeneration(gen);
	}
    }
	    

    /**
     * used for debugging, display some internal information about the state 
     * of the cache
     */
    protected synchronized void checkState() {
	long byteCount         = state.getByteCount();
	long storeCount        = state.getStoreCount();
	long crCount           = state.getCrCount();
	int  entryNum          = state.getEntryNum();
	int  nbGeneration      = state.getNbGeneration();
	int  currentGeneration = state.getCurrentGeneration();
	double ratio = (((double) byteCount/ bytelimit) * 100);
	String rt = String.valueOf(ratio);
	if (rt.length() > 5)
	    rt = rt.substring(0,5);
	System.out.println("  Ratio (BC/BL)*100    : "+rt+" %");    
	System.out.println(">>> Generations <<<");
	CacheGeneration cg = getMRUGeneration();
	System.out.println("  Id  | Loaded  | CR cnt   | BT lim   | "+
			   "BT cnt   | ST cnt   | ratio");
	System.out.println(" ---------------------------------------------"+
			   "---------------------------");
	long bc = 0;
	long sc = 0;
	while (cg != null) {
	    long gbc = cg.getCachedByteCount();
	    long gsc = cg.getStoredByteCount();
	    long gbl = cg.getByteLimit();
	    bc += gbc;
	    sc += gsc;
	    double percent = (((double)gbc / gbl) * 100);
	    String pc = String.valueOf(percent);
	    if (pc.length() > 5)
		pc = pc.substring(0,5);
	    System.out.print("  "+getFileNumber(cg.getId(),2));
	    System.out.print("  |  "+cg.isLoaded()+" ");
	    System.out.print("  | "+getFileNumber((int)cg.getCRCount(), 7));
	    System.out.print("  | "+getFileNumber((int)gbl, 7));
	    System.out.print("  | "+getFileNumber((int)gbc,7));
	    System.out.print("  | "+getFileNumber((int)gsc,7));
	    System.out.println("  | "+pc+" %");
	    cg = getNextGeneration(cg);
	}
	System.out.println(">>> Check State <<<");
	System.out.println("  Byte Count  <= Byte Limit    : "+
			   ( byteCount <= bytelimit ));
	System.out.println("  Store Count <= Store Limit   : "+
			   ( storeCount <= storelimit ));
	System.out.println("  CR Count    <= CR Limit      : "+
			   ( crCount <= cr_limit ));
	System.out.println("  Byte Count  <= Store Count   : "+
			   ( byteCount <= storeCount ));
	System.out.println("  CR Count    <= Entry Num     : "+
			   ( crCount <= entryNum ));
	System.out.println("  Current gen >= Number of gen : "+
			   ( currentGeneration >= nbGeneration ));
	System.out.println("  Generations SC == Store SC   : "+
			   (sc == storeCount));
	System.out.println("  Generations BC == Store BC   : "+ 
			   (bc == byteCount));
    }

    /**
     * initialize this CacheStore, and get some infos from the parent,
     * aka the cache filter
     * @param filter a CacheFilter, our parent
     * @exception InvalidCacheException if the cache not initialized
     */
    public void initialize(CacheFilter filter) 
	throws InvalidCacheException
    {
	this.filter = filter;
	props = filter.props;
	cache_dir = props.getFile(CACHE_DIRECTORY_P, null);
	if (cache_dir == null) {
	    cache_dir = new File(System.getProperty("user.dir"));
	    cache_dir = new File(cache_dir, ".web-cache");
	}
	// the capacity of the cache, defaulting to 20Mo
	bytelimit = props.getLong(CacheFilter.CACHE_SIZE_P, 20971520);
	// the store capacity of the cache, defaulting to 20Mo + 10%
	storelimit = props.getLong(STORE_SIZE_P, 23068672);
	// the delay between two store sync
	sync_delay = props.getLong(SYNCHRONIZATION_DELAY_P, 60000);
	// the delay between two attempts to compact the database
	gencomp_delay = props.getLong(GENERATION_COMPACT_DELAY_P, 60000);
	// the garbage collection flag
	garbageCollectionEnabled =
	    props.getBoolean(GARBAGE_COLLECTION_ENABLED_P, true);
	// the threshold (as in an LRU-threshold cache policy
	threshold = props.getDouble(FILE_SIZE_RATIO_P, 0.1);
	// the gc limit
	gc_kept_ratio = props.getDouble(GARBAGE_COLLECTION_THRESHOLD_P, 0.80);
	// the maximal number of generations
	max_generation = props.getInteger(MAX_GENERATIONS_P, 10);
	// the generation limit size
	generationlimit = bytelimit / max_generation;
	// debug flag
	debug = props.getBoolean(CacheFilter.DEBUG_P, false);
	//load
	cr_limit = props.getInteger(MAX_CACHED_RESOURCES_P, 50000); 
	// load the store state
	this.statefile = new File(cache_dir, "state");
	loadState();
	// check the subdirectories
	checkDirs();
	// load the generations...
	System.out.println("Loading generations...");
	loadGenerations();
	// clean
	System.out.println("Cleaning cache directories...");
	int cleaned = cleanCacheDir();
	if (cleaned > 0) {
	    System.out.println(cleaned+" unused files deleted.");
	}
    }
}

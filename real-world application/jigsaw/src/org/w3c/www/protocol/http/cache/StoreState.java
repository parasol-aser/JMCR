// StoreState.java
// $Id: StoreState.java,v 1.2 2010/06/15 17:53:00 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache;

import java.util.Hashtable;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.LongAttribute;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class StoreState extends AttributeHolder {

    /**
     * Attribute index - The current generation number (id)
     */
    protected static int ATTR_CURRENT_GENERATION = -1;   

    /**
     * Attribute index - The  nb of generations (mem+disk)
     */
    protected static int ATTR_NB_GENERATION = -1;   

    /**
     * Attribute index - The the current cache size
     */
    protected static int ATTR_BYTE_COUNT = -1;   

    /**
     * Attribute index - The the current store count
     */
    protected static int ATTR_STORE_COUNT = -1;   

    /**
     * Attribute index - The current number of CachedResource in memory
     */
    protected static int ATTR_CR_COUNT = -1;   

    /**
     * Attribute index - The number used to store the file
     */
    protected static int ATTR_ENTRY_NUM = -1;   

    static {
	Attribute a = null;
	Class     c = null;
	try {
	    c = Class.forName("org.w3c.www.protocol.http.cache.StoreState");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}

	// Declare the current generation number (id)
	a = new IntegerAttribute("current-generation"
				 , null
				 , Attribute.COMPUTED);
	ATTR_CURRENT_GENERATION = AttributeRegistry.registerAttribute(c, a);

	// Declare the  nb of generations (mem+disk)
	a = new IntegerAttribute("nb-generation"
				 , null
				 , Attribute.COMPUTED);
	ATTR_NB_GENERATION = AttributeRegistry.registerAttribute(c, a);

	// Declare the 
	a = new IntegerAttribute("entry-num"
				 , null
				 , Attribute.COMPUTED);
	ATTR_ENTRY_NUM = AttributeRegistry.registerAttribute(c, a);

	// Declare the 
	a = new LongAttribute("byte-count"
			      , null
			      , Attribute.COMPUTED);
	ATTR_BYTE_COUNT = AttributeRegistry.registerAttribute(c, a);

	// Declare the 
	a = new LongAttribute("store-count"
			      , null
			      , Attribute.COMPUTED);
	ATTR_STORE_COUNT = AttributeRegistry.registerAttribute(c, a);

	// Declare the 
	a = new IntegerAttribute("cr-count"
			      , null
			      , Attribute.COMPUTED);
	ATTR_CR_COUNT = AttributeRegistry.registerAttribute(c, a);
	
    }

    private int currentGeneration;
    private int nbGeneration;
    private int entryNum;

    private long byteCount;
    private long storeCount;
    private int crCount;

    /**
     * Synchronize the state
     */
    public void sync() {
	setValue(ATTR_CURRENT_GENERATION, new Integer(currentGeneration));
	setValue(ATTR_NB_GENERATION, new Integer(nbGeneration));
	setValue(ATTR_ENTRY_NUM, new Integer(entryNum));

	setValue(ATTR_BYTE_COUNT, new Long(byteCount));
	setValue(ATTR_STORE_COUNT, new Long(storeCount));
	setValue(ATTR_CR_COUNT, new Integer(crCount));
    }

    /**
     * Modify the current generation value
     * @return the new value
     */
    public synchronized int incrCurrentGeneration() {
	return ++currentGeneration;
    }

    /**
     * Get the current Generation number
     * @return an integer
     */
    public synchronized int getCurrentGeneration() {
	return currentGeneration;
    }

    /**
     * Get the number of generation
     * @return an integer
     */
    public synchronized int getNbGeneration() {
	return nbGeneration;
    }

    /**
     * Get the current cached byte count.
     * @return a long
     */
    public synchronized long getByteCount() {
	return byteCount;
    }

    /**
     * Get the current stored byte count
     * @return a long
     */
    public synchronized long getStoreCount() {
	return storeCount;
    }

    /**
     * Get the current CachedResource count
     * @return a long
     */
    public synchronized int getCrCount() {
	return crCount;
    }

    /**
     * Modify the current entry number
     * @return the new value
     */
    public synchronized int incrEntryNum() {
	return ++entryNum;
    }

    /**
     * Get the current entry number
     * @return an int
     */
    public synchronized int getEntryNum() {
	return entryNum;
    }

    /**
     * increment the current generation number
     * @return the new value
     */
    public synchronized int incrGenerationNum() {
	return ++nbGeneration;
    }

    /**
     * decrement the current generation number
     * @return an int
     */
    public synchronized int decrGenerationNum() {
	return --nbGeneration;
    }

    /**
     * Notify the store state, a generation has just been created
     * @param cg the new generation
     */
    public synchronized void notifyGenerationCreated(CacheGeneration cg) {
	storeCount += cg.getStoredByteCount();
	byteCount  += cg.getCachedByteCount();
	crCount    += cg.getCRCount();
	cg.setLoaded(true);
	nbGeneration++;
    }

    /**
     * Notify the store state, a generation has just been loaded
     * @param cg the loaded generation
     */
    public synchronized void notifyGenerationLoaded(CacheGeneration cg) {
	byteCount += cg.getCachedByteCount();
	crCount   += cg.getCRCount();
    }

    /**
     * Notify the store state, a generation has just been unloaded
     * @param cg the unloaded generation
     */
    public synchronized void notifyGenerationUnloaded(CacheGeneration cg) {
	byteCount -= cg.getCachedByteCount();
	crCount   -= cg.getCRCount();
	cg.unload();
    }

    /**
     * Notify the store state, a resource has just been added
     * @param cr the added resource
     */
    public synchronized void notifyResourceAdded(CachedResource cr, 
						 long oldsize) 
    {
	long size = cr.getCurrentLength();
	storeCount += (size - oldsize);
	byteCount  += size;
	crCount++;
    }

    /**
     * Notify the store state, a resource has just been updated
     * @param cr the updated resource
     */
    public synchronized void notifyResourceReplaced(CachedResource cr, 
						    long oldsize) 
    {
	long size  = cr.getCurrentLength();
	long delta = size - oldsize;
	storeCount += size;
	byteCount  += delta;
    }

    /**
     * Notify the store state, a resource has been removed (from cache)
     * @param cr the resource removed
     */
    public synchronized void notifyResourceRemoved(CachedResource cr) {
	byteCount -= cr.getCurrentLength();
	crCount--;
    }

    /**
     * Notify the store state, a resource has been moved in the 
     * "to be deleted" list
     * @param cr the resource to delete
     */
    public synchronized void notifyResourceToBeDeleted(CachedResource cr) {
	byteCount -= cr.getCurrentLength();
	crCount--;
    }

    /**
     * Notify the store state, a resource has just been deleted
     * @param cr the deleted resource
     */
    public synchronized void notifyResourceDeleted(CachedResource cr) {
	storeCount -= cr.getCurrentLength();
    }

    /**
     * The basic initialization
     * @param values the values
     */
    public void pickleValues(Hashtable defs) {
        super.pickleValues(defs);
	currentGeneration = getInt(ATTR_CURRENT_GENERATION, 0);
	nbGeneration      = getInt(ATTR_NB_GENERATION, 0);
	entryNum          = getInt(ATTR_ENTRY_NUM, 0);
	byteCount         = getLong(ATTR_BYTE_COUNT, 0);
	storeCount        = getLong(ATTR_STORE_COUNT, 0);
	crCount           = getInt(ATTR_CR_COUNT, 0);
    }

    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("\n>>> Store State <<<");
	buffer.append("\n  Store count          : ").append(storeCount);
	buffer.append("\n  Byte count           : ").append(byteCount);
	buffer.append("\n  CR count             : ").append(crCount);
	buffer.append("\n  Current Generation   : ").append(currentGeneration);
	buffer.append("\n  Number of generation : ").append(nbGeneration);
	buffer.append("\n  Entry number         : ").append(entryNum);
	return buffer.toString();
    }

    /**
     * Constructor.
     * @param file the store state file 
     */
    public StoreState() {
    }
}

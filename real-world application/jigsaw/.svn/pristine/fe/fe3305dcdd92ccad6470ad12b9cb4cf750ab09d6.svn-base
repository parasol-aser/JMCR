// SimpleCacheSweeper.java
// $Id: SimpleCacheSweeper.java,v 1.1 2010/06/15 12:25:11 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache;

import java.util.Enumeration;

import java.io.PrintStream;

public class SimpleCacheSweeper extends CacheSweeper {
    // usual debug flag
    private static final boolean debug = false;
    // the default waiting times
    private static final int WAIT_MIN = 5000;
    private static final int WAIT_MAX = 60000;
    // the current state
    protected int state = STATE_CLEAN_STORED;
    // our father
    private CacheFilter filter = null;
    // used to signal an action required
    private boolean signal = false;
    // the wating time between two sweeper calls
    private long wait_time = 60000;
    /** 
     * Used to trigger a signal
     */
    public synchronized void signal() {
	signal = true;
	notifyAll();
    }

    public synchronized void waitSignal() {
	// Use to trigger cache sync to disk:
	long sync_time = 0;
	long gencomp_time = 0;
	signal    = false;
	wait_time = WAIT_MIN;
	while ( ! signal ) {
	    // Wait for something to happen:
	    try {
		wait(wait_time);
	    } catch (InterruptedException ex) {
		continue;
	    }
	    // What was signaled ?
	    if ( signal )
		// Trigger a garbage collection
		break;
	    // Update generation if needed:
	    if ( debug ) {
		System.out.println("# Sweeper waited for "+wait_time);
	    }
	    // regulary check for removed files
	    try {
		collectStored();
	    } catch (Exception ex) {
	    }
	    // Recompute our wait_time value in any case
	    CacheStore store = filter.getStore();
	    wait_time = (long)(WAIT_MAX*(1 - store.getMRUGenerationRatio())) /
		state;
	    wait_time = Math.max(WAIT_MIN, Math.min(wait_time, WAIT_MAX));
	    if ( debug ) {
		System.out.println("# Sweeper will wait for "+wait_time);
		System.out.println("# Sweeper sync time "+sync_time);
		switch (state) {
		case STATE_CLEAN_STORED:
		    System.out.println("State: STATE_CLEAN_STORED");
		    break;
		case STATE_FORCE_CLEAN_STORED:
		    System.out.println("State: STATE_FORCE_CLEAN_STORED");
		    break;
		case STATE_CLEAN_GENERATIONS:
		    System.out.println("State: STATE_CLEAN_GENERATIONS");
		    break;
		case STATE_FORCE_CLEAN_GENERATIONS:
		    System.out.println("State: STATE_FORCE_CLEAN_GENERATIONS");
		    break;
		}
		System.out.println(store.toString());
		store.checkState();
	    }
	    sync_time += wait_time;
	    gencomp_time += wait_time;
	    // compact everything if possible FIXME time for that
	    if (gencomp_time >= store.getCompactGenerationDelay()) {
		gencomp_time = 0;
		store.compactGenerations();
	    } 
	    // Sync if possible:
	    if (sync_time >= store.getSyncDelay()) {
		sync_time = 0;
		store.sync();
	    } 
	}
	// Do the work:
	signal = false;
    }

    public void run() {
	while ( true ) {
	    try {
		waitSignal();
		garbageCollect();
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }

    /**
     * Run the garbage collector.
     */
     public void garbageCollect() {
	 long to_save;
	 switch (state) {
	 case CacheSweeper.STATE_CLEAN_STORED:
	     try {
		 collectStored();
	     } catch (Exception ex) {
		 // probably thrown due to the lack of synchronization
	     };
	     break;
	 case CacheSweeper.STATE_FORCE_CLEAN_STORED:
	     // force the cleaning by syncrhonizing the call
	     synchronized (filter.getStore()) {
		 collectStored();
	     }
	     break;
	 case CacheSweeper.STATE_CLEAN_GENERATIONS:
	     to_save = filter.getStore().getRequiredByteNumber();
	     try {
		 // get the info from the store about bytes
		 to_save -= collectCached(to_save, true);
		 to_save -= collectCached(to_save, false);
		 // and erase them
		 collectStored();
	     } catch (Exception ex) {
		 // sync pb, don't worry about this one
	     }
	     break;
	 case CacheSweeper.STATE_FORCE_CLEAN_GENERATIONS:
	     synchronized (filter.getStore()) {
		 to_save = filter.getStore().getRequiredByteNumber();
		 // get the info from the store about bytes
		 to_save -= collectCached(to_save, true);
		 to_save -= collectCached(to_save, false);
		 // and erase them
		 collectStored();
	     }
	     break;
	 }
	 // try to upgrade the priority level according to the store
	 // usage
	 filter.getStore().updateSweeperPriority();
     }

    /**
     * change the state of the Sweeper
     * @param an integer, setting the new cache state
     */
    protected synchronized void setState(int state) {
	this.state = state;
    }
	
    /**
     * collect the still stored resources
     * @param generation, the CacheGeneration to clean
     */
    protected void collectStored(CacheGeneration generation) {
 	Enumeration e = generation.getDeletedResources();
 	CachedResource cr;
 	while (e.hasMoreElements()) {
 	    cr = (CachedResource) e.nextElement();
 	    generation.deleteStored(cr);
 	}
    }

    /**
     * collect the still stored resources
     * in the whole cache
     * It will NOT block the cache during the process
     */
    protected void collectStored() {
	if (debug)
	    System.out.println("*** Sweeper: collect stored");
	CacheGeneration gen;
	gen = (CacheGeneration) filter.getStore().getLRUGeneration();
	while (gen != null) {
	    collectStored(gen);
	    gen = filter.getStore().getPrevGeneration(gen);
	}
    }

    /**
     * collect the existing resources
     * @param generation, the CacheGeneration to clean
     * @param bytes, a long. The number of bytes to collect
     * @param check, a boolean. If true, then only the stale resources
     * will be removed
     */
    protected long collectCached(CacheGeneration generation,
				 long bytes, boolean check) {
	if (bytes > 0) {
	    return generation.collectSpace(bytes, check);
	}
	return 0;
    }

    /**
     * collect the existing resources
     * @param bytes, a long. The number of bytes to collect
     * @param check, a boolean. If true, then only the stale resources
     * will be removed
     * @return a long, the number of collected bytes
     */
    protected long collectCached(long bytes, boolean check) {
	CacheGeneration gen, next;
	long collected = 0;
	CacheStore store = filter.getStore();
	gen = (CacheGeneration) store.getLRUGeneration();
	while (gen != null) {
	    next = filter.getStore().getPrevGeneration(gen);
	    collected += gen.collectSpace(bytes - collected, check);
	    if (collected >= bytes) {
		break;
	    }
	    gen = next;
	}
	return collected;
    }

    /**
     * initialize the sweeper
     */
    public void initialize(CacheFilter filter) {
	this.filter = filter;
	this.setDaemon(true);
	this.setPriority(3);
	this.setName("CacheSweeper");
    }
}



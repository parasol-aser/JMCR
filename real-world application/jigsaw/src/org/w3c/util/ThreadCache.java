// ThreadCache.java
// $Id: ThreadCache.java,v 1.1 2010/06/15 12:25:38 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996-1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.util;

class CachedThread extends Thread {
    Runnable     runner     = null;
    boolean      alive      = true;
    ThreadCache  cache      = null;
    CachedThread next       = null;
    CachedThread prev       = null;
    boolean      terminated = false;
    boolean      started    = false;
    boolean      firstime   = true;

    synchronized boolean isTerminated() {
	boolean ret = terminated;
	terminated = true;
	return ret;
    }

    synchronized Runnable waitForRunner() {
	boolean to = false;

	while ( alive ) {
	    // Is a runner available ?
	    if ( runner != null ) {
		Runnable torun = runner;
		firstime = false;
		runner   = null;
		return torun;
	    } else if ( firstime ) {
		// This thread will not be declared free until it runs once:
		try {
		    wait();
		} catch (InterruptedException ex) {
		}
	    } else if ( alive = cache.isFree(this, to) ) {
		// Notify the cache that we are free, and continue if allowed:
		try {
		    int idleto = cache.getIdleTimeout();
		    to = false;
		    if ( idleto > 0 ) {
			wait(idleto);
			to = (runner == null);
		    } else {
			wait();
		    }
		} catch (InterruptedException ex) {
		}
	    }
	}
	return null;
    }

    synchronized void kill() {
	alive = false;
	notify();
    }

    synchronized boolean wakeup(Runnable runnable) {
	if ( alive ) {
	    runner = runnable;
	    if ( ! started ) {
	    	synchronized (this) {
	    		this.start();
	    		this.started = true;
	    	}
	    }
		//this.startNew();
	    notify();
	    return true;
	} else {
	    return false;
	}
    }

    //STARTBUG
    public synchronized void startNew() {
	super.start();
	this.started = true;
    }

    public void run() {
	try {
	    while ( true ) {
		// Wait for a runner:
		Runnable torun = waitForRunner();
		// If runner, run:
		if ( torun != null ) 
		    torun.run();
		// If dead, stop
		if ( ! alive )
		    break;
	    }
	} finally {
	    cache.isDead(this);
	}
    }

    CachedThread(ThreadCache cache, int id) {
	super(cache.getThreadGroup(), cache.getThreadGroup().getName()+":"+id);
	this.cache = cache;
	setPriority(cache.getThreadPriority());
	setDaemon(true);
    }

}

public class ThreadCache {
    private static final boolean debug = false;

    /**
     * Default number of cached threads.
     */
    private static final int DEFAULT_CACHESIZE = 5;
    /**
     * Has this thread cache been initialized ?
     */
    protected boolean inited = false;
    /**
     * The thread group for this thread cache.
     */
    protected ThreadGroup group = null;
    /**
     * Number of cached threads.
     */
    protected int cachesize = DEFAULT_CACHESIZE;
    /**
     * Number of created threads.
     */
    protected int threadcount = 0;
    /**
     * Uniq thread identifier within this ThreadCache instance.
     */
    protected int threadid = 0;
    /**
     * Number of idle threads to always maintain alive.
     */
    protected int idlethreads = 0;
    /**
     * Should we queue thread requests, rather then creating new threads.
     */
    protected boolean growasneeded = false;
    /**
     * Number of used threads
     */
    protected int usedthreads = 0;
    /**
     * List of free threads.
     */
    protected CachedThread freelist = null;
    protected CachedThread freetail = null;
    /**
     * The idle timeout, for a thread to wait before being killed.
     * Defaults to <strong>5000</strong> milliseconds.
     */
    protected int idletimeout = 5000;
    /**
     * Cached thread priority.
     */
    protected int threadpriority = 5;

    /**
     * Get the idle timeout value for this cache.
     * @return The idletimeout value, or negative if no timeout applies.
     */

    synchronized final int getIdleTimeout() {
	return (threadcount <= idlethreads) ? -1 : idletimeout;
    }

    /**
     * The given thread is about to be declared free.
     * @return A boolean, <strong>true</strong> if the thread is to continue
     * running, <strong>false</strong> if the thread should stop.
     */

    final synchronized boolean isFree(CachedThread t, boolean timedout) {
	if ( timedout && (threadcount > idlethreads) ) {
	    if ( ! t.isTerminated() ) {
		threadcount--;
		usedthreads--;
		notifyAll();
	    } 
	    return false;
	} else if ( threadcount <= cachesize ) {
	    t.prev   = freetail;
	    if (freetail != null)
		freetail.next = t;
	    freetail = t;
	    if (freelist == null)
		freelist = t;
	    usedthreads--;
	    notifyAll();
	    return true;
	} else {
	    if ( ! t.isTerminated() ) {
		threadcount--;
		usedthreads--;
		notifyAll();
	    }
	    return false;
	}
    }

    /**
     * The given thread has terminated, cleanup any associated state.
     * @param dead The dead CachedThread instance.
     */

    final synchronized void isDead(CachedThread t) {
	if ( debug )
	    System.out.println("** "+t+": is dead tc="+threadcount);
	if ( ! t.isTerminated() ) {
	    threadcount--;
	    notifyAll();
	}
    }

    /**
     * Create a new thread within this thread cache.
     * @return A new CachedThread instance.
     */

    private synchronized CachedThread createThread() {
	threadcount++;
	threadid++;
	return new CachedThread(this, threadid);
    }

    /**
     * Allocate a new thread, as requested.
     * @param waitp Should we wait until a thread is available ?
     * @return A launched CachedThread instance, or <strong>null</strong> if 
     * unable to allocate a new thread, and <code>waitp</code> is <strong>
     * false</strong>.
     */

    protected synchronized CachedThread allocateThread(boolean waitp) {
	CachedThread t = null;
	while ( true ) {
	    if ( freelist != null ) {
		if ( debug )
		    System.out.println("*** allocateThread: free thread");
		t        = freelist;
		freelist = freelist.next;
		if (freelist != null) {
		    freelist.prev = null;
		} else {
		    freetail = null;
		}
		t.next = null;
		break;
	    } else if ((threadcount < cachesize) || growasneeded) {
		if ( debug )
		    System.out.println("*** create new thread.");
		t = createThread();
		break;
	    } else if ( waitp ) {
		if ( debug )
		    System.out.println("*** wait for a thread.");
		// Wait for a thread to become available
		try {
		    wait();
		} catch (InterruptedException ex) {
		}
	    } else {
		return null;
	    }
	}
	return t;
    }

    /**
     * Set the thread cache size.
     * This will also update the number of idle threads to maintain, if 
     * requested.
     * @param cachesize The new thread cache size.
     * @param update If <strong>true</strong> also update the number of
     * threads to maintain idle.
     */

    public synchronized void setCachesize(int cachesize, boolean update) {
	this.cachesize = cachesize;
	if ( update ) 
	    this.idlethreads = (cachesize>>1);
    }

    /**
     * Set the thread cache size.
     * Updaet the number of idle threads to keep alive.
     * @param cachesize The new thread cache size.
     */

    public void setCachesize(int cachesize) {
	setCachesize(cachesize, true);
    }

    /**
     * Enable/disable the thread cache to grow as needed.
     * This flag should be turned on only if always getting a thread as fast
     * as possible is critical.
     * @param onoff The toggle.
     */

    public void setGrowAsNeeded(boolean onoff) {
	this.growasneeded = onoff;
    }

    /**
     * Set all the cached threads priority.
     * Changing the cached thread priority should be done before the thread
     * cache is initialized, it will <em>not</em> affect already created 
     * threads.
     * @param priority The new cachewd threads priority.
     */

    public void setThreadPriority(int priority) {
	threadpriority = priority;
    }

    /**
     * Get the cached thread normal priority.
     * @return Currently assigned cached thread priority.
     */

    public int getThreadPriority() {
	return threadpriority;
    }

    /**
     * Set the idle timeout. 
     * The idle timeout value is used to eliminate threads that have remain 
     * idle for too long (although the thread cache will ensure that a 
     * decent minimal number of threads stay around).
     * @param idletimeout The new idle timeout.
     */

    public synchronized void setIdleTimeout(int idletimeout) {
	this.idletimeout = idletimeout;
    }

    /**
     * Request a thread to run on the given object.
     * @param runnable The object to run with the allocated thread.
     * @param waitp If <strong>true</strong> wait until a free thread is 
     * available, otherwise, return <strong>false</strong>.
     * @return A boolean, <strong>true</strong> if a thread was successfully
     * allocated for the given object, <strong>false</strong> otherwise.
     */

    public boolean getThread(Runnable runnable, boolean waitp) {
	if ( debug )
	    System.out.println("*** getting a thread for "+runnable);
	if ( ! inited )
	    throw new RuntimeException("Uninitialized thread cache");
	// Allocate and launch the thread:
	while ( true ) {
	    CachedThread t = allocateThread(waitp);
	    if ( t != null ) {
		if ( t.wakeup(runnable) ) {
		    synchronized (this) {
			usedthreads++;
		    }
		    return true;
		}
	    } else {
		return false;
	    }
	}
    }

    /**
     * Get the ThreadGroup managed by this ThreadCache instance.
     * @return A ThreadGroup instance.
     */

    public ThreadGroup getThreadGroup() {
	return group;
    }

    /**
     * Wait until all the threads have finished their duty
     */

    public synchronized void waitForCompletion() {
	while (usedthreads > 0) {
	    if ( debug )
		System.out.println("*** Waiting for "+usedthreads+ " threads");
	    try {
		wait();
	    } catch (InterruptedException ex) {
	    }
	}
    }

    /**
     * Initialize the given thread cache.
     * This two stage initialize method is done so that configuration
     * of the thread cache can be done before any thread get actually
     * created.
     */

    public synchronized void initialize() {
	CachedThread t = createThread();
	freelist = t;
	freetail = t;
	t.next = null;
	t.prev = null;
	synchronized (t) {
		t.start();
		t.started = true;
	}
	//t.startNew();
	for (int i = 1 ; i < idlethreads ; i++) {
	    t = createThread();
	    t.next = freelist;
	    t.prev = null;
	    freelist.prev = t;
	    freelist = t;
		synchronized (t) {
			t.start();
			t.started = true;
		}
	    //t.startNew();
	}
	inited = true;
    }

    /**
     * Create a thread cache, whose threads are to be children of the group.
     * @param group The thread group to which this thread cache is bound.
     * @param nstart Number of thread to create in advance.
     */

    public ThreadCache(ThreadGroup group) {
	this.group = group;
    }

    /**
     * Create a thread cache, after creating a new thread group.
     * @param name The name of the thread group to create.
     */

    public ThreadCache(String name) {
	this(new ThreadGroup(name));
    }

    /**
     * Create a thread cache, after creating a new thread group.
     * @param parent The parent of the thread group to create.
     * @param name The name of the thread group.
     */

    public ThreadCache(ThreadGroup parent, String name) {
	this(new ThreadGroup(parent, name));
    }

}

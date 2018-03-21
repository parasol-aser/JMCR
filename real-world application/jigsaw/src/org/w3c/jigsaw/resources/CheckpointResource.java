// CheckpointResource.java
// $Id: CheckpointResource.java,v 1.2 2010/06/15 17:53:05 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// please first read the full copyright statement in file COPYRIGHT.HTML

package org.w3c.jigsaw.resources;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ServerInterface;

import org.w3c.jigsaw.http.Logger;
import org.w3c.jigsaw.http.httpd;

import java.util.Date;

/**
 * A resource that will checkpoint the configuration at regular intervals.
 * This resource will make sure that current configuration is backed up to 
 * disk at regular (configurable) intervals.
 * <p>The webmaster can customize what part of the configuration is to be 
 * backed up through boolean attributes.
 */

public class CheckpointResource extends FramedResource implements Runnable {

    private static final boolean debug = true;

    /**
     * Attribute index - Backup interval, in seconds.
     */
    protected static int ATTR_INTERVAL = -1;
    /**
     * Attribute index - The priority of the flusher thread.
     */
    protected static int ATTR_PRIORITY = -1;
    /**
     * Attribute index - Should we flush the logs too ?
     */
    protected static int ATTR_FLUSHLOG = -1;
    /**
     * Attrbute index - Should we save the properties too ?
     */
    protected static int ATTR_FLUSHPROPS = -1;
    /**
     * Attribute index - Should we save the configuration ?
     */
    protected static int ATTR_FLUSHCONFIG = -1;
    /**
     * Attribute index - should we display a trace when we perform checkpoint
     */
    protected static int ATTR_TRACE_CHECK = -1;

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigsaw.resources.CheckpointResource");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register the interval attribute:
	a = new IntegerAttribute("interval"
				 , new Integer(60)
				 , Attribute.EDITABLE);
	ATTR_INTERVAL = AttributeRegistry.registerAttribute(c, a);
	// Register the flusher thread priority
	a = new IntegerAttribute("thread-priority"
				 , new Integer(2)
				 , Attribute.EDITABLE);
	ATTR_PRIORITY = AttributeRegistry.registerAttribute(c, a);
	// Register the flushlog boolean property:
	a = new BooleanAttribute("flush-logs"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_FLUSHLOG = AttributeRegistry.registerAttribute(c, a);
	// Register the flush properties property:
	a = new BooleanAttribute("flush-properties"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_FLUSHPROPS = AttributeRegistry.registerAttribute(c, a);
	// Register the flush configuration property:
	a = new BooleanAttribute("flush-configuration"
				 , Boolean.TRUE
				 , Attribute.EDITABLE);
	ATTR_FLUSHCONFIG = AttributeRegistry.registerAttribute(c, a);
	// Register the trace property
	a = new BooleanAttribute("trace-checkpoint"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_TRACE_CHECK = AttributeRegistry.registerAttribute(c, a);
    }
    /**
     * Our thread, if one is currently attached.
     */
    protected Thread thread = null;
    /**
     * Last date at which we checkpointed the configuration
     */
    protected Date checkpoint = null;
    /**
     * Is our attached thread still alive ?
     */
    protected boolean alive = false;

    /**
     * Start the thread for this object, only if needed.
     */

    public synchronized void activate() {
	if (getFlushLog() || getFlushProperties() || getFlushConfiguration()) {
	    if (getInterval() > 0) {
		alive = true;
		if (thread == null) {
		    thread = new Thread(this);
		    thread.setName("checkpointer");
		    thread.setPriority(getPriority());
		    thread.start();
		    return;
		} else {
		    return;
		}
	    }
	} 
	// The thread should not be running any more, stop and kill it:
	if ( thread != null ) {
	    alive = false;
	    thread.stop();
	    thread = null;
	}
	return;
    }

    /**
     * Force our attached thread to stop.
     */

    protected synchronized void stop() {
	alive  = false;
	thread = null;
	notify();
    }

    /**
     * Get the sync interval.
     * @return An integer number of seconds, or <strong>-1</strong> if 
     * undefined.
     */

    public int getInterval() {
	return getInt(ATTR_INTERVAL, -1);
    }

    /**
     * Get the priority for our attached thread.
     * @return An integer priority for the thread, which defaults to
     * <strong>2</strong> if undefined.
     */

    public int getPriority() {
	return getInt(ATTR_PRIORITY, 2);
    }

    /**
     * Get the flush log flag.
     * @return A boolean, <strong>true</strong> if the log is to be flushed at 
     * each refresh interval, <strong>false</strong> otherwise.
     */

    public boolean getFlushLog() {
	return getBoolean(ATTR_FLUSHLOG, false);
    }

    public boolean getTraceFlag() {
	return getBoolean(ATTR_TRACE_CHECK, false);
    }

    /**
     * Get the flush properties flag.
     * @return A boolean, <strong>true</strong> if the properties are to be
     * flushed, <strong>false</strong> otherwise.
     */

    public boolean getFlushProperties() {
	return getBoolean(ATTR_FLUSHPROPS, false);
    }

    /**
     * Get the flush configuration flag.
     * @return A boolean, <strong>true</strong> oif the configuration is to be
     * flushed at each interval, <strong>false</strong> otherwise.
     */

    public boolean getFlushConfiguration() {
	return getBoolean(ATTR_FLUSHCONFIG, true);
    }

    /**
     * This is the only resource that will refuse to be unloaded !
     * @return Always <strong>false</strong>.
     */

    public boolean acceptUnload() {
	return false;
    }

    /**
     * This resource is being unloaded.
     * Unloading that object will also stop the thread. However, there is
     * a bug here, since if the resource gets unloaded for some reason, it
     * will not be able to wakeup itself at next checkpoint time.
     */

    public void notifyUnload() {
	stop();
	super.notifyUnload();
    }

    /**
     * We are attached a thread, now it's time to performt the job.
     */

    public void run() {
	httpd   server   = (httpd) getServer();
	int     interval = -1;
	try {
	    while ( alive ) {
		// Are we still alive ?
		interval = getInterval();
		alive    = ((getFlushLog() 
			     || getFlushProperties() 
			     || getFlushConfiguration())
			    && (interval > 0 ));
		if ( ! alive )
		    break;
		// Wait for something to do:
		synchronized(this) {
		    try {
			wait(interval*1000);
		    } catch (InterruptedException ex) {
		    }
		} 
		// Do what is to be done:
		checkpoint = new Date();
		if ( getTraceFlag() )
		    System.out.println("*** Checkpoint ("+
				       server.getIdentifier()+
				       ") at "+
				       checkpoint);
		if (alive && getFlushConfiguration() ) 
		    server.checkpoint();
		if (alive && getFlushLog()) {
		    Logger logger = ((httpd)getServer()).getLogger();
		    if ( logger != null)
			logger.sync();
		}
	    }
	} catch (Exception ex) {
	    String msg = ("exception while running \""+
			  ex.getMessage() + "\". Stopped.");
	    server.errlog(this, msg);
	} finally {
	    thread = null;
	}
    }

    /**
     * Activate the checkpointer at initialization time.
     * @poaram values Default attribute values.
     */

    public void initialize(Object values[]) {
	super.initialize(values);
	try {
	    registerFrameIfNone("org.w3c.jigsaw.resources.CheckpointFrame",
				"checkpoint-frame");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}

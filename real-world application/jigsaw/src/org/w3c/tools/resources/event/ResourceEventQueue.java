// ResourceEventQueue.java
// $Id: ResourceEventQueue.java,v 1.1 2010/06/15 12:26:41 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.event;

import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceReference;

public class ResourceEventQueue {

    public final static boolean debug = false;

    class QueueCell {
	ResourceEvent event;
	QueueCell     next;
	int           id;

	QueueCell (ResourceEvent revt) {
	    this.event = revt;
	    this.next  = null;
	    this.id    = revt.getID();
	}
    }

    private QueueCell queue = null;
    private QueueCell last  = null;

    /**
     * Send a ResourceEvent in the queue.
     * @param evt The Resource Event to put in the queue.
     */
    public synchronized void sendEvent(ResourceEvent evt) {
	if (debug) 
	    System.out.println("[QUEUE] : sendEvent "+evt.getID()); 
	// => lock()
	QueueCell ncell = new QueueCell(evt);
	if (queue == null) {
	    queue = ncell;
	    last  = ncell;
	    notifyAll();
	} else {
	    if ((ncell.id == Events.RESOURCE_MODIFIED) ||
		(ncell.id == Events.FRAME_MODIFIED)) {
		if (debug) 
		    System.out.println("[QUEUE] : remove old modified event");
		QueueCell cell = queue;
		while (cell != null) {
		    if ((cell.id == ncell.id) && 
			(cell.event.getSource() == ncell.event.getSource())) 
			{
			    if (debug) 
				System.out.println("[QUEUE] : Found one!");
			    cell.event = ncell.event;
			    break;
			}
		    cell = cell.next;
		}
		if (cell == null) {
		    if (debug) 
			System.out.println("[QUEUE] : add new event.");
		    last.next = ncell;
		    last = ncell;
		}
	    } else {
		last.next = ncell;
		last = ncell;
	    } 
	} 
    }

    /**
     * Get the next Event in the queue, wait if there is no event
     * available in the queue.
     * @return a ResourceEvent instance.
     * @exception InterruptedException Is unable to get the next event due to
     * some interruption.
     */
    public synchronized ResourceEvent getNextEvent()
	throws InterruptedException 
    {
	while (queue == null)
	    wait();
	QueueCell next = queue;
	queue = queue.next;
	if (queue == null)
	    last = null;
	return next.event;
    }

    /**
     * Remove all the Events comming from the given source object.
     * @param source The Object where the events to remove are comming from.
     */
    public synchronized void removeSourceEvents(Object source) {
	QueueCell current = queue;
	QueueCell prev    = null;
	while (current != null) {
	    if (current.event.getSource() == source) {
		if (prev == null)
		    queue = current.next;
		else {
		    prev.next = current.next;
		    if (prev.next == null)
			last = prev;
		}
	    }
	    prev    = current;
	    current = current.next;
	}
    }

    public ResourceEventQueue() {
	queue = null;
	new Dispatcher("ResourceEventQueue Dispatcher", this).start();
    }

}

class Dispatcher extends Thread {
    private ResourceEventQueue queue    = null;
    private boolean            dispatch = true;

    public void stopDispatching() {
	dispatch = false;
    }

    public void run() {
	while (dispatch) {
	    try {
		ResourceEvent event = queue.getNextEvent();
		if (queue.debug) 
		    System.out.println("[QUEUE] : getNextEvent()");
		Object src = event.getSource();
		ResourceReference rr = null;
		try {
		    FramedResource source = null;
		    if (src instanceof ResourceReference) {
			rr = (ResourceReference)src;
			Resource res = rr.lock();
			if (res instanceof FramedResource)
			    source = (FramedResource) res;
			else 
			    source = null;
		    } else if (src instanceof FramedResource) {
			source = (FramedResource) src;
		    }
	
		    if (source != null) {
			if (queue.debug) 
			    System.out.println("[QUEUE] : processEvent "+
					       event);
			source.processEvent(event);
		    }
		} catch (InvalidResourceException ex) {
		    if (queue.debug) {
			System.err.println("Exception occurred "+
					   "in EventDispatcher:");
			ex.printStackTrace();
		    }
		} finally {
		    if (rr != null)
			rr.unlock();
		}
	    } catch (ThreadDeath death) {
		return;
	    } catch (Throwable e) {
		if (queue.debug) {
		    System.err.println("Exception occurred "+
				       "during event dispatching:");
		    e.printStackTrace();
		}
	    }
	}
    }

    Dispatcher (String name, ResourceEventQueue queue) {
	super(name);
	this.queue = queue;
    }

}

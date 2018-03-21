// ICPWaiter.java
// $Id: ICPWaiter.java,v 1.1 2010/06/15 12:27:34 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// please first read the full copyright statement in file COPYRIGHT.HTML

package org.w3c.www.protocol.http.icp;

import java.util.Vector;

class ICPWaiter {

    /**
     * The identifier this waiter is waitin for.
     */
    protected int id = -1;
    /**
     * The queue of replies, as they arrive:
     */
    protected Vector replies = null;

    /**
     * Get the identifier this waiter is waiting on.
     * @return The integer identifier.
     */

    protected final int getIdentifier() {
	return id;
    }

    /**
     * Get next matching reply until timeout expires.
     * @param timeout The timeout to wait until filaure.
     * @return A ICPReply instance, if available, or <strong>null</strong>
     * if timeout has expired.
     */

    protected synchronized ICPReply getNextReply(long timeout) {
	// Do we have a reply handy ?
	if ( replies.size() > 0 ) {
	    ICPReply reply = (ICPReply) replies.elementAt(0);
	    replies.removeElementAt(0);
	    return reply;
	}
	// Wait for timeout, or notification.
	try {
	    wait(timeout);
	} catch (InterruptedException ex) {
	}
	// Return, depnding on timeout expiration or reply available:
	if ( replies.size() == 0 )
	    return null;
	ICPReply reply = (ICPReply) replies.elementAt(0);
	replies.removeElementAt(0);
	return reply;
    }

    /**
     * Notify that waiter that a matching reply was received.
     * @param reply The matching ICP reply.
     */

    protected synchronized void notifyReply(ICPReply reply) {
	// Add that reply to the queue, and notify:
	replies.addElement(reply);
	notifyAll();
    }

    ICPWaiter(int id) {
	this.replies = new Vector(4);
	this.id      = id;
    }

}

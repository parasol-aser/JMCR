// RequestObserver.java
// $Id: RequestObserver.java,v 1.1 2010/06/15 12:25:16 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http;

/**
 * The interface to be implemented by request observers.
 * Request observers are objects that will be notified of the progress
 * made in processing an asynchronous request. Asynchronous requests are 
 * launched by a call to the <code>runRequest</code> method of the
 * <code>Httpmanager</code> class.
 * <p>While being processed, a request goes through a number of different
 * status, described below. Each time the status of a request changes, the 
 * appropriate observer gets called back.
 */

public interface RequestObserver {

    /**
     * Call back, invoked by the HttpManager callback thread.
     * Each time a request status changes (due to progress in its processing)
     * this callback gets called, with the new status as an argument.
     * @param preq The pending request that has made some progress.
     * @param event The event to broadcast.
     */

    public void notifyProgress(RequestEvent event) ;

}

// TeeMonitor.java
// $Id: TeeMonitor.java,v 1.1 2010/06/15 12:25:11 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache;

public interface TeeMonitor {
    /**
     * Called when the Tee stream fails, it allows you to notify a listener
     * of an error in the stream.
     * @parameter the size received so far, an integer
     */
    public void notifyTeeFailure(int size);

    /**
     * Called when the tee succeed, it allows you to notify a listener of the 
     * Tee that the download completed succesfully with a specific size
     * @parameter the size received, an integer
     */
    public void notifyTeeSuccess(int size);
}



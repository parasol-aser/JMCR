// MuxStreamHandler.java
// $Id: MuxStreamHandler.java,v 1.1 2010/06/15 12:26:35 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mux;

public interface MuxStreamHandler {

    /**
     * Someone has contacted us to speak that protocol, what should we do ?
     * This method gets called by a MuxStream instance when a new session
     * willing to speak some protocol is about to be established.
     * @param stream The stream on which that new session establishment was
     * received.
     * @param sessid The proposed session identifier for that new session.
     * @param protid The protocol identifier of the protocol to be
     * spoken on that session.
     */

    public boolean acceptSession(MuxStream stream, int sessid, int protid);

    /**
     * A new session has been accepted, and is now ready to run.
     * This method should setup an appropriate protocol handler to deal with
     * the protocol specific to the given session.
     * @param session The session that has been accepted.
     */

    public void notifySession(MuxSession session);

}

// ProtocolException.java
// $Id: ProtocolException.java,v 1.1 2010/06/15 12:20:18 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

public class ProtocolException extends Exception {
    String msg   = null ;
    ReplyInterface  error = null ;

    /**
     * Was a reply provided with the exception ?
     * @return True if a reply is available.
     */

    public boolean hasReply () {
        return error != null  ;
    }

    /**
     * Get this exception reply.
     * @return The reply to send back to requesting process.
     */

    public ReplyInterface getReply() {
        return error ;
    }

    public ProtocolException (String msg) {
        super (msg) ;
        this.error = null ;
    }

    public ProtocolException (String msg, ReplyInterface error) {
        super (msg) ;
        this.error = error ;
    }
    public ProtocolException (ReplyInterface error) {
        super ((String) null) ;
        this.error = error ;
    }

}

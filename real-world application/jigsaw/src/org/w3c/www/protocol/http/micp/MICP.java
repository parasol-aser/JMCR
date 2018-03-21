// MICP.java
// $Id: MICP.java,v 1.1 2010/06/15 12:21:47 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.micp;

/**
 * MICP constants.
 */

public interface MICP {
    /**
     * MICP current version.
     */
    public static final byte MICP_VERSION  = (byte) 1;
    /**
     * MIC opcodes - query for an URL.
     */
    public static final byte MICP_OP_QUERY = (byte) 1;
    /**
     * MICP opcode - reply to a query.
     */
    public static final byte MICP_OP_REPLY = (byte) 2;
}

// ICP.java
// $Id: ICP.java,v 1.1 2010/06/15 12:27:35 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// please first read the full copyright statement in file COPYRIGHT.HTML

package org.w3c.www.protocol.http.icp;

/**
 * Internet Cache Protocol well known constants.
 * ICP is a light-weight proxy to proxy protocol (FIXME, url)
 */

public interface ICP {
    /**
     * ICP constants - The major version of ICP we speak.
     */
    public static final int ICP_VERSION = 3;
    /**
     * ICP constants - Invalid payload (must not be sent).
     */
    public final static int ICP_OP_INVALID = 0;
    public final static int ICP_OP_QUERY   = 1;
    public final static int ICP_OP_HIT     = 2;
    public final static int ICP_OP_MISS    = 3;
    public final static int ICP_OP_ERR     = 4;
    public final static int ICP_OP_SEND    = 5;
    public final static int ICP_OP_SENDA   = 6;
    public final static int ICP_OP_DATABEG = 7;
    public final static int ICP_OP_DATA    = 8;
    public final static int ICP_OP_DATAEND = 9;
    public final static int ICP_OP_SECHO   = 10;
    public final static int ICP_OP_DECHO   = 11;
    public final static int ICP_OP_RELOADING = 21;
    public final static int ICP_OP_DENIED    = 22;
    public final static int ICP_OP_HIT_OBJ   = 23;

    public final static int ICP_FLAG_HIT_OBJ = 0x80000000;

   
}



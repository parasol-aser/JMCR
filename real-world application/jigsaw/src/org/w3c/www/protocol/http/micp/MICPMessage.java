// MICPMessage.java
// $Id: MICPMessage.java,v 1.1 2010/06/15 12:21:47 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.micp;

public class MICPMessage {
    public int    version = -1;
    public int    op      = -1;
    public int    src     = -1;
    public int    id      = -1;
    public String url     = null;
}

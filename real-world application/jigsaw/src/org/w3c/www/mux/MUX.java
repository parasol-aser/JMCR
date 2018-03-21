// MUX.java
// $Id: MUX.java,v 1.1 2010/06/15 12:26:34 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mux;

public interface MUX {
    // Impl constants:
    public static final int RECEIVER_DEFAULT_CREDIT      = 4096;
    public static final int SENDER_DEFAULT_CREDIT        = 4096;
    public static final int WRITER_BUFFER_SIZE           = 4096;
    public static final int READER_BUFFER_SIZE           = 4096;
    public static final int SENDER_DEFAULT_FRAGMENT_SIZE = 512;

    public static final int SESSIONS_INCR = 8;

    // Protocol constants:

    public static final int MAX_SESSION = 256;

    public static final int LONG_LENGTH = 0x80000000;
    public static final int CONTROL     = 0x40000000;
    public static final int SYN         = 0x20000000;
    public static final int FIN         = 0x10000000;
    public static final int RST         = 0x08000000;
    public static final int PUSH        = 0x04000000;
    public static final int SESSION     = 0x3FC00000;
    public static final int LENGTH      = 0x0003FFFF;

    public static final int CTRL_CODE          = 0x3C000000;
    public static final int CTRL_DEFINE_STRING = 0;
    public static final int CTRL_DEFINE_STACK  = 1;
    public static final int CTRL_MUX_CONTROL   = 2;
    public static final int CTRL_SEND_CREDIT   = 3;

}

// SocketClientState.java
// $Id: SocketClientState.java,v 1.1 2010/06/15 12:26:09 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http.socket ;

import org.w3c.util.LRUAble;

public class SocketClientState implements LRUAble {
    static final int C_IDLE = 0;	// Zombie
    static final int C_BUSY = 1;	// Is in busy list
    static final int C_FREE = 2;	// Is in free list
    static final int C_KILL = 3;	// Being killed
    static final int C_FIN  = 4;
    LRUAble next   = null;
    LRUAble prev   = null;
    SocketClient  client = null;
    int     id     = 0;
    int     status = C_IDLE;
    boolean bound  = false;
    boolean marked = false;

    SocketClientState csnext = null;
    SocketClientState csprev = null;

    static int nextid = 0;
    static final synchronized int nextId() {
	return nextid++;
    }

    public final LRUAble getNext() {
	return next;
    }

    public final LRUAble getPrev() {
	return prev;
    }

    public final void setNext(LRUAble next) {
	this.next = next;
    }

    public final void setPrev(LRUAble prev) {
	this.prev = prev;
    }

    SocketClientState(SocketClientState cshead) {
	this.status   = C_IDLE;
	this.id       = nextId();
	cshead.csprev = this;
	this.csnext   = cshead;
    }

    // Used to create the head of the list.
    SocketClientState() {
    }

}



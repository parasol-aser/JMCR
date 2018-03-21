// LRUList.java
// $Id: LRUList.java,v 1.1 2010/06/15 12:25:41 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.util ;

public abstract class LRUList {
    protected LRUNode head ;
    protected LRUNode tail ;

    public LRUList()
    {
	this.head = new LRUNode() ;
	this.tail = new LRUNode() ;
	head.prev = null ;
	head.next = tail ;
	tail.prev = head ;
	tail.next = null ;
    }

    /**
     * Moves node to front of list. It can be a new node, or it can be 
     * an existing node.
     * @param node the node
     */

    public abstract void toHead(LRUAble node) ;

    /**
     * Moves node to back of list. It can be a new node, or it can be
     * an existing node.
     * @param node the node
     */

    public abstract void toTail(LRUAble node) ;

    /**
     * Removes node if it's in list.
     * Does nothing if it's not.
     * When a node is removed, both its links are set to null.
     * @param node The node to remove
     * @return the same node
     */

    public abstract LRUAble remove(LRUAble node) ;

    /**
     * Obtain the backmost node.
     * @return the backmost node, or null if list is empty
     */

    public abstract LRUAble getTail() ;

    /**
     * Obtain the frontmost node.
     * @return the frontmost node, or null if list is empty
     */

    public abstract LRUAble getHead() ;

    /**
     * Obtain the backmost node, and remove it from list too.
     * @return the backmost node, or null if list is empty
     */

    public abstract LRUAble removeTail() ;

    /**
     * Get the next node of this list.
     * @return The next node, or <strong>null</strong> if this one was
     * last.
     */

    abstract public LRUAble getNext(LRUAble node) ;

    /**
     * Get the previous node of this list.
     * @return The previous node, or <strong>null</strong> if this one was
     * last.
     */

    abstract public LRUAble getPrev(LRUAble node) ;

}

// SyncLRUList.java
// $Id: SyncLRUList.java,v 1.1 2010/06/15 12:25:41 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.util ;

public class SyncLRUList extends LRUList {
    public synchronized void toHead(LRUAble node) {
	// First remove it if it's in the list already
	_remove(node) ;
	
	// Then link it to the front
	node.setNext(head.next) ;
	node.setPrev(head) ;
	head.next.setPrev(node) ;
	head.next = node ;
    }

    public synchronized void toTail(LRUAble node) {
	// First remove it if it's in the list already
	_remove(node) ;

	// Then link it to the back
	node.setPrev(tail.prev);
	node.setNext(tail) ;
	tail.prev.setNext(node) ;
	tail.prev = node ;
	
    }

    private final synchronized void _remove(LRUAble node) {
	LRUAble itsPrev, itsNext ;
	itsPrev = node.getPrev() ;
	// note assumption: if its prev is not null, neither is its next
	if(itsPrev==null) return ;
	itsNext = node.getNext() ;
	itsPrev.setNext(itsNext) ;
	itsNext.setPrev(itsPrev) ;
    }

    public final synchronized LRUAble remove(LRUAble node) {
	_remove(node) ;
	node.setNext((LRUAble) null) ;
	node.setPrev((LRUAble) null) ;
	return node ;
    }

    public final synchronized LRUAble getTail() {
	LRUAble prev = tail.prev ;
	return (prev == head) ? null : prev ;
    }

    public final synchronized LRUAble getHead() {
	LRUAble next = head.next ;
	return (next == tail) ? null : next ;
    }

    public final synchronized LRUAble removeTail() {
	if ( tail.prev != head )
	    return remove(tail.prev) ;
	return null;
    }

    public final synchronized LRUAble getNext(LRUAble node) {
	LRUAble next = node.getNext();
	return ((next == tail) || (next == head)) ? null : next;
    }

    public final synchronized LRUAble getPrev(LRUAble node) {
	LRUAble prev = node.getPrev();
	return ((prev == tail) || (prev == head)) ? null : prev;
    }

}

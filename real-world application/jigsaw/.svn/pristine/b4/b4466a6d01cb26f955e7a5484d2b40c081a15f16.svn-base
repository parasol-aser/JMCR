// LRUNode.java
// $Id: LRUNode.java,v 1.1 2010/06/15 12:25:39 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.util ;

public class LRUNode implements LRUAble {
    protected LRUAble prev ;
    protected LRUAble next ;

    public LRUNode() {
	this.prev = null ;
	this.next = null ;
    }
	
    public LRUNode(LRUAble prev,LRUAble next) {
	this.prev = prev ;
	this.next = next ;
    }

    public LRUAble getNext() {
	return next ;
    }
	
    public LRUAble getPrev() {

	return prev;
    }

    public void setPrev(LRUAble prev) {
	this.prev = prev ;
    }

    public void setNext(LRUAble next) {
	this.next = next ;
    }
}

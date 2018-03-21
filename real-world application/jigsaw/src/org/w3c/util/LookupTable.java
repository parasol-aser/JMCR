// LookupTable.java
// $Id: LookupTable.java,v 1.1 2010/06/15 12:25:39 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.util;

import java.io.Serializable;

import java.util.Enumeration;

/**
 * A kind of hashtable (maps keys to values), useful for a limited number
 * of elements.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class LookupTable implements Cloneable, Serializable {

    /**
     * The default capacity.
     */
    public static final int DEFAULT_CAPACITY = 10;

    private Object elements[] = null;

    private Object keys[]     = null;

    private int count    = 0;

    private int capacity = 0;

    /**
     * Returns the number of keys in this lookuptable.
     *
     * @return  the number of keys in this lookuptable.
     */
    public int size() {
	return count;
    }

    /**
     * Tests if this lookuptable maps no keys to values.
     * @return  <code>true</code> if this lookuptable maps no keys to values;
     *          <code>false</code> otherwise.
     */
    public boolean isEmpty() {
	return count == 0;
    }

    /**
     * Returns an enumeration of the keys in this lookuptable.
     *
     * @return  an enumeration of the keys in this lookuptable.
     * @see     java.util.Enumeration
     * @see     #elements()
     */
    public synchronized Enumeration keys() {
	return new ArrayEnumeration(keys, count);
    }

    /**
     * Returns an enumeration of the values in this lookuptable.
     * Use the Enumeration methods on the returned object to fetch the elements
     * sequentially.
     *
     * @return  an enumeration of the values in this lookuptable.
     * @see     java.util.Enumeration
     * @see     #keys()
     */
    public synchronized Enumeration elements() {
	return new ArrayEnumeration(elements, count);
    }

    /**
     * Tests if some key maps into the specified value in this lookuptable.
     * @param      value   a value to search for.
     * @return     <code>true</code> if and only if some key maps to the
     *             <code>value</code> argument in this lookuptable as 
     *             determined by the <tt>equals</tt> method;
     *             <code>false</code> otherwise.
     * @exception  NullPointerException  if the value is <code>null</code>.
     * @see        #containsKey(Object)
     */
    public synchronized boolean contains(Object value) {
	return (contains(elements, count, value) != -1);
    }

    /**
     * Tests if the specified object is a key in this lookuptable.
     * @param   key   possible key.
     * @return  <code>true</code> if and only if the specified object 
     *          is a key in this lookuptable, as determined by the 
     *          <tt>equals</tt> method; <code>false</code> otherwise.
     * @see     #contains(Object)
     */
    public synchronized boolean containsKey(Object key) {
	return (contains(keys, count, key) != -1);
    }

    private int contains(Object array[], int size, Object value) {
	if (value == null) {
	    throw new NullPointerException();
	}
	for (int i = 0 ; i < size ; i++) {
	    if (array[i].equals(value))
		return i;
	}
	return -1;
    }

    /**
     * Returns the value to which the specified key is mapped in this 
     * lookuptable.
     * @param   key   a key in the lookuptable.
     * @return  the value to which the key is mapped in this lookuptable;
     *          <code>null</code> if the key is not mapped to any value in
     *          this lookuptable.
     * @see     #put(Object, Object)
     */
    public synchronized Object get(Object key) {
	int idx = contains(keys, count, key);
	if (idx != -1)
	    return elements[idx];
	return null;
    }

    /**
     * Maps the specified <code>key</code> to the specified 
     * <code>value</code> in this lookuptable. Neither the key nor the 
     * value can be <code>null</code>. <p>
     *
     * The value can be retrieved by calling the <code>get</code> method 
     * with a key that is equal to the original key. 
     *
     * @param      key     the lookuptable key.
     * @param      value   the value.
     * @return     the previous value of the specified key in this lookuptable,
     *             or <code>null</code> if it did not have one.
     * @exception  NullPointerException  if the key or value is
     *               <code>null</code>.
     * @see     Object#equals(Object)
     * @see     #get(Object)
     */
    public synchronized Object put(Object key, Object value) {
	if (value == null) {
	    throw new NullPointerException();
	}
	int idx = contains(keys, count, key);
	if (idx == -1) {
	    if (count >= capacity) {
		grow();
	    }
	    keys[count]     = key;
	    elements[count] = value;
	    count++;
	    return null;
	} else {
	    Object previousValue = elements[idx];
	    elements[idx] = value;
	    return previousValue;
	}
    }

    /**
     * Increases the capacity. This method is called automatically when the 
     * number of keys in the lookuptable exceeds this lookuptable's capacity.
     */ 
    protected void grow() {
	int newCapacity = capacity * 2 + 1;
	Object newElements[] = new Object[newCapacity];
	Object newKeys[]     = new Object[newCapacity];
	System.arraycopy(elements, 0, newElements, 0, count);
	System.arraycopy(keys, 0, newKeys, 0, count);
	this.keys     = newKeys;
	this.elements = newElements;
	this.capacity = newCapacity;
    }

    /**
     * Removes the key (and its corresponding value) from this 
     * lookuptable. This method does nothing if the key is not in the 
     * lookuptable.
     *
     * @param   key   the key that needs to be removed.
     * @return  the value to which the key had been mapped in this lookuptable,
     *          or <code>null</code> if the key did not have a mapping.
     */
    public synchronized Object remove(Object key) {
	int idx = contains(keys, count, key);
	if (idx != -1) {
	    //remove this one by moving the last one here.
	    Object oldvalue = elements[idx];
	    count--;
	    keys[idx]       = keys[count];
	    elements[idx]   = elements[count];
	    keys[count]     = null;
	    elements[count] = null;
	    return oldvalue;
	}
	return null;
    }

    /**
     * Clears this lookuptable so that it contains no keys. 
     */
    public synchronized void clear() {
	this.count     = 0;
	this.keys      = new Object[capacity];
	this.elements  = new Object[capacity];
    }

    /**
     * Creates a shallow copy of this lookuptable. All the structure of the 
     * lookuptable itself is copied, but the keys and values are not cloned. 
     * This is a relatively expensive operation.
     *
     * @return  a clone of the lookuptable.
     */
    public synchronized Object clone() {
	try {
	    LookupTable l = (LookupTable)super.clone();
	    l.keys = new Object[capacity];
	    System.arraycopy(keys, 0, l.keys, 0, count);
	    l.elements = new Object[capacity];
	    System.arraycopy(elements, 0, l.elements, 0, count);
	    l.capacity = capacity;
	    l.count    = count;
	    return l;
	} catch (CloneNotSupportedException e) { 
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}
    }

    /**
     * Returns a string representation of this <tt>Lookuptable</tt> object 
     * in the form of a set of entries, enclosed in braces and separated 
     * by the ASCII characters "<tt>,&nbsp;</tt>" (comma and space). Each 
     * entry is rendered as the key, an equals sign <tt>=</tt>, and the 
     * associated element, where the <tt>toString</tt> method is used to 
     * convert the key and element to strings. <p>Overrides to 
     * <tt>toString</tt> method of <tt>Object</tt>.
     *
     * @return  a string representation of this lookuptable.
     */
    public synchronized String toString() {
	StringBuffer buffer = new StringBuffer();
	for (int i = 0 ; i < count ; i++) {
	    buffer.append("["+keys[i]+","+elements[i]+"]");
	}
	return buffer.toString();
    }

    /**
     * Constructor.
     * @param capacity the initial capacity
     */
    public LookupTable(int capacity) {
	this.count    = 0;
	this.capacity = capacity;
	this.elements = new Object[capacity];
	this.keys     = new Object[capacity];
    }

    /**
     * Constructor, build a LookupTable with a initial capacity set to
     * DEFAULT_CAPACITY.
     */
    public LookupTable() {
	this(DEFAULT_CAPACITY);
    }

}

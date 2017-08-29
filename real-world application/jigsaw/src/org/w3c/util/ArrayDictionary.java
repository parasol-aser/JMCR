// ArrayDictionary.java
// $Id: ArrayDictionary.java,v 1.1 2010/06/15 12:25:41 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.util ;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Vector;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Random-access dictionary:
 * like a dictionary but with a certain Vector-ness to it
 * Besides all the methods from Dictionary, it also has methods that
 * permit direct access to the nth element or nth key.
 * Should be used with care...it's not too well tested yet, and it
 * is very exposed.
 * <p>This class does <em>not</em> provide thread-safeness, for the sake of
 * efficiency, again it should be used with care !
 * @author Antonio Ram&iacute;rez
 */

public class ArrayDictionary extends Dictionary implements Cloneable {
    /** The array of keys */
    protected Object[] keys ;

    /** The array of corresponding values */
    protected Object[] values ;

    /** How many real elements are in */
    protected int nelems ;

    /** By how much to grow */
    protected int incr ;

    /**
     * Create an ArrayDictionary using default values for initial size and
     * increment.
     */
    public ArrayDictionary() {
	this(10,10) ;
    }

    /**
     * Create an ArrayDictionary using the given initial size.
     * (The increment is set to the same value).
     * @param init The initial size
     */
    public ArrayDictionary(int init) {
	this(init,init) ;
    }

    /**
     * Clone this array dictionary.
     * <p>As for hashtables, a shallow copy is made, the keys and elements
     * themselves are <em>not</em> cloned.
     * @return The clone.
     */

    public Object clone() {
	try {
	    ArrayDictionary cl = (ArrayDictionary) super.clone();
	    cl.values = new Object[values.length];
	    System.arraycopy(values, 0, cl.values, 0, values.length);
	    cl.keys = new Object[values.length];
	    System.arraycopy(keys, 0, cl.keys, 0, keys.length);
	    return cl;
	} catch (CloneNotSupportedException ex) {
	    throw new InternalError();
	}
    }

    /**
     * Create an ArrayDictionary using the given initial size and
     * the given increment for growing the array.
     * @param init the initial size
     * @param incr the increment
     */
    public ArrayDictionary(int init, int incr) {
	keys = new Object[init] ;
	values = new Object[init] ;
	this.incr = incr ;
	nelems = 0 ;
    }

    /**
     * Create an ArrayDictionary, contructing the arrays of keys and
     * values from the two given vectors.
     * The two vectors should have the same size.
     * The increment is set to the number of elements.
     * @param keys the vector of keys
     * @param values the vector of values
     */
    public ArrayDictionary(Vector keys,Vector values) {
	this(keys,values,values.size()) ;
    }
    /**
     * Create an ArrayDictionary, contructing the arrays of keys and
     * values from the two given vectors.
     * The two vectors should have the same size.
     * @param keys the vector of keys
     * @param values the vector of values
     * @param incr the increment for growing the arrays
     */
    public ArrayDictionary(Vector keys, Vector values, int incr) {
	this.incr = incr ;
	nelems = keys.size() ;
	this.keys = new Object[nelems] ;
	this.values = new Object[nelems] ;
	keys.copyInto(this.keys) ;
	values.copyInto(this.values) ;
    }

    /**
     * Create an ArrayDicitonary, <em>using</em> (not copying) the given pair
     * of arrays as keys and values. The increment is set to the length of the
     * arrays. 
     * @param keys the array of keys
     * @param values the array of values
     */
    public ArrayDictionary(Object[] keys, Object[] values) {
	this(keys,values,values.length) ;
    }

    /**
     * Create an ArrayDicitonary, <em>using</em> (not copying) the given pair
     * of arrays as keys and values.
     * @param keys the array of keys
     * @param values the array of values
     * @param incr the increment for growing the arrays
     */
    public ArrayDictionary(Object[] keys, Object[] values, int incr) {
	this.incr = incr ;
	nelems = keys.length ;
	this.keys = keys ;
	this.values = values ;
    }

    protected final void grow() {
	grow(keys.length+incr) ;
    }

    protected void grow(int newCapacity) {
	Object[] newKeys = new Object[newCapacity] ;
	Object[] newVals = new Object[newCapacity] ;

	System.arraycopy(keys,0,newKeys,0,keys.length) ;
	System.arraycopy(values,0,newVals,0,values.length) ;

	keys = newKeys ;
	values = newVals ;
    }

    /**
     * Returns an enumeration of the elements of the dictionary.
     * @return the enumeration
     */
    public Enumeration elements() {
	return new ArrayEnumeration(values,nelems) ;
    }

    /**
     * Returns the value that maps to the given key.
     * @param key the key
     * @return the value
     */
    public Object get(Object key) {
	int n,i;
	for(i=0,n=0;i<keys.length;i++) {
	    if(n >= nelems)
		break ;
	    if ( keys[i] == null )
		continue;
	    if(keys[i].equals(key)) 
		return values[i] ;
	    n++ ;
	}
	return null;
    }

    /**
     * "Optimized" method to obtain the values
     * corresponding to several keys, in one swoop.
     * @param rKeys An array of requested keys
     * @return An array of corresponding values
     */
    public Object[] getMany(Object[] rKeys) {
	Object[] rValues = new Object[rKeys.length] ;
	int i,n ;
	for(i=0,n=0;i<keys.length;i++) {
	    if(n >= nelems) break ;
	    if(keys[i]==null)
		continue ;
	inloop:
	    for(int j=0;j<rKeys.length;j++) 
		if(keys[i].equals(rKeys[j])) {
		    rValues[j] = values[i] ;
		    break inloop ;
		}

	    n++ ;
	}
	return rValues ;
    }

    
    /**
     * Are there any entries in the dictionary?
     * @return <strong>true</strong> if there are no entries,
     *         <strong>false></strong> otherwise.
     */
    public final boolean isEmpty() {
	return nelems==0 ;
    }

    /**
     * Increases the capacity of this dictionary to at least the
     * specified number of key/value mappings.
     * @param minCapacity the desired minimum capacity
     */
    public final void ensureCapacity(int minCapacity) {
	if(minCapacity>keys.length) grow(minCapacity) ;
    }

    /**
     * Returns an enumeration of the keys of the dictionary.
     * @return the enumeration
     */
    public Enumeration keys() {
	return new ArrayEnumeration(keys,nelems) ;
    }

    /**
     * Adds a mapping between a key and a value to the dictionary.
     * Will grow the arrays if necessary.
     * @param key the key
     * @param value the corresponding value
     * @return the previous value corresponding to the key, or null if
     *         the key is new.
     */
    public Object put(Object key,Object value) {
	int empty = -1 ;
	int i,n ;
	for(i=0,n=0;i<keys.length;i++) {
	    if(n >= nelems)
		break ;
	    if(keys[i] == null) {
		empty = i ;
		continue ;
	    }
	    if(keys[i].equals(key)) {
		Object prev = values[i] ;
		values[i]=value;
		return prev ;
	    }
	    n++ ;
	}

	if(empty!=-1) {
	    keys[empty]=key ;
	    values[empty]=value ;
	    nelems++ ;
	} else {
	    grow() ;
	    keys[nelems] = key ;
	    values[nelems++] = value ;
	}

	return null ;
    }

    /**
     * Removes a key (and its value) from the dictionary;
     * @param key the key to remove
     * @return the value that used to map to that key
     */
    public Object remove(Object key) {
	int i,n ;
	for(i=0,n=0;i<keys.length;i++) {
	    if(n >= nelems)
		break ;
	    if(keys[i] == null)
		continue ;
	    if(keys[i].equals(key)) {
		nelems-- ;
		Object prev = values[i] ;
		keys[i] = values[i] = null ;
		return prev ;
	    }
	    n++ ;
	}
	return null ;
    }

    /**
     * Returns the number of elements in the dictionary
     * @return the number of elements
     */
    public final int size() { return nelems ; }

    /**
     * Returns the maximum number of keys the dictionary can hold
     * without reallocating an array.
     * @return the capacity of the dictionary
     */
    public final int capacity() { return keys.length ; } 

    /**
     * Returns the nth key.
     * @param n the index of the desired key
     * @return the nth key, or null if no key in that place.
     */
    public final Object keyAt(int n) {
	return keys[n] ;
    }

    /**
     * Returns the nth element (value).
     * @param n the index of the desired element
     * @return the nth element, or null if no element in that place.
     */
    public final Object elementAt(int n) {
	return values[n] ;
    }

    /**
     * Sets the element at the nth place in the array.
     * @param n the index of the element to change
     * @param newVal the value to change it to
     * @return the old value
     */
    public Object setElementAt(int n,Object newVal) {
	Object prev = values[n] ;
	values[n] = newVal ;
	return prev ;
    }

    /**
     * Removes the nth mapping (key/value pair) in the dictionary.
     * @param n the index of the element to remove
     * @return the old value of the element at the nth place
     */
    public Object removeElementAt(int n) {
	if(values[n]!=null) {
	    Object prev = values[n] ;
	    values[n] = keys[n] = null ;
	    nelems--;
	    return prev;
	} else return null ;
    }

    /**
     * Creates a string representation of the dictionary
     * @return the string representation.
     */
    public String toString() {
	StringBuffer buf = new StringBuffer(100) ;
	buf.append('[') ;
	for(int i=0;i<keys.length;i++) {
	    if(keys[i]==null)
		continue ;
	    buf.append(keys[i]) ;
	    buf.append('=') ;
	    buf.append(values[i]) ;
	    buf.append(' ') ;
	}
	buf.append(']') ;
	return buf.toString() ;

    }

    /**
     * A kludge for testing ArrayDictionary
     */
    public static void main(String[] args)
    {
	try {
	    PrintStream out = System.out ;
	    DataInputStream in = new DataInputStream(System.in) ;
	    
	    String line = null ;

	    out.print("n ? ") ; out.flush() ;
	    line = in.readLine() ;
	    int n = Integer.parseInt(line) ;
	    ArrayDictionary ad = new ArrayDictionary(n) ;

	    String key = null, value = null;
	    while(true) {
		out.print("action ? ") ; out.flush() ;
		line = in.readLine() ;

		switch(line.charAt(0)) {
		  case 'p':
		  case 'P':
		      out.print("key ? ") ; out.flush() ;
		      key = in.readLine() ;
		      out.print("value ? ") ; out.flush() ;
		      value = in.readLine() ;
		      value = (String ) ad.put(key,value) ;
		      out.println("old: "+value) ;
		      break ;
		  case 'r':
		  case 'R':
		      out.print("key ? ") ; out.flush() ;
		      key = in.readLine() ;
		      value = (String) ad.remove(key) ;
		      out.println("old: "+value) ;
		      break ;
		  case 'g':
		  case 'G':
		      out.print("key ? ") ; out.flush() ;
		      key = in.readLine() ;
		      value = (String) ad.get(key) ;
		      out.println("value: "+value) ;
		      break ;
		  case 'd':
		  case 'D':
		      out.println(ad.toString()) ;
		      break ;
		  case 'q':
		  case 'Q':
		      return ;
		}
	    }
	} catch(Exception ex) {
	    ex.printStackTrace() ;
	}
    }

}



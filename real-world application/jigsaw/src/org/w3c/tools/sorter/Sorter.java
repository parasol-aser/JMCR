// Sorter.java
// $Id: Sorter.java,v 1.1 2010/06/15 12:27:06 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.sorter ;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import java.io.File;

/**
 * This class implements a bunch of different ways of sorting things.
 */

public class Sorter {

    private static int compare(File file1, File file2) {
	if (file1.isDirectory() && file2.isFile())
	    return -1;
	else if (file1.isFile() && file2.isDirectory())
	    return 1;
	else return file1.compareTo(file2);
    }

    /**
     * Insert a File into a vector, maintaing the order:<p>
     * Directory then files in alphabetical order.
     * @param file The File used to sort.
     * @param into The target sorted vector.
     */
    public static void orderedFileInsert(File file, Vector into) {
	int  lo   = 0 ;
	int  hi   = into.size() - 1 ;
	int  idx  = -1 ;
	File item = null ;
	int  cmp  = 0 ;

	if ( hi >= lo ) {
	    while ((hi - lo) > 1) {
		idx  = (hi-lo) / 2 + lo ;
		item = (File) into.elementAt(idx) ;
		cmp  = compare(item, file) ;
		if ( cmp == 0 ) {
		    return ;
		} else if ( cmp < 0 ) {
		    lo = idx ;
		} else if ( cmp > 0 ) {
		    hi = idx ;
		}
	    }
	    switch (hi-lo) {
	    case 0:
		item = (File) into.elementAt(hi) ;
		if (item.equals(file))
		    return ;
		idx = (compare(item, file) < 0) ? hi + 1 : hi ;
		break ;
	    case 1:
		File loitem = (File) into.elementAt(lo) ;
		File hiitem = (File) into.elementAt(hi) ;
		if ( loitem.equals(file) )
		    return ;
		if ( hiitem.equals(file) )
		    return ;
		if ( compare(file, loitem) < 0 ) {
		    idx = lo ;
		} else if ( compare(file, hiitem) < 0 ) {
		    idx = hi ;
		} else {
		    idx = hi + 1 ;
		}
		break ;
	    default:
		throw new RuntimeException ("implementation bug.") ;
	    }
	}
	// Add this file to the vector:
	if ( idx < 0 ) 
	    idx = 0 ;
	into.insertElementAt(file, idx) ;
	return ;
    }

    /**
     * Insert a String into a vector, maintaing the order.
     * @param key The string to insert.
     * @param into The target sorted vector.
     */

    static void orderedStringInsert(String key, Vector into) {
	int lo  = 0 ;
	int hi  = into.size() - 1 ;
	int idx = -1 ;
	String item = null ;
	int cmp = 0 ;

	if ( hi >= lo ) {
	    while ((hi - lo) > 1) {
		idx  = (hi-lo) / 2 + lo ;
		item = (String) into.elementAt(idx) ;
		cmp  = item.compareTo(key) ;
		if ( cmp == 0 ) {
		    return ;
		} else if ( cmp < 0 ) {
		    lo = idx ;
		} else if ( cmp > 0 ) {
		    hi = idx ;
		}
	    }
	    switch (hi-lo) {
	      case 0:
		item = (String) into.elementAt(hi) ;
		if (item.equals(key))
		    return ;
		idx = (item.compareTo(key) < 0) ? hi + 1 : hi ;
		break ;
	      case 1:
		String loitem = (String) into.elementAt(lo) ;
		String hiitem = (String) into.elementAt(hi) ;
		if ( loitem.equals(key) )
		    return ;
		if ( hiitem.equals(key) )
		    return ;
		if ( key.compareTo(loitem) < 0 ) {
		    idx = lo ;
		} else if ( key.compareTo(hiitem) < 0 ) {
		    idx = hi ;
		} else {
		    idx = hi + 1 ;
		}
		break ;
	      default:
		throw new RuntimeException ("implementation bug.") ;
	    }
	}
	// Add this key to the vector:
	if ( idx < 0 ) 
	    idx = 0 ;
	into.insertElementAt(key, idx) ;
	return ;
    }

    /**
     * Quick sort the given chunk of the array in place.
     * @param array The array to sort.
     * @param lo0 The low bound of the chunk of the array to sort.
     * @param hi0 The high bound of the array to sort.
     */

    static void quickSortStringArray(String array[], int lo0, int hi0) {
	int lo = lo0 ;
	int hi = hi0 ;
	String mid = null ;

	if ( hi0 > lo0 ) {
	    mid = array[(lo0+hi0)/2] ;
	    while (lo <= hi) {
		while ((lo < hi0) && (array[lo].compareTo(mid) < 0)) 
		    ++lo ;
		while ((hi > lo0) && (array[hi].compareTo(mid) > 0))
		    --hi ;
		if ( lo <= hi ) {
		    String tmp = array[lo] ;
		    array[lo]  = array[hi] ;
		    array[hi]  = tmp ;
		    ++lo ;
		    --hi ;
		}
	    }
	    if ( lo0 < hi )
		quickSortStringArray(array, lo0, hi) ;
	    if ( lo < hi0 )
		quickSortStringArray(array, lo, hi0) ;
	}
    }

    /**
     * Get the keys of this hashtable, sorted.
     * @param h The hashtable whose String keys are wanted.
     */

    public static Vector sortStringKeys(Hashtable h) {
	return sortStringEnumeration(h.keys()) ;
    }

    /**
     * Sort the given String enumeration.
     * @return A sorted vector of String.
     */

    public static Vector sortStringEnumeration(Enumeration e) {
	Vector sorted = new Vector() ;
	while ( e.hasMoreElements() ) {
	    orderedStringInsert((String) e.nextElement(), sorted) ;
	}
	sorted.trimToSize();
	return sorted ;
    }

    /**
     * Insert a Comparable into a vector, maintaing the order.
     * @param key The string to insert.
     * @param into The target sorted vector.
     */

    static void orderedComparableInsert(Comparable key, Vector into) {
	int lo  = 0 ;
	int hi  = into.size() - 1 ;
	int idx = -1 ;
	Comparable item = null ;
	int cmp = 0 ;

	if ( hi >= lo ) {
	    while ((hi - lo) > 1) {
		idx  = (hi-lo) / 2 + lo ;
		item = (Comparable) into.elementAt(idx) ;
		if (item.greaterThan(key)) {
		    hi = idx;
		} else {
		    lo = idx;
		}
	    }
	    switch (hi-lo) {
	      case 0:
		item = (Comparable) into.elementAt(hi) ;
		idx = (item.greaterThan(key)) ? hi : hi+1;
		break ;
	      case 1:
		Comparable loitem = (Comparable) into.elementAt(lo) ;
		Comparable hiitem = (Comparable) into.elementAt(hi) ;
		if ( loitem.greaterThan(key) ) {
		    idx = lo ;
		} else if ( hiitem.greaterThan(key) ) {
		    idx = hi ;
		} else {
		    idx = hi + 1 ;
		}
		break ;
	      default:
		throw new RuntimeException ("implementation bug.") ;
	    }
	}
	// Add this key to the vector:
	if ( idx < 0 ) 
	    idx = 0 ;
	into.insertElementAt(key, idx) ;
	return ;
    }

    /**
     * Get the keys of this hashtable, sorted.
     * @param h The hashtable whose Comparable keys are wanted.
     */

    public static Vector sortComparableKeys(Hashtable h) {
	return sortComparableEnumeration(h.keys()) ;
    }

    /**
     * Sort the given Comparable enumeration.
     * @return A sorted vector of Comparable instance.
     */

    public static Vector sortComparableEnumeration(Enumeration e) {
	Vector sorted = new Vector() ;
	while ( e.hasMoreElements() ) {
	    orderedComparableInsert((Comparable) e.nextElement(), sorted) ;
	}
	sorted.trimToSize();
	return sorted ;
    }

    /**
     * Sort the given String array in place.
     * @param array The array of String to sort.
     * @param inplace Sort the array in place if <strong>true</strong>, 
     *    allocate a fresh array for the result otherwise.
     * @return The same array, with string sorted.
     */

    public static String[] sortStringArray(String array[], boolean inplace) {
	String tosort[] = array ;
	if ( ! inplace ) {
	    tosort = new String[array.length] ;
	    System.arraycopy(array, 0, tosort, 0, array.length) ;
	}
	quickSortStringArray(tosort, 0, tosort.length-1) ;
	return tosort ;
    }

    /**
     * Quick sort the given chunk of the array in place.
     * @param array The array to sort.
     * @param lo0 The low bound of the chunk of the array to sort.
     * @param hi0 The high bound of the array to sort.
     */

    static void quickSortCompArray(Comparable array[], int lo0, int hi0) {
	int lo = lo0 ;
	int hi = hi0 ;
	Comparable mid = null ;

	if ( hi0 > lo0 ) {
	    mid = array[(lo0+hi0)/2] ;
	    while (lo <= hi) {
		while ((lo < hi0) && (mid.greaterThan(array[lo])))
		    ++lo ;
		while ((hi > lo0) && (array[hi].greaterThan(mid)))
		    --hi ;
		if ( lo <= hi ) {
		    Comparable tmp = array[lo] ;
		    array[lo]  = array[hi] ;
		    array[hi]  = tmp ;
		    ++lo ;
		    --hi ;
		}
	    }
	    if ( lo0 < hi )
		quickSortCompArray(array, lo0, hi) ;
	    if ( lo < hi0 )
		quickSortCompArray(array, lo, hi0) ;
	}
    }

    public static Comparable[] sortComparableArray(Comparable array[],
						   boolean inplace) 
    {
	Comparable tosort[] =array;
	if ( ! inplace ) {
	    tosort = new Comparable[array.length] ;
	    System.arraycopy(array, 0, tosort, 0, array.length) ;
	}
	quickSortCompArray(tosort, 0, tosort.length-1) ;
	return tosort ;
    }

}

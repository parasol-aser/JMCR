/*******************************************************************************
 * Copyright (c) 2013 University of Illinois
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package edu.tamu.aser.graph;
/*************************************************************************
 *  Compilation:  javac ST.java
 *  Execution:    java ST
 *  
 *  Sorted symbol table implementation using a java.util.TreeMap.
 *  Does not allow duplicate keys.
 *
 *  % java ST
 *
 *************************************************************************/

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *  This class represents an ordered symbol table. It assumes that
 *  the elements are <tt>Comparable</tt>.
 *  It supports the usual <em>put</em>, <em>get</em>, <em>contains</em>,
 *  and <em>delete</em> methods.
 *  It also provides ordered methods for finding the <em>minimum</em>,
 *  <em>maximum</em>, <em>floor</em>, and <em>ceiling</em>.
 *  <p>
 *  The class uses the convention that values cannot be null. Setting the
 *  value associated with a key to null is equivalent to removing the key.
 *  <p>
 *  This implementation uses a balanced binary search tree.
 *  The <em>add</em>, <em>contains</em>, <em>delete</em>, <em>minimum</em>,
 *  <em>maximum</em>, <em>ceiling</em>, and <em>floor</em> methods take
 *  logarithmic time.
 *  <p>
 *  For additional documentation, see <a href="http://introcs.cs.princeton.edu/44st">Section 4.4</a> of
 *  <i>Introduction to Programming in Java: An Interdisciplinary Approach</i> by Robert Sedgewick and Kevin Wayne. 
 *
 */
public class ST<Key extends Comparable<Key>, Value> implements Iterable<Key> {
    private TreeMap<Key, Value> st;

    /**
     * Create an empty symbol table.
     */
    public ST() {
        st = new TreeMap<Key, Value>();
    }

    /**
     * Put key-value pair into the symbol table. Remove key from table if
     * value is null.
     */
    public void put(Key key, Value val) {
        if (val == null) st.remove(key);
        else             st.put(key, val);
    }

    /**
     * Return the value paired with given key; null if key is not in table.
     */
    public Value get(Key key) {
        return st.get(key);
    }

    /**
     * Delete the key (and paired value) from table.
     * Return the value paired with given key; null if key is not in table.
     */
    public Value delete(Key key) {
        return st.remove(key);
    }

    /**
     * Is the key in the table?
     */
    public boolean contains(Key key) {
        return st.containsKey(key);
    }

    /**
     * How many keys are in the table?
     */
    public int size() {
        return st.size();
    }

    /**
     * Return an <tt>Iterator</tt> for the keys in the table.
     * To iterate over all of the keys in the symbol table <tt>st</tt>, use the
     * foreach notation: <tt>for (Key key : st)</tt>.
     */ 
    public Iterator<Key> iterator() {
        return st.keySet().iterator();
    }


    /**
     * Return an <tt>Iterable</tt> for the keys in the table.
     * To iterate over all of the keys in the symbol table <tt>st</tt>, use the
     * foreach notation: <tt>for (Key key : st.keys())</tt>.
     */ 
    public Iterable<Key> keys() {
        return st.keySet();
    }

    /**
     * Return the smallest key in the table.
     */ 
    public Key min() {
        return st.firstKey();
    }

    /**
     * Return the largest key in the table.
     */ 
    public Key max() {
        return st.lastKey();
    }


    /**
     * Return the smallest key in the table >= k.
     */ 
    public Key ceil(Key k) {
        SortedMap<Key, Value> tail = st.tailMap(k);
        if (tail.isEmpty()) return null;
        else return tail.firstKey();
    }

    /**
     * Return the largest key in the table <= k.
     */ 
    public Key floor(Key k) {
        if (st.containsKey(k)) return k;

        // does not include key if present (!)
        SortedMap<Key, Value> head = st.headMap(k);
        if (head.isEmpty()) return null;
        else return head.lastKey();
    }

}

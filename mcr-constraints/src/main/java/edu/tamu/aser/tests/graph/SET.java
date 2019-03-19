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
 *  Compilation:  javac SET.java
 *  Execution:    java SET
 *  
 *  Set implementation using Java's TreeSet library.
 *  Does not allow duplicates.
 *
 *  % java SET
 *  128.112.136.11
 *  208.216.181.15
 *  null
 *
 *************************************************************************/

import java.util.TreeSet;
import java.util.Iterator;
import java.util.SortedSet;



/**
 *  The <tt>SET</tt> class represents an ordered set. It assumes that
 *  the elements are <tt>Comparable</tt>.
 *  It supports the usual <em>add</em>, <em>contains</em>, and <em>delete</em>
 *  methods. It also provides ordered methods for finding the <em>minimum</em>,
 *  <em>maximum</em>, <em>floor</em>, and <em>ceiling</em>.
 *  <p>
 *  This implementation uses a balanced binary search tree.
 *  The <em>add</em>, <em>contains</em>, <em>delete</em>, <em>minimum</em>,
 *  <em>maximum</em>, <em>ceiling</em>, and <em>floor</em> methods take
 *  logarithmic time.
 *  <p>
 *  For additional documentation, see <a href="http://introcs.cs.princeton.edu/44st">Section 4.4</a> of
 *  <i>Introduction to Programming in Java: An Interdisciplinary Approach</i> by Robert Sedgewick and Kevin Wayne.
 *  */

public class SET<Key extends Comparable<Key>> implements Iterable<Key> {
    private TreeSet<Key> set;

    /**
     * Create an empty set.
     */
    public SET() {
        set = new TreeSet<Key>();
    }

    /**
     * Is this set empty?
     */
    public boolean isEmpty() {
        return set.isEmpty();
    }
 
    /**
     * Add the key to this set.
     */
    public void add(Key key) {
        set.add(key);
    }

    /**
     * Does this set contain the given key?
     */
    public boolean contains(Key key) {
        return set.contains(key);
    }

    /**
     * Delete the given key from this set.
     */
    public void delete(Key key) {
        set.remove(key);
    }

    /**
     * Return the number of keys in this set.
     */
    public int size() {
        return set.size();
    }

    /**
     * Return an Iterator for this set.
     */
    public Iterator<Key> iterator() {
        return set.iterator();
    }

    /**
     * Return the key in this set with the maximum value.
     */
    public Key max() {
        return set.last();
    }

    /**
     * Return the key in this set with the minimum value.
     */
    public Key min() {
        return set.first();
    }

    /**
     * Return the smallest key in this set >= k.
     */
    public Key ceil(Key k) {
        SortedSet<Key> tail = set.tailSet(k);
        if (tail.isEmpty()) return null;
        else return tail.first();
    }

    /**
     * Return the largest key in this set <= k.
     */
    public Key floor(Key k) {
        if (set.contains(k)) return k;

        // does not include key if present (!)
        SortedSet<Key> head = set.headSet(k);
        if (head.isEmpty()) return null;
        else return head.last();
    }

    /**
     * Return the union of this set with that set.
     */
    public SET<Key> union(SET<Key> that) {
        SET<Key> c = new SET<Key>();
        for (Key x : this) { c.add(x); }
        for (Key x : that) { c.add(x); }
        return c;
    }

    /**
     * Return the intersection of this set with that set.
     */
    public SET<Key> intersects(SET<Key> that) {
        SET<Key> c = new SET<Key>();
        if (this.size() < that.size()) {
            for (Key x : this) {
                if (that.contains(x)) c.add(x);
            }
        }
        else {
            for (Key x : that) {
                if (this.contains(x)) c.add(x);
            }
        }
        return c;
    }

   /***********************************************************************
    * Test routine.
    **********************************************************************/
    public static void main(String[] args) {
        SET<String> set = new SET<String>();


        // insert some keys
        set.add("www.cs.princeton.edu");
        set.add("www.cs.princeton.edu");    // overwrite old value
        set.add("www.princeton.edu");
        set.add("www.math.princeton.edu");
        set.add("www.yale.edu");
        set.add("www.amazon.com");
        set.add("www.simpsons.com");
        set.add("www.stanford.edu");
        set.add("www.google.com");
        set.add("www.ibm.com");
        set.add("www.apple.com");
        set.add("www.slashdot.com");
        set.add("www.whitehouse.gov");
        set.add("www.espn.com");
        set.add("www.snopes.com");
        set.add("www.movies.com");
        set.add("www.cnn.com");
        set.add("www.iitb.ac.in");


        System.out.println(set.contains("www.cs.princeton.edu"));
        System.out.println(!set.contains("www.harvardsucks.com"));
        System.out.println(set.contains("www.simpsons.com"));
        System.out.println();

        System.out.println("ceil(www.simpsonr.com) = " + set.ceil("www.simpsonr.com"));
        System.out.println("ceil(www.simpsons.com) = " + set.ceil("www.simpsons.com"));
        System.out.println("ceil(www.simpsont.com) = " + set.ceil("www.simpsont.com"));
        System.out.println("floor(www.simpsonr.com) = " + set.floor("www.simpsonr.com"));
        System.out.println("floor(www.simpsons.com) = " + set.floor("www.simpsons.com"));
        System.out.println("floor(www.simpsont.com) = " + set.floor("www.simpsont.com"));
        System.out.println();


        // print out all keys in the set in lexicographic order
        for (String s : set) {
            System.out.println(s);
        }

    }

}

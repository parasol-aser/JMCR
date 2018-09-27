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

//JGraphT

/*************************************************************************
 *  Compilation:  javac Graph.java
 *  Dependencies: ST.java SET.java In.java
 *  
 *  Undirected graph data type implemented using a symbol table
 *  whose keys are vertices (String) and whose values are sets
 *  of neighbors (SET of Strings).
 *
 *  Remarks
 *  -------
 *   - Parallel edges are not allowed
 *   - Self-loop are allowed
 *   - Adjacency lists store many different copies of the same
 *     String. You can use less memory by interning the strings.
 *
 *************************************************************************/

/**
 *  The <tt>Graph</tt> class represents an undirected graph of vertices
 *  with string names.
 *  It supports the following operations: add an edge, add a vertex,
 *  get all of the vertices, iterate over all of the neighbors adjacent
 *  to a vertex, is there a vertex, is there an edge between two vertices.
 *  Self-loops are permitted; parallel edges are discarded.
 *  <p>
 *  For additional documentation, see <a href="http://introcs.cs.princeton.edu/45graph">Section 4.5</a> of
 *  <i>Introduction to Programming in Java: An Interdisciplinary Approach</i> by Robert Sedgewick and Kevin Wayne.
 */
public class Graph {

    // symbol table: key = string vertex, value = set of neighboring vertices
    private ST<Integer, SET<Integer>> st;

    // number of edges
    private int E;

   /**
     * Create an empty graph with no vertices or edges.
     */
    public Graph() {
        st = new ST<Integer, SET<Integer>>();
    }

     /**
     * Number of vertices.
     */
    public int V() {
        return st.size();
    }

   /**
     * Number of edges.
     */
    public int E() {
        return E;
    }

   /**
     * Degree of this vertex.
     */
    public int degree(int v) {
        if (!st.contains(v)) throw new RuntimeException(v + " is not a vertex");
        else return st.get(v).size();
    }

   /**
     * Add edge v-w to this graph (if it is not already an edge)
     */
    public void addEdge(int v, int w) {
        //if (!hasEdge(v, w)) E++;
        if (!hasVertex(v)) addVertex(v);
        if (!hasVertex(w)) addVertex(w);
        st.get(v).add(w);
        //st.get(w).add(v);//let's make it directly graph
    }

   /**
     * Add vertex v to this graph (if it is not already a vertex)
     */
    public void addVertex(int v) {
        if (!hasVertex(v)) st.put(v, new SET<Integer>());
    }


   /**
     * Return the set of vertices as an Iterable.
     */
    public Iterable<Integer> vertices() {
        return st;
    }

   /**
     * Return the set of neighbors of vertex v as in Iterable.
     */
    public Iterable<Integer> adjacentTo(Integer v) {
        // return empty set if vertex isn't in graph
        if (!hasVertex(v)) return new SET<Integer>();
        else               return st.get(v);
    }

   /**
     * Is v a vertex in this graph?
     */
    public boolean hasVertex(int v) {
        return st.contains(v);
    }

   /**
     * Is v-w an edge in this graph?
     */
    public boolean hasEdge(int v, int w) {
        if (!hasVertex(v)) return false;
        return st.get(v).contains(w);
    }

   /**
     * Return a T representation of the graph.
     */
    public String toString() {
        String s = "";
        for (Integer v : st) {
            s += v + ": ";
            for (Integer w : st.get(v)) {
                s += w + " ";
            }
            s += "\n";
        }
        return s;
    }


}

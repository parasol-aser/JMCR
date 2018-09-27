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
 *  Compilation:  javac PathFinder.java
 *  Execution:    java Pathfinder input.txt delimiter source
 *  Dependencies: Queue.java Stack.java Graph.java
 *  
 *  Runs breadth first search algorithm from source s on a graph G.
 *  After preprocessing the graph, can process shortest path queries
 *  from s to any vertex t.
 *
 *  % java PathFinder routes.txt " " JFK
 *  LAX
 *     JFK
 *     ORD
 *     PHX
 *     LAX
 *  distance 3
 *  MCO
 *     JFK
 *     MCO
 *  distance 1
 *  DFW
 *     JFK
 *     ORD
 *     DFW
 *  distance 2
 *
 *************************************************************************/

public class PathFinder {

    // prev[v] = previous vertex on shortest path from s to v
    // dist[v] = length of shortest path from s to v
    private ST<Integer, Integer>  prev = new ST<Integer, Integer>();
    private ST<Integer, Integer> dist = new ST<Integer, Integer>();

    // run BFS in graph G from given source vertex s
    public PathFinder(Graph G, Integer s) {

        // put source on the queue
        Queue<Integer> q = new Queue<Integer>();
        q.enqueue(s);
        dist.put(s, 0);
        
        // repeated remove next vertex v from queue and insert
        // all its neighbors, provided they haven't yet been visited
        while (!q.isEmpty()) {
            int v = q.dequeue();
            for (int w : G.adjacentTo(v)) {
                if (!dist.contains(w)) {
                    q.enqueue(w);
                    dist.put(w, 1 + dist.get(v));
                    //prev.put(w, v);//disable this func
                }
            }
        }
    }

    // is v reachable from the source s?
    public boolean isReachable(Integer v) {
        return dist.contains(v);
    }

    // return the length of the shortest path from v to s
    public int distanceTo(Integer v) {
        if (!dist.contains(v)) return Integer.MAX_VALUE;
        return dist.get(v);
    }

    // return the shortest path from v to s as an Iterable
    public Iterable<Integer> pathTo(Integer v) {
        Stack<Integer> path = new Stack<Integer>();
        while (v != null && dist.contains(v)) {
            path.push(v);
            v = prev.get(v);
        }
        return path;
    }


    public static void main(String[] args) {

    	Graph G = new Graph();
    	
    	G.addEdge(1, 2);
    	G.addEdge(2, 3);
    	G.addEdge(3, 4);
    	
    	G.addEdge(5, 6);
    	G.addEdge(6, 7);
    	G.addEdge(7, 8);

    	G.addEdge(2, 6);

        PathFinder pf = new PathFinder(G, 1);
        
        if(pf.isReachable(5))        
            System.out.println("1->5: YES");
        else
        	System.out.println("1->5: NO");
        
        if(pf.isReachable(8))        
            System.out.println("1->8: YES");
        else
        	System.out.println("1->8: NO");

    }


}

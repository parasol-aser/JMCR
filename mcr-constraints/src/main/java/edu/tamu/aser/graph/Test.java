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

import java.util.Random;

public class Test {
	
	private static int N=10000, M=2;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {


		long ts = System.currentTimeMillis();

		m1();
		
		long te = System.currentTimeMillis();

		long tt = (te-ts)/1000;
		
		System.out.println("Total time for "+N+" nodes: "+tt+"secs");

	}

	private static void m1()
	{
		Graph G = new Graph();
		
		System.out.println("Intra Thread Edges:");				

		for(int k=0;k<M;k++)
		{
			for(int i= N/M*k + 1;i<N/M*(k+1);i++)
			{
				int j = i+1;
				G.addEdge(i, j);
				System.out.print(i+"->"+j+" ");				
			}
			System.out.println();				
		}
		
		int percent = 10;//100 scale
		
		int Ne = N*percent/100;
		Random ran = new Random();
		
		System.out.println("Cross Thread Edges:");				

		for(int k = 0;k<Ne;k++)
		{
			int t1 = ran.nextInt(M);
			int t2 = ran.nextInt(M);
			if(t1!=t2)
			{
				int pos1 = ran.nextInt(N/M);
				int pos2 = ran.nextInt(N/M);
				
				int i1 = t1*N/M+pos1;
				int i2 = t2*N/M+pos2;
				G.addEdge(i1, i2);
				
				System.out.print(i1+"->"+i2+" ");				

			}
		}
		
		System.out.println("\nAnswers:");				

		for(int i=1;i<=N/M;i++)
		{			
			for(int j=N/M+1;j<=N;j++)
			{
				PathFinder pf = new PathFinder(G, i);

				if(pf.isReachable(j));
//					System.out.println(i+" "+j+" --> YES");
//				else 
//					System.out.println(i+" "+j+" --> NO");
			}
		}
	}
	private static void m2()
	{
		ReachabilityEngine engine = new ReachabilityEngine();
		
		System.out.println("Intra Thread Edges:");				

		for(int k=0;k<M;k++)
		{
			for(int i= N/M*k + 1;i<N/M*(k+1);i++)
			{
				int j = i+1;
				engine.addEdge(i, j);
				System.out.print(i+"->"+j+" ");				
			}
			System.out.println();				
		}
		
		int percent = 10;//100 scale
		
		int Ne = N*percent/100;
		Random ran = new Random();
		
		System.out.println("Cross Thread Edges:");				

		for(int k = 0;k<Ne;k++)
		{
			int t1 = ran.nextInt(M);
			int t2 = ran.nextInt(M);
			if(t1!=t2)
			{
				int pos1 = ran.nextInt(N/M);
				int pos2 = ran.nextInt(N/M);
				
				int i1 = t1*N/M+pos1;
				int i2 = t2*N/M+pos2;
				engine.addEdge(i1, i2);
				
				System.out.print(i1+"->"+i2+" ");				

			}
		}
		
		engine.allEdgeAdded();

		System.out.println("\nAnswers:");				

		for(int i=1;i<=N/M;i++)
			for(int j=N/M+1;j<=N;j++)
			{
				engine.canReach(i, j);
				
//				long t1 = System.currentTimeMillis();
//				if(engine.canReach(i, j))
//					System.out.println(i+" "+j+" --> YES");
//				else 
//					System.out.println(i+" "+j+" --> NO");
//				long t2 = System.currentTimeMillis();
//				long t = (t2-t1)/1000;
//				System.out.println(t+"secs");
			}
	}
}

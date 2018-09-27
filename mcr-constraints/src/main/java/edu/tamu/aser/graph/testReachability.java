package edu.tamu.aser.graph;

public class testReachability {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ReachabilityEngine engine = new ReachabilityEngine();
//		int n1 = 1;
//		int n2 = 2;
		engine.addEdge(1, 2);
		engine.addEdge(2, 3);
		if (engine.canReach(1, 3))
			System.out.println("wrong");
		
		if (engine.canReach(2, 3))
			System.out.println("wrong");
	}

}

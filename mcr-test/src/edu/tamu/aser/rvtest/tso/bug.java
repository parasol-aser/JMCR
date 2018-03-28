package edu.tamu.aser.rvtest.tso;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexecution.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class bug {
	
	
    static class Point {
        int x;
        int y;
//        Point(int x, int y) {
//            this.x = x;
//            this.y = y;
//        }
    }
    
	
	
	public static void main(String[] args) {
		
		Point currentPos = new Point();
		
		System.out.println(currentPos.x);
		
//		new Thread() {
//            void f(Point p) {
//                synchronized(this) {}
//                System.out.println(p.x+" "+p.y);
//                if (p.x+1 != p.y) {
//                    System.out.println(p.x+" "+p.y);
//                    System.exit(1);
//                }
//            }
//            @Override
//            public void run() {
//                while (currentPos == null);
//                while (true)
//                    f(currentPos);
//            }
//        }.start();
        
        //the main thread
//        while (true){
//            currentPos = new Point(currentPos.x+1, currentPos.y+1);
  //      	System.out.println("x="+currentPos.x+" "+"y="+currentPos.y);
//        }
    }
	
	@Test
	public void test() throws InterruptedException {
		try {
		
		bug.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}

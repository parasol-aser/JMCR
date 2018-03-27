package edu.tamu.aser.rvtest.pso;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class bugSimplified {
	
	 static int x;
	 static int y;
	 static int z;

	public static void main(String[] args) {
		
		//int a = 0;	
		
		x = 0;
		//x = 1;
		y = 0;
		z = 0;
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				
				if( z==1 ){
					int a = x+1;
					int b = y;
					if (a != b) {
						System.out.println("x=" +x +","+ "y=" + y);
					}
				}
				
				
			}
			

		});
		t2.start();
	
		x = 0;
		y = 0;
		x = 2;
		y = 3;
		z = 1;
		
		try {
			t2.join();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() throws InterruptedException {
		try {
		
		bugSimplified.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}
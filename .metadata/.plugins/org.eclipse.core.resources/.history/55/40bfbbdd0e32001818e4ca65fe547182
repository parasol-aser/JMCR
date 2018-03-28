package edu.tamu.aser.rvtest.pso;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexcution.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class testPSO {
	
	 static int x;
	 static int y;
	 static int z;
	 
	 private static Object lock = new Object();

	public static void main(String[] args) {
		
		//int a = 0;	
		
		x = 0;
		//x = 1;
		y = 0;
		//z = 0;
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				
				//if(z==1){
				synchronized (lock) {
					if (y==1) {
						if(x==0)
							System.out.println("error");
					}
				}
					
				//}
				
				
			}
			

		});
		t2.start();
		
		//z = 1;
		//z = 1;
		synchronized (lock) {
			x = 1;
			y = 1;
			//z = 1;
		}
		
		//b = y;
		//int c = y;
		
		try {
			t2.join();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() throws InterruptedException {
		try {
		
		testPSO.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}
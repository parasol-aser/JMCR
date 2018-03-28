package edu.tamu.aser.rvtest_simple_tests;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexcution.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class NidhuggTest2 {
	static int x = 0;
	static int y = 0;
	
	static Object lock = new Object();
	public static void main() {
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}
		});
		
		t1.start();
		t2.start();
		
		for(int i=0; i<2; i++){
			synchronized (lock) {
				x = 1;
				y = 1;
			}
		}
		
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void test(){
		
	}

}

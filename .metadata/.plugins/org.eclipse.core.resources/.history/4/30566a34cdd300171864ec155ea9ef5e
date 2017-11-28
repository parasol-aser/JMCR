package edu.tamu.aser.rvtest_simple_tests;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class TestLocks {

	private static Object lock = new Object();
	static int x = 0;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				synchronized (lock) {
					x = 1;
				}
			}
		});
		
		t1.start();
		
		int r2 = x;
		synchronized (lock) {
			x = 2;
		}
		
//		int r = x;
		
		try {
			t1.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void test(){
		TestLocks.main(null);
	}

}

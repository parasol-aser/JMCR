package edu.tamu.aser.rvtest_simple_tests;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class TestDividebyZero {
	static Object lock;
	static int x;
	
	static MyThread t;
	
	public static void main(String[] args) throws InterruptedException
	{
		 lock = new Object();
		t = new MyThread();
		t.start();
		synchronized(lock)
		{
//			t.start();
			x++;
			System.out.println(1/x);
		}
		
	}
	
	static class MyThread extends Thread
	{
		public void run()
		{
			//comment the lock can trigger the error
//			synchronized(lock)
//			{
				x++;
//			}
			x=0;
		}
	}
	
	@Test
	public void test() throws InterruptedException {
		try {
			x = 0;
			TestDividebyZero.main(null);
		} catch (Exception e) {
			fail();
		}
	}
}

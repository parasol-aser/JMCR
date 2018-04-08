package edu.tamu.aser.rvtest.omcr;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexecution.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class Example1 {

	private static int x,y;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				int r1 = x;
			}
		});
		
		Thread t2 = new Thread(new Runnable() {

			@Override
			public void run() {
				y = 1;
			}
		});
		
		Thread t3 = new Thread(new Runnable() {

			@Override
			public void run() {
				int r2 = y;
			}
		});
		

		t1.start();
		t2.start();
		t3.start();

		x = 1;

		try {
			t1.join();
			t2.join();
			t3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test() throws InterruptedException {
		try {
			x = 0;
			y = 0;
		Example1.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}

}

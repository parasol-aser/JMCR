package edu.tamu.aser.rvtest.comparisonWithNidhugg;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexecution.JUnit4MCRRunner;
import junit.framework.Assert;

@RunWith(JUnit4MCRRunner.class)
public class Test1 {

	static int x;
	public static void main(String[] args) {
		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				x = 1;
			}
		});

		t1.start();
		x = 1;
		int r = x;

		try {
			t1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() throws InterruptedException {
		try {
			x = 0;
		Test1.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}
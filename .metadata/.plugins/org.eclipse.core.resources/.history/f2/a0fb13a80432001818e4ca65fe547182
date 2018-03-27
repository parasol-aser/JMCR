package edu.tamu.aser.rvtest_simple_tests;

import static org.junit.Assert.*;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.sun.org.apache.bcel.internal.generic.StackInstruction;

import edu.tamu.aser.exploration.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class Example_Reviewer_CAV {

	private static int x;
	public static void main(String[] args) {
		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				x = x+1;
			}
		});
		
		Thread t2 = new Thread(new Runnable() {

			@Override
			public void run() {
				x = x+1;
			}
		});

		t1.start();
		t2.start();

		x = 1;

		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() throws InterruptedException {
		try {
			x = 0;
		Example_Reviewer_CAV.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}
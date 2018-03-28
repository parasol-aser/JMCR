package edu.tamu.aser.rvtest.pso;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexecution.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class testArray {
	
	 
	static int[] arr = new int[2];
	public static void main(String[] args) {
		
		for (int i=0; i<2; i++)
			arr[i]=0;
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				
				for(int i=0; i<2; i++){
					System.out.println("arr["+i+"]="+arr[i]);
				}
				
				
			}
			

		});
		t2.start();
		
		for(int i=0; i<2; i++){
			arr[i] = i;
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
		
		testArray.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}
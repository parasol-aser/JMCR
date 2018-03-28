/**
 * This package is for testing MCR under RMMs
 */
/**
 * @author Alan
 *
 */
package edu.tamu.aser.rvtest.tso;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexcution.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class mix1 {
	 static int x;
	 static int y;
	 static int z;
	 static int b1;
	 static int b2;
	 static int b3;
	public static void main(String[] args) {
		
		//int a = 0;	
		
		x = 0;
		y = 0;
		z = 0;
		
		b1 = 0;
		b2 = 0;
		b3 = 0;
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {			
				y = 1;
				int a = y;
				int b = z;
				if(a==1&&b==0)
					b2 = 1;
			}

		});
		
		Thread t3 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				z = 1;
				int a = z;
				int b = x;
				if(a==1&&b==0)
					b3 = 1;
			}
		});
		
		t2.start();
		t3.start();

		x = 1;
		int a = x;
		int b = y;
		if(a==1&&b==0)
			b1 = 1;
		
		try {
			t2.join();
			t3.join();
			System.out.println("b1= " + b1 + ","+ "b2= "+b2+","+"b3="+b3);
			if(b1==1 && b2==1 && b3==1){
				System.out.println("SC Violation");
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() throws InterruptedException {
		try {
		
		mix1.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}
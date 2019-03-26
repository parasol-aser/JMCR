package edu.tamu.aser.tests.mutual_exclusion;

import static org.junit.Assert.*;

import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnit4MCRRunner.class)
public class Dekker {

	final static int N1 = 1;
	final static int N2 = 1;
	
	public static int flag1;
	public static int flag2;
	public static int turn;
	public static int x;
	public static void main(String[] args) {
		
		flag1 = 0;
		flag2 = 0;
		turn = 1;
		x = 0;
		
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int n1 = 0;
				int n2 = 0;
				flag1 = 1;
				while(flag2==1){
					if(n2++ > N2)break;
					if (turn != 1) {
						flag1 = 0;
						while (turn!=1) {
							if(n1++ > N1)break;
						}
						flag1 = 1;
					}
					else{
						//break;
					}
				}
				
				//critical section
				x = 1;
				if(x != 1)
					System.out.println("error");
				//assert(x==1);
				turn = 2;
				flag1 = 0;
				
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int n1 = 0;
				int n2 = 0;
				
				flag2 = 1;
				while(flag1==1){
					if(n2++ > N2)break;
					if (turn != 2) {
						flag2 = 0;
						while (turn!=2) {
							if(n1++ > N1)break;
						}
						flag2 = 1;
					}
					else {
						//break;
					}
				}
				
				//critical section
				x = 2;
				if(x != 2)
					System.out.println("error");
				//assert(x==2);
				turn = 1;
				flag2 = 0;
				
			}
		});
		
		t1.start();
		t2.start();
		
		try {
			t1.join();
			t2.join();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
	
	
	@Test
	public void test() throws InterruptedException {
		try {
		
		Dekker.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}

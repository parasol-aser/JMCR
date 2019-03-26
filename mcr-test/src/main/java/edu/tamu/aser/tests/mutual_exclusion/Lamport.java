package edu.tamu.aser.tests.mutual_exclusion;

import static org.junit.Assert.*;

import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnit4MCRRunner.class)
public class Lamport {

	final static int N1 = 1;
	final static int N2 = 1;
	final static int N3 = 1;
	final static int N4 = 1;
	
	public static int flag1;
	public static int flag2;
	public static int x, y;
	//public static
	public static int shared;
	public static void main(String[] args) {
		
		flag1 = 0;
		flag2 = 0;
		x = 0;
		y = 0;
		shared = 0;
		
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int n1 = 0;
				int n2 = 0;
				int n3 = 0;
				int n4 = 0;
				
				while(true){
					if(n1++ > N1)break;
					flag1 = 1;
					x = 1;
					if(y != 0){
						flag1 = 0;					
						while (y != 0) {
							if(n2++ > N2)break;
						}
						continue;
					}
					y = 1;
					if(x != 1){
						flag1 = 0;
						while (flag2 >= 1) {
							if(n3++ > N3)break;
						}
						if(y != 1){
							while(y != 0){
								if (n4++ > N4) {
									break;
								}
							}
							continue;
						}
					}
					
					break;
				}
				
				//critical section
				shared = 1;
				//assert(x==1);
				if(shared != 1)
					System.out.println("error");
				y = 0;
				flag1 = 0;
				
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int n1 = 0;
				int n2 = 0;
				int n3 = 0;
				int n4 = 0;
				
				while(true){
					if(n1++ > N1)break;
					flag2 = 1;
					x = 2;
					if(y != 0){
						flag2 = 0;
						
						while (y != 0) {
							if(n2++ > N2)break;
						}
						
						continue;
					}
					y = 1;
					if(x != 2){
						flag2 = 0;
						while (flag2 >= 1) {
							if(n3++ > N3)break;
						}
						if(y != 2){
							while(y != 0){
								if (n4++ > N4) {
									break;
								}
							}
							
							continue;
						}
					}
					
					break;
				}
				
				//critical section
				shared = 2;
				if(shared != 2)
					System.out.println("error");
				//assert(x==2);
				y = 0;
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
		
		Lamport.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}

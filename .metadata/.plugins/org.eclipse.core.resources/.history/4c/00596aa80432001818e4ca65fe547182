package edu.tamu.aser.rvtest.tso;


import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.sun.jndi.url.iiopname.iiopnameURLContextFactory;

import edu.tamu.aser.exploration.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class bakery {
	
	//for state space exposion
//	final static int N1 = 10;
//	final static int N2 = 10;
	
	//for bug exposion
	final static int N1 = 20;
	final static int N2 = 20;
	
	final static int NUM = 2;
	public static int[] enter = new int[NUM];
	public static int[] number = new int[NUM];
	
	public static int x;
	
	public static int max(int[] num){
		int max = 0;
		for(int i=0; i<NUM; i++){
			if(num[i]>max)
				max = num[i];
		}
		return max;
	}
	
	public static void  lock(int i){
		
		int n1 = 0;
		int n2 = 0;
		
		enter[i] = 1;
		number[i] = 1 + max(number);
		enter[i] = 0;
		
		for(int j=0; j<NUM; j++){
			while(enter[j]==1){
				if(n1++>N1)break;
			}
			while(number[j] != 0 && (number[j] < number[i])){
				if (n2++ > N2) {
					break;
				}
			}
			
		}
	}
	
	public static void unlock(int i){
		number[i] = 0;
	}
	
	
	public static void main(String[] args) {
		for(int i=0; i < NUM; i++){
			enter[i] = 0;
			number[i] = 0;
		}
		
		x = 0;
		
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				int i = 0;
				lock(i);
				x = 1;
				if(x!=1){
					fail();
					System.out.println("error");
				}
				
				unlock(i);
				
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int i = 1;
				lock(i);
				x = 2;
//				if(x!=2){
//					System.out.println("error");
//					fail();
//				}
				
				unlock(i);
				
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
		
		bakery.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}

}

package edu.tamu.aser.rvtest.tso;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexcution.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class spinlock {
	
	static int lock = 0;
	static int x;
	
	public synchronized static int CAS(int lock, int old, int _new){
		int temp = lock;
		if(temp==old){
			lock = _new;
			return 1;
		}
		else{
			return 0;
		}
	}
	
	static int lock(int lock){
		while(CAS(lock, 0, 1)==0){
			
		}
		return 1;
	}
	
	static void unlock(int lock){
		lock = 0;
	}
	
	public static void foo1(){
		if(lock(lock)==1){
			x = 1;
			//assert x==1;
			
			unlock(lock);
		}
	}
	
	public static void foo2(){
		if(lock(lock)==1){
			x = 2;
			//assert x==2;
			
			unlock(lock);
		}
	}
	
	public static void main(String[] args) {
		
		x = 0;
		
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				foo1();
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				foo2();
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
		
		spinlock.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}

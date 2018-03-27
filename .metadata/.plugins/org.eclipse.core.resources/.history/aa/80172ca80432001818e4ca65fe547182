package edu.tamu.aser.rvtest_simple_tests;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class MyCounter
{
    public static int counter;
    public final static int MAX=5;
    
	public static void main(String[] args)
    {
       
		counter = 0;
        Thread inc = new Thread(new Runnable() {
		
			@Override
			public void run() {
				for (int i=0; i<MAX; i++){
					counter = counter +1;
				}
			}
		});
        Thread dec = new Thread(new Runnable() {		
			@Override
			public void run() {
				for (int i=0; i<MAX; i++){
					counter = counter - 1;
				}
			}
		});

        inc.start();
        dec.start();
        
        try {
			inc.join();
			dec.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Test
   	public void test() throws InterruptedException {
   		try {
   			MyCounter.main(null);
   		} catch (Exception e) {
   			System.out.println("here");
   			fail();
   		}
   	}
}

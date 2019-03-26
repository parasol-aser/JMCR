package edu.tamu.aser.tests.stringbuffer;

import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.lang.*;


/**
 * tested for the initial events error
 * @author Alan
 *
 */

@RunWith(JUnit4MCRRunner.class)
public class StringBufferTest {

	static java.lang.StringBuffer al1;
    static java.lang.StringBuffer al2;
    public static void main(String args[]) throws InterruptedException {
    	
    	al1 = new java.lang.StringBuffer("H");
    	al2 = new java.lang.StringBuffer("W");
    	
        WorkThread t1 = new WorkThread(0);
        WorkThread t2 =  new WorkThread(1);
        
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        System.out.println(al1.toString());
        
    }
    
    @Test
	public void test() throws InterruptedException {
    	StringBufferTest.main(null);
    }
    
    static class WorkThread extends Thread
    {
//        StringBuffer al1, al2;
        int choice;
        
//        public WorkThread(StringBuffer al1, StringBuffer al2, int choice) {
        public WorkThread(int choice) {
//            this.al1 = al1;
//            this.al2 = al2;
            this.choice = choice;
        }
        
        public void run() {
            //System.out.println("started " + Thread.currentThread());
            System.out.flush();
            switch (choice) {
                case 0:
                    al1.append(al2);
                    break;
                case 1:
                    al1.delete(0, al1.length());
                    break;
            }
        }
    }
}
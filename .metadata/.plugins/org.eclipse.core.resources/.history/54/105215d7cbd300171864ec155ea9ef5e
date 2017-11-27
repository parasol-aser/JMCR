package edu.tamu.aser.rvtest.stringbuffer;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;



/**
 * tested for the initial events error
 * @author Alan
 *
 */

@RunWith(JUnit4MCRRunner.class)
public class StringBufferTest {

	static StringBuffer al1;
    static StringBuffer al2;
    public static void main(String args[]) throws InterruptedException {
    	
    	al1 = new StringBuffer("H");
    	al2 = new StringBuffer("W");
    	
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
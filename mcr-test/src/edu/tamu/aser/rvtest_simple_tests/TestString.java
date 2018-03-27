package edu.tamu.aser.rvtest_simple_tests;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexcution.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class TestString {

	static String al1;
    static String al2;
    public static void main(String args[]) throws InterruptedException {
    	
    	al1 = new String("H");
    	al2 = new String("W");

        WorkThread t1 = new WorkThread(0);
        WorkThread t2 =  new WorkThread(1);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        System.out.println(al1);
        
    }
    
    @Test
	public void test() throws InterruptedException {
    	TestString.main(null);
    }
    
    static class WorkThread extends Thread
    {
        int choice;
        
        public WorkThread(int choice) {
            this.choice = choice;
        }
        
        public void run() {
//            System.out.flush();
            switch (choice) {
                case 0:
                    al1 = al1 + al2;
                    break;
                case 1:
                    al1 = "";
                    break;
            }
        }
    }
}
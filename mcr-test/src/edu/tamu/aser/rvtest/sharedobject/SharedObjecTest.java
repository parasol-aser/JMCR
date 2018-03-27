package edu.tamu.aser.rvtest.sharedobject;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexcution.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class SharedObjecTest {

	static SharedObject sObject = new SharedObject();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		WorkThread t1 = new WorkThread(0);
        WorkThread t2 =  new WorkThread(1);
        t1.start();
        t2.start();
        try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        System.out.println(sObject.al1);
        
    }
    
    @Test
	public void test() throws InterruptedException {
    	SharedObjecTest.main(null);
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
                    sObject.al1 = sObject.al1 + sObject.al2;
                    break;
                case 1:
                	sObject.al1 = "L";
                    break;
            }
        }
	}

}

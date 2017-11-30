package test.reentranlock;

import static org.junit.Assert.fail;

import java.util.concurrent.locks.ReentrantLock;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;

/**
 * Java program to show, how to use ReentrantLock in Java.
 * Reentrant lock is an alternative way of locking
 * apart from implicit locking provided by synchronized keyword in Java.
 *
 * @author  Javin Paul
 */
@RunWith(JUnit4MCRRunner.class)
public class reentranlockTest {

    private static ReentrantLock lock;
    private int count = 0;

     //Locking using Lock and ReentrantLock
     public int getCount() {
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " gets Count: " + count);
            return count++;
        } finally {
            lock.unlock();
        }
     }

     //Implicit locking using synchronized keyword
     public synchronized int getCountTwo() {
            return count++;
     }

    

    public static void main(String args[]) {
        final reentranlockTest counter = new reentranlockTest();
        Thread t1 = new Thread() {

            @Override
            public void run() {
                while (counter.getCount() < 6) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();                    
                     }
                }
            }
        };
      
        Thread t2 = new Thread() {

            @Override
            public void run() {
                while (counter.getCount() < 6) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
      
        t1.start();
        t2.start();
        
        
        try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
      
    }
    
    @Test
	public void test() throws InterruptedException {
		try {
			lock = new ReentrantLock();
			reentranlockTest.main(null);
		} catch (Exception e) {
			System.err.println("here");
			e.printStackTrace();
//			System.out.println(e.getMessage());
			fail();
		}
	}
}
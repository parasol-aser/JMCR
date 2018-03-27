package edu.tamu.aser.rvtest_simple_tests;

import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexcution.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class JavaUtilConcurrentTests {

    private int counter;

    @Test
    public void testReentrantLock() throws InterruptedException {
        counter = 0;
        ReentrantLock lock = new ReentrantLock();
        Thread t1 = new Thread(new LockProtectedIncrement(lock));
        Thread t2 = new Thread(new LockProtectedIncrement(lock));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        assert 2 == counter;
    }

    private final class LockProtectedIncrement implements Runnable {

        private final ReentrantLock lock;

        public LockProtectedIncrement(ReentrantLock lock) {
            this.lock = lock;
        }

        @Override
        public void run() {
            lock.lock();
            try {
                counter++;
            } finally {
                lock.unlock();
            }
        }
    }


}

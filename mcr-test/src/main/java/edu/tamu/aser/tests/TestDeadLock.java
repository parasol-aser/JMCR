package edu.tamu.aser.tests;
import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnit4MCRRunner.class)
public class TestDeadLock {

    private int field = 0;
    private Object mutex1 = new Object();
    private Object mutex2 = new Object();

    public void increment1() {
        synchronized (mutex1) {
            this.field = 1;
            synchronized (mutex2) {

            }
        }
    }

    public void increment2() {
        synchronized (mutex2) {
            /*
             * this is to trigger the deadlock, in the first execution, r reads 1 from line 20
             * then it expects to read the initial value, so lock_mutex2
             * then the first thread gets mutex1 and continue requiring mutex2, a deadlock happens
             */
            int r = this.field;
            synchronized (mutex1) {

            }
        }
    }

    public int getField() {
        return this.field;
    }

    @Test
    public void test() throws InterruptedException {

        final TestDeadLock counter = new TestDeadLock();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                counter.increment1();
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                counter.increment2();
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }
}
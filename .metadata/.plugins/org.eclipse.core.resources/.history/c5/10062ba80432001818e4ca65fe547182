package edu.tamu.aser.rvtest_simple_tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class MonitorsAndFieldModification {

    private int field;

    public synchronized void syncMethodIncrement() {
        this.field++;
    }

    public void syncBlockIncrement() {
        synchronized (this) {
            this.field++;
        }
    }

    public void increment() {
        this.field++;
    }

    public int getField() {
        return this.field;
    }

    @Test
    public void test() throws InterruptedException {

        final MonitorsAndFieldModification counter = new MonitorsAndFieldModification();

        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {
                counter.syncMethodIncrement();
            }
        });

        Thread t2 = new Thread(new Runnable() {

            @Override
            public void run() {
                counter.syncBlockIncrement();
            }
        });

        /*
         * Thread t3 = new Thread(new Runnable() {
         * 
         * @Override public void run() { counter.increment(); } });
         */

        t1.start();
        t2.start();
        /* t3.start(); */
        
        t1.join();
        t2.join();
        /* t3.join(); */

        System.out.println("Value: " + counter.getField());
        assertTrue(counter.getField() == 2);

    }

    public static void main(String[] args) throws InterruptedException {
        new MonitorsAndFieldModification().test();
    }

}

package edu.tamu.aser.rvtest_simple_tests;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexcution.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class ArrayAccessTests {

    @Test
    public void testArrayAccess() throws InterruptedException {
        final int[] ints = new int[1];
        ints[0] = 0;
        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {
                ints[0]++;
                ints[0]++;
                ints[0]++;
            }
        });

        t1.start();

        ints[0]++;
        ints[0]++;

        t1.join();
        System.out.println("result: " + ints[0]);
    }

}

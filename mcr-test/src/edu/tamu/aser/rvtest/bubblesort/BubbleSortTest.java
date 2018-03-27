package edu.tamu.aser.rvtest.bubblesort;

import static org.junit.Assert.*;

import org.junit.Test;
//import org.junit.internal.runners.statements.Fail;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexcution.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class BubbleSortTest {

    @Test
    public void testSortPositiveNumbers() throws Exception {
        sortAndCheck(new int[] { 463, 2435, 89 });
    }

//    @Test
//    public void testSortNegativeNumbers() throws Exception {
//        sortAndCheck(new int[] { -7, -2343, -0, -7890 });
//    }

//    @Test
//    public void testSortMixedNumbers() throws Exception {
//        sortAndCheck(new int[] { -0, 73, -908, -7654 });
//    }

//    @Test
//    public void testSortSameNumbers() throws Exception {
//        sortAndCheck(new int[] { 2, 2, 1, 1 });
//    }

//    @Test
//    public void testSortOneNumber() throws Exception {
//        sortAndCheck(new int[] { 4334, 12 });
//    }

//    @Test
//    public void testSortNoNumbers() throws Exception {
//        sortAndCheck(new int[] {});
//    }

    public void sortAndCheck(int[] array) throws Exception {
        BubbleSort bs = new BubbleSort(array);
        bs.Sort();

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
            	fail();
                System.err.println("(Bug Found: The number at place " + (i) + '(' + array[i] + ')' + ", is bigger then the number at place " + (i + 1)
                        + '(' + array[i + 1] + ").)>");
            }
        }
    }

}

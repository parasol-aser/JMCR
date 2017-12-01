package edu.tamu.aser.rvtest.bubblesort;

/**
 * Created by IntelliJ IDEA.
 * User: Alex
 * Date: 11/06/2003
 * Time: 13:40:01
 * To change this template use Options | File Templates.
 */

/**
 * This Class is responsible on one 'bubble' sort, meaning one run through an
 * array in order to bubble up the highest number in the array
 */
public class OneBubble extends Thread {
    // pointer to the array
    private final int[] arr;

    /**
     * Constructs a thread by pointing to an array of integers and getting the
     * array's size
     * 
     * @param array
     *            The array to be sorted
     * @param sleepingTime
     *            determines how many milliseconds this thread should sleep
     */
    public OneBubble(int[] array) {
        arr = array;
    }

    /**
     * The function that does the thread's work, running on the array once and
     * bubbling up the highest number
     */
    public void run() {
        // running on the whole array once
        /* TO RE-INTRODUCE BUG remove the block synchronization */
//        synchronized (arr) {
            for (int i = 0; i < arr.length - 1; i++) {
                // in case higher number is benieth
                if (arr[i] > arr[i + 1])
                    // lower number
                    // bubbling up the higher number
                    SwapConsecutives(i);
//            }
        }
    }

    // swapping the i-th number with the i + 1 number in this instance array
    private void SwapConsecutives(int i) {
        int temp = arr[i];
        arr[i] = arr[i + 1];
        arr[i + 1] = temp;
    }

}

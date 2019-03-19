package edu.tamu.aser.bubblesort;

/**
 * 
 * Created by IntelliJ IDEA.
 * User: Alex
 * Date: 11/06/2003
 * Time: 13:29:18
 * To change this template use Options | File Templates.
 */

import java.io.IOException;

/**
 * 
 * BubbleSort sorts an array by the Bubble sort algorithm
 */
public class BubbleSort {
  // holds the array to be sorted
  private final int[] arr;

  /**
   * getting the array to be sorted and it's size
   * 
   * @param array The array to be sorted
   */
  public BubbleSort(int[] array) throws IOException {
    arr = array;
  }

  /**
   * Sorting the array by the bubble sort algorithem: Running 'size' times on
   * the array, each time a thread is created that's supposed to bubble up the
   * next biggest number not sorted yet. Since it is unknown which thread may
   * finish 1st, and cause a bug, every thread runs from the biginning to the
   * end of the the array, to correct possible mistakes
   * 
   * @throws Exception
   */
  public void Sort() throws Exception {
    final OneBubble[] bubbelesArr = new OneBubble[arr.length];

    for (int i = 0; i < arr.length; i++)
      bubbelesArr[i] = new OneBubble(arr);

    for (int i = 0; i < arr.length; i++)
      bubbelesArr[i].start();

    for (int i = 0; i < arr.length; ++i)
      bubbelesArr[i].join();
  }

}

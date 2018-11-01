package edu.tamu.aser.results.allocationvector;

/**
 * class TestThread1: Used to run thread which allocates and frees blocks by
 * given AllocationVector object.
 */
public class AllocateAndFreeThread extends Thread {
    /**
     * Reference to class AllocationVector object with which the thread will
     * work.
     */
    private AllocationVector vector = null;

    /**
     * An array to which the resulting allocated blocks indexes will be stored.
     * It's lenght indicates the number of accesses, which to perform to
     * 'vector'.
     */
    private int[] resultBuf = null;

    /**
     * Whether to allocate only (0) or free only (1) or allocate and free (2).
     */
    private int mode;

    /**
     * Constructor: Constructs thread which will work on class AllocationVector
     * object 'vec', to which the thread will perform 'resBuf.length' accesses
     * of allocation and 'resBuf.length' accesses of frees. The resulting
     * allocated blocks indexes will be stored to 'resBuf'.
     * 
     * @param vec
     *                class AllocationVector object on which the thread will
     *                work. resBuf Buffer for resulting allocated blocks
     *                indexes, which also indicates the number of access which
     *                to perform to 'vec'. NOTE:
     */
    public AllocateAndFreeThread(AllocationVector vec, int[] resBuf, int mode) {
        if ((vec == null) || (resBuf == null) || (mode < 0 || mode > 2)) {
            throw new IllegalArgumentException();
        }
        vector = vec;
        resultBuf = resBuf;
    }

    /**
     * Perform 2 * 'resultBuf.length' accesses to 'vector', in the following
     * way: 'resultBuf.length' blocks allocations. 'resultBuf.length' blocks
     * frees. NOTE: If allocation/free block error occurs, function sets
     * resultBuf[0] to -2/-3.
     */
    public void run() {
        try {

            if (mode == 0 || mode == 2) {
                // Allocating 'resultBuf.length' blocks.
                for (int i = 0; i < resultBuf.length; i++) {
                    // TO RE-INTRODUCE BUG uncomment the following code and
                    // remove
                    // the single call that replaces it
                     resultBuf[i] = vector.getFreeBlockIndex();
                     if (resultBuf[i] != -1) {
                     vector.markAsAllocatedBlock(resultBuf[i]);
                     }
                    //resultBuf[i] = vector.getFreeBlockIndexAndMarkAsAllocated();
                }
            }

            if (mode == 1 || mode == 2) {
                // Freeing 'resultBuf.length' blocks.
                for (int i = 0; i < resultBuf.length; i++) {
                    if (resultBuf[i] != -1) {
                        vector.markAsFreeBlock(resultBuf[i]);
                    }
                }
            }
        } catch (Exception e) {
            if (e.getMessage().compareTo("Allocation") == 0) {
                resultBuf[0] = -2;
            } else {
                resultBuf[0] = -3;
            }
        }
    }
}

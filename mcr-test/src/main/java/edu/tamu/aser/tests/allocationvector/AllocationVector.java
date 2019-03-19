package edu.tamu.aser.tests.allocationvector;


/**
 * class AllocationVector: Used to manage allocation and freeing of blocks. BUG
 * DOCUMENTATION: There is a synchronization GAP between the methods
 * "getFreeBlockIndex" and "markAsAllocatedBlock", in which anything can be
 * done.
 */
public class AllocationVector {
    /**
     * Character vector which holds information about allocated and free blocks,
     * in the following way: if vector[i] == 'F' -> i-th block is free. if
     * vector[i] == 'A' -> i-th block is allocated.
     */
    private char[] vector = null;

    /**
     * Constructor: Constructs AllocationVector for 'size' blocks, when all
     * blocks are free.
     * 
     * @param size
     *            Size of AllocationVector.
     */
    public AllocationVector(int size) {
        // Allocating vector of size 'size', when all blocks are assigned to
        // free.
        vector = new char[size];
        for (int i = 0; i < size; i++) {
            vector[i] = 'F';
        }
    }

    /**
     * Returns index of free block, if such exists. If no free block, then -1 is
     * returned.
     * 
     * @return Index of free block if such exists, else -1.
     */
    synchronized public int getFreeBlockIndex() {
        /*
         * Deterministic allocation , different from original, look at jnoise
         * code for original
         */
        int freeBlockIndex = -1;
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] == 'F') {
                freeBlockIndex = i;
                break;
            }
        }
        return freeBlockIndex;
    }

    /**
     * TO RE-INTRODUCE BUG remove this method and call the two methods
     * individually.
     * 
     * @throws Exception
     */
    public int getFreeBlockIndexAndMarkAsAllocated()
            throws Exception {
        synchronized (this) 
        {
            int freeBlockIndex = getFreeBlockIndex();
            if (freeBlockIndex != -1) {
                markAsAllocatedBlock(freeBlockIndex);
            }
            return freeBlockIndex;
        }
    }

    /**
     * Marks i-th block as allocated.
     * 
     * @param i
     *            Index of block to allocate. NOTE: If allocating already
     *            allocated block, then Exception is thrown.
     */
    synchronized public void markAsAllocatedBlock(int i) throws Exception {
        if (vector[i] != 'A') {
            vector[i] = 'A'; // Allocates i-th block.
        } else {
            throw new Exception("Allocation");
        }
    }

    /**
     * Marks i-th block as free.
     * 
     * @param i
     *            Index of block to free. NOTE: If freeing already free block,
     *            then Exception is thrown.
     */
    synchronized public void markAsFreeBlock(int i) throws Exception {
        if (vector[i] != 'F') {
            vector[i] = 'F'; // Frees i-th block.
        } else {
            throw new Exception("Freeing");
        }
    }

}
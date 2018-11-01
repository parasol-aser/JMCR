package edu.tamu.aser.results.allocationvector;

import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnit4MCRRunner.class)
public class AllocationTest {

    public static void main(String[] args) throws Exception {
        AllocationTest allocationTest = new AllocationTest();
        allocationTest.testOneTenthAllocation();
        allocationTest.testOneTenthFreeing();
        allocationTest.testOneTenthAllocationAndFree();
        allocationTest.testHalfAllocation();
        allocationTest.testHalfFreeing();
        allocationTest.testHalfAllocationAndFree();
        allocationTest.testFullAllocation();
        allocationTest.testFullFreeing();
        allocationTest.testFullAllocationAndFree();
    }

    @Test
    public void testOneTenthAllocation() throws Exception {
        allocateAndEnsureNoErrors(40, 2, 2);
    }

    @Test
    public void testOneTenthFreeing() throws Exception {
        freeAndEnsureNoErrors(40, 2, 2);
    }

    @Test
    public void testOneTenthAllocationAndFree() throws Exception {
        allocateAndFreeAndEnsureNoErrors(40, 2, 2);
    }

    @Test
    public void testHalfAllocation() throws Exception {
        allocateAndEnsureNoErrors(8, 2, 2);
    }

    @Test
    public void testHalfFreeing() throws Exception {
        freeAndEnsureNoErrors(8, 2, 2);
    }

    @Test
    public void testHalfAllocationAndFree() throws Exception {
        allocateAndFreeAndEnsureNoErrors(8, 2, 2);
    }

    @Test
    public void testFullAllocation() throws Exception {
        allocateAndEnsureNoErrors(4, 2, 2);
    }

    @Test
    public void testFullFreeing() throws Exception {
        freeAndEnsureNoErrors(4, 2, 2);
    }

    @Test
    public void testFullAllocationAndFree() throws Exception {
        allocateAndFreeAndEnsureNoErrors(4, 2, 2);
    }

    private AllocationVector vector;

    public void allocateAndFreeAndEnsureNoErrors(int vectorSize, int allocationSize, int numThreads) throws Exception {

        vector = new AllocationVector(vectorSize);
        AllocateAndFreeThread[] threads = new AllocateAndFreeThread[numThreads];
        int[][] threadResults = new int[numThreads][allocationSize];

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new AllocateAndFreeThread(vector, threadResults[i], 2);
        }

        for (AllocateAndFreeThread thread : threads) {
            thread.start();
        }

        for (AllocateAndFreeThread thread : threads) {
            thread.join();
        }

        checkForErrors(threadResults);
    }

    public void allocateAndEnsureNoErrors(int vectorSize, int allocationSize, int numThreads) throws Exception {

        vector = new AllocationVector(vectorSize);
        AllocateAndFreeThread[] threads = new AllocateAndFreeThread[numThreads];
        int[][] threadResults = new int[numThreads][allocationSize];

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new AllocateAndFreeThread(vector, threadResults[i], 0);
        }

        for (AllocateAndFreeThread thread : threads) {
            thread.start();
        }

        for (AllocateAndFreeThread thread : threads) {
            thread.join();
        }

        checkForErrors(threadResults);
    }

    public void freeAndEnsureNoErrors(int vectorSize, int allocationSize, int numThreads) throws Exception {

        vector = new AllocationVector(vectorSize);
        AllocateAndFreeThread[] threads = new AllocateAndFreeThread[numThreads];
        int[][] threadResults = new int[numThreads][allocationSize];

        // Perform allocation only to set up data for freeing
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new AllocateAndFreeThread(vector, threadResults[i], 0);
        }

        for (AllocateAndFreeThread thread : threads) {
            thread.start();
        }

        for (AllocateAndFreeThread thread : threads) {
            thread.join();
        }

        checkForErrors(threadResults);

        // Perform freeing only
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new AllocateAndFreeThread(vector, threadResults[i], 1);
        }

        for (AllocateAndFreeThread thread : threads) {
            thread.start();
        }

        for (AllocateAndFreeThread thread : threads) {
            thread.join();
        }

        checkForErrors(threadResults);
    }

    private void checkForErrors(int[][] threadResults) {
        for (int[] threadResult : threadResults) {
            if (threadResult[0] == -2 || threadResult[0] == -3) {
                //Assert.fail("Tried to allocate a block which was already allocated or free a block that was already free");
                System.out.println("Tried to allocate a block which was already allocated or free a block that was already free");

            }
        }
    }

}

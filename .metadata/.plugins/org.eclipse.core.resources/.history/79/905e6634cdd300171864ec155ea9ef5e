package edu.tamu.aser.rvtest_simple_tests;

public class TestSynchronizedMethod extends Thread {

Object obj;

public TestSynchronizedMethod(Object obj){
    this.obj = obj;
}

public synchronized void testPrint() {
    System.out.println("I am sleeping..."
            + Thread.currentThread().getName());
    try {
        Thread.sleep(3000);
        System.out.println("I am done sleeping..."
                + Thread.currentThread().getName());
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}

public void run() {
    ((TestSynchronizedMethod)obj).testPrint();
    System.out.println("I am out..." + Thread.currentThread().getName());
}

public static void main(String[] args) {

    Object obj = new TestSynchronizedMethod(null);

    TestSynchronizedMethod t1 = new TestSynchronizedMethod(obj);
    TestSynchronizedMethod t2 = new TestSynchronizedMethod(obj);

    t1.start();
    t2.start();

}
}
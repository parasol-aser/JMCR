package external.jpf_test_cases.nestedmonitors;

public class Semaphore {

    private int value;

    public Semaphore (int initial) {
        value = initial;
    }

    synchronized public void up() {
        ++value;
        notifyAll();  // should be notify() but does not work in some browsers
    }

    synchronized public void down() throws InterruptedException {
        while (value==0) wait();
        --value;
    }
}

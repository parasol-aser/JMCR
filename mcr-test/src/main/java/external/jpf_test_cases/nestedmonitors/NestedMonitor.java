package external.jpf_test_cases.nestedmonitors;


/* from http://www.doc.ic.ac.uk/~jnm/book/ */
/* Concurrency: State Models & Java Programs - Jeff Magee & Jeff Kramer */
/* has a deadlock */



/*********************SEMABUFFER*****************************/

class  Buffer {
  // protected Object[] buf;
  // protected int in = 0;
  // protected int out= 0;
  // protected int count= 0;
  protected int size;

  Semaphore full; //counts number of items
  Semaphore empty;//counts number of spaces
    public static final int N = 5;

    Buffer(int size) {
      this.size = size;
      // buf = new Object[size];
      full = new Semaphore(0);
      empty= new Semaphore(size);
    }

  synchronized public void put(Object o)
              throws InterruptedException {
    empty.down();
    // buf[in] = o;
    // ++count;
    // in=(in+1) % size;
    full.up();
  }

  synchronized public Object get()
               throws InterruptedException{
    full.down();
    // Object o =buf[out];
    // buf[out]=null;
    // --count;
    // out=(out+1) % size;
    empty.up();
    return (null); //(o);
  }
}


public class NestedMonitor  {
  static int SIZE = 4; /* parameter */
  static Buffer buf;

    public static void main(String [] args) {
        buf = new Buffer(SIZE);

	    new Producer(buf).start();
    	new Consumer(buf).start();
        new Producer(buf).start();
    }
}

/*******************PRODUCER************************/
class Producer extends Thread {

    Buffer buf;

    Producer(Buffer b) {buf = b;}

    public void run() {
      try {
        // int tmp = 0;
        for(int i=0;i<Buffer.N;i++) {
            buf.put(null); // (new Integer(tmp));
            //System.out.println(this + " produced " + tmp);
            //tmp=tmp+1;
        }
      } catch (InterruptedException e){}
    }
}

/********************CONSUMER*******************************/
class Consumer extends Thread {

    Buffer buf;

    Consumer(Buffer b) {buf = b;}

    public void run() {
      try {
          for(int i=0;i<Buffer.N;i++) {
            buf.get(); // int tmp = ((Integer)buf.get()).intValue();
            //System.out.println(this+" consumed "+tmp);
        }
      } catch(InterruptedException e ){}
    }
}

//@The following comments are auto-generated to save options for testing the current file
//@jcute.optionPrintOutput=false
//@jcute.optionLogPath=true
//@jcute.optionLogTraceAndInput=false
//@jcute.optionGenerateJUnit=false
//@jcute.optionExtraOptions=
//@jcute.optionJUnitOutputFolderName=d:\sync\work\cute\java
//@jcute.optionJUnitPkgName=
//@jcute.optionNumberOfPaths=2
//@jcute.optionLogLevel=2
//@jcute.optionLogStatistics=true
//@jcute.optionDepthForDFS=0
//@jcute.optionSearchStrategy=0
//@jcute.optionSequential=false
//@jcute.optionQuickSearchThreshold=100
//@jcute.optionLogRace=true
//@jcute.optionLogDeadlock=true
//@jcute.optionLogException=true
//@jcute.optionLogAssertion=true
//@jcute.optionUseRandomInputs=false

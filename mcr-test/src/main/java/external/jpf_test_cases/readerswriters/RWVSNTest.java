package external.jpf_test_cases.readerswriters;


// An instance of Doug Lea's RWVSN system:
// http://gee.cs.oswego.edu/dl/cpj/classes/


public class RWVSNTest {
    static RWPrinter rwp;

    public static void main (String argv[]) {
        rwp = new RWPrinter();
        
        new Writer(rwp).start();
        new Reader(rwp).start();
        new Writer(rwp).start();
//        new Reader(rwp).start();
    }
}


final class Reader extends Thread {
  protected RWPrinter rwp;

  public Reader(RWPrinter r) { rwp = r;}

  public void run() {
     for(int i=0;i<3;i++) rwp.read();
  } 
}

final class Writer extends Thread {
  protected RWPrinter rwp;
   
  public Writer(RWPrinter r) { rwp = r;}

  public void run() {
    for(int i=0;i<3;i++) rwp.write();
  } 
}

class RWPrinter {
  int numWrite = 0; 

  /**
   *    PRE ReadMutex : numWrite==0;
   */
  protected void read_() {
    assert(numWrite==0);
    //System.out.println("reading");
  }

  protected void write_() {
    numWrite++;
    //System.out.println("writing");
    numWrite--;
  }


  protected int activeReaders_ = 0;     //counts
  protected int activeWriters_ = 0;
  protected int waitingReaders_ = 0;
  // the size of the waiting writers vector serves as its count

  // one monitor holds all waiting readers
  protected Object waitingReaderMonitor_ = this;

  // vector of monitors each holding one waiting writer
  protected Vector waitingWriterMonitors_ = new Vector();

  public void read() { beforeRead(); read_();  afterRead(); }
  public void write() { beforeWrite(); write_(); afterWrite(); }

  protected boolean allowReader() { // call under proper synch
    boolean result = activeWriters_ == 0 || 
      waitingWriterMonitors_.size() == 0;
    return result;
    // error : change the && to an ||
  }

  protected boolean allowWriter() { 
    boolean result = waitingWriterMonitors_.size() == 0 && 
           activeReaders_ == 0 &&
           activeWriters_ == 0;
    return result;
  } 

  protected void beforeRead() {
    synchronized(waitingReaderMonitor_) {
      synchronized(this) { // test condition under synch
        if (allowReader()) {
          ++activeReaders_;
          return;
        }
        else
          ++waitingReaders_;
      }
      try { waitingReaderMonitor_.wait(); } 
      catch (InterruptedException ex) {}
    }
  }

  protected void beforeWrite() {
    Object monitor = new Object();
    synchronized (monitor) {
      synchronized(this) {
        if (allowWriter()) {
          ++activeWriters_;
          return;
        }
        waitingWriterMonitors_.addElement(monitor); // append
      }
      try { monitor.wait(); } catch (InterruptedException ex) {}
    }
  }

  protected synchronized void notifyReaders() { // waken readers
    synchronized(waitingReaderMonitor_) { 
      waitingReaderMonitor_.notifyAll();
    }
    activeReaders_ = waitingReaders_; // all waiters now active
    waitingReaders_ = 0;
  }

  protected synchronized void notifyWriter() { // waken 1 writer
    if (waitingWriterMonitors_.size() > 0) {
      Object oldest = waitingWriterMonitors_.firstElement();
      waitingWriterMonitors_.removeElementAt(0);
      synchronized(oldest) { oldest.notify(); }
      ++activeWriters_;
    }
  }

  protected synchronized void afterRead()  {
    --activeReaders_; 
    if (activeReaders_ == 0)
      notifyWriter(); 
  }


  protected synchronized void afterWrite() { 
    --activeWriters_; 
    if (waitingReaders_ > 0) // prefer waiting readers
      notifyReaders();
    else
      notifyWriter();
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
//@jcute.optionNumberOfPaths=8
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

package external.jpf_test_cases.pipeline;

// "pipeline stop" precedes any "stage return"

public class PipeInttest {
  static Pipeline pipe;
  static boolean stopCalled = false;
  static public void main (String argv[]) {
    pipe = new Pipeline(6);

    // abstract : i with signs
    for (int i=0; i<2; i++) 
      pipe.add(i);

    stopCalled = true;
    pipe.stop();
  }
}

class Pipeline {
  BlockingQueue first;

  Pipeline(int numStages) {
    BlockingQueue in, out;
    first = out  = new BlockingQueue();
    for (int i=0; i<numStages; i++) { 
      in = out;
      out = new BlockingQueue();
      (new Stage(in,out)).start();  
    }
    (new Listener(out)).start(); 
  }

  public void add(int o) { first.add(o); }
  public void stop() { first.stop(); }
}


final class BlockingQueue  {
  int queue = -1;
  public final synchronized int take() {
    int value;

    while ( queue < 0 ) 
      try { wait(); } catch ( InterruptedException ex) {}

    value = queue;
    queue = -1;
    return value;
  }
  public final synchronized void add(int o) {
    queue = o;
    notifyAll();
  }
  public final synchronized void stop() {
    queue = 0;
    notifyAll();
  }
}


final class Stage extends Thread {
  BlockingQueue input, output; 
  public Stage(BlockingQueue in, BlockingQueue out) {
    input = in; output = out;
  }
  /**
   *   POST ProperExit: PipeInttest.stopCalled;
   */
  public void run() {
    int tmp = -1;
    //while (tmp != 0) {
    // error : leaving out the negation
    while (tmp != 0) {  //corrected
      tmp = input.take();
      if (tmp == 0) break; 
      output.add(tmp+1);
    } 
    output.stop();
    assert(PipeInttest.stopCalled);
  } 
}

final class Listener extends Thread {
  BlockingQueue input;
  public Listener(BlockingQueue in) {
    input = in; 
  }
  public void run() {
    int tmp = -1;
    while (tmp != 0) {
      tmp = input.take();
      if (tmp == 0) break; 
      //System.out.println("output is " + tmp);
    } 
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
//@jcute.optionNumberOfPaths=3
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

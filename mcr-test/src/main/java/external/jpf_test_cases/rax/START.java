package external.jpf_test_cases.rax;

class Event{
  int count=0;
  public synchronized void wait_for_event(){
    try{wait();}catch(InterruptedException e){};
  }
  public synchronized void signal_event(){
    count = count + 1;
    notifyAll();
  }
}

class FirstTask extends java.lang.Thread{
  Event event1,event2;
  int count=0;
  public FirstTask(Event e1, Event e2){
    this.event1 = e1; this.event2 = e2;
  }
  public void run(){
    count = event1.count;
    for(int i=0;i<3;i++){
      if (count == event1.count)  
        event1.wait_for_event(); 
      count = event1.count;
      event2.signal_event();
    }
  }
}

class SecondTask extends java.lang.Thread{
  Event event1,event2;
  int count=0;
  public SecondTask(Event e1, Event e2){
    this.event1 = e1; this.event2 = e2;
  }
  public void run(){
    count = event2.count;
    for(int i=0;i<3;i++){    
        event1.signal_event();
        if (count == event2.count)
          event2.wait_for_event();
        count = event2.count;
    }
  }
}

public class START {
  public static void main(String[] args){
    Event event1 = new Event();
    Event event2 = new Event();
    FirstTask  task1 = new FirstTask(event1,event2);
    SecondTask task2 = new SecondTask(event1,event2);
    task1.start(); task2.start();

  }
}
//@The following comments are auto-generated to save options for testing the current file
//@jcute.optionPrintOutput=false
//@jcute.optionLogPath=true
//@jcute.optionLogTraceAndInput=true
//@jcute.optionGenerateJUnit=false
//@jcute.optionExtraOptions=
//@jcute.optionJUnitOutputFolderName=D:\sync\work\cute\java
//@jcute.optionJUnitPkgName=
//@jcute.optionNumberOfPaths=2
//@jcute.optionLogLevel=2
//@jcute.optionDepthForDFS=0
//@jcute.optionSearchStrategy=0
//@jcute.optionSequential=false
//@jcute.optionQuickSearchThreshold=100
//@jcute.optionLogRace=true
//@jcute.optionLogDeadlock=true
//@jcute.optionLogException=true
//@jcute.optionLogAssertion=true
//@jcute.optionUseRandomInputs=false

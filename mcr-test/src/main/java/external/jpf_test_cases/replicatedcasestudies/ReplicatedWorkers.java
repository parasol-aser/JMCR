package external.jpf_test_cases.replicatedcasestudies;

import external.jpf_test_cases.MyRandom;

//
// The replicated workers coordination abstraction consists
// of a collection of 3 kinds of objects:
//
//   a single instance of the ReplicatedWorkers type
//   a single instance of the Coordinator type
//   multiple instances of the Worker type
//
// Instantition of the abstraction requires a user to provide
//   a configuration object
//   work and result collections for pool data
//   work and result computations
//   and the number and granularity of workers
// as parameters to the ReplicatedWorkers constructor.  
//
// The user interacts with the instance of the abstractions by making
// calls on methods of the ReplicatedWorkers class.  We refer to the
// thread making such calls as the "main" thread.  The Worker objects are
// called "worker" threads. 
//
// The architecture of the implementation is as follows:
//
//  picture  of main interacting with workers through coordinator
//
// The role of the coordinator is to control access to shared
// objects of the abstraction, synchronize execution and detect
// termination.  To do this it encapsulates, hides, and controls
// access to all components of abstraction state that can effect
// guard-conditions on wait statements.  A single flat, centralized
// abstraction  is used to eliminate nested-monitor problems and
// decouple pool, "main" and "worker" thread functionality from coordination
// issues.
// 


/* simplified by me */

//package ca.replicatedworkers;

//import ca.replicatedworkers.Collection;
//import ca.replicatedworkers.SynchronizedCollection;
//import ca.replicatedworkers.Configuration;
//import util.StandardCountingSemaphore;
//import util.StandardBarrier;
//import java.util.Vector;

final public class ReplicatedWorkers {
  private Vector workers;
  protected boolean stop;
  protected boolean done;
  protected StandardCountingSemaphore resultLock;
  protected StandardBarrier mainWorkerBarrier;
  protected Collection workPool; 
  protected SynchronizedCollection resultPool;
  protected Coordinator theCoord;
  protected Configuration theConfig;
  protected int numWorkers;
  protected int numItems;

 
// added by me: for checking assertion
  public final int getPoolSize() {return workPool.size();}
  public boolean GlobalDone = false;

  public ReplicatedWorkers(Configuration newConfig,
                           Collection workCollection,
                           Collection resultCollection, 
                           int newNumWorkers, int newNumItems)
  {
//System.out.println("ReplicatedWorkers : starting");
    done = true;
    stop = false;
    numWorkers = newNumWorkers;
    numItems = newNumItems;
    theConfig = newConfig;

    workPool = workCollection;
    resultPool = new SynchronizedCollection(resultCollection);
    resultLock = new StandardCountingSemaphore(1);
    mainWorkerBarrier = new StandardBarrier(numWorkers+1);
    theCoord = new Coordinator(this);


//System.out.println("ReplicatedWorkers : started workers");
    workers = new Vector(numWorkers);
    for ( int i=0; i<numWorkers; i++ ) {
       workers.addElement(new Worker(this));      
    }

//System.out.println("ReplicatedWorkers : end");
  } // end public ReplicatedWorkers

  public final synchronized void destroy()
  {
//System.out.println("ReplicatedWorkers.destroy : start");
    stop = true; 
    mainWorkerBarrier.await();
//System.out.println("ReplicatedWorkers.destroy : end");
  }

  public final synchronized void putWork()
  {
    theCoord.add();
  } // end public void putWork()

  public final synchronized int getResults()
  {
    return resultPool.take();
  } // end public  getResults()


  public final synchronized void execute()
  {
//System.out.println("ReplicatedWorkers.execute : start");

    done = false; 
    mainWorkerBarrier.await();

// Add conditional for asynch execution

    // Detect termination
    //
    // Workers can signal termination based on local computation
    // results.  This will initiate a shutdown (release of all blocked
    // workers).
    //
    // Draining the work pool can also terminate the computation.
    // In which case we need to initiate a shutdown here.
//System.out.println("ReplicatedWorkers.execute : await terminate");
    theCoord.mainAwaitTerminate();
    done = true; 
    theCoord.notifyDoneChange();

    // In any case, we need to make sure that all workers have
    // sucessfully terminated their computations and are awaiting
    // execution.
//System.out.println("ReplicatedWorkers.execute : await finalize");
    mainWorkerBarrier.await();

//System.out.println("ReplicatedWorkers.execute : end");
  } // end public void execute()
}


final class Coordinator 
{
  private int blockedTakers;
  private ReplicatedWorkers memberOf;

  public Coordinator(ReplicatedWorkers instance)
  {
    memberOf = instance;
    blockedTakers = 0;
  } 

  protected final synchronized void notifyTakerChange()
  {
    if ( blockedTakers == memberOf.numWorkers ) 
      notifyAll();
  }

  protected final synchronized void notifyDoneChange()
  {
    if ( memberOf.done ) 
      notifyAll();
  }

  protected final synchronized void notifyPoolChange()
  {
     if (memberOf.workPool.size() >= 0) 
       notifyAll();
  }


  // termination : one of the termination conditions is met and all
  //               workers either block taking work or head to the finalization
  //               point, the main thread must free any blocked threads
  //  conditions : done is true OR
  //               all workers are waiting and 
  //               (config.IsSOme and size<num  OR size==0)

  protected final synchronized void mainAwaitTerminate()
  {   // seeded error: change != to ==
      while ( blockedTakers == memberOf.numWorkers &&
            !memberOf.done ) {
      try { wait(); } catch ( InterruptedException ex) {}
    }
  }

  public final synchronized int take()
  {
//System.out.println("Coordinator.take : start");
     ++blockedTakers;

     if ( memberOf.workPool.size() == 0 && !memberOf.done)
       memberOf.theCoord.notifyTakerChange();

     // Only accept a get request if:
     //    workPool.size() >= 1
     // or
     //    theConfig.isSome() & workPool.size() > 0
     // or
     //    done
     //

     while ( memberOf.workPool.size() == 0 && !memberOf.done) {
//System.out.println("Coordinator.take : in loop");
       try { wait(); } catch ( InterruptedException ex) {}
     }

     // Create a vector of up to num elements
     if (memberOf.done) {
//System.out.println("Coordinator.take : returning empty vector");
       --blockedTakers;
       memberOf.theCoord.notifyTakerChange();

       return 0;

     } else {
//System.out.println("Coordinator.take : loading return vector");
       int returnVal = 0;
       returnVal = memberOf.workPool.take();

       --blockedTakers;
       memberOf.theCoord.notifyTakerChange();

       return returnVal;
     }
  }

  public final synchronized void add()
  {
//System.out.println("Coordinator.add : start");

     memberOf.workPool.add();
     notifyPoolChange();
//System.out.println("Coordinator.add : end");
  }
}


final class Worker implements Runnable {
  private ReplicatedWorkers memberOf; 
  private Thread thisThread;

  public Worker(ReplicatedWorkers rwInstance)
  {
    memberOf = rwInstance;
    thisThread = new Thread(this);
    thisThread.start();
  } // end public Worker()

  public void run() 
  {
    int theWork;
    boolean done = false;

    while (true) {
      memberOf.mainWorkerBarrier.await(); 

      if ( memberOf.stop ) break;

      // Repeatedly get work, process it, put new work
      while (true) {

        // Attempt to get new work
        theWork = memberOf.theCoord.take();

        // An empty work is only returned when the computation
        // has terminated
        if (theWork == 0) break;
//System.out.println("do work");
          // stub for doWork(newWork, theResults)
          // done = false;
          done =  MyRandom.nextRandom(); // external non-determinism


        // Short-circuit the computation if indicated
        if (done) {
          memberOf.done = true;
          memberOf.theCoord.notifyDoneChange();
memberOf.GlobalDone=true; // for checking assertion
        }

        // Process the results
          if (! memberOf.theConfig.isResultNone() ) {
            if ( memberOf.theConfig.isResultExclusive() ) {
              memberOf.resultLock.await();
            }

            // stub for doResults()
//System.out.println("do result");
            // done = false;
            done =  MyRandom.nextRandom(); // external non-determinism

  
            if ( memberOf.theConfig.isResultExclusive() ) {
              memberOf.resultLock.signal();
            }

            // Short-circuit the computation if indicated
            if (done) {
              memberOf.done = true;
              memberOf.theCoord.notifyDoneChange();
memberOf.GlobalDone=true; // for checking assertion
            }

          } else {
            // save up the results
            memberOf.resultPool.add();

          } // end if process some results

        // Put the new work back
        while(MyRandom.nextRandom()) // external non-determinism
          memberOf.theCoord.add();

      } // end work phase loop

      memberOf.mainWorkerBarrier.await(); 
    } // end outer loop
  } // end public void run()

}













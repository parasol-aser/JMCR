package external.jpf_test_cases.replicatedcasestudies;

//package ca.replicatedworkers;

//import ca.replicatedworkers.Collection;
//import ca.replicatedworkers.ReplicatedWorkers;
//import java.util.Vector;
//import java.util.Enumeration;

final class SynchronizedCollection {
  private Collection theCollection;

  public SynchronizedCollection(Collection  c)
  {
    theCollection = c; 
  } 

  public final synchronized int size()
  {
    return theCollection.size();
  }

  public final synchronized int take()
  {
    return theCollection.take();
  }

  public final synchronized void add()
  {
       theCollection.add();
  }
}


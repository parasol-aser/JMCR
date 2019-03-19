package external.jpf_test_cases.replicatedcasestudies;

//package ca.replicatedworkers;

public class Configuration 
{ 
  public final static int EXCLUSIVE = 1, CONCURRENT = 2, NONE = 3;
  private int theResultSemantics;

  public final static int SYNCHRONOUS = 1, ASYNCHRONOUS = 2;
  private int theExecuteSemantics;

  public final static int ALLVALUES = 1, SOMEVALUES = 2;
  private int theSubProblemSemantics;
 
  public Configuration() 
  {
    theResultSemantics = NONE; 
    theExecuteSemantics = SYNCHRONOUS; 
    theSubProblemSemantics = ALLVALUES; 
  }

  public Configuration(int call, int exec, int subprob) 
  {
    theResultSemantics = call; 
    theExecuteSemantics = exec; 
    theSubProblemSemantics = subprob; 
  }

  public boolean isResultExclusive() {return theResultSemantics == EXCLUSIVE;}
  public boolean isResultConcurrent() {return theResultSemantics == CONCURRENT;}
  public boolean isResultNone() {return theResultSemantics == NONE;}

  public boolean isSynchronous() {return theExecuteSemantics == SYNCHRONOUS;}
  public boolean isAsynchronous() {return theExecuteSemantics == ASYNCHRONOUS;}

  public boolean isAll() {return theSubProblemSemantics == ALLVALUES;}
  public boolean isSome() {return theSubProblemSemantics == SOMEVALUES;}
}


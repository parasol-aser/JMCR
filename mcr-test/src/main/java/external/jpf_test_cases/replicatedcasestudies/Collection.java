package external.jpf_test_cases.replicatedcasestudies;

import external.jpf_test_cases.MyRandom;

//import ca.replicatedworkers.Collection;

class Collection
{
  int count; // abstract with signs; zero_pos

  public final int size() {
    return count;
  }

  public final int take()
  { int tmp = 0; 
    if(count==1) { // POS//if(count>0) {
      if(MyRandom.nextRandom())// non-deterministic choice
         count = 0;
      else
         count = 1;//count = count - 1;
      tmp = 1;
    }
    // System.out.println("take count "+count);
    return tmp;
  }

  public final void add()
  {
    count=1; //count = count + 1;
    // System.out.println("add count "+count); 
  }

}



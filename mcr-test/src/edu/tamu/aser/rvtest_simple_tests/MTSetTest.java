package edu.tamu.aser.rvtest_simple_tests;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexecution.JUnit4MCRRunner;
import instrumented.java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Koushik Sen (ksen@cs.uiuc.edu)
 * Date: Dec 29, 2005
 * Time: 10:21:17 AM
 */
@RunWith(JUnit4MCRRunner.class)
public class MTSetTest extends Thread {
    Set s1,s2;
    int c;

    static class SetThread extends Thread{
    	Set s1, s2;
    	int c;
    	public SetThread(Set s1, Set s2,int c) {
            this.s1 = s1;
            this.s2 = s2;
            this.c = c;
        }

        public void run() {
            SimpleObject o1 = new SimpleObject(MyRandom.nextInt(3));
            switch(c){
                case 0:
                    s1.add(o1);
                    break;
                case 1:
                    s1.addAll(s2);
                    break;
                case 2:
                    s1.clear();
                    break;
                case 3:
                    s1.contains(o1);
                    break;
                case 4:
                    s1.containsAll(s2);
                    break;
                case 5:
                    s1.remove(o1);
                    break;
                default:
                    s1.removeAll(s2);
                    break;
            }
        }
    }

    public static void main(String[] args) {
        Set s1 = Collections.synchronizedSet(new HashSet());
        Set s2 = Collections.synchronizedSet(new HashSet());
        (new SetThread(s1,s2,0)).start();
        (new SetThread(s2,s1,1)).start();
        (new SetThread(s1,s2,2)).start();
        (new SetThread(s2,s1,3)).start();
        (new SetThread(s1,s2,4)).start();
        (new SetThread(s2,s1,5)).start();
        (new SetThread(s1,s2,6)).start();
    }
    
    @Test
   	public void test() throws InterruptedException {
    	MTSetTest.main(null);
    }
}
//@The following comments are auto-generated to save options for testing the current file
//@jcute.optionPrintOutput=true
//@jcute.optionLogPath=true
//@jcute.optionLogTraceAndInput=false
//@jcute.optionGenerateJUnit=false
//@jcute.optionExtraOptions=
//@jcute.optionJUnitOutputFolderName=d:\sync\work\cute\java
//@jcute.optionJUnitPkgName=
//@jcute.optionNumberOfPaths=20000
//@jcute.optionLogLevel=1
//@jcute.optionLogStatistics=true
//@jcute.optionDepthForDFS=0
//@jcute.optionSearchStrategy=0
//@jcute.optionSequential=false
//@jcute.optionQuickSearchThreshold=100
//@jcute.optionLogRace=true
//@jcute.optionLogDeadlock=false
//@jcute.optionLogException=true
//@jcute.optionLogAssertion=false
//@jcute.optionUseRandomInputs=false

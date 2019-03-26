package edu.tamu.aser.tests.MTSet;

import edu.tamu.aser.reex.JUnit4MCRRunner;
import external.instrumented.java.util.Collections;
import external.instrumented.java.util.HashSet;
import external.instrumented.java.util.Set;
import external.jpf_test_cases.MyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: Koushik Sen (ksen@cs.uiuc.edu)
 * Date: Dec 29, 2005
 * Time: 10:21:17 AM
 */
@RunWith(JUnit4MCRRunner.class)
public class MTSetTest extends Thread {
    Set s1, s2;
    int c;
//
//    public MTSetTest(Set s1, Set s2, int c) {
//        this.s1 = s1;
//        this.s2 = s2;
//        this.c = c;
//    }

    public void run() {
        SimpleObject o1 = new SimpleObject(MyRandom.nextInt(3000));
        switch (c) {
            case 0:
                s1.add(o1);
                break;
            case 1:
                s1.size();
                break;
            case 2:
                s1.clear();
                break;
            case 3:
                s1.contains(o1);
                break;
            case 4:
                s1.remove(o1);
                break;
            case 5:
                s1.toArray();
                break;
            case 6:
                s1.isEmpty();
                break;
            case 7:
                s1.iterator();
                break;
            case 8:
                s1.addAll(s2);
                break;
            case 9:
                s1.equals(s2);
                break;
            case 10:
                s1.retainAll(s2);
                break;
            case 11:
                s1.containsAll(s2);
                break;
            default:
                s1.removeAll(s2);
                break;
        }
    }

    public static void main(String[] args) {
        Set s1 = Collections.synchronizedSet(new HashSet());
        Set s2 = Collections.synchronizedSet(new HashSet());
        s1.add(new SimpleObject(MyRandom.nextInt(3000)));
        s1.add(new SimpleObject(MyRandom.nextInt(3000)));
        s2.add(new SimpleObject(MyRandom.nextInt(3000)));
        s2.add(new SimpleObject(MyRandom.nextInt(3000)));
        for (int i = 12; i >= 0; i--) {
            MTSetTest mtSetTest = new MTSetTest();
            setField(s1, s2, i, mtSetTest);
            mtSetTest.start();
//            (new MTSetTest(s1, s2, i)).start();
        }
        for (int i = 7; i >=0 ; i--) {
            MTSetTest mtSetTest = new MTSetTest();
            setField(s2, s1, i, mtSetTest);
            mtSetTest.start();
//            (new MTSetTest(s2, s1, i)).start();
        }
    }

    private static void setField(Set s1, Set s2, int i, MTSetTest mtSetTest) {
        mtSetTest.s1 = s1;
        mtSetTest.s2 = s2;
        mtSetTest.c = i;
    }

    @Test
    public void test() throws InterruptedException {
        try {
            MTSetTest.main(null);
        } catch (Exception e) {
            System.out.println("here");
            fail();
        }
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

package omcr.hazelcast;

import java.util.*;
import java.io.*; 
import narada.util.*;

public class hazelcastTest{ 

    public static List<List<Parameter>> col0;
    public static List<List<Parameter>> col1;
    public static List<Parameter> racyParameters0;
    public static List<Parameter> racyParameters1;

    /*
    * Object setter for access 1.
    */

    public static void setter0() {

        List<Parameter> paramList0 = col0.get(0);
        com.hazelcast.map.impl.mapstore.writebehind.WriteBehindQueue par0var0 = null;
        com.hazelcast.map.impl.mapstore.writebehind.WriteBehindQueue par0var1 = (com.hazelcast.map.impl.mapstore.writebehind.WriteBehindQueue)paramList0.get(0).returnStored();


             // Driving the object to required state
            par0var0 = com.hazelcast.map.impl.mapstore.writebehind.WriteBehindQueues.createSafeWriteBehindQueue(par0var1);

        { List<Parameter> temp = new ArrayList<Parameter>();
          temp.addAll(paramList0);
         paramList0.clear(); 
         Parameter<Object> param = new Parameter<Object> (par0var0);
         paramList0.add(param);
         paramList0.addAll(temp);
        }
        { List<Parameter> temp = new ArrayList<Parameter>();
          Parameter<Object> param = new Parameter<Object> (par0var0);
         racyParameters0.set(0, param);
        }
    }

    /*
    * Object setter for access 2.
    */

    public static void setter1() {

        List<Parameter> paramList0 = col1.get(0);
        com.hazelcast.map.impl.mapstore.writebehind.WriteBehindQueue par0var0 = null;
        com.hazelcast.map.impl.mapstore.writebehind.WriteBehindQueue par0var1 = (com.hazelcast.map.impl.mapstore.writebehind.WriteBehindQueue)paramList0.get(0).returnStored();


             // Driving the object to required state
            par0var0 = com.hazelcast.map.impl.mapstore.writebehind.WriteBehindQueues.createSafeWriteBehindQueue(par0var1);

        { List<Parameter> temp = new ArrayList<Parameter>();
          temp.addAll(paramList0);
         paramList0.clear(); 
         Parameter<Object> param = new Parameter<Object> (par0var0);
         paramList0.add(param);
         paramList0.addAll(temp);
        }
        { List<Parameter> temp = new ArrayList<Parameter>();
          Parameter<Object> param = new Parameter<Object> (par0var0);
         racyParameters1.set(0, param);
        }
    }

    /* Impose the constraints on the collected objects*/
    public static void imposeConstraint() { 

        Parameter shared = col1.get(0).get(0);
        col0.get(0).set(0, shared);
    }

    /*
    * Test for the race.
     */

    public static void main(String args[]) { 

        Initializer.initialize(1);
        // Objects collected from run 1 to run 1
        col0 = Initializer.collectObjects(0,0);	
        // Objects collected from run 2 to run 2
        col1 = Initializer.collectObjects(1,1);	


        /* Objects collected from run : 3
         Invocation of method : removeAll
        Line : 27 in SynchronizedWriteBehindQueueTest*/
        racyParameters0 = Initializer.getRacyObjects(2);	

        /* Objects collected from run : 4
         Invocation of method : offer
        Line : 21 in SynchronizedWriteBehindQueueTest*/
        racyParameters1 = Initializer.getRacyObjects(3);	


        imposeConstraint();

        setter0();
        setter1();

        Thread t0 = new Thread0(racyParameters0);
        Thread t1 = new Thread1(racyParameters1);

        t0.start();
        t1.start();

        try { 
            t0.join();
            t1.join();
        }
        catch(Exception e) { }
    }
}

class Thread0 extends Thread {
    List<Parameter> parameters;

    public Thread0(List<Parameter> paramList) {
        this.parameters = paramList;
    }
    public void run() {
        List<Parameter> paramList = parameters;
        com.hazelcast.map.impl.mapstore.writebehind.WriteBehindQueue par1 = 
        		(com.hazelcast.map.impl.mapstore.writebehind.WriteBehindQueue)paramList.get(0).returnStored();
        try {
             // Invocation leading to races
        	par1.removeAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
}


class Thread1 extends Thread {
    List<Parameter> parameters;

    public Thread1(List<Parameter> paramList) {
        this.parameters = paramList;
    }
    public void run() {
        List<Parameter> paramList = parameters;
        com.hazelcast.map.impl.mapstore.writebehind.WriteBehindQueue par1 = (com.hazelcast.map.impl.mapstore.writebehind.WriteBehindQueue)paramList.get(0).returnStored();
        java.lang.Object par2 = (java.lang.Object)paramList.get(1).returnStored();
        try {
             // Invocation leading to races
            par1.offer(par2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
}


package omcr.colt;

import java.util.*;
import java.io.*; 
import narada.util.*;
public class coltTest{ 

//    public static List<List<Parameter>> col0;
//    public static List<List<Parameter>> col1;
    public static List<Parameter> racyParameters0;
    public static List<Parameter> racyParameters1;

    /* Impose the constraints on the collected objects*/
    public static void imposeConstraint() { 

        Parameter racy = racyParameters0.get(0);
        racyParameters1.set(0, racy);
    }

    /*
    * Test for the race.
     */

    public static void main(String args[]) { 

        Initializer.initialize(0);


        /* Objects collected from run : 1
         Invocation of method : sortedElements
        Line : 40 in DynamicBin1DSequentialTest*/
        racyParameters0 = Initializer.getRacyObjects(0);	

        /* Objects collected from run : 2
         Invocation of method : setFixedOrder
        Line : 34 in DynamicBin1DSequentialTest*/
        racyParameters1 = Initializer.getRacyObjects(1);	


        imposeConstraint();

//    	racyParameters0 = new ArrayList<>();
//    	racyParameters1 = new ArrayList<>();
//    	racyParameters0.add(1, null);
//    	racyParameters0.add(2, null);
//    	racyParameters1.add(1, null);
//    	racyParameters1.add(2, null);
    	

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
        hep.aida.bin.DynamicBin1D par1 = (hep.aida.bin.DynamicBin1D)paramList.get(0).returnStored();
        try {
             // Invocation leading to races
            par1.sortedElements();
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
        hep.aida.bin.DynamicBin1D par1 = (hep.aida.bin.DynamicBin1D)paramList.get(0).returnStored();
        boolean par2 = (boolean)paramList.get(1).returnStoredBoolean();
        try {
             // Invocation leading to races
            par1.setFixedOrder(par2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
}


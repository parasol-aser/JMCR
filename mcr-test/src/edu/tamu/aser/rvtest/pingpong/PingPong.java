package edu.tamu.aser.rvtest.pingpong;
/**
 *@author Golan
 * 17/10/2003
 * 11:59:43
 *@version 1.0
 */


import java.io.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexecution.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
/**
 *
 */
public class PingPong {


    private static BuggedProgram bug;


    private static DataOutputStream out;


    private static int threadsNumber;


    /**
     *
     * @param threadsNumber
     */
//    private PingPong(DataOutputStream output, int threadsNumber) {
//        this.out = output;
//        this.threadsNumber = threadsNumber;
//        this.bug = new BuggedProgram(output, threadsNumber);
//    }


//    public PingPong() {
//    	
//    }
    
    public void doWork() {
        String newLine = System.getProperty("line.separator");
        try {
            out.writeBytes("Number Of Threads: " + this.threadsNumber + " Number Of Bugs: ");
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        this.bug.doWork();
    }


    public static void main(String[] args) {
        File output = new File("output.txt");

        DataOutputStream out = null;
        try {
            FileOutputStream os = new FileOutputStream(output);
            out = new DataOutputStream(os);
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
        }
        try {

            String newLine = System.getProperty("line.separator");
            out.writeBytes("In this file you will find the number of the bug appearances " +
                    "accordingly to the number of threads that the " +
                    "bugged program utilized with:" + newLine + newLine);

            out.writeBytes("Few Threads: " + newLine + newLine);
            PingPong fewThreads = new PingPong();
            setup(out, 5); //initialize the value since it can't have constructor
            fewThreads.doWork();
//            out.writeBytes(newLine + "************************************" + newLine + newLine);
//            out.writeBytes("Average Threads: " + newLine + newLine);
//            ProgramRunner averageThreads = new ProgramRunner(out, 40);
//            averageThreads.doWork();
//            out.writeBytes(newLine + "************************************" + newLine + newLine);
//            out.writeBytes("A Lot Of Threads: " + newLine + newLine);
//            ProgramRunner aLotOfThreads = new ProgramRunner(out, 120);
//            aLotOfThreads.doWork();
//            out.writeBytes(newLine + "************************************" + newLine + newLine);

        } catch (IOException e) {
            e.printStackTrace(System.err);
        }


    }
    private static void setup(DataOutputStream out2, int i) {
		// TODO Auto-generated method stub
		out = out2;
		threadsNumber = i;
		bug = new BuggedProgram(out, threadsNumber);
	}


	@Test
  	public void test() {
  	   PingPong.main(new String[]{});
    }

}

package edu.tamu.aser.listeners;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import edu.tamu.aser.ExploreSeedInterleavings;
import edu.tamu.aser.config.Configuration;
import edu.tamu.aser.trace.Trace;
import edu.tamu.aser.reex.Scheduler;
import edu.tamu.aser.scheduling.filtering.LocationBasedPreemptionFilter;
import edu.tamu.aser.scheduling.filtering.SchedulingFilter;

/**
 * Listener for collecting stats for the ISSTA 2011 paper on Change-Aware
 * Preemption Prioritization.
 */
public class PaperStatsListener extends ExplorationStatsListener {

    private static final String FAILURE_NOT_DETECTED = "\\failureNotDetected{";
    private static final String TIMEOUT_OCCURED = "\\timeoutOccured{";
    private static final String CLOSE_BRAC = "}";
    private static final String EMPTY_STRING = "";
    private static final String STATS_STRING = "\\newcommand{\\%s%s%s}{%s%s%s}";
    private static final String DOT = ".";
    private HashMap<String, String> testToExampleMap;
    private String example;
    private String mode;

    public PaperStatsListener() {
        this.testToExampleMap = new HashMap<String, String>();
        this.testToExampleMap.put("airline.AirlineTest", "Airline");
        this.testToExampleMap.put("allocationvector.AllocationTest", "Allocation");
        this.testToExampleMap.put("bubblesort.BubbleSortTest", "BubbleSort");
        this.testToExampleMap.put("deadlock.Deadlock", "Deadlock");
        this.testToExampleMap.put("org.apache.commons.lang.HashCodeTest", "Lang");
        this.testToExampleMap.put("org.apache.mina.core.TestFilterWrite", "Mina");
        this.testToExampleMap.put("org.apache.commons.pool.impl.TestGenericKeyedObjectPool", "PoolThree");
        this.testToExampleMap.put("TestMemoryAwareMap", "Groovy");
        this.testToExampleMap.put("boundedbuffer.BoundedBuffer", "BoundedBuffer");
        
        this.testToExampleMap.clear();
        this.example = "Test";
        this.mode = "Basic";
    }

    @Override
    public void startingExploration(String name) {
        super.startingExploration(name);
        example = this.testToExampleMap.get(name.substring(0, name.lastIndexOf(DOT)));
        if (example == null) {
            File currentDir = new File(DOT);
            String currentDirPath = null;
            try {
                currentDirPath = currentDir.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
                // Should not happen
                System.exit(2);
            }
            if (currentDirPath.contains("pool-107")) {
                example = "PoolOne";
            } else if (currentDirPath.contains("pool-120")) {
                example = "PoolTwo";
            } else if (currentDirPath.contains("pool-x")) {
                example = "PoolFour";
            } else {
                example = "Test";
            }
        }
        SchedulingFilter schedulingFilter = Scheduler.getSchedulingFilter();
        mode = EMPTY_STRING;
        if (schedulingFilter instanceof LocationBasedPreemptionFilter) {
            mode = ((LocationBasedPreemptionFilter) schedulingFilter).getMode().getAbbrv();
        }
//        SchedulingStrategy schedulingStrategy = Scheduler.getSchedulingStrategy();
//        if (schedulingStrategy instanceof CAPPStrategy || schedulingStrategy instanceof RandomCAPPStrategy) {
//            mode = ((CAPPStrategy) schedulingStrategy).getMode().getAbbrv();
//        }
    }

    @SuppressWarnings("unused")
    private void printStats(String prefix, String postfix) {
        System.out.println();
        System.out.println(String.format(STATS_STRING, example, mode, "Schedules", prefix, numSchedules, postfix));
        System.out.println(String.format(STATS_STRING, example, mode, "Choices", prefix, numChoices, postfix));
        System.out.println(String.format(STATS_STRING, example, mode, "MaxChoices", prefix, maxDepth, postfix));
        System.out.println(String.format(STATS_STRING, example, mode, "Events", prefix, numEvents, postfix));
        System.out.println(String.format(STATS_STRING, example, mode, "MaxEvents", prefix, maxNumEvents, postfix));
        System.out.println(String.format(STATS_STRING, example, mode, "Threads", prefix, numThreads, postfix));
        System.out.println(String.format(STATS_STRING, example, mode, "MaxThreads", prefix, maxNumThreads, postfix));
        System.out.println(String.format(STATS_STRING, example, mode, "Time", prefix, System.currentTimeMillis() - startTime, postfix));
    }

    @Override
    public void completedExploration() {
        super.completedExploration();
//        if (failureDetected) {
//            printStats(EMPTY_STRING, EMPTY_STRING);
//        } else if (timeoutOccurred) {
//            printStats(TIMEOUT_OCCURED, CLOSE_BRAC);
//        } else {
//            printStats(FAILURE_NOT_DETECTED, CLOSE_BRAC);
//        }

        String fileName = "ConstraintsProfile";
        File dir_fileName = new File(fileName);
        if(!dir_fileName.exists()){
            dir_fileName.mkdir();
        }
        
        String c_name = null;
        if (Configuration.class_name == null) {
            c_name = Trace.appname;
        }
        else
            c_name = Configuration.class_name;
        
        fileName += "/" + c_name;        
        if (Configuration.Optimize){
            fileName = fileName+ "_opt";
        }        
        FileWriter fWriter;
        try {           
            System.out.println("Writing the #reads, #constraints, time to file " + fileName);            
            fWriter = new FileWriter(fileName);
            BufferedWriter bWriter = new BufferedWriter(fWriter);           
            bWriter.write(ExploreSeedInterleavings.output);
            bWriter.close();
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Can't open file");
            e.printStackTrace();
        }
        
    }

}

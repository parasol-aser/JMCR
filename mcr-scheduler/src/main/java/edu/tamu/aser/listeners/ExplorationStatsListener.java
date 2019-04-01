package edu.tamu.aser.listeners;

import java.util.Calendar;
import java.util.List;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;

import edu.tamu.aser.instrumentation.MCRProperties;
import edu.tamu.aser.runtime.RVRunTime;
import edu.tamu.aser.scheduling.events.EventDesc;
import edu.tamu.aser.scheduling.strategy.ChoiceType;
import edu.tamu.aser.scheduling.strategy.ThreadInfo;

public class ExplorationStatsListener extends ExplorationListenerAdapter {

    private static final String NUMBER_OF_THREADS = "NUMBER OF THREADS: ";
    private static final String FAILURE_DETECTED_MESSAGE = "A failure was detected during this exploration!";
    private static final String EXPLORATION_TIME = "EXPLORATION TIME: ";
    private static final String EXPLORING = "\n\nEXPLORING: ";
    private static final String MAX_NUM_THREADS_IN_A_SCHEDULE = "MAX NUM THREADS IN A SCHEDULE: ";
    private static final String MAX_NUM_EVENTS_IN_A_SCHEDULE = "MAX NUM EVENTS IN A SCHEDULE: ";
    private static final String NUMBER_OF_EVENTS = "NUMBER OF EVENTS: ";
    private static final String MAX_CHOICE_DEPTH = "MAX CHOICE DEPTH: ";
    private static final String NUMBER_OF_CHOICES = "NUMBER OF CHOICES: ";
    private static final String NUMBER_OF_SCHEDULES = "NUMBER OF SCHEDULES: ";
    private static final String EXPLORATION_STATS_FOOTER = "=================================================";
    private static final String EXPLORATION_STATS_HEADER = "=============== EXPLORATION STATS ===============";

    private static final long TIMEOUT_ONE_HOUR_IN_MSEC = 24*1000L * 60L * 60L;   //24h

    private final static Long timeoutValue = Long.parseLong(MCRProperties.getInstance().getProperty(MCRProperties.EXPLORATION_TIMEOUT_KEY,
            "" + TIMEOUT_ONE_HOUR_IN_MSEC));

    protected long numSchedules;
    protected long numChoices;
    protected long numEvents;
    protected long currentDepth;
    protected long maxDepth;
    protected long currentNumThreads;
    protected long maxNumThreads;
    protected long currentNumEvents;
    protected long maxNumEvents;
    protected long startTime;
    protected boolean failureDetected;
    protected long numThreads;
    protected boolean timeoutOccurred = false;

    
    
    protected void startTimeoutTask() {
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                timeoutOccurred = true;
                completedSchedule(null);   // update stats
                completedExploration();    // print stats
                System.exit(124);
            }

        }, timeoutValue);
    }

    @Override
    public void startingExploration(String name) {
        numSchedules = 0;
        numChoices = 0;
        numEvents = 0;
        currentDepth = 0;
        maxDepth = 0;
        currentNumThreads = 0;
        maxNumThreads = 0;
        currentNumEvents = 0;
        maxNumEvents = 0;
        numThreads = 0;
        System.out.println(EXPLORING + name + String.format(": %tT", Calendar.getInstance()));
        failureDetected = false;
        startTimeoutTask();
        startTime = System.currentTimeMillis();
    }

    @Override
    public void startingSchedule() {
        currentDepth = 0;
        currentNumThreads = 0;
        currentNumEvents = 0;
        numSchedules++;
        if (numSchedules % 1000 == 0) {
            System.out.println("Progress: Completed " + numSchedules + " schedules!");
        }
    }

    @Override
    public void beforeForking(ThreadInfo childThread) {
        currentNumThreads++;
    }

    @Override
    public void afterEvent(EventDesc eventDesc) {
        numEvents++;
        currentNumEvents++;
    }

    @Override
    public void makingChoice(SortedSet<? extends Object> choices, ChoiceType choiceType) {
        numChoices++;
        currentDepth++;
    }

    @Override
    public void completedSchedule(List<Integer> choicesMade) {
        maxDepth = maxDepth < currentDepth ? currentDepth : maxDepth;
        numThreads += currentNumThreads;
        maxNumThreads = maxNumThreads < currentNumThreads ? currentNumThreads : maxNumThreads;
        maxNumEvents = maxNumEvents < currentNumEvents ? currentNumEvents : maxNumEvents;
    }

    @Override
    public void completedExploration() {
//        System.out.println("\nEXPLORATION COMPLETE: Exploration was performed with respect to classes in the following packages:");
//        System.out.println(EXPLORATION_STATS_FOOTER);
//        for (String pckg : Instrumentor.packagesThatWereInstrumented) {
//            System.out.print(pckg.replace("/", ".") + ";");
//        }
        System.out.println("\n");
        System.out.println(EXPLORATION_STATS_HEADER);
        System.out.println(NUMBER_OF_SCHEDULES + numSchedules);
//        System.out.println(NUMBER_OF_CHOICES + numChoices);
//        System.out.println(MAX_CHOICE_DEPTH + maxDepth);
//        System.out.println(NUMBER_OF_EVENTS + numEvents);
//        System.out.println(MAX_NUM_EVENTS_IN_A_SCHEDULE + maxNumEvents);
//        System.out.println(NUMBER_OF_THREADS + numThreads);
//        System.out.println(MAX_NUM_THREADS_IN_A_SCHEDULE + maxNumThreads);
        System.out.println(EXPLORATION_TIME + getDurationString(System.currentTimeMillis() - startTime));
        if (failureDetected) {
            System.out.println(FAILURE_DETECTED_MESSAGE);
        }
        if (timeoutOccurred) {
            System.out.println("A timeout has occurred after " + getDurationString(timeoutValue));
        }
        System.out.println(EXPLORATION_STATS_FOOTER);
        
//        
//        System.out.println("Races: "+MCRTest.races.size()+"\n"+MCRTest.races);
//        System.out.println("NPEs: "+JUnit4MCRRunner.npes.size()+"\n"+JUnit4MCRRunner.npes);

    }

    @Override
    public void failureDetected(String errorMsg, List<Integer> choicesMade) {
        completedSchedule(choicesMade);
        failureDetected = true;
        System.out.flush();
        System.err.flush();
        System.err.println("\n!!! FAILURE DETECTED DURING EXPLORATION OF SCHEDULE #" + numSchedules + ": " + ((errorMsg == null) ? "" : errorMsg));
        System.err.println("The following trace triggered this error:");
        System.err.println(errorMsg);
        
//        completedExploration();
//        System.exit(-1);
       
//        System.err.println(MCRProperties.SCHEDULING_STRATEGY_KEY + "=" + ReproScheduleStrategy.class.getName());
//        System.err.println(MCRProperties.SCHEDULING_REPRO_CHOICES_KEY + "=" + choicesMade + "\n");
        
        for (int i = 0; i < RVRunTime.failure_trace.size() ; i++) {
                System.err.println("       " + RVRunTime.failure_trace.get(i));
        }
        System.err.flush();
        completedExploration();
    }

    private String getDurationString(long milliSecs) {
        
        long secs = milliSecs / 1000;
        long mins = secs / 60;
        long hours = mins / 60;
        secs = secs % 60;
        mins = mins % 60;
        
        long milli = milliSecs - secs *1000;
        
        return String.format("%d:%02d:%02d  + %02d milli sec", hours, mins, secs, milli);
        
    }

}

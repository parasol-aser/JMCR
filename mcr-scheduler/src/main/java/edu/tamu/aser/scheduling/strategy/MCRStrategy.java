package edu.tamu.aser.scheduling.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.SortedSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.tamu.aser.StartExploring;
import edu.tamu.aser.config.Configuration;
import edu.tamu.aser.instrumentation.RVGlobalStateForInstrumentation;
import edu.tamu.aser.trace.Trace;
import edu.tamu.aser.trace.TraceInfo;
import edu.tamu.aser.runtime.RVRunTime;
import edu.tamu.aser.scheduling.events.EventType;

public class MCRStrategy extends SchedulingStrategy {

	private Queue<List<String>> toExplore;
	public static List<Integer> choicesMade;
	public static List<String> schedulePrefix = new ArrayList<String>();
    private static Trace currentTrace;
	private boolean notYetExecutedFirstSchedule;
	private final static int NUM_THREADS = 10;
	private volatile static ExecutorService executor;
    private ThreadInfo previousThreadInfo;
    public static final Boolean fullTrace = false;  //default

	private int count;
	public MCRStrategy() {
		count = 0;
	}


	/**
	 * before the execution
	 */
	@Override
	public void startingExploration() {
		this.toExplore = new ConcurrentLinkedQueue<List<String>>();
		this.notYetExecutedFirstSchedule = true;
		MCRStrategy.choicesMade = new ArrayList<Integer>();
		MCRStrategy.schedulePrefix = new ArrayList<String>();

		RVRunTime.currentIndex = 0;
		executor = Executors.newFixedThreadPool(NUM_THREADS);	

	}

	/**
	 * called before a new schedule starts
	 */
	@Override
	public void startingScheduleExecution() {
		List<String> prefix = this.toExplore.poll();
		if (!MCRStrategy.choicesMade.isEmpty()) {   // when not empty
			MCRStrategy.choicesMade.clear();
			MCRStrategy.schedulePrefix = new ArrayList<String>();
			assert prefix != null;
			MCRStrategy.schedulePrefix.addAll(prefix);
//			for (String choice : prefix) {
//				MCRStrategy.schedulePrefix.add(choice);
//			}
		}
		
		RVRunTime.currentIndex = 0;
		RVRunTime.failure_trace.clear();
		initTrace();
		
        previousThreadInfo = null;
	}
	
    public static Trace getTrace() {
        return currentTrace;
    }
    
    /* problem here
    * in the first execution, the initialized trace will be used by the aser-engine project
    * however, in the first initialization, the trace hasn't been complete yet.
    */
	private void initTrace() {
       RVRunTime.init();
       TraceInfo traceInfo = new TraceInfo(
                RVGlobalStateForInstrumentation.variableIdSigMap,
                new HashMap<Integer, String>(), 
                RVGlobalStateForInstrumentation.stmtIdSigMap,
                RVRunTime.threadTidNameMap);
       traceInfo.setVolatileAddresses(RVGlobalStateForInstrumentation.instance.volatilevariables);
       currentTrace = new Trace(traceInfo);
	}

	/**
	 * generate new schedules from the trace by this execution
	 */
	public void completedScheduleExecution() {
		this.notYetExecutedFirstSchedule = false;

		Vector<String> prefix = new Vector<String>();
		for (String choice : MCRStrategy.schedulePrefix) {
			prefix.add(choice);
		}

		if (Configuration.DEBUG) {
		    System.out.print("<< Exploring trace executed along causal schedule " + count + ": ");
	        count++;
	        System.err.println(choicesMade);
	        System.out.print("\n");
        }

		//executeMultiThread(trace, prefix);
		
		/*
		 * after executing the program along the given prefix
		 * then the model checker will analyze the trace generated 
		 * to computer more possible interleavings
		 */
		executeSingleThread(prefix);
	}
	
	/**
	 * here creates a runnable object and it can then run the method 
	 * @param prefix
	 */

	private void executeSingleThread(Vector<String> prefix) {
	    
	    currentTrace.getTraceInfo().updateIdSigMap( RVGlobalStateForInstrumentation.stmtIdSigMap );   //solving the first trace initialization problem
	    
		StartExploring causalTrace = new StartExploring(currentTrace, prefix, this.toExplore);
		Thread causalTraceThread = new Thread(causalTrace);
		causalTraceThread.start();
		try {
			causalTraceThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
    private void executeMultiThread(Trace trace, Vector<String> prefix) {

		StartExploring causalTrace = new StartExploring(trace, prefix,
				this.toExplore);
		StartExploring.executorsCount.increase();
		MCRStrategy.executor.submit(causalTrace);
	}

	@Override
	public boolean canExecuteMoreSchedules() {
		boolean result = (!this.toExplore.isEmpty())
				|| this.notYetExecutedFirstSchedule;
		if (!result) {
			while (StartExploring.executorsCount.getValue() > 0) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			result = (!this.toExplore.isEmpty())
					|| this.notYetExecutedFirstSchedule;
			return result;
		} else {
			return true;
		}

	}

	/**
	 * choose the next statement to execute
	 * this function needs more inspection
	 */
	@Override
	public Object choose(SortedSet<? extends Object> objectChoices, ChoiceType choiceType)
	{
		/*
		 * Initialize choice
		 */
		int chosenIndex = 0;
		Object chosenObject = null;

		//for the rest events, executed in random schedule
		if (MCRStrategy.schedulePrefix.size() > RVRunTime.currentIndex) {
			/*
			 * Make the choice to be made according to schedule prefix
			 */
			// chosenIndex = MCRStrategy.schedulePrefix
			// .get(this.currentIndex);
			chosenIndex = getChosenThread(objectChoices, RVRunTime.currentIndex);
			chosenObject = getChosenObject(chosenIndex, objectChoices);
			
			if (Configuration.DEBUG) {
			    if (chosenObject != null) 
                System.out.println(RVRunTime.currentIndex + ":" + chosenObject.toString());
            }
			
			if (chosenObject == null) {
			    
			    //one case that can cause this is due to the wait event
			    //wait has no corresponding schedule index, it has to be announced 
			    //chose the wait to execute, the wait is trying to acquire the semaphore
				for (Object objectChoice : objectChoices) {
					ThreadInfo threadInfo = (ThreadInfo) objectChoice;
					if (threadInfo.getEventDesc().getEventType() == EventType.WAIT) {
						return threadInfo;
					}
				}
			    
			    //what if the chosenObject is still null??
			    //it might not correct
//			    if (chosenObject == null) {
//		            chosenIndex = 0;
//		            while (true) {
//		                chosenObject = getChosenObject(chosenIndex, objectChoices);
//
//		                if(choiceType.equals(ChoiceType.THREAD_TO_FAIR)
//		                        && chosenObject.equals(previousThreadInfo))
//		                {
//		                    //change to a different thread
//		                }
//		                else
//		                    break;
//		                chosenIndex++;
//		            }
//		        }
//		        MCRStrategy.choicesMade.add(chosenIndex);
//
//		        this.previousThreadInfo = (ThreadInfo) chosenObject;
//                return chosenObject;
            }
			
		}
		
		//it might be that the wanted thread is blocked, waiting to be added to the paused threads
		if (chosenObject == null) {
			chosenIndex = 0;
			while (true) {
			    chosenObject = getChosenObject(chosenIndex, objectChoices);
		          
                if(choiceType.equals(ChoiceType.THREAD_TO_FAIR)
                        && chosenObject.equals(previousThreadInfo))
                {
                    //change to a different thread
                }
                else 
                    break;
                chosenIndex++;
                
            }
			
		}
		MCRStrategy.choicesMade.add(chosenIndex);
		this.previousThreadInfo = (ThreadInfo) chosenObject;
		
		return chosenObject;
	}

	@Override
	public List<Integer> getChoicesMadeDuringThisSchedule() {
		return MCRStrategy.choicesMade;
	}
	
	
	/**
	 * chose a thread object based on the index
	 * return -1 if not found
	 * @param objectChoices set of object choices
	 * @param index the given index
	 * @return return the index of chosen thread object
	 */
	private int getChosenThread(SortedSet<? extends Object> objectChoices, int index) {
		String name = MCRStrategy.schedulePrefix.get(index).split("_")[0];
		long tid = -1;
		for (Entry<Long, String> entry : RVRunTime.threadTidNameMap.entrySet()) {
			if (name.equals(entry.getValue())) {
				tid = entry.getKey();
				break;
			}
		}

		Iterator<? extends Object> iter = objectChoices.iterator();
		int currentIndex = -1;
		while (iter.hasNext()) {
			++currentIndex;
			ThreadInfo ti = (ThreadInfo) iter.next();
			if (ti.getThread().getId() == tid) {
				return currentIndex;
			}
		}

		return -1;
	}
}

package edu.tamu.aser.scheduling.strategy;

import com.google.common.collect.ImmutableSortedSet;
import edu.tamu.aser.runtime.RVPORRunTime;
import edu.tamu.aser.scheduling.MCRProperties;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class RVPORStrategy extends SchedulingStrategy {

    
    private static HashMap<Integer,ImmutableSortedSet> 
        backTrackSetMap = new HashMap<Integer,ImmutableSortedSet>();
	/**
	 * Work queues
	 */
    public static Queue<List<Integer>> toExplore;
    public static Queue<List<Integer>> toExploreNext;
	/**
	 * Current schedule choices
	 */
	public static List<Integer> choicesToMake;

	public static List<Integer> choicesMade;

	public static List<String> schedulePrefix = new ArrayList<String>();

	/**
	 * Current index in the prefix
	 */
	public static int currentIndex = 0;

	/**
	 * Additional state for termination check
	 */
	private boolean notYetExecutedFirstSchedule;

	public volatile static ExecutorService executor;

	public final static boolean fullTrace;

    /**
     * Preemption info
     */
    private static final int MAX_NUM_PREEMPTIONS;
    private static int currentMaxPreemptions;
    private static int currentNumPreemptions;
    protected ThreadInfo previousThreadInfo;
    
	static {
		fullTrace = Boolean.parseBoolean(MCRProperties.getInstance()
				.getProperty(MCRProperties.RV_CAUSAL_FULL_TRACE, "true"));
		
        MAX_NUM_PREEMPTIONS = Integer.parseInt(MCRProperties.getInstance().getProperty(MCRProperties.PREEMPTION_BOUND_KEY, Integer.MAX_VALUE + ""));
        if (MAX_NUM_PREEMPTIONS == Integer.MAX_VALUE) {
            System.out.println("\nWARNING: no value specified for property \"" +
                    MCRProperties.PREEMPTION_BOUND_KEY + "\"");
            System.out.println("WARNING: performing unbounded search, " +
                    "exploration may not terminate if there are an infinite number of schedules\n");
        }
	}

	@Override
	public void startingExploration() {
	    this.toExplore = new LinkedList<List<Integer>>();
        this.toExploreNext = new LinkedList<List<Integer>>();

        this.choicesMade = new ArrayList<Integer>();
        this.choicesToMake = new ArrayList<Integer>();

		this.notYetExecutedFirstSchedule = true;
		RVPORStrategy.currentIndex = 0;

        this.currentMaxPreemptions = 0;

	}

	@Override
	public void startingScheduleExecution() {
	    
        if (!this.choicesMade.isEmpty()) {
            this.choicesMade.clear();
            this.choicesToMake = this.toExplore.poll();
        }
		RVPORStrategy.currentIndex = 0;
        this.currentNumPreemptions = 0;
        previousThreadInfo = null;
        
		backTrackSetMap.clear();
		
		RVPORRunTime.init();
	}

	@Override
	public void completedScheduleExecution() {

        if (this.toExplore.isEmpty() && !this.toExploreNext.isEmpty()) {
            
            this.currentMaxPreemptions++;
            if (currentMaxPreemptions <= MAX_NUM_PREEMPTIONS)
                this.toExplore.addAll(this.toExploreNext);
            
            this.toExploreNext.clear();
        }
        this.notYetExecutedFirstSchedule = false;
        
        System.out.println(choicesMade);
	}

	@Override
	public boolean canExecuteMoreSchedules() {
	    
        return !(this.toExplore.isEmpty() && this.toExploreNext.isEmpty()) || this.notYetExecutedFirstSchedule;
	}

	@Override
	public Object choose(SortedSet<? extends Object> objectChoices,
			ChoiceType choiceType) {
	    
	    backTrackSetMap.put((++currentIndex), ImmutableSortedSet.copyOf(objectChoices));
	    	    
	    
	       /*
         * Initialize choice
         */
        int chosenIndex = 0;
        Object chosenObject = null;
        
        
        boolean preemptionPossible = this.previousThreadInfo != null && objectChoices.contains(this.previousThreadInfo)
                && choiceType.equals(ChoiceType.THREAD_TO_SCHEDULE);
        
        
        
        if (this.choicesToMake.size() > this.choicesMade.size()
                && this.currentNumPreemptions<=currentMaxPreemptions
                ) {
            /*
             * Make the choice to be made
             */
            chosenIndex = this.choicesToMake.get(this.choicesMade.size());
        }

        chosenObject = getChosenObject(chosenIndex, objectChoices);

            while(chosenObject==null)
            {
                chosenObject = getChosenObject(--chosenIndex, objectChoices);
            }
//            int choiceIndex = chosenIndex;
//            if (currentNumPreemptions < currentMaxPreemptions || !preemptionPossible) {
//
//            } else {
//                /*
//                 * We have reached the preemption limit. Explore the
//                 * non-preempting choice now and add the rest to toExploreNext.
//                 */
//                for (Iterator<? extends Object> iterator = objectChoices.iterator(); iterator.hasNext(); choiceIndex++) {
//                    Object objectChoice = iterator.next();
//                    if (objectChoice.equals(this.previousThreadInfo)) {
//                        chosenIndex = choiceIndex;
//                        chosenObject = objectChoice;
//                    } else {
//                        List<Integer> choicesToExploreNext = new ArrayList<Integer>(this.choicesMade);
//                        choicesToExploreNext.add(choiceIndex);
//                        this.toExploreNext.add(choicesToExploreNext);
//                    }
//                }
//            }
            
//            ThreadInfo threadInfo=  (ThreadInfo) chosenObject;
//            long threadInfo_tid = threadInfo.getThread().getId();
//            
//	    int conflictIndex = RVPORRunTime.checkConflict(threadInfo_tid);    
//	    
//	    
//	    if(conflictIndex>=0)
//	    {
//
//	    }
	    
	    //continue the current thread	    
	    
	    //how to get E??
	    


		this.choicesMade.add(chosenIndex);

        /*
         * Track preemptions
         */
        if (preemptionPossible && !chosenObject.equals(previousThreadInfo)) {
            this.currentNumPreemptions++;
        }

        /*
         * Remember the chosen thread
         */
        if (choiceType.equals(ChoiceType.THREAD_TO_SCHEDULE)) {
            this.previousThreadInfo = (ThreadInfo) chosenObject;
        }
				
		return chosenObject;
	}

	public static void addBackTrack(int conflictIndex)
	{
        //add backtrack point
        //to explore: 
        
        if(conflictIndex>choicesToMake.size())
                //||currentIndex<choicesToMake.size()
                
        {
            
            //if (currentNumPreemptions < MAX_NUM_PREEMPTIONS) 
            {
                
                //Check the insertion point -- previous
                //if is the preempted thread, then add to toExploreNext, otherwise toExplore
                
            ImmutableSortedSet choices = backTrackSetMap.get(conflictIndex);
            if(conflictIndex==1)
            {
                for (int i=1; i<choices.size();i++)
                {
                    List<Integer> choicesToExplore = new ArrayList<Integer>();
                    choicesToExplore.add(i);
                    toExplore.add(choicesToExplore);
                }
            }
            else
            {
                ImmutableSortedSet choices_pre = backTrackSetMap.get(conflictIndex-1);
                Object[] choices_pre_array = choices_pre.toArray();
                int choiceIndex_pre = choicesMade.get(conflictIndex-2);
                long tid2_pre =((ThreadInfo) choices_pre_array[choiceIndex_pre]).getThread().getId();
                String tid_name2_pre = RVPORRunTime.getThreadCanonicalName(tid2_pre);
                
                Object[] choices_array = choices.toArray();
                int choiceIndex = choicesMade.get(conflictIndex-1);
                long tid2 = ((ThreadInfo) choices_array[choiceIndex]).getThread().getId();
                String tid_name2 = RVPORRunTime.getThreadCanonicalName(tid2);
                
                int index = 0;
                
                for (Iterator<? extends Object> iterator = choices.iterator(); iterator.hasNext(); index++) {
                    Object objectChoice = iterator.next();

                    long tid = ((ThreadInfo)  objectChoice).getThread().getId();
                    String tid_name = RVPORRunTime.getThreadCanonicalName(tid);

                    //the current thread
                    if(RVPORRunTime.getThreadCanonicalName(Thread.currentThread().getId()).equals(tid_name))
                    //if(!tid_name.equals(tid_name2))//not the chosen thread
                    {
                        
                        List<Integer> choicesToExploreNext = new ArrayList<Integer>(choicesMade.subList(0, conflictIndex-1));
                        choicesToExploreNext.add(index);
                        

                        if(tid_name2.equals(tid_name2_pre))
                        {

                            toExploreNext.add(choicesToExploreNext);
                        }
                        else
                        {
                            toExplore.add(choicesToExploreNext);
                        }
                        

                    }
                }
                }
            }
        }
	}
	@Override
	public List<Integer> getChoicesMadeDuringThisSchedule() {
		return this.choicesMade;
	}
}

package edu.tamu.aser.scheduling.strategy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.SortedSet;

//import edu.tamu.aser.scheduling.strategy.ChoiceType;
import edu.tamu.aser.scheduling.MCRProperties;

/**
 * Performs iterative context bounding as described in the original CHESS paper.
 * 
 * @author Vilas Jagannath <vbangal2@illinois.edu>
 * 
 */
public class IterativeContextBoundingStrategy extends SchedulingStrategy {

    /**
     * Work queues
     */
    protected Queue<List<Integer>> toExplore;
    protected Queue<List<Integer>> toExploreNext;

    /**
     * Current schedule choices
     */
    protected List<Integer> choicesToMake;
    protected List<Integer> choicesMade;

    /**
     * Preemption info
     */
    private static final int MAX_NUM_PREEMPTIONS;
    private int currentMaxPreemptions;
    private int currentNumPreemptions;
    protected ThreadInfo previousThreadInfo;

    /**
     * Additional state for termination check
     */
    private boolean notYetExecutedFirstSchedule;

    static {
        MAX_NUM_PREEMPTIONS = Integer.parseInt(MCRProperties.getInstance().getProperty(MCRProperties.PREEMPTION_BOUND_KEY, Integer.MAX_VALUE + ""));
        if (MAX_NUM_PREEMPTIONS == Integer.MAX_VALUE) {
            System.out.println("\nWARNING: no value specified for property \"" + MCRProperties.PREEMPTION_BOUND_KEY + "\"");
            System.out.println("WARNING: performing unbounded search, exploration may not terminate if there are an infinite number of schedules\n");
        }
    }

    @Override
    public void startingExploration() {
        this.toExplore = new LinkedList<List<Integer>>();
        this.toExploreNext = new LinkedList<List<Integer>>();

        this.choicesMade = new ArrayList<Integer>();
        this.choicesToMake = new ArrayList<Integer>();

        this.currentMaxPreemptions = 0;

        this.notYetExecutedFirstSchedule = true;
    }

    @Override
    public void startingScheduleExecution() {
        if (!this.choicesMade.isEmpty()) {
            this.choicesMade.clear();
            this.choicesToMake = this.toExplore.poll();
        }
        this.currentNumPreemptions = 0;
        this.previousThreadInfo = null;
    }

    @Override
    public void completedScheduleExecution() {
        if (this.toExplore.isEmpty() && !this.toExploreNext.isEmpty()) {
            this.currentMaxPreemptions++;
            if (currentMaxPreemptions <= MAX_NUM_PREEMPTIONS) {
                this.toExplore.addAll(this.toExploreNext);
            }
            this.toExploreNext.clear();
        }
        this.notYetExecutedFirstSchedule = false;
    }

    @Override
    public boolean canExecuteMoreSchedules() {
        return !(this.toExplore.isEmpty() && this.toExploreNext.isEmpty()) || this.notYetExecutedFirstSchedule;
    }


    @Override
    public Object choose(SortedSet<? extends Object> objectChoices, ChoiceType choiceType) {
        /*
         * Initialize choice and preemption possibility
         */
        int chosenIndex = 0;
        Object chosenObject = null;
        boolean preemptionPossible = this.previousThreadInfo != null && objectChoices.contains(this.previousThreadInfo)
                && choiceType.equals(ChoiceType.THREAD_TO_SCHEDULE);

        if (this.choicesToMake.size() > this.choicesMade.size()) {
            /*
             * Make the choice to be made
             */
            chosenIndex = this.choicesToMake.get(this.choicesMade.size());
            chosenObject = getChosenObject(chosenIndex, objectChoices);
        } else {
            /*
             * Make new choices
             */
            int choiceIndex = chosenIndex;
            if (currentNumPreemptions < currentMaxPreemptions || !preemptionPossible) {
                /*
                 * We are below the preemption limit or preemption is not
                 * possible. Explore the first choice now and add the rest to
                 * toExplore.
                 */
                chosenObject = getChosenObject(chosenIndex, objectChoices);
                for (choiceIndex = chosenIndex + 1; choiceIndex < objectChoices.size(); choiceIndex++) {
                    List<Integer> choicesToExplore = new ArrayList<Integer>(this.choicesMade);
                    choicesToExplore.add(choiceIndex);
                    this.toExplore.add(choicesToExplore);
                }
            } else {
                /*
                 * We have reached the preemption limit. Explore the
                 * non-preempting choice now and add the rest to toExploreNext.
                 */
                for (Iterator<? extends Object> iterator = objectChoices.iterator(); iterator.hasNext(); choiceIndex++) {
                    Object objectChoice = iterator.next();
                    if (objectChoice.equals(this.previousThreadInfo)) {
                        chosenIndex = choiceIndex;
                        chosenObject = objectChoice;
                    } else {
                        List<Integer> choicesToExploreNext = new ArrayList<Integer>(this.choicesMade);
                        choicesToExploreNext.add(choiceIndex);
                        this.toExploreNext.add(choicesToExploreNext);
                    }
                }
            }
        }
        
//        while(chosenObject==null)
//        {
//            chosenObject = getChosenObject(--chosenIndex, objectChoices);
//        }
        
        
        /*
         * Update made choices
         */
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

        /*
         * Return the chosen object
         */
        return chosenObject;
    }

    @Override
    public List<Integer> getChoicesMadeDuringThisSchedule() {
        return this.choicesMade;
    }

}

package edu.tamu.aser.scheduling.strategy;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;


/**
 * Just picks the first schedulable thread and only allows one schedule.
 * 
 * @author Vilas Jagannath <vbangal2@illinois.edu>
 * 
 */
public class DefaultStrategy extends SchedulingStrategy {

    private boolean performedExecution;

    private int choiceCount;

    public DefaultStrategy() {
        startingExploration();
    }

    @Override
    public void startingExploration() {
        this.performedExecution = false;
        this.choiceCount = 0;
    }

    @Override
    public void startingScheduleExecution() {
    }

    @Override
    public void completedScheduleExecution() {
        this.performedExecution = true;
    }

    @Override
    public boolean canExecuteMoreSchedules() {
        return !performedExecution;
    }

    @Override
    public Object choose(SortedSet<? extends Object> objectChoices, ChoiceType choiceType) {
        choiceCount++;
        return objectChoices.iterator().next();
    }

    @Override
    public List<Integer> getChoicesMadeDuringThisSchedule() {
        Integer[] choices = new Integer[choiceCount];
        for (int i = 0; i < choices.length; i++) {
            choices[i] = 0;
        }
        return Arrays.asList(choices);
    }

}

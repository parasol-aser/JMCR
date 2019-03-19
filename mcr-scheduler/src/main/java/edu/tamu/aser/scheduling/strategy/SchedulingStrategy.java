package edu.tamu.aser.scheduling.strategy;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

/**
 * Abstract class that should be extended to specify a scheduling strategy that
 * is to be using during exploration.

 */
public abstract class SchedulingStrategy {

    /**
     * This method is called when a new exploration is started.
     */
    public abstract void startingExploration();

    /**
     * This method is called when a new schedule execution is started.
     */
    public abstract void startingScheduleExecution();

    /**
     * This method is called when a schedule execution is completed.
     */
    public abstract void completedScheduleExecution();

    /**
     * This method is called to decide whether more schedules should be
     * explored.
     * 
     * @return <code>true</code> if more schedules exist, <code>false</code>
     *         otherwise.
     */
    public abstract boolean canExecuteMoreSchedules();

    /**
     * This is the core of the {@link SchedulingStrategy}. This method is called
     * to choose one of the given {@link Object}s. The type of choice is
     * specified by the {@link ChoiceType}.
     * 
     * @param objectChoices
     *            {@link SortedSet} of the {@link Object}s to choose from.
     * @param choiceType
     *            the type of choice that is being made
     * @return the chosen {@link Object}.
     */
    public abstract Object choose(SortedSet<? extends Object> objectChoices, ChoiceType choiceType);

    /**
     * Returns the choices made until now during the execution of the current
     * schedule. This is useful for capturing and reproducing certain schedules.
     * 
     * @return {@link List} of choices made until now during the execution of
     *         this schedule.
     */
    public abstract List<Integer> getChoicesMadeDuringThisSchedule();

    /**
     * Helper method for retrieving the chosen object from the sorted set of
     * object choices given the chosen index
     * 
     * @param chosenIndex
     * @param objectChoices
     * @return retrieves the chosen object from the sorted set of choices given
     *         the chosen index
     */
    protected Object getChosenObject(int chosenIndex, SortedSet<? extends Object> objectChoices) {
        for (Iterator<? extends Object> iterator = objectChoices.iterator(); iterator.hasNext();) {
            Object choice = iterator.next();
            if (chosenIndex == 0) {
                return choice;
            } else {
                chosenIndex--;
            }
        }
        return null;
    }

}

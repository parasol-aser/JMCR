package edu.tamu.aser.listeners;

import java.util.List;
import java.util.SortedSet;

import edu.tamu.aser.scheduling.events.EventDesc;
import edu.tamu.aser.scheduling.events.EventType;
import edu.tamu.aser.scheduling.strategy.ChoiceType;
import edu.tamu.aser.scheduling.strategy.ThreadInfo;

/**
 * Interface for listeners that want to observe various stages of the
 * exploration.
 */
public interface ExplorationListener {

    public void startingExploration(String name);

    public void startingSchedule();

    /**
     * Fired before the {@link Thread} represented by the given childThread
     * {@link ThreadInfo} is going to be forked by the current {@link Thread}.
     * 
     * @param childThread
     *            {@link ThreadInfo} of the {@link Thread} that is about to be
     *            forked by the current {@link Thread}.
     */
    public void beforeForking(ThreadInfo childThread);

    /**
     * Fired before an event described by the given {@link EventDesc} takes
     * place. Events are one of the types listed in {@link EventType}.
     * 
     * @param eventDesc
     *            description of the event that is about to occur.
     */
    public void beforeEvent(EventDesc eventDesc);

    /**
     * Fired after an event described by the given {@link EventDesc} has taken
     * place. Events are one of the types listed in {@link EventType}.
     * 
     * @param eventDesc
     *            description of the event that has occured.
     */
    public void afterEvent(EventDesc eventDesc);

    /**
     * Fired when a choice point is encountered.
     * 
     * @param objectChoices
     *            the {@link Object}s that are going to be chosen from.
     * @param choiceType
     *            the {@link ChoiceType}.
     */
    public void makingChoice(SortedSet<? extends Object> objectChoices, ChoiceType choiceType);

    /**
     * Fired when a choice has been made (from a choice point).
     * 
     * @param choice
     *            the choice made.
     */
    public void choiceMade(Object choice);

    /**
     * Fired when a schedule has been completed.
     * 
     * @param choicesMade
     *            the list of choices made during the schedule.
     */
    public void completedSchedule(List<Integer> choicesMade);

    public void completedExploration();

    public void failureDetected(String errorMsg, List<Integer> choicesMade);

}

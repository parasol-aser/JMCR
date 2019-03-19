package edu.tamu.aser.listeners;

import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;

import edu.tamu.aser.reex.Scheduler;
import edu.tamu.aser.scheduling.events.EventDesc;
import edu.tamu.aser.scheduling.strategy.ChoiceType;
import edu.tamu.aser.scheduling.strategy.ThreadInfo;

public class ExplorationDebugListener extends ExplorationListenerAdapter {

    private static final String CHOICES_MADE_DURING_SCHEDULE = "CHOICES MADE DURING SCHEDULE :";
    private static final String CHOICE_MADE = "CHOICE MADE: ";
    private static final String COMMA = ", ";
    private static final String CHOOSING = "Choosing :";
    private static final String LIVE = "LIVE: [ ";
    private static final String CHOICES = "CHOICES: [ ";
    private static final String END_BRACKET = "] ";
    private static final String BEFORE_FORKING_THREAD = "BEFORE FORKING THREAD: ";
    private static final String FROM = " FROM: ";
    private static final String AFTER = "AFTER: ";
    private static final String COLON = ":";
    private static final String BEFORE = "BEFORE: ";

    @Override
    public void startingExploration(String name) {
        System.out.println("============================== STARTING EXPLORATION ==============================");
    }

    @Override
    public void startingSchedule() {
        System.out.println("==================== STARTING SCHEDULE ====================");
    }

    @Override
    public void makingChoice(SortedSet<? extends Object> choices, ChoiceType choiceType) {
        System.out.println("=============== CHOICE POINT INFO ===============");
        System.out.println(LIVE);
        for (Entry<Thread, ThreadInfo> liveThreadEntry : Scheduler.getLiveThreadInfos().entrySet()) {
            ThreadInfo liveThreadInfo = liveThreadEntry.getValue();
            System.out.println(liveThreadInfo + COMMA);
        }
        System.out.println(END_BRACKET);
        System.out.println(CHOOSING + choiceType);
        System.out.println(CHOICES);
        for (Object choice : choices) {
            System.out.println(choice + COMMA);
        }
        System.out.println(END_BRACKET);
        System.out.println("=================================================");
    }

    @Override
    public void choiceMade(Object choice) {
        System.out.println(CHOICE_MADE + choice);
        System.out.println("=================================================");
    }

    @Override
    public void completedSchedule(List<Integer> choicesMade) {
        System.out.println("==================== COMPLETED SCHEDULE ====================");
        System.out.println(CHOICES_MADE_DURING_SCHEDULE + choicesMade);

    }

    @Override
    public void completedExploration() {
        System.out.println("============================== COMPLETED EXPLORATION ==============================");
    }

    @Override
    public void beforeForking(ThreadInfo childThread) {
        System.out.println(BEFORE_FORKING_THREAD + childThread.getThread().getId() + FROM + Thread.currentThread().getId());
    }

    @Override
    public void beforeEvent(EventDesc eventDesc) {
        System.out.println(BEFORE + COLON + Thread.currentThread().getId() + COLON + eventDesc);
    }

    @Override
    public void afterEvent(EventDesc eventDesc) {
        System.out.println(AFTER + COLON + Thread.currentThread().getId() + COLON + eventDesc);
    }

}

package edu.tamu.aser.listeners;

import java.util.List;
import java.util.SortedSet;

import edu.tamu.aser.reex.Scheduler;
import edu.tamu.aser.scheduling.events.*;
import edu.tamu.aser.scheduling.strategy.ChoiceType;
import edu.tamu.aser.scheduling.strategy.ThreadInfo;

public class EventsListener extends ExplorationListenerAdapter {

    private static final String CHOICES_MADE_DURING_SCHEDULE = "CHOICES MADE DURING SCHEDULE :";
    private static final String CHOSEN_THREAD = "CHOSEN THREAD: ";
    private static final String AT = " AT ";
    private static final String LOCK = "LOCK: ";
    private static final String UNLOCK = "UNLOCK: ";
      
    private static boolean startedExploration = false;

    private static boolean finishedExploration = false;

    @Override
    public void startingExploration(String name) {
        System.out.println("============================== STARTING EXPLORATION ==============================");
        startedExploration = true;
    }

    @Override
    public void startingSchedule() {
        System.out.println("==================== STARTING SCHEDULE ====================");
    }

    @Override
    public void makingChoice(SortedSet<? extends Object> choices, ChoiceType choiceType) {
        // System.out.println("=============== CHOICE POINT INFO ===============");
        // System.out.println(LIVE);
        // for (Entry<Thread, ThreadInfo> liveThreadEntry :
        // Scheduler.getLiveThreadInfos().entrySet()) {
        // Thread liveThread = liveThreadEntry.getKey();
        // ThreadInfo liveThreadInfo = liveThreadEntry.getValue();
        // System.out.println(liveThread.getId() + DOING +
        // liveThreadInfo.getEventDesc() + AT + liveThreadInfo.getLocationDesc()
        // + IN_STATE
        // + liveThread.getState() + COMMA);
        // }
        // System.out.println(END_BRACKET);
        // System.out.println(CHOOSING + choiceType);
        // System.out.println(CHOICES);
        // for (ThreadInfo choiceThreadInfo : threadChoices) {
        // System.out.println(choiceThreadInfo.getThread().getId() + DOING +
        // choiceThreadInfo.getEventDesc() + AT
        // + choiceThreadInfo.getLocationDesc() + COMMA);
        // }
        // System.out.println(END_BRACKET);
        // System.out.println("=================================================");
    }

    @Override
    public void choiceMade(Object choice) {
        // System.out.println(CHOSEN_THREAD + choice.getThread().getId());
        if (!startedExploration || finishedExploration)
            return;
        // if (Scheduler.getPausedThreadInfos().size() <= 1)
        //   return;
        // for (Entry<Thread, ThreadInfo> liveThreadEntry : Scheduler.getLiveThreadInfos().entrySet()) {
        //     Thread liveThread = liveThreadEntry.getKey();
        //     ThreadInfo liveThreadInfo = liveThreadEntry.getValue();
        //     System.out.println(liveThread.getId() + DOING + liveThreadInfo.getEventDesc() + AT + liveThreadInfo.getLocationDesc() + IN_STATE
        //             + liveThread.getState() + COMMA);
        // }
        // if (choice instanceof ThreadInfo) {
        //     ThreadInfo threadChoice = (ThreadInfo) choice;
        //     System.out.println(CHOSEN_THREAD + threadChoice.getThread().getId() + " " + threadChoice.getLocationDesc());
        //     System.out.println();
        // }
        // System.out.println("=================================================");
    }

    @Override
    public void completedSchedule(List<Integer> choicesMade) {
        System.out.println("==================== COMPLETED SCHEDULE ====================");
        System.out.println(CHOICES_MADE_DURING_SCHEDULE + choicesMade);

    }

    @Override
    public void completedExploration() {
        finishedExploration = true;
        // System.out.println("============================== COMPLETED EXPLORATION ==============================");
    }

    @Override
    public void beforeForking(ThreadInfo childThread) {
        // System.out.println(BEFORE_FORKING_THREAD +
        // childThread.getThread().getId() + FROM +
        // Thread.currentThread().getId());
    }

    @Override
    public void beforeEvent(EventDesc eventDesc) {
        // System.out.println(BEFORE + COLON + Thread.currentThread().getId() +
        // COLON + eventDesc);
    }

    @Override
    public void afterEvent(EventDesc eventDesc) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String stackTrace = "";
        for (StackTraceElement s : stack) {
          if (s.getClassName().contains("org.apache.derby")) {
            stackTrace += "#" + s.getClassName() + "%" + s.getLineNumber();
          }
        }
        System.out.println(CHOSEN_THREAD + Thread.currentThread().getName() + AT + stackTrace);
        System.out.println();

        if (eventDesc.getEventType() == EventType.LOCK || eventDesc.getEventType() == EventType.UNLOCK) {
          LockEventDesc lockEvent = (LockEventDesc)eventDesc;
          if (eventDesc.getEventType() == EventType.LOCK)
            System.out.println(LOCK + System.identityHashCode(lockEvent.getLockObject()));
          else
            System.out.println(UNLOCK + System.identityHashCode(lockEvent.getLockObject()));
          System.out.println();
        }
      ThreadInfo ti = Scheduler.getLiveThreadInfos().get(Thread.currentThread());
      int line = ti.getLocationDesc().getFromLineNumber();
      String className = ti.getLocationDesc().getClassName();
      String threadName = Thread.currentThread().getName();
      System.out.println("(" + threadName +  ", " + className + ", " + line + ")");
    }

}

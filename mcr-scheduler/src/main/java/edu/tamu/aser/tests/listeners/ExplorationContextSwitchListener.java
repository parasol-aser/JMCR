package edu.tamu.aser.listeners;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.aser.reex.Scheduler;
import edu.tamu.aser.scheduling.events.EventDesc;
import edu.tamu.aser.scheduling.events.LocationDesc;
import edu.tamu.aser.scheduling.strategy.ThreadInfo;

public class ExplorationContextSwitchListener extends ExplorationListenerAdapter {
    
    private ThreadInfo previousThreadInfo;
    private LocationDesc previousThreadLocation;
    private List<String> contextSwitchInfo;
    
    @Override
    public void startingSchedule() {
        previousThreadInfo = null;
        contextSwitchInfo = new ArrayList<String>();
    }
    
    @Override
    public void choiceMade(Object choice) {
        if (choice instanceof ThreadInfo) {
            ThreadInfo currentChoice = (ThreadInfo) choice;
            if (currentChoice != previousThreadInfo) {
                if (previousThreadInfo != null && previousThreadLocation != null) {
                    String switchFrom = "Switched after " + previousThreadInfo.getThread().getName() + " at "
                            + previousThreadLocation.getClassName() + " "  
                            + previousThreadLocation.getToLineNumber();
                    contextSwitchInfo.add(switchFrom);
                }
                String switchTo = "To " + 
                        currentChoice.getThread().getName()
                        //+ " at " + 
                        //currentChoice.getLocationDesc().getClassName() + " " + 
                        //currentChoice.getLocationDesc().getToLineNumber()
                        ;
                contextSwitchInfo.add(switchTo);
                previousThreadInfo = currentChoice;
               
            }
        }
    }
    
    @Override
    public void completedSchedule(List<Integer> choicesMade) {
        previousThreadInfo = null;
        previousThreadLocation = null;
    }
    
    @Override
    public void failureDetected(String errorMsg, List<Integer> choicesMade) {
        System.out.println("============================== CONTEXT SWITCH INFO ==============================");
        for (String s : contextSwitchInfo) {
            System.out.println(s);
        }
//        System.out.println("============================================================");
    }
    
    @Override
    public void afterEvent(EventDesc eventDesc) {
        ThreadInfo ti = Scheduler.getLiveThreadInfos().get(Thread.currentThread());
        previousThreadLocation = ti.getLocationDesc();
    }

}

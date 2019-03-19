package edu.tamu.aser.listeners;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import edu.tamu.aser.instrumentation.MCRProperties;
import edu.tamu.aser.scheduling.events.EventDesc;
import edu.tamu.aser.scheduling.strategy.ChoiceType;
import edu.tamu.aser.scheduling.strategy.ThreadInfo;


public class Listeners {

    private static final String DEBUG_EXPLORATION_DEFAULT = "false";

    private static final Set<ExplorationListener> explorationListeners;

    public static final boolean debugExploration;
    
    public static final boolean printContextSwitch = true;

    static {
        MCRProperties mcrProps = MCRProperties.getInstance();
        explorationListeners = new HashSet<ExplorationListener>();

        if (debugExploration = Boolean.parseBoolean(mcrProps.getProperty(MCRProperties.EXPLORATION_DEBUG_KEY, DEBUG_EXPLORATION_DEFAULT))) {
            explorationListeners.add(new ExplorationDebugListener());
        }
        
        if (printContextSwitch) {
            explorationListeners.add(new ExplorationContextSwitchListener());
        }

        /* Add the other listeners to be used */
//        String s = ExplorationStatsListener.class.getName();
//        String s1 = MCRProperties.LISTENERS_KEY;
        String listenerClassNames = mcrProps.getProperty(MCRProperties.LISTENERS_KEY, ExplorationStatsListener.class.getName());
        if (listenerClassNames != null) {
            for (String listenerClassName : listenerClassNames.split(",")) {
                try {
                    ExplorationListener listener = (ExplorationListener) Class.forName(listenerClassName).newInstance();
                    explorationListeners.add(listener);
                } catch (Exception e) {
                    System.err.println("Unable to obtain instance of: " + listenerClassName);
                    e.printStackTrace();
                }
            }
        }
    }

    public static void addListener(ExplorationListener listener) {
        explorationListeners.add(listener);
    }

    public static void removeListener(ExplorationListener listener) {
        explorationListeners.remove(listener);
    }

    public static void fireStartingExploration(String name) {
        for (ExplorationListener listener : explorationListeners) {
            listener.startingExploration(name);
        }
    }

    public static void fireStartingSchedule() {
        for (ExplorationListener listener : explorationListeners) {
            listener.startingSchedule();
        }
    }

    public static void fireBeforeForking(ThreadInfo childThread) {
        for (ExplorationListener listener : explorationListeners) {
            listener.beforeForking(childThread);
        }
    }

    public static void fireBeforeEvent(EventDesc eventDesc) {
        for (ExplorationListener listener : explorationListeners) {
            listener.beforeEvent(eventDesc);
        }
    }

    public static void fireAfterEvent(EventDesc eventDesc) {
        for (ExplorationListener listener : explorationListeners) {
            listener.afterEvent(eventDesc);
        }
    }

    public static void fireMakingChoice(SortedSet<? extends Object> objectChoices, ChoiceType choiceType) {
        for (ExplorationListener listener : explorationListeners) {
            listener.makingChoice(objectChoices, choiceType);
        }
    }

    public static void fireChoiceMade(Object choice) {
        for (ExplorationListener listener : explorationListeners) {
            listener.choiceMade(choice);
        }
    }

    public static void fireCompletedSchedule(List<Integer> choicesMade) {
        for (ExplorationListener listener : explorationListeners) {
            listener.completedSchedule(choicesMade);
        }
    }

    public static void fireCompletedExploration() {
        for (ExplorationListener listener : explorationListeners) {
            listener.completedExploration();
        }
    }

    public static void fireFailureDetected(String errorMsg, List<Integer> choicesMade) {
        for (ExplorationListener listener : explorationListeners) {
            listener.failureDetected(errorMsg, choicesMade);
        }
    }

}

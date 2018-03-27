package edu.tamu.aser.scheduling.filtering;

import java.util.SortedSet;

import edu.tamu.aser.listeners.ExplorationListenerAdapter;
import edu.tamu.aser.listeners.Listeners;
import edu.tamu.aser.rvinstrumentation.MCRProperties;
import edu.tamu.aser.scheduling.strategy.ChoiceType;
import edu.tamu.aser.scheduling.strategy.ThreadInfo;

/**
 * Filters thread choices (based on the location they are executing).
 */
public class LocationBasedPreemptionFilter extends ExplorationListenerAdapter implements SchedulingFilter {

    private static final String MODE_DEFAULT = "METHOD_ALL";

    /* Mode used to filter choices based on the allowed locations */
    private final LocationFilterMode mode;

    /* The thread that was last chosen/executed */
    private ThreadInfo lastChoice;

    public LocationBasedPreemptionFilter() {

        /* Setting the mode property */
        String modeString = MCRProperties.getInstance().getProperty(MCRProperties.MODE_KEY, MODE_DEFAULT);
        this.mode = LocationFilterMode.valueOf(modeString);

        this.lastChoice = null;
        Listeners.addListener(this);
    }

    @Override
    public SortedSet<? extends Object> filterChoices(SortedSet<? extends Object> choices, ChoiceType choiceType) {
        /*
         * If preemption is not possible, return the choices without any
         * filtering
         */
        if (choiceType != ChoiceType.THREAD_TO_SCHEDULE || choices.size() == 1 || !choices.contains(this.lastChoice)) {
            return choices;
        }
        return mode.filter((SortedSet<ThreadInfo>) choices, this.lastChoice);
    }

    @Override
    public void choiceMade(Object choice) {
        if(choice instanceof ThreadInfo) {
            this.lastChoice = (ThreadInfo) choice;
        }
    }

    public LocationFilterMode getMode() {
        return mode;
    }

}

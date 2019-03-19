package edu.tamu.aser.scheduling.filtering;

import java.util.SortedSet;

import edu.tamu.aser.listeners.ExplorationListenerAdapter;
import edu.tamu.aser.listeners.Listeners;
//import edu.tamu.aser.rvinstrumentation.MCRProperties;
import edu.tamu.aser.scheduling.strategy.ChoiceType;
import edu.tamu.aser.scheduling.strategy.ThreadInfo;

/**
 * Filters thread choices (based on the location they are executing).
 */
public class LocationBasedPreemptionFilter extends ExplorationListenerAdapter implements SchedulingFilter {

    /* Scheduling related properties */
//    public static final String SCHEDULING_STRATEGY_KEY = "mcr.exploration.schedulingstrategy";

    public static final String PREEMPTION_BOUND_KEY = "mcr.exploration.preemptionbound";
    public static final String SEED_KEY = "mcr.exploration.randomseed";
    public static final String MODE_KEY = "mcr.locfiltering.mode";
    public static final String ALLOWED_LOCS_FILE_KEY = "mcr.locfiltering.allowedfile";
    public static final String SCHEDULING_FILTER_KEY = "mcr.exploration.schedulingfilter";
    public static final String STOP_ON_FIRST_ERROR_KEY = "mcr.exploration.stoponfirsterror";
    public static final String RV_CAUSAL_FULL_TRACE = "mcr.exploration.fulltrace";

    private static final String MODE_DEFAULT = "METHOD_ALL";

    /* Mode used to filter choices based on the allowed locations */
    private final LocationFilterMode mode;

    /* The thread that was last chosen/executed */
    private ThreadInfo lastChoice;

    public LocationBasedPreemptionFilter() {

        /* Setting the mode property */
        String modeString = MODE_KEY;
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

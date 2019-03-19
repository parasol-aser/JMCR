package edu.tamu.aser.scheduling.filtering;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

//import edu.tamu.aser.rvinstrumentation.MCRProperties;
import edu.tamu.aser.scheduling.events.LocationDesc;
import edu.tamu.aser.scheduling.strategy.ThreadInfo;

public enum LocationFilterMode {

    LINE_ALL(LocationMatchType.LINE, ContainmentMatchType.ALL, "CLAL"), LINE_ALL_RECURSIVE(LocationMatchType.LINE_RECURSIVE,
            ContainmentMatchType.ALL, "CLALR"), LINE_ANY(LocationMatchType.LINE, ContainmentMatchType.ANY, "CLAN"), LINE_ANY_RECURSIVE(
            LocationMatchType.LINE_RECURSIVE, ContainmentMatchType.ANY, "CLANR"), METHOD_ALL(LocationMatchType.METHOD, ContainmentMatchType.ALL,
            "CMAL"), METHOD_ALL_RECURSIVE(LocationMatchType.METHOD_RECURSIVE, ContainmentMatchType.ALL, "CMALR"), METHOD_ANY(
            LocationMatchType.METHOD, ContainmentMatchType.ANY, "CMAN"), METHOD_ANY_RECURSIVE(LocationMatchType.METHOD_RECURSIVE,
            ContainmentMatchType.ANY, "CMANR"), CLASS_ALL(LocationMatchType.CLASS, ContainmentMatchType.ALL, "CCAL"), CLASS_ALL_RECURSIVE(
            LocationMatchType.CLASS_RECURSIVE, ContainmentMatchType.ALL, "CCALR"), CLASS_ANY(LocationMatchType.CLASS, ContainmentMatchType.ANY,
            "CCAN"), CLASS_ANY_RECURSIVE(LocationMatchType.CLASS_RECURSIVE, ContainmentMatchType.ANY, "CCANR"), FIELD_ALL(LocationMatchType.FIELD,
            ContainmentMatchType.ALL, "CFAL"), FIELD_ANY(LocationMatchType.FIELD, ContainmentMatchType.ANY, "CFAN"), METHOD_PRIORITIZE(
            LocationMatchType.METHOD, ContainmentMatchType.PRIORITIZE, "CMPR"), METHOD_PRIORITIZE_RECURSIVE(LocationMatchType.METHOD_RECURSIVE,
            ContainmentMatchType.PRIORITIZE, "CMPRR"), CLASS_PRIORITIZE(LocationMatchType.CLASS, ContainmentMatchType.PRIORITIZE, "CCPR"), CLASS_PRIORITIZE_RECURSIVE(
            LocationMatchType.CLASS_RECURSIVE, ContainmentMatchType.PRIORITIZE, "CCPRR"), LINE_PRIORITIZE(LocationMatchType.LINE,
            ContainmentMatchType.PRIORITIZE, "CLPR"), LINE_PRIORITIZE_RECURSIVE(LocationMatchType.LINE_RECURSIVE, ContainmentMatchType.PRIORITIZE,
            "CLPRR");

    protected enum ContainmentMatchType {
        ALL, ANY, PRIORITIZE
    }

    private final ContainmentMatchType containmentMatchType;
    private final LocationMatchType locationMatchType;
    private final String abbrv;
    /* The locations at which preemption is allowed (based on the filter mode) */
    private final Set<LocationDesc> allowedLocs;
    private final LocationMatchPrioritizer locMatchPrioritizer;

    public static final String ALLOWED_LOCS_FILE_KEY = "mcr.locfiltering.allowedfile";

    private LocationFilterMode(LocationMatchType locationMatchType, ContainmentMatchType containmentMatchType, String abbrv) {
        this.locationMatchType = locationMatchType;
        this.containmentMatchType = containmentMatchType;
        this.abbrv = abbrv;

        /* Setting the allowed locs file property */
        String allowedLocsFile = ALLOWED_LOCS_FILE_KEY;
        if (allowedLocsFile == null) {
            throw new IllegalArgumentException("The allowed preemption locs file should be provided via the mcr.locfiltering.allowedfile property!");
        }

        /* Reading allowed locs from the specified file */
        this.allowedLocs = new HashSet<LocationDesc>();
        Scanner allowedLocsScanner;
        try {
            allowedLocsScanner = new Scanner(new File(allowedLocsFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("The specified allowed preemption locs file could not be found!");
        }
        while (allowedLocsScanner.hasNextLine()) {
            this.allowedLocs.add(LocationDesc.locDescFromChgString(allowedLocsScanner.nextLine()));
        }

        this.locMatchPrioritizer = new LocationMatchPrioritizer(this.locationMatchType, this.allowedLocs);
    }

    /**
     * Filters the given {@link ThreadInfo}s choices based on allowedLocs and
     * {@link LocationFilterMode}.
     * 
     * @param choices
     *            {@link SortedSet} of {@link ThreadInfo}s of {@link Thread}s
     *            that are enabled and can be scheduled.
     * @param lastChoice
     *            the thread that was last chosen/executed. Can also call it the
     *            "currentThread". You know for sure that the currentThread is
     *            enabled, since filter() is called only when the preemption is
     *            possible among the given choices.
     * */
    public SortedSet<ThreadInfo> filter(SortedSet<ThreadInfo> choices, ThreadInfo lastChoice) {
        if (this.containmentMatchType.equals(ContainmentMatchType.PRIORITIZE)) {
            SortedSet<ThreadInfo> prioritizedChoices = new TreeSet<ThreadInfo>(this.locMatchPrioritizer);
            prioritizedChoices.addAll(choices);
            return prioritizedChoices;
        }
        for (ThreadInfo threadInfo : choices) {
            if (match(threadInfo, allowedLocs)) {
                if (this.containmentMatchType.equals(ContainmentMatchType.ANY)) {
                    /*
                     * ANY: we can allow preemptions if any thread is at an
                     * allowed loc
                     */
                    return choices;
                }
            } else {
                /*
                 * ALL: we should wait until all threads are at allowed locs
                 * before allowing preemptions
                 */
                if (this.containmentMatchType.equals(ContainmentMatchType.ALL)) {
                    TreeSet<ThreadInfo> choicesCopy = new TreeSet<ThreadInfo>(choices);
                    choicesCopy.retainAll(Collections.singleton(threadInfo));
                    return choicesCopy;
                }
            }
        }
        if (this.containmentMatchType.equals(ContainmentMatchType.ANY)) {
            /*
             * ANY: none of the threads were at allowed locs so do not allow
             * preemption
             */
            TreeSet<ThreadInfo> choicesCopy = new TreeSet<ThreadInfo>(choices);
            choicesCopy.retainAll(Collections.singleton(lastChoice));
            return choicesCopy;
        } else {
            /*
             * ALL: all the threads were at changed locs, so allow preemption
             */
            return choices;
        }
    }

    private boolean match(ThreadInfo threadInfo, Set<LocationDesc> allowedLocs) {
        for (LocationDesc locationDesc : allowedLocs) {
            if (this.locationMatchType.match(threadInfo, locationDesc)) {
                return true;
            }
        }
        return false;
    }

    public String getAbbrv() {
        return abbrv;
    }

}
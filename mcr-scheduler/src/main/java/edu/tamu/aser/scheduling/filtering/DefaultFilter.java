package edu.tamu.aser.scheduling.filtering;

import java.util.SortedSet;

import edu.tamu.aser.scheduling.strategy.ChoiceType;

/**
 * Default {@link SchedulingFilter} that does not perform any filtering.
 */
public class DefaultFilter implements SchedulingFilter {

    @Override
    public SortedSet<? extends Object> filterChoices(SortedSet<? extends Object> choices, ChoiceType choiceType) {
        return choices;
    }

}

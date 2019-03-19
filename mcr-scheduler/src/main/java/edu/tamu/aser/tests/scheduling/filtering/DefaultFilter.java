package edu.tamu.aser.tests.scheduling.filtering;

import java.util.SortedSet;

import edu.tamu.aser.tests.scheduling.strategy.ChoiceType;

/**
 * Default {@link SchedulingFilter} that does not perform any filtering.
 */
public class DefaultFilter implements SchedulingFilter {

    @Override
    public SortedSet<? extends Object> filterChoices(SortedSet<? extends Object> choices, ChoiceType choiceType) {
        return choices;
    }

}

package edu.tamu.aser.scheduling.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import edu.tamu.aser.instrumentation.MCRProperties;

/**
 * Reproduces a given schedule.
 */
public class ReproScheduleStrategy extends SchedulingStrategy {

    private static final String COMMA = ",";

    private final List<Integer> choicesToMake;
    private boolean performedExecution;
    private int choiceIndex;

    public ReproScheduleStrategy() {
        String choicesToMakeString = MCRProperties.getInstance().getProperty(MCRProperties.SCHEDULING_REPRO_CHOICES_KEY);
        if (choicesToMakeString == null) {
            throw new IllegalArgumentException(
                    "The choices (comma separated integers) to reproduce should be provided via the mcr.exploration.reprochoices property!");
        }
        this.choicesToMake = new ArrayList<Integer>();
        for (String choiceString : choicesToMakeString.split(COMMA)) {
            choicesToMake.add(Integer.parseInt(choiceString));
        }
        startingExploration();
    }

    @Override
    public void startingExploration() {
        this.performedExecution = false;
        this.choiceIndex = 0;
    }

    @Override
    public void startingScheduleExecution() {
    }

    @Override
    public void completedScheduleExecution() {
        this.performedExecution = true;
    }

    @Override
    public boolean canExecuteMoreSchedules() {
        return !performedExecution;
    }

    @Override
    public Object choose(SortedSet<? extends Object> objectChoices, ChoiceType choiceType) {
        if (choiceIndex >= choicesToMake.size()) {
            System.err.println("No more choicesToMake to make!");
            System.exit(2);
        }
        int chosenIndex = choicesToMake.get(choiceIndex);
        choiceIndex++;
        return getChosenObject(chosenIndex, objectChoices);
    }

    @Override
    public List<Integer> getChoicesMadeDuringThisSchedule() {
        return choicesToMake.subList(0, choiceIndex);
    }

}

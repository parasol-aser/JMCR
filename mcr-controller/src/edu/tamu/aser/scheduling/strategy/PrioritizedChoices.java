package edu.tamu.aser.scheduling.strategy;

import java.util.Iterator;
import java.util.List;


public class PrioritizedChoices implements Comparable<PrioritizedChoices> {

    private List<Integer> priorities;
    private List<Integer> choices;

    public PrioritizedChoices(List<Integer> priorities, List<Integer> choices) {
        super();
        this.priorities = priorities;
        this.choices = choices;
    }

    public List<Integer> getChoices() {
        return choices;
    }

    public List<Integer> getPriorities() {
        return priorities;
    }

    @Override
    public int compareTo(PrioritizedChoices other) {
        if (this.priorities.size() != other.priorities.size()) {
            throw new IllegalArgumentException("Cannot compare PrioritizedChoices with different priorities lengths");
        }
        Iterator<Integer> iterator = this.priorities.iterator();
        Iterator<Integer> otherIterator = other.priorities.iterator();
        while (iterator.hasNext()) {
            int priority = iterator.next();
            int otherPriority = otherIterator.next();
            if (priority != otherPriority) {
                return priority - otherPriority;
            }
        }
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((choices == null) ? 0 : choices.hashCode());
        result = prime * result + ((priorities == null) ? 0 : priorities.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PrioritizedChoices other = (PrioritizedChoices) obj;
        if (choices == null) {
            if (other.choices != null) {
                return false;
            }
        } else if (!choices.equals(other.choices)) {
            return false;
        }
        if (priorities == null) {
            if (other.priorities != null) {
                return false;
            }
        } else if (!priorities.equals(other.priorities)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PrioritizedChoices [priorities=" + priorities + ", choices=" + choices + "]\n";
    }

}

package edu.tamu.aser.scheduling.filtering;

import edu.tamu.aser.scheduling.events.FieldAccessEventDesc;
import edu.tamu.aser.scheduling.events.LocationDesc;
import edu.tamu.aser.scheduling.strategy.ThreadInfo;

public enum LocationMatchType {

    LINE {

        @Override
        boolean match(ThreadInfo toMatch, LocationDesc allowedLoc) {
            return toMatch.getLocationDesc().within(allowedLoc);
        }
    },
    LINE_RECURSIVE {

        @Override
        boolean match(ThreadInfo toMatch, LocationDesc allowedLoc) {
            if (LINE.match(toMatch, allowedLoc)) {
                return true;
            } else {
                for (StackTraceElement stackElement : toMatch.getThread().getStackTrace()) {
                    if (stackElement.getClassName().equals(allowedLoc.getClassName().replace(SLASH, DOT))) {
                        String locMethod = allowedLoc.getMethodName();
                        if (locMethod != null && locMethod.equals(stackElement.getMethodName())) {
                            int stackLine = stackElement.getLineNumber();
                            if (allowedLoc.getFromLineNumber() <= stackLine && allowedLoc.getToLineNumber() >= stackLine) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        }
    },
    METHOD {

        @Override
        boolean match(ThreadInfo toMatch, LocationDesc allowedLoc) {
            LocationDesc toMatchLoc = toMatch.getLocationDesc();
            return toMatchLoc.getClassName().equals(allowedLoc.getClassName()) && toMatchLoc.getMethodName() != null
                    && toMatchLoc.getMethodName().equals(allowedLoc.getMethodName());
        }
    },
    METHOD_RECURSIVE {

        @Override
        boolean match(ThreadInfo toMatch, LocationDesc allowedLoc) {
            if (METHOD.match(toMatch, allowedLoc)) {
                return true;
            } else {
                for (StackTraceElement stackElement : toMatch.getThread().getStackTrace()) {
                    if (stackElement.getClassName().equals(allowedLoc.getClassName().replace(SLASH, DOT))) {
                        String locMethod = allowedLoc.getMethodName();
                        if (locMethod != null && locMethod.equals(stackElement.getMethodName())) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
    },
    FIELD {

        @Override
        boolean match(ThreadInfo toMatch, LocationDesc allowedLoc) {
            if (toMatch.getEventDesc() instanceof FieldAccessEventDesc) {
                FieldAccessEventDesc toMatchEvent = (FieldAccessEventDesc) toMatch.getEventDesc();
                return toMatchEvent.getFieldOwner().equals(allowedLoc.getClassName())
                        && toMatchEvent.getFieldName().equals(allowedLoc.getFieldName());
            }
            return false;
        }
    },
    CLASS {

        @Override
        boolean match(ThreadInfo toMatch, LocationDesc allowedLoc) {
            return toMatch.getLocationDesc().getClassName().equals(allowedLoc.getClassName());
        }
    },
    CLASS_RECURSIVE {

        @Override
        boolean match(ThreadInfo toMatch, LocationDesc allowedLoc) {
            if (CLASS.match(toMatch, allowedLoc)) {
                return true;
            } else {
                for (StackTraceElement stackElement : toMatch.getThread().getStackTrace()) {
                    if (stackElement.getClassName().equals(allowedLoc.getClassName().replace(SLASH, DOT))) {
                        return true;
                    }
                }
                return false;
            }
        }
    };

    private static final String SLASH = "/";
    private static final String DOT = ".";

    abstract boolean match(ThreadInfo toMatch, LocationDesc allowedLoc);
}
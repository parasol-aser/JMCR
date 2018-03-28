package edu.tamu.aser.scheduling.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Describes a source location. Used to specify the source location of the
 * various events in {@link EventType}.
 */
public class LocationDesc {

    private static final String SLASH = "/";
    private static final String DOT = ".";
    private static final String HYPHEN = "-";
    private static final String OPEN_PARAN = "(";
    private static final String EMPTY_STRING = "";
    private static final String COLON = ":";

    private final String className;
    private final String methodName;
    private final String fieldName;
    private final int fromLineNumber;
    private final int toLineNumber;

    public LocationDesc(String className, String methodName, int lineNumber) {
        this(className, methodName, null, lineNumber, lineNumber);
    }

    public LocationDesc(String className, String methodName, String fieldName, int fromLineNumber, int toLineNumber) {
        this.className = className;
        this.methodName = methodName;
        this.fieldName = fieldName;
        this.fromLineNumber = fromLineNumber;
        this.toLineNumber = toLineNumber;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public int getFromLineNumber() {
        return fromLineNumber;
    }

    public int getToLineNumber() {
        return toLineNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
        result = prime * result + fromLineNumber;
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result + toLineNumber;
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
        LocationDesc other = (LocationDesc) obj;
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (fieldName == null) {
            if (other.fieldName != null) {
                return false;
            }
        } else if (!fieldName.equals(other.fieldName)) {
            return false;
        }
        if (fromLineNumber != other.fromLineNumber) {
            return false;
        }
        if (methodName == null) {
            if (other.methodName != null) {
                return false;
            }
        } else if (!methodName.equals(other.methodName)) {
            return false;
        }
        if (toLineNumber != other.toLineNumber) {
            return false;
        }
        return true;
    }

    /**
     * Return whether this {@link LocationDesc} is within the given
     * {@link LocationDesc}.
     * 
     * @param locDesc
     * @return whether this {@link LocationDesc} is within the given
     *         {@link LocationDesc}.
     */
    public boolean within(LocationDesc locDesc) {
        if (this == locDesc) {
            return true;
        }
        if (locDesc == null) {
            return false;
        }
        if (className == null) {
            if (locDesc.className != null) {
                return false;
            }
        } else if (!className.equals(locDesc.className)) {
            return false;
        }
        if (fieldName == null) {
            if (locDesc.fieldName != null) {
                return false;
            }
        } else if (!fieldName.equals(locDesc.fieldName)) {
            return false;
        }
        if (methodName == null) {
            if (locDesc.methodName != null) {
                return false;
            }
        } else if (!methodName.equals(locDesc.methodName)) {
            return false;
        }
        return locDesc.fromLineNumber <= fromLineNumber && locDesc.toLineNumber >= toLineNumber;
    }

    @Override
    public String toString() {
        return "LocationDesc [className=" + className + ", methodName=" + methodName + ", fieldName=" + fieldName + ", fromLineNumber="
                + fromLineNumber + ", toLineNumber=" + toLineNumber + "]";
    }

    /**
     * Constructs and returns a {@link LocationDesc} instance using the
     * information in the given changeDescString. The accepted changeDescString
     * format is:
     * [+|-|c]:fullyQualifiedClassName:[methodName()|fieldName]:[lineNumber|
     * fromLineNumber-toLineNumber].
     * 
     * @param changeDescString
     * @return a {@link LocationDesc} instance constructed using the information
     *         in the given changeDescString.
     */
    public static LocationDesc locDescFromChgString(String changeDescString) {
        String[] changeDescParts = changeDescString.split(COLON);
        String className = changeDescParts[1].replace(DOT, SLASH).trim();
        String methodName = null;
        String fieldName = null;
        if (changeDescParts.length > 2 && !changeDescParts[2].equals(EMPTY_STRING)) {
            if (changeDescParts[2].contains(OPEN_PARAN)) {
                methodName = changeDescParts[2].substring(0, changeDescParts[2].indexOf(OPEN_PARAN)).trim();
            } else {
                fieldName = changeDescParts[2].trim();
            }

        }
        int fromLineNumber = -1;
        int toLineNumber = -1;
        if (!changeDescParts[0].equals(HYPHEN) && changeDescParts.length == 4 && !changeDescParts[3].equals(EMPTY_STRING)) {
            String[] lineNumberStrings = changeDescParts[3].split(HYPHEN);
            fromLineNumber = Integer.parseInt(lineNumberStrings[0].trim());
            if (lineNumberStrings.length == 2) {
                toLineNumber = Integer.parseInt(lineNumberStrings[1].trim());
            } else {
                toLineNumber = fromLineNumber;
            }
        }
        return new LocationDesc(className, methodName, fieldName, fromLineNumber, toLineNumber);
    }

    /**
     * For debugging
     */
    public static void main(String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(
                "/home/vilas/projects/iconcurrent/imunit/regression-projects/eval/commons-lang/lang-481/structure-diff"));
        while (scanner.hasNextLine()) {
            System.out.println(locDescFromChgString(scanner.nextLine()));
            System.out.flush();
        }
    }

}

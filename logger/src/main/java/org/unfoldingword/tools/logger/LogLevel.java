package org.unfoldingword.tools.logger;

/**
 * Created by joel on 6/18/16.
 */
public enum LogLevel {
    Info(0, "I"),
    Warning(1, "W"),
    Error(2, "E");

    LogLevel(int i, String label) {
        this.level = i;
        this.label = label;
    }

    private int level;
    private String label;

    public int getIndex() {
        return level;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Returns a level by it's label
     *
     * @param label the case insensitive label of the level
     * @return null if the level does not exist
     */
    public static LogLevel getLevel(String label) {
        for (LogLevel l : LogLevel.values()) {
            if (l.getLabel().toLowerCase().equals(label.toLowerCase())) {
                return l;
            }
        }
        return null;
    }

    /**
     * Returns a level by it's index
     *
     * @param index the level index
     * @return null if the level does not exist
     */
    public static LogLevel getLevel(int index) {
        for (LogLevel l : LogLevel.values()) {
            if (l.getIndex() == index) {
                return l;
            }
        }
        return null;
    }
}
package org.unfoldingword.tools.logger;

import java.util.Date;

/**
 * Created by joel on 6/18/16.
 */
public class LogEntry {
    public final Date date;
    public final LogLevel level;
    public final String classPath;
    public final String message;
    private String mDetails;

    /**
     * Creates a new error object
     *
     * @param date
     * @param level
     * @param classPath
     * @param message
     */
    public LogEntry(Date date, LogLevel level, String classPath, String message) {
        this.date = date;
        this.level = level;
        this.classPath = classPath;
        this.message = message;
    }

    /**
     * Creates a new error object
     *
     * @param date
     * @param level
     * @param classPath
     * @param message
     * @param details
     */
    public LogEntry(Date date, LogLevel level, String classPath, String message, String details) {
        this.date = date;
        this.level = level;
        this.classPath = classPath;
        this.message = message;
        mDetails = details;
    }

    /**
     * Sets the error log details
     *
     * @param details
     */
    public void setDetails(String details) {
        mDetails = details;
    }

    /**
     * Returns the error log details
     *
     * @return
     */
    public String getDetails() {
        return mDetails;
    }
}

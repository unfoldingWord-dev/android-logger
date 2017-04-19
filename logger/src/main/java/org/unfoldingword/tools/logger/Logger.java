package org.unfoldingword.tools.logger;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Logs messages using the android Log class and also records logs to a file if configured.
 * Requires permission WRITE_EXTERNAL_STORAGE in AndroidManifest.xml.
 */
public class Logger {

    /**
     * The pattern to match the leading log line
     */
    public final static String PATTERN = "(\\d+\\/\\d+\\/\\d+\\s+\\d+:\\d+\\s+[A|P]M)\\s+([A-Z|])\\/(((?!:).)*):(.*)";
    private final File mLogFile;
    private final LogLevel mMinLoggingLevel;
    private final long mMaxLogFileSize;
    private static Logger sInstance;
    private static final long DEFAULT_MAX_LOG_FILE_SIZE = 1024 * 200;
    private File stacktraceDir = null;

    static {
        sInstance = new Logger(null, LogLevel.Info);
    }

    /**
     * @param logFile
     * @param minLogingLevel
     */
    private Logger(File logFile, LogLevel minLogingLevel) {
        mLogFile = logFile;
        if (minLogingLevel == null) {
            mMinLoggingLevel = LogLevel.Info;
        } else {
            mMinLoggingLevel = minLogingLevel;
        }
        mMaxLogFileSize = DEFAULT_MAX_LOG_FILE_SIZE;
    }

    /**
     * @param logFile
     * @param minLogingLevel
     * @param maxLogFileSize
     */
    private Logger(File logFile, LogLevel minLogingLevel, long maxLogFileSize) {
        mLogFile = logFile;
        if (minLogingLevel == null) {
            mMinLoggingLevel = LogLevel.Info;
        } else {
            mMinLoggingLevel = minLogingLevel;
        }
        mMaxLogFileSize = maxLogFileSize;
    }

    /**
     * Registers the global exception handler.
     * The main process will be killed automatically when an exception occurs.
     *
     * @param stacktraceDir the directory where stacktraces will be stored
     */
    public static void registerGlobalExceptionHandler(File stacktraceDir) {
        registerGlobalExceptionHandler(stacktraceDir, true);
    }

    /**
     * Registers the global exception handler
     * @param stacktraceDir the directory where stacktraces will be stored
     * @param autoKill kills the main process automatically when an exception occurs
     */
    public static void registerGlobalExceptionHandler(File stacktraceDir, boolean autoKill) {
        if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof GlobalExceptionHandler)) {
            sInstance.stacktraceDir = stacktraceDir;
            GlobalExceptionHandler geh = new GlobalExceptionHandler(stacktraceDir);
            geh.setKillOnException(autoKill);
            Thread.setDefaultUncaughtExceptionHandler(geh);
        }
    }

    /**
     * Removes the exception handler.
     */
    public static void unRegisterGlobalExceptionHandler() {
        sInstance.stacktraceDir = null;
        Thread.setDefaultUncaughtExceptionHandler(null);
    }

    /**
     * Returns the stracktrace directory
     * @return
     */
    public static File getStacktraceDir() {
        return sInstance.stacktraceDir;
    }

    /**
     * Returns an array of stacktrace files found in the directory
     * @return
     */
    public static File[] listStacktraces() {
        if(sInstance.stacktraceDir != null) {
            return GlobalExceptionHandler.getStacktraces(sInstance.stacktraceDir);
        } else {
            return new File[0];
        }
    }

    /**
     * Configures the logger to write log messages to a file
     *
     * @param logFile        the file where logs will be written
     * @param minLogingLevel the minimum level a log must be before it is recorded to the log file
     */
    public static void configure(File logFile, LogLevel minLogingLevel) {
        sInstance = new Logger(logFile, minLogingLevel);
    }

    /**
     * Configures the logger to write log messages to a file
     *
     * @param logFile        the file where logs will be written
     * @param minLogingLevel the minimum level a log must be before it is recorded to the log file
     * @param maxLogFileSize the maximum size the log file may become before old logs are truncated
     */
    public static void configure(File logFile, LogLevel minLogingLevel, long maxLogFileSize) {
        sInstance = new Logger(logFile, minLogingLevel, maxLogFileSize);
    }

    /**
     * Sends an error message to LogCat and to a log file.
     *
     * @param logMessageTag A tag identifying a group of log messages. Should be a constant in the
     *                      class calling the logger.
     * @param logMessage    The message to add to the log.
     */
    public static void e(String logMessageTag, String logMessage) {
        try {
            int logResult = Log.e(logMessageTag, logMessage);
            if (logResult > 0) {
                sInstance.logToFile(LogLevel.Error, logMessageTag, logMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a warning message to LogCat and to a log file.
     *
     * @param logMessageTag A tag identifying a group of log messages. Should be a constant in the
     *                      class calling the logger.
     * @param logMessage    The message to add to the log.
     */
    public static void w(String logMessageTag, String logMessage) {
        try {
            int logResult = Log.w(logMessageTag, logMessage);
            if (logResult > 0) {
                sInstance.logToFile(LogLevel.Warning, logMessageTag, logMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends an info message to LogCat and to a log file.
     *
     * @param logMessageTag A tag identifying a group of log messages. Should be a constant in the
     *                      class calling the logger.
     * @param logMessage    The message to add to the log.
     */
    public static void i(String logMessageTag, String logMessage) {
        try {
            int logResult = Log.i(logMessageTag, logMessage);
            if (logResult > 0) {
                sInstance.logToFile(LogLevel.Info, logMessageTag, logMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends an error message and the exception to LogCat and to a log file.
     *
     * @param logMessageTag      A tag identifying a group of log messages. Should be a constant in the
     *                           class calling the logger.
     * @param logMessage         The message to add to the log.
     * @param throwableException An exception to log
     */
    public static void e(String logMessageTag, String logMessage, Throwable throwableException) {
        try {
            int logResult = Log.e(logMessageTag, logMessage, throwableException);
            if (logResult > 0) {
                sInstance.logToFile(LogLevel.Error, logMessageTag, logMessage + "\r\n" + Log.getStackTraceString(throwableException));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message and the exception to LogCat and to a log file.
     *
     * @param logMessageTag      A tag identifying a group of log messages. Should be a constant in the
     *                           class calling the logger.
     * @param logMessage         The message to add to the log.
     * @param throwableException An exception to log
     */
    public static void w(String logMessageTag, String logMessage, Throwable throwableException) {
        try {
            int logResult = Log.w(logMessageTag, logMessage, throwableException);
            if (logResult > 0) {
                sInstance.logToFile(LogLevel.Warning, logMessageTag, logMessage + "\r\n" + Log.getStackTraceString(throwableException));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Empties the log file and deletes stack traces
     */
    public static void flush() {
        if (sInstance.mLogFile != null) {
            sInstance.mLogFile.delete();
        }
        if(sInstance.stacktraceDir != null) {
            FileUtils.deleteRecursive(sInstance.stacktraceDir);
        }
    }

    /**
     * Gets a stamp containing the current date and time to write to the log.
     *
     * @return The stamp for the current date and time.
     */
    private static String getDateTimeStamp() {
        Date dateNow = Calendar.getInstance().getTime();
        return (DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.ENGLISH).format(dateNow));
    }

    /**
     * Writes a message to the log file on the device.
     *
     * @param logMessageTag A tag identifying a group of log messages.
     * @param logMessage The message to add to the log.
     */
    private synchronized void logToFile(LogLevel level, String logMessageTag, String logMessage) {
        // filter out logging levels
        if (level.getIndex() >= mMinLoggingLevel.getIndex() && mLogFile != null) {
            try {
                if (!mLogFile.exists()) {
                    mLogFile.getParentFile().mkdirs();
                    mLogFile.createNewFile();
                }

                // append log message

                String log = FileUtils.readFileToString(mLogFile);
                log = String.format("%1s %2s/%3s: %4s\r\n%5s", getDateTimeStamp(), level.getLabel(), logMessageTag, logMessage, log);
                mLogFile.delete();
                FileUtils.writeStringToFile(mLogFile, log);

                // truncate the log if it gets too big.
                if (mLogFile.length() > mMaxLogFileSize) {
                    FileChannel outChan = new FileOutputStream(mLogFile, true).getChannel();
                    outChan.truncate(mMaxLogFileSize * (long) 0.8);
                    outChan.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the path to the current log file
     * @return
     */
    public static File getLogFile() {
        return sInstance.mLogFile;
    }

    /**
     * Returns a list of log entries
     * @return
     */
    public static List<LogEntry> getLogEntries() {
        List<LogEntry> logs = new ArrayList<>();
        if (sInstance.mLogFile != null) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sInstance.mLogFile)));
                StringBuilder sb = new StringBuilder();
                String line;
                Pattern pattern = Pattern.compile(Logger.PATTERN);
                LogEntry log = null;
                while ((line = br.readLine()) != null) {
                    if (Thread.interrupted()) break;
                    Matcher match = pattern.matcher(line);
                    if (match.find()) {
                        // save log
                        if (log != null) {
                            log.setDetails(sb.toString().trim());
                            logs.add(log);
                            sb.setLength(0);
                        }
                        // start new log
                        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy hh:mm a");
                        log = new LogEntry(format.parse(match.group(1)), LogLevel.getLevel(match.group(2)), match.group(3), match.group(5));
                    } else {
                        // build log details
                        sb.append(line);
                    }
                }
                // save the last log
                if (log != null) {
                    log.setDetails(sb.toString().trim());
                    logs.add(log);
                    sb.setLength(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.w(Logger.class.getName(), "The log file has not been configured and cannot be read");
        }
        return logs;
    }
}

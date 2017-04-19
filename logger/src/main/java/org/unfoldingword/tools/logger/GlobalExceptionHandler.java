package org.unfoldingword.tools.logger;
import android.os.Process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * This class writes exceptions to a file on disk before killing the app
 * This allows you to retrieve them later for debugging.
 * http://stackoverflow.com/questions/601503/how-do-i-obtain-crash-data-from-my-android-application
 */
class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String STACKTRACE_EXT = "stacktrace";
    private Thread.UncaughtExceptionHandler defaultUEH;
    private final String mStracktraceDir;
    private boolean killOnException = true;

    /**
     * if any of the parameters is null, the respective functionality
     * will not be used
     * @param stacktraceDir
     */
    public GlobalExceptionHandler(File stacktraceDir) {
        if(!stacktraceDir.exists()) {
            stacktraceDir.mkdirs();
        }
        this.mStracktraceDir = stacktraceDir.getAbsolutePath();
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    /**
     * Sets whether this class should kill the main process to shut down the app if
     * an exception occurs.
     * @param kill
     */
    public void setKillOnException(boolean kill) {
        this.killOnException = kill;
    }

    /**
     * Returns a list of stacktrace files found in the directory
     * @param stacktraceDir
     * @return
     */
    public static File[] getStacktraces(File stacktraceDir) {
        File[] files = stacktraceDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                String pieces[] = filename.split("\\.");
                String ext = pieces[pieces.length - 1];
                return new File(dir, filename).isFile() && ext.equals(STACKTRACE_EXT);
            }
        });
        if(files != null) {
            return files;
        } else {
            return new File[0];
        }
    }

    /**
     * Handles the uncaught exception
     * @param t
     * @param e
     */
    public void uncaughtException(Thread t, Throwable e) {
        Long tsLong = System.currentTimeMillis();
        String timestamp = tsLong.toString();
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        String filename = timestamp + "." + STACKTRACE_EXT;

        if (mStracktraceDir != null) {
            writeToFile(stacktrace, filename);
        }

        defaultUEH.uncaughtException(t, e);

        if(killOnException) {
            // force shut down so we don't end up with un-initialized objects
            Process.killProcess(Process.myPid());
            System.exit(0);
        }
    }

    /**
     * Writes the stacktrace to the log directory
     * @param stacktrace
     * @param filename
     */
    public void writeToFile(String stacktrace, String filename) {
        try {
            BufferedWriter bos = new BufferedWriter(new FileWriter(
                    mStracktraceDir + "/" + filename));
            bos.write(stacktrace);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
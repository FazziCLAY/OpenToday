package com.fazziclay.opentoday.util;

import android.util.Log;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.util.time.TimeUtil;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

public class Logger {
    private static final String ANDROID_LOG_TAG = "OpenTodayLogger";
    private static final StringBuilder LOGS = new StringBuilder();
    private static final String[] SHOW_STACKTRACE_IF_CONTAINS = {
            "[Tab] Attempt to getRoot in unattached Tab.",
    };

    public static <T> T dur(String tag, String message, Supplier<T> supplier) {
        if (!App.LOG) return supplier.get();

        long start = System.currentTimeMillis();
        T val = supplier.get();
        long duration = System.currentTimeMillis() - start;
        i(tag, String.format("%sms: %s", duration, message));
        return val;
    }

    public static int countOnlyDur(Runnable r) {
        long start = System.currentTimeMillis();
        r.run();
        return (int) (System.currentTimeMillis() - start);
    }

    public static void dur(String tag, String message, Runnable runnable) {
        if (!App.LOG) {
            runnable.run();
            return;
        }

        long start = System.currentTimeMillis();
        runnable.run();
        long duration = System.currentTimeMillis() - start;
        i(tag, String.format("%sms: %s", duration, message));
    }

    private static void log(final String s) {
        log(s, false);
    }

    private static void log(final String s, boolean noChecks) {
        final String time = TimeUtil.getDebugDate(System.currentTimeMillis());
        LOGS.append("[").append(time).append("] ").append(s).append("\n");

        logToFile("[" + time + "] " + s + "\n");
        if (noChecks) return;

        for (String ifContain : SHOW_STACKTRACE_IF_CONTAINS) {
            if (s.contains(ifContain)) {
                Exception exception = new Exception(s);
                log(stackTrace(exception), true);
                exception.printStackTrace();
            }
        }
    }

    private static void logToFile(String s) {
        if (!App.LOGS_SAVE) return;

        final App app = App.get();
        if (app == null) return;
        final File file = app.getLogsFile();
        if (file == null) return;
        if (file.length() < 1024*1024) {
            FileUtil.addText(file, s);
        } else {
            FileUtil.setText(file, TimeUtil.getDebugDate(System.currentTimeMillis()) + " == LOG FILE SIZE > 1024*1024. RESETTING ==\n" + s);
        }
    }

    private static String stackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    public static void e(String tag, String m, Throwable e) {
        if (!App.LOG) return;
        Log.e(ANDROID_LOG_TAG, String.format("[%s] %s", tag, m), e);
        log("OTL/ERROR [" + tag + "] " + m + " E: " + e);
    }


    public static void w(String tag, String m) {
        if (!App.LOG) return;
        Log.w(ANDROID_LOG_TAG, String.format("[%s] %s", tag, m));
        log("OTL/WARN [" + tag + "] " + m);
    }

    public static void i(String tag, String m) {
        if (!App.LOG) return;
        Log.i(ANDROID_LOG_TAG, String.format("[%s] %s", tag, m));
        log("OTL/INFO [" + tag + "] " + m);
    }

    public static void d(String tag, Object... m) {
        if (!App.LOG) return;
        if (m.length == 1) {
            Log.d(ANDROID_LOG_TAG, String.format("[%s] %s", tag, m[0]));
            log("OTL/DEBUG [" + tag + "] " + m[0]);
        } else {
            StringBuilder s = new StringBuilder();
            for (Object o : m) {
                s.append(o).append(" ");
            }
            Log.d(ANDROID_LOG_TAG, String.format("[%s] %s", tag, s.substring(0, s.length()-1)));
            log("OTL/DEBUG [" + tag + "] " + s.substring(0, s.length()-1));
        }
    }

    public static StringBuilder getLOGS() {
        return LOGS;
    }
}

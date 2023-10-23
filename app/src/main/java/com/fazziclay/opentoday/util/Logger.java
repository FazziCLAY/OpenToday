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
    private static final String[] SHOW_STACKTRACE_IF_CONTAINS = {
            "[Tab] Attempt to getRoot in unattached Tab.",
            "call get() without context"
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

    private static void log(Level level, final String s) {
        log(level, s, false);
    }

    private static void log(Level level, final String s, boolean noChecks) {
        if (level == Level.DEBUG && !App.DEBUG) return;
        final String time = TimeUtil.getDebugDate(System.currentTimeMillis());
        final App app = App.get();
        if (app == null) return;

        app.getLogs().append(level.getUiprefix()).append("[").append("$[S10]").append(time).append("$[Sreset]").append("] ").append(s).append(level.getUisuffix()).append("\n");

        logToFile(app, "[" + time + "] " + s + "\n");
        if (noChecks) return;

        for (String ifContain : SHOW_STACKTRACE_IF_CONTAINS) {
            if (s.contains(ifContain)) {
                Exception exception = new Exception(s);
                log(level, stackTrace(exception), true);
                exception.printStackTrace();
            }
        }
    }

    private static void logToFile(App app, String s) {
        if (!App.LOGS_SAVE) return;
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
        return sw.toString().substring(0, sw.toString().lastIndexOf("\n"));
    }

    public static void e(String tag, String m, Throwable e) {
        if (!App.LOG) return;
        Log.e(ANDROID_LOG_TAG, String.format("[%s] %s", tag, m), e);
        log(Level.ERROR, "OTL/ERROR [" + tag + "] " + m + " E: " + e);
        log(Level.ERROR, "OTL/ERROR [" + tag + "] " + stackTrace(e));
    }


    public static void w(String tag, String m) {
        if (!App.LOG) return;
        Log.w(ANDROID_LOG_TAG, String.format("[%s] %s", tag, m));
        log(Level.WARNING, "OTL/WARN [" + tag + "] " + m);
    }

    public static void i(String tag, String m) {
        if (!App.LOG) return;
        Log.i(ANDROID_LOG_TAG, String.format("[%s] %s", tag, m));
        log(Level.INFO, "OTL/INFO [" + tag + "] " + m);
    }

    public static void d(String tag, Object... m) {
        if (!App.LOG) return;
        if (m.length == 1) {
            Log.d(ANDROID_LOG_TAG, String.format("[%s] %s", tag, m[0]));
            log(Level.DEBUG, "OTL/DEBUG [" + tag + "] " + m[0]);
        } else {
            StringBuilder s = new StringBuilder();
            for (Object o : m) {
                s.append(o).append(" ");
            }
            Log.d(ANDROID_LOG_TAG, String.format("[%s] %s", tag, s.substring(0, s.length()-1)));
            log(Level.DEBUG, "OTL/DEBUG [" + tag + "] " + s.substring(0, s.length()-1));
        }
    }

    public static StringBuilder getLogs() {
        return App.get().getLogs();
    }

    public static void trace(String tag, String s) {
        s = s + "\n" + stackTrace(new Throwable("Stacktrace throwable"));
        d(tag, s);
    }

    private enum Level {
        ERROR("$[-#ff1111]", "$[||]"),
        WARNING("$[-#FFFF00]", "$[||]"),
        INFO("$[-#ffffff;=#77000000]", "$[||]"),
        DEBUG("$[-#d9d9d9]", "$[||]");

        private final String uiprefix;
        private final String uisuffix;

        Level(String uiprefix, String uisuffix) {
            this.uiprefix = uiprefix;
            this.uisuffix = uisuffix;
        }

        public String getUiprefix() {
            return uiprefix;
        }

        public String getUisuffix() {
            return uisuffix;
        }
    }
}

package ru.fazziclay.opentoday.app;

import android.os.Build;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CrashReport {
    private final UUID id;
    private final Thread thread;
    private final Throwable throwable;
    private final long crashTimeMillis;
    private final long crashTimeNano;
    private final Map<Thread, StackTraceElement[]> allStackTraces;

    public static CrashReport create(Thread thread, Throwable throwable, long crashTimeMillis, long crashTimeNano, Map<Thread, StackTraceElement[]> allStackTraces) {
        return new CrashReport(thread, throwable, crashTimeMillis, crashTimeNano, allStackTraces);
    }

    private CrashReport(Thread thread, Throwable throwable, long crashTimeMillis, long crashTimeNano, Map<Thread, StackTraceElement[]> allStackTraces) {
        this.id = UUID.randomUUID();
        this.thread = thread;
        this.throwable = throwable;
        this.crashTimeMillis = crashTimeMillis;
        this.crashTimeNano = crashTimeNano;
        this.allStackTraces = allStackTraces;
    }

    public UUID getID() {
        return this.id;
    }

    public String convertToText() {
        String text = "=== OpenToday Crash ===\n" +
                "CrashID: %_CRASH_ID_%\n" +
                "Application: (%_APPLICATION_PACKAGE_%)\n" +
                " * VERSION_BUILD: %_APPLICATION_VERSION_BUILD_%\n" +
                " * VERSION_NAME: %_APPLICATION_VERSION_NAME_%\n" +
                " * DATA_VERSION: %_APPLICATION_DATA_VERSION_%\n" +
                " * DEBUG: %_APPLICATION_DEBUG_%\n" +
                " * DEBUG_TICK_NOTIFICATION: %_APPLICATION_DEBUG_TICK_NOTIFICATION_%\n" +
                " * DEBUG_MAIN_ACTIVITY_START_SLEEP: %_APPLICATION_DEBUG_MAIN_ACTIVITY_START_SLEEP_%\n" +
                " * DEBUG_APP_START_SLEEP: %_APPLICATION_DEBUG_APP_START_SLEEP_%\n" +
                " * DEBUG_MAIN_ACTIVITY: %_APPLICATION_DEBUG_MAIN_ACTIVITY_%\n" +
                " * DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY: %_APPLICATION_DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY_%\n" +
                "\n" +
                "Device:\n" +
                " * SDK_INT: %_DEVICE_ANDROID_SDK_INT_%\n" +
                " * BASE_OS: %_DEVICE_ANDROID_BASE_OS_%\n" +
                "\n" +
                "Time:\n" +
                "* Formatted: %_TIME_FORMATTED_%\n" +
                "* Millis: %_TIME_MILLIS_%\n" +
                "* Nano: %_TIME_NANO_%\n" +
                "\n" +
                "Thread: %_THREAD_%\n" +
                "Throwable:\n" +
                "%_THROWABLE_%\n" +
                "--- OpenToday Crash ---\n";

        String timeFormatted;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.US);
            timeFormatted = dateFormat.format(new Date(this.crashTimeMillis));

        } catch (Exception e) {
            timeFormatted = "(Unknown: " + e + ")";
        }

        String throwableText;
        try {
            if (this.throwable == null) {
                throwableText = "null";
            } else {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                this.throwable.printStackTrace(pw);
                pw.flush();
                throwableText = sw.toString();
            }
        } catch (Exception e) {
            throwableText = "(Unknown: " + e + ")";
        }

        text = text.replace("%_CRASH_ID_%", (this.id == null ? "null" : this.id.toString()));
        text = text.replace("%_APPLICATION_PACKAGE_%", App.APPLICATION_ID);
        text = text.replace("%_APPLICATION_VERSION_BUILD_%", String.valueOf(App.VERSION_CODE));
        text = text.replace("%_APPLICATION_VERSION_NAME_%", App.VERSION_NAME);
        text = text.replace("%_APPLICATION_DATA_VERSION_%", String.valueOf(App.APPLICATION_DATA_VERSION));
        text = text.replace("%_APPLICATION_DEBUG_%", String.valueOf(App.DEBUG));
        text = text.replace("%_APPLICATION_DEBUG_TICK_NOTIFICATION_%", String.valueOf(App.DEBUG_TICK_NOTIFICATION));
        text = text.replace("%_APPLICATION_DEBUG_MAIN_ACTIVITY_START_SLEEP_%", String.valueOf(App.DEBUG_MAIN_ACTIVITY_START_SLEEP));
        text = text.replace("%_APPLICATION_DEBUG_APP_START_SLEEP_%", String.valueOf(App.DEBUG_APP_START_SLEEP));
        text = text.replace("%_APPLICATION_DEBUG_MAIN_ACTIVITY_%", String.valueOf(App.DEBUG_MAIN_ACTIVITY));
        text = text.replace("%_APPLICATION_DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY_%", String.valueOf(App.DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY));
        text = text.replace("%_TIME_FORMATTED_%", timeFormatted);
        text = text.replace("%_TIME_MILLIS_%", String.valueOf(this.crashTimeMillis));
        text = text.replace("%_TIME_NANO_%", String.valueOf(this.crashTimeNano));
        ;
        text = text.replace("%_THREAD_%", this.thread != null ? this.thread.toString() : "null");
        text = text.replace("%_THROWABLE_%", throwableText);
        text = text.replace("%_DEVICE_ANDROID_SDK_INT_%", String.valueOf(Build.VERSION.SDK_INT));
        text = text.replace("%_DEVICE_ANDROID_BASE_OS_%", String.valueOf(Build.VERSION.BASE_OS));

        return text;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Thread getThread() {
        return thread;
    }
}

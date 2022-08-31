package ru.fazziclay.opentoday.app;

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
                "Application:\n" +
                " * Build: %_APPLICATION_VERSION_BUILD_%\n" +
                " * Name: %_APPLICATION_VERSION_NAME_%\n" +
                " * DEBUG: %_APPLICATION_DEBUG_%\n" +
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
        text = text.replace("%_APPLICATION_VERSION_BUILD_%", String.valueOf(App.VERSION_CODE));
        text = text.replace("%_APPLICATION_VERSION_NAME_%", App.VERSION_NAME);
        text = text.replace("%_APPLICATION_DEBUG_%", String.valueOf(App.DEBUG));
        text = text.replace("%_TIME_FORMATTED_%", timeFormatted);
        text = text.replace("%_TIME_MILLIS_%", String.valueOf(this.crashTimeMillis));
        text = text.replace("%_TIME_NANO_%", String.valueOf(this.crashTimeNano));

        text = text.replace("%_THREAD_%", this.thread != null ? this.thread.toString() : "null");
        text = text.replace("%_THROWABLE_%", throwableText);

        return text;
    }
}

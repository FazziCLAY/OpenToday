package ru.fazziclay.opentoday.app;

import android.os.Build;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import ru.fazziclay.javaneoutil.NonNull;
import ru.fazziclay.opentoday.util.L;

public class CrashReport {
    private final UUID id;
    private final Thread thread;
    private final Throwable throwable;
    private final long crashTimeMillis;
    private final long crashTimeNano;
    private final Map<Thread, StackTraceElement[]> allStackTraces;
    private FatalEnum fatal = FatalEnum.UNKNOWN;

    public static CrashReport create(Throwable throwable) {
        return create(Thread.currentThread(), throwable, System.currentTimeMillis(), System.nanoTime(), Thread.getAllStackTraces());
    }

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
                "// %_RANDOM_COMMENT_%\n" +
                "CrashID: %_CRASH_ID_%\n" +
                "Fatal: %_FATAL_%\n" +
                "Application: (%_APPLICATION_PACKAGE_%)\n" +
                " * instanceId: %_INSTANCE_ID_%\n" +
                " * VERSION_BUILD: %_APPLICATION_VERSION_BUILD_%\n" +
                " * VERSION_NAME: %_APPLICATION_VERSION_NAME_%\n" +
                " * DATA_VERSION: %_APPLICATION_DATA_VERSION_%\n" +
                " * version file: \n" +
                "%_VERSION_FILE_%\n" +
                " * DEBUG: %_APPLICATION_DEBUG_%\n" +
                " * DEBUG_TICK_NOTIFICATION: %_APPLICATION_DEBUG_TICK_NOTIFICATION_%\n" +
                " * DEBUG_MAIN_ACTIVITY_START_SLEEP: %_APPLICATION_DEBUG_MAIN_ACTIVITY_START_SLEEP_%\n" +
                " * DEBUG_APP_START_SLEEP: %_APPLICATION_DEBUG_APP_START_SLEEP_%\n" +
                " * DEBUG_MAIN_ACTIVITY: %_APPLICATION_DEBUG_MAIN_ACTIVITY_%\n" +
                " * DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY: %_APPLICATION_DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY_%\n" +
                " * featureFlags: %_FEATURE_FLAGS_%\n" +
                "\n" +
                "Device:\n" +
                " * SDK_INT: %_DEVICE_ANDROID_SDK_INT_%\n" +
                " * BASE_OS: %_DEVICE_ANDROID_BASE_OS_%\n" +
                " * Product: %_DEVICE_PRODUCT_%\n" +
                " * Brand: %_DEVICE_BRAND_%\n" +
                " * Model: %_DEVICE_MODEL_%\n" +
                " * Manufacturer: %_DEVICE_MANUFACTURER_%\n" +
                " * Display: %_DEVICE_DISPLAY_%\n" +
                " * Bootloader: %_DEVICE_BOOTLOADER_%\n" +
                "\n" +
                "Time:\n" +
                "* Formatted: %_TIME_FORMATTED_%\n" +
                "* Millis: %_TIME_MILLIS_%\n" +
                "* Nano: %_TIME_NANO_%\n" +
                "\n" +
                "L(debug logger):\n" +
                "%_L_LOGS_%\n" +
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

        String versionFileText;
        try {
            versionFileText = App.get().getVersionData().toString(2);
            String[] r = versionFileText.split("\n");
            StringBuilder temp = new StringBuilder();
            for (String s : r) {
                temp.append(" * * ").append(s).append("\n");
            }
            versionFileText = temp.substring(0, temp.length()-1);

        } catch (Exception e) {
            versionFileText = "(Unknown " + e + ")";
        }

        UUID instanceId = null;
        try {
            instanceId = App.get().getInstanceId();
        } catch (Exception ignored) {}

        String featureFlags;
        try {
            featureFlags = Arrays.toString(App.get().getFeatureFlags());
        } catch (Exception e) {
            featureFlags = "(Unknown: "+e+")";
        }

        text = text.replace("%_INSTANCE_ID_%", (instanceId == null ? "null" : instanceId.toString()));
        text = text.replace("%_RANDOM_COMMENT_%", generateRandomComment());
        text = text.replace("%_FEATURE_FLAGS_%", featureFlags);
        text = text.replace("%_CRASH_ID_%", (this.id == null ? "null" : this.id.toString()));
        text = text.replace("%_FATAL_%", (this.fatal == null ? "null" : this.fatal.name()));
        text = text.replace("%_APPLICATION_PACKAGE_%", App.APPLICATION_ID);
        text = text.replace("%_APPLICATION_VERSION_BUILD_%", String.valueOf(App.VERSION_CODE));
        text = text.replace("%_APPLICATION_VERSION_NAME_%", App.VERSION_NAME);
        text = text.replace("%_APPLICATION_DATA_VERSION_%", String.valueOf(App.APPLICATION_DATA_VERSION));
        text = text.replace("%_VERSION_FILE_%", versionFileText == null ? "null" : versionFileText);
        text = text.replace("%_APPLICATION_DEBUG_%", String.valueOf(App.DEBUG));
        text = text.replace("%_APPLICATION_DEBUG_TICK_NOTIFICATION_%", String.valueOf(App.DEBUG_TICK_NOTIFICATION));
        text = text.replace("%_APPLICATION_DEBUG_MAIN_ACTIVITY_START_SLEEP_%", String.valueOf(App.DEBUG_MAIN_ACTIVITY_START_SLEEP));
        text = text.replace("%_APPLICATION_DEBUG_APP_START_SLEEP_%", String.valueOf(App.DEBUG_APP_START_SLEEP));
        text = text.replace("%_APPLICATION_DEBUG_MAIN_ACTIVITY_%", String.valueOf(App.DEBUG_MAIN_ACTIVITY));
        text = text.replace("%_APPLICATION_DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY_%", String.valueOf(App.DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY));
        text = text.replace("%_TIME_FORMATTED_%", timeFormatted);
        text = text.replace("%_TIME_MILLIS_%", String.valueOf(this.crashTimeMillis));
        text = text.replace("%_TIME_NANO_%", String.valueOf(this.crashTimeNano));
        text = text.replace("%_THREAD_%", this.thread != null ? this.thread.toString() : "null");
        text = text.replace("%_THROWABLE_%", throwableText);
        text = text.replace("%_DEVICE_ANDROID_SDK_INT_%", String.valueOf(Build.VERSION.SDK_INT));
        text = text.replace("%_DEVICE_ANDROID_BASE_OS_%", String.valueOf(Build.VERSION.BASE_OS));
        text = text.replace("%_DEVICE_PRODUCT_%", String.valueOf(Build.PRODUCT));
        text = text.replace("%_DEVICE_BRAND_%", String.valueOf(Build.BRAND));
        text = text.replace("%_DEVICE_MODEL_%", String.valueOf(Build.MODEL));
        text = text.replace("%_DEVICE_MANUFACTURER_%", String.valueOf(Build.MANUFACTURER));
        text = text.replace("%_DEVICE_DISPLAY_%", String.valueOf(Build.DISPLAY));
        text = text.replace("%_DEVICE_BOOTLOADER_%", String.valueOf(Build.BOOTLOADER));

        String loggerLLogs;
        try {
            loggerLLogs = L.getInstance().getFinalText();
        } catch (Exception e) {
            loggerLLogs = "(Unknown " + e + ")";
        }
        text = text.replace("%_L_LOGS_%", loggerLLogs != null ? loggerLLogs : "null");

        return text;
    }

    @NonNull
    private String generateRandomComment() {
        String result = "HelloWorld";

        String[] comments = new String[] {
                // 2022.10.29
                "FazziCLAY genius",
                "@FazziCLAY",
                "Marvel cool",
                "this is 2022.10.29?????",
                "truban skyblock",
                "Minecraft feature this random comment: noooo :)",
                "v0.9.7.3 added this feature",
                "allStackTraces?????????????????????????????????",
                "Sorry please",

                // 2022.10.30
                "FeatureFlags from mc1.20????????// :)",

                // 2022.11.01
                "Big changes 2022.11.01: The world big commit :)",
        };
        Random random = new Random();
        int max = comments.length;
        int pos = random.nextInt(max);
        if (pos < 0) {
            pos = pos * -1;
        }
        try {
            result = comments[pos];
        } catch (Exception ignored) {}

        if (random.nextBoolean()) {
            if (random.nextBoolean()) {
                result = result + " :)";
            } else {
                if (random.nextBoolean()) {
                    result = result + " :(";
                } else {
                    result = result + " :/";
                }
            }
        }

        return result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Thread getThread() {
        return thread;
    }

    public void setFatal(FatalEnum fatal) {
        this.fatal = fatal;
    }

    public FatalEnum getFatal() {
        return fatal;
    }

    public enum FatalEnum {
        UNKNOWN,
        YES,
        NO;

        @NonNull
        public static FatalEnum fromBoolean(boolean b) {
            return b ? YES : NO;
        }
    }
}

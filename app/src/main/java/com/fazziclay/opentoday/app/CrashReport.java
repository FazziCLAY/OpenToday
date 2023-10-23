package com.fazziclay.opentoday.app;

import android.os.Build;

import com.fazziclay.javaneoutil.NonNull;
import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.util.time.TimeUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CrashReport {
    private final UUID id;
    private final Thread thread;
    private final Throwable throwable;
    private final long crashTimeMillis;
    private final long crashTimeNano;
    private final Map<Thread, StackTraceElement[]> allStackTraces;
    private FatalEnum fatal = FatalEnum.UNKNOWN;

    public static CrashReport create(Thread thread, Throwable throwable) {
        return create(thread, throwable, System.currentTimeMillis(), System.nanoTime(), Thread.getAllStackTraces());
    }

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
        String text = """
                === OpenToday Crash ===
                // %_RANDOM_COMMENT_%
                CrashID: %_CRASH_ID_%
                Fatal: %_FATAL_%
                Thread: %_THREAD_%
                
                Time:
                * Formatted: %_TIME_FORMATTED_%
                * Millis: %_TIME_MILLIS_%
                * Nano: %_TIME_NANO_%
                
                == Throwable ==
                %_THROWABLE_%
                
                Application: (%_APPLICATION_PACKAGE_%)
                 * instanceId: %_INSTANCE_ID_%
                 * appStartupTime: %_APP_STARTUP_TIME_%
                 * VERSION_BUILD: %_APPLICATION_VERSION_BUILD_%
                 * VERSION_NAME: %_APPLICATION_VERSION_NAME_%
                 * DATA_VERSION: %_APPLICATION_DATA_VERSION_%
                 * RELEASE_TIME: %_APPLICATION_VERSION_RELEASE_TIME_%
                 * version-data: %_VERSION_DATA_%
                 * build-report: %_BUILD_REPORT_%
                 * DEBUG: %_APPLICATION_DEBUG_%
                 * DEBUG_TICK_NOTIFICATION: %_APPLICATION_DEBUG_TICK_NOTIFICATION_%
                 * DEBUG_MAIN_ACTIVITY_START_SLEEP: %_APPLICATION_DEBUG_MAIN_ACTIVITY_START_SLEEP_%
                 * DEBUG_APP_START_SLEEP: %_APPLICATION_DEBUG_APP_START_SLEEP_%
                 * DEBUG_MAIN_ACTIVITY: %_APPLICATION_DEBUG_MAIN_ACTIVITY_%
                 * DEBUG_TEST_EXCEPTION_ON_LAUNCH: %_DEBUG_TEST_EXCEPTION_ON_LAUNCH_%
                 * featureFlags: %_FEATURE_FLAGS_%

                Device:
                 * SDK_INT: %_DEVICE_ANDROID_SDK_INT_%
                 * BASE_OS: %_DEVICE_ANDROID_BASE_OS_%
                 * Product: %_DEVICE_PRODUCT_%
                 * Brand: %_DEVICE_BRAND_%
                 * Model: %_DEVICE_MODEL_%
                 * Manufacturer: %_DEVICE_MANUFACTURER_%
                 * Display: %_DEVICE_DISPLAY_%
                 * Bootloader: %_DEVICE_BOOTLOADER_%

                Debug:
                 * App.debug(false): %_DEBUG_RESULT_OF_FALSE_APP_DEBUG_FUNCTION_%
                 * App.debug(true): %_DEBUG_RESULT_OF_TRUE_APP_DEBUG_FUNCTION_%
                %_DEBUG_GET_TEXT_%
                
                CrashReportContext:
                %_CRASH_REPORT_CONTEXT_%
                
                Logger:
                %_L_LOGS_%

                All stack traces & threads:
                %_ALL_STACK_TRACES_%
                --- OpenToday Crash ---
                """;
        text = text.replace("%_CRASH_REPORT_CONTEXT_%", getText(() -> startAllLines("| ", CrashReportContext.getAsString())));
        text = text.replace("%_DEBUG_RESULT_OF_FALSE_APP_DEBUG_FUNCTION_%", getText(() -> App.debug(false)));
        text = text.replace("%_DEBUG_RESULT_OF_TRUE_APP_DEBUG_FUNCTION_%", getText(() -> App.debug(true)));
        text = text.replace("%_APPLICATION_VERSION_RELEASE_TIME_%", getText(() -> App.VERSION_RELEASE_TIME + " (" + TimeUtil.getDebugDate(App.VERSION_RELEASE_TIME*1000) + ")"));
        text = text.replace("%_DEBUG_GET_TEXT_%", getText(() -> startAllLines(" * |", Debug.getDebugInfoText())));
        text = text.replace("%_ALL_STACK_TRACES_%", getText(() -> {
            if (this.allStackTraces == null) {
                return null;
            }
            StringBuilder result = new StringBuilder();
            StringBuilder threads = new StringBuilder("Threads: ");
            for (Thread t : allStackTraces.keySet()) {
                threads.append(t.toString()).append(", ");
            }
            threads.delete(threads.length() - 2, threads.length() - 1);
            result.append(threads).append("\n");
            for (Thread t : allStackTraces.keySet()) {
                StackTraceElement[] stackTrace = allStackTraces.get(t);
                StringBuilder stack = new StringBuilder("StackTrace ").append(t.toString()).append(":");
                if (stackTrace.length == 0) {
                    stack.append(" <empty>\n");
                } else {
                    stack.append("\n");
                }
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                for (StackTraceElement traceElement : stackTrace)
                    pw.println("\tat " + traceElement);
                pw.flush();
                stack.append(sw);

                result.append(stack);
            }

            return result.toString();
        }));
        text = text.replace("%_INSTANCE_ID_%", getText(() -> App.get().getInstanceId().toString()));
        text = text.replace("%_APP_STARTUP_TIME_%", getText(() -> App.get().getAppStartupTime() + "ms"));
        text = text.replace("%_RANDOM_COMMENT_%", getText(this::generateRandomComment));
        text = text.replace("%_FEATURE_FLAGS_%", getText(() -> Arrays.toString(App.get().getFeatureFlags().toArray())));
        text = text.replace("%_CRASH_ID_%", getText(id));
        text = text.replace("%_FATAL_%", getText(fatal));
        text = text.replace("%_APPLICATION_PACKAGE_%", getText(App.APPLICATION_ID));
        text = text.replace("%_APPLICATION_VERSION_BUILD_%", getText(App.VERSION_CODE));
        text = text.replace("%_APPLICATION_VERSION_NAME_%", getText(App.VERSION_NAME));
        text = text.replace("%_APPLICATION_DATA_VERSION_%", getText(App.APPLICATION_DATA_VERSION));
        text = text.replace("%_VERSION_DATA_%", getText(() -> App.get().versionDataPutLatestStart(App.get().getVersionData()).toString()));
        text = text.replace("%_APPLICATION_DEBUG_%", getText(App.DEBUG));
        text = text.replace("%_APPLICATION_DEBUG_TICK_NOTIFICATION_%", getText(Debug.DEBUG_TICK_NOTIFICATION));
        text = text.replace("%_APPLICATION_DEBUG_MAIN_ACTIVITY_START_SLEEP_%", getText(Debug.DEBUG_MAIN_ACTIVITY_START_SLEEP));
        text = text.replace("%_APPLICATION_DEBUG_APP_START_SLEEP_%", getText(Debug.DEBUG_APP_START_SLEEP));
        text = text.replace("%_APPLICATION_DEBUG_MAIN_ACTIVITY_%", getText(Debug.DEBUG_MAIN_ACTIVITY));
        text = text.replace("%_DEBUG_TEST_EXCEPTION_ON_LAUNCH_%", getText(Debug.DEBUG_TEST_EXCEPTION_ON_LAUNCH));
        text = text.replace("%_TIME_FORMATTED_%", getText(() -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS", Locale.ENGLISH);
            return dateFormat.format(new Date(this.crashTimeMillis));
        }));
        text = text.replace("%_TIME_MILLIS_%", getText(this.crashTimeMillis));
        text = text.replace("%_TIME_NANO_%", getText(this.crashTimeNano));
        text = text.replace("%_THREAD_%", getText(this.thread));
        text = text.replace("%_THROWABLE_%", getText(() -> {
            if (this.throwable == null) {
                return null;
            }

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            this.throwable.printStackTrace(pw);
            pw.flush();
            return sw.toString();
        }));
        text = text.replace("%_DEVICE_ANDROID_SDK_INT_%", getText(Build.VERSION.SDK_INT));
        text = text.replace("%_DEVICE_ANDROID_BASE_OS_%", getText(Build.VERSION.BASE_OS));
        text = text.replace("%_DEVICE_PRODUCT_%", getText(Build.PRODUCT));
        text = text.replace("%_DEVICE_BRAND_%", getText(Build.BRAND));
        text = text.replace("%_DEVICE_MODEL_%", getText(Build.MODEL));
        text = text.replace("%_DEVICE_MANUFACTURER_%", getText(Build.MANUFACTURER));
        text = text.replace("%_DEVICE_DISPLAY_%", getText(Build.DISPLAY));
        text = text.replace("%_DEVICE_BOOTLOADER_%", getText(() -> Build.BOOTLOADER));
        text = text.replace("%_L_LOGS_%", getText(() -> startAllLines("| ", App.get().getLogs().toString())));
        text = text.replace("%_BUILD_REPORT_%", getText(com.fazziclay.opentoday.Build::getBuildDebugReport));

        return text;
    }

    private String startAllLines(String start, String text) {
        String[] lines = text.split("\n");
        StringBuilder temp = new StringBuilder();
        for (String line : lines) {
            temp.append(start).append(line).append("\n");
        }
        return temp.substring(0, temp.length()-1);
    }

    private String toFinalText(Object o) {
        if (o == null) return "null";
        try {
            if (o instanceof Enum<?> anEnum) {
                return anEnum.name();
            }
            return String.valueOf(o);
        } catch (Exception e) {
            return exceptionToText(e);
        }
    }

    private String getText(UnstableSupplier<Object> t) {
        try {
            return toFinalText(t.get());
        } catch (Exception e) {
            return exceptionToText(e);
        }
    }

    private String getText(Object t) {
        return toFinalText(t);
    }

    private String exceptionToText(Exception e) {
        if (e == null) return "(Unknown null exception)";
        return "(Unknown Exception: " + e + ")";
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
                /*2022.11.02*/ "feature/optimization",

                //2023.05.27
                "extend-filters so big....",
                "sorry fgu...",
                "await async discord.py",
                "RandomUtil is pretty",
                "FeatureFlags cleanup in v1.1",
                "v.1.1 \"this is a only extend-filters\" kek",
                "DRY in CrashReport?? :))))",
                "DirtRenderer is love",
                "OpenOptimizeMC is love",
                "try also Google keep",

                // 2023.06.04...........
                "ItemManager -> TabsManager",
                "'Zakviel and Minecraft' :love:",

                // 2023.06.17 (08:30 UTC) (date of release v1.1.4)
                "I love OpenToday v1.1.4",
                "I love OpenToday 0.9.............",
                "MathGame added in 1.1.4",
                "CrashReportContext added in 1.1.4",
                "Summer love OpenToday developing.. :cry:",
                "Stretchly, thanks!",

                // 2023.10.22 (15:33 UTC)
                "{no-fun}Celeste - Lena Raine",
                "{no-fun}but, C418 - Excursions",
                "{no-fun}r2I4I",
                "{no-fun}r5T",
                "{no-fun}r1N",
                "{no-fun}r7[SPACE]",
                "This is only tags hehe",
        };
        Random random = new Random();
        int max = comments.length;
        int pos = Math.abs(random.nextInt(max));
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

        if (result.startsWith("{no-fun}")) {
            return (random.nextInt(1000) == 753 ? "oOOoOOoOOoOOo 1000 == 753, AND " : "")+result
                    .replace("{no-fun}", "")
                    .replace(":)", ":(")
                    .replace(":/", ":(");
        }

        if (random.nextInt(1000) == 753) {
            return "OooOOooOOOOOooo 1000 == 753";
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

    private interface UnstableSupplier<T> {
        T get() throws Exception;
    }
}

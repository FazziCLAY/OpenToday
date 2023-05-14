package com.fazziclay.opentoday.util;

import android.util.Log;

import com.fazziclay.opentoday.app.App;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.function.Supplier;

public class Logger {
    private static final String ANDROID_LOG_TAG = "OpenTodayLogger";
    private static final StringBuilder LOGS = new StringBuilder();

    public static <T> T dur(String tag, String message, Supplier<T> supplier) {
        if (!App.LOG) return supplier.get();

        long start = System.currentTimeMillis();
        T val = supplier.get();
        long duration = System.currentTimeMillis() - start;
        i(tag, String.format("%sms: %s", duration, message));
        return val;
    }

    private static void log(String s) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd EEEE HH:mm:ss", Locale.getDefault());
        String time = dateFormat.format(GregorianCalendar.getInstance().getTime());
        LOGS.append("[").append(time).append("] ").append(s).append("\n");
    }

    public static void e(String tag, String m, Throwable e) {
        if (!App.LOG) return;
        Log.e(ANDROID_LOG_TAG, String.format("[%s] %s", tag, m), e);
        log("OTL [" + tag + "] " + m + " E: " + e);
    }

    public static void i(String tag, String m) {
        if (!App.LOG) return;
        Log.i(ANDROID_LOG_TAG, String.format("[%s] %s", tag, m));
        log("OTL [" + tag + "] " + m);
    }

    public static void d(String tag, Object... m) {
        if (!App.LOG) return;
        if (m.length == 1) {
            Log.d(ANDROID_LOG_TAG, String.format("[%s] %s", tag, m[0]));
            log("OTL [" + tag + "] " + m[0]);
        } else {
            StringBuilder s = new StringBuilder();
            for (Object o : m) {
                s.append(o).append(" ");
            }
            Log.d(ANDROID_LOG_TAG, String.format("[%s] %s", tag, s.substring(0, s.length()-1)));
            log("OTL [" + tag + "] " + s.substring(0, s.length()-1));
        }
    }

    public static StringBuilder getLOGS() {
        return LOGS;
    }
}

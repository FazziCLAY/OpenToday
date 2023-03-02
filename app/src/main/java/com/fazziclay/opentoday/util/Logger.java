package com.fazziclay.opentoday.util;

import android.util.Log;

public class Logger {
    public static void e(String tag, String m, Throwable e) {
        Log.e(tag, m, e);
    }

    public static void i(String tag, String m) {
        Log.i(tag, m);
    }

    public static void d(String tag, String m) {
        Log.d(tag, m);
    }
}

package com.fazziclay.opentoday.util;

import android.util.Log;

public class Logger {
    public static void e(String tag, String m, Throwable e) {
        Log.e("OpenTodayLogger", String.format("[%s] %s", tag, m), e);
    }

    public static void i(String tag, String m) {
        Log.i("OpenTodayLogger", String.format("[%s] %s", tag, m));
    }

    public static void d(String tag, Object... m) {
        if (m.length == 1) {
            Log.d("OpenTodayLogger", String.format("[%s] %s", tag, m[0]));
        } else {
            StringBuilder s = new StringBuilder();
            for (Object o : m) {
                s.append(o).append(" ");
            }
            Log.d("OpenTodayLogger", String.format("[%s] %s", tag, s.substring(0, s.length()-1)));
        }
    }
}

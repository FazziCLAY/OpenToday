package com.fazziclay.opentoday.util;

import com.fazziclay.opentoday.app.App;

public class DebugUtil {
    public static void sleep(int millis) {
        if (!App.DEBUG) return;
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ignored) {}
        }
    }
}

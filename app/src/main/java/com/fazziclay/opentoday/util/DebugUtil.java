package com.fazziclay.opentoday.util;

public class DebugUtil {
    public static void sleep(int millis) {
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ignored) {}
        }
    }
}

package com.fazziclay.opentoday.app;

import androidx.annotation.NonNull;

/**
 * Collect debug info
 */
public class Debug {
    public static int latestTickDuration = -1;
    public static long latestSave = -1;
    public static int latestSaveRequestsCount = -1;

    @NonNull
    public static String getDebugInfoText() {
        return String.format("[Tick] %sms" + "\n" +
                "[Save] %s ago; req=%s",

                latestTickDuration, ago(latestSave), latestSaveRequestsCount);
    }

    public static long ago(long l) {
        return (System.currentTimeMillis() - l) / 1000;
    }


    public static void saved() {
        latestSave = System.currentTimeMillis();
    }
}

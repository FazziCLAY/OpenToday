package com.fazziclay.opentoday;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.App;

/**
 * Collect debug info and other debug constants
 */
public class Debug {
    public static final boolean CUSTOM_ITEMSTABINCLUDE_BACKGROUND = App.debug(false);
    public static final boolean CUSTOM_MAINACTIVITY_BACKGROUND = App.debug(false);

    private static final int DEF = -1;

    public static int latestTickDuration = DEF;
    public static long latestSave = DEF;
    public static int latestSaveRequestsCount = DEF;
    public static int latestPersonalTickDuration = DEF;

    @NonNull
    public static String getDebugInfoText() {
        return String.format("[Tick] %sms; pers: %sms" + "\n" +
                "[Save] %s ago; req=%s",

                latestTickDuration, latestPersonalTickDuration, ago(latestSave), latestSaveRequestsCount);
    }

    public static long ago(long l) {
        return (System.currentTimeMillis() - l) / 1000;
    }


    public static void saved() {
        latestSave = System.currentTimeMillis();
    }
}

package com.fazziclay.opentoday;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.App;

/**
 * Collect debug info
 */
public class Debug {
    // TODO: 5/22/23 w
    public static final boolean CUSTOM_ITEMSTABINCLUDE_BACKGROUND = App.debug(false);
    public static final boolean CUSTOM_MAINACTIVITY_BACKGROUND = App.debug(false);
    public static final boolean CUSTOM_ITEMSEDITORROOTFRAGMENT_BACKGROUND = true;

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

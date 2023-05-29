package com.fazziclay.opentoday;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.App;

/**
 * Collect debug info and other debug constants
 */
public class Debug {
    public static final boolean CUSTOM_ITEMSTABINCLUDE_BACKGROUND = App.debug(false);
    public static final boolean CUSTOM_MAINACTIVITY_BACKGROUND = App.debug(false);
    public static final boolean SHOW_PATH_TO_ITEM_ON_ITEMTEXT = App.debug(false);

    public static final int DEF = -1;

    public static int latestTickDuration = DEF;
    public static int latestPersonalTickDuration = DEF;
    public static long latestSave = DEF;
    public static int latestSaveRequestsCount = DEF;
    public static long appStartupTime = DEF;
    public static Object itemsStorageToolbarContext = DEF;

    @NonNull
    public static String getDebugInfoText() {
        return String.format("""
                        [Tick] %sms; pers: %sms
                        [Save] %s ago; req=%s
                        [App] %sms
                        [Toolbar] ctx=%s""",

                latestTickDuration, latestPersonalTickDuration, ago(latestSave), latestSaveRequestsCount, appStartupTime, itemsStorageToolbarContext);
    }

    public static void free() {
        itemsStorageToolbarContext = null;
    }

    public static long ago(long l) {
        return (System.currentTimeMillis() - l) / 1000;
    }


    public static void saved() {
        latestSave = System.currentTimeMillis();
    }
}

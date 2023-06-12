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
    public static final boolean SHOW_ID_ON_ITEMTEXT = App.debug(true);

    public static final int DEF = -1;

    public static long latestTick = DEF;
    public static Object itemTextEditor = null;
    private static long latestPersonalTick = DEF;
    public static int latestTickDuration = DEF;
    public static int latestPersonalTickDuration = DEF;
    public static long latestSave = DEF;
    public static int latestSaveRequestsCount = DEF;
    public static long appStartupTime = DEF;
    public static long mainActivityStartupTime = DEF;
    public static Object itemsStorageToolbarContext = null;

    @NonNull
    public static String getDebugInfoText() {
        return String.format("""
                        $[@bold;-#ffff00][Tick]$[||] %s ago %sms; Personal: %s ago %sms
                        [Save] %s ago; req=%s
                        [App] init=%sms
                        [GUI] %sms; Toolbar-ctx=$[@italic;-#0055ff]%s$[||]
                        [ItemTextEditor] %s""",

                ago(latestTick), latestTickDuration, ago(latestPersonalTick), latestPersonalTickDuration,
                ago(latestSave), latestSaveRequestsCount,
                appStartupTime,
                mainActivityStartupTime, itemsStorageToolbarContext,
                itemTextEditor);
    }

    public static void free() {
        final String D = "(Debug.free() called)";
        if (itemsStorageToolbarContext != null) itemsStorageToolbarContext = D;
        if (itemTextEditor != null) itemTextEditor = D;
    }

    public static long ago(long l) {
        if (l <= 0) return l;
        return (System.currentTimeMillis() - l) / 1000;
    }


    public static void saved() {
        latestSave = System.currentTimeMillis();
    }

    public static void ticked() {
        latestTick = System.currentTimeMillis();
    }

    public static void tickedPersonal() {
        latestPersonalTick = System.currentTimeMillis();
    }
}

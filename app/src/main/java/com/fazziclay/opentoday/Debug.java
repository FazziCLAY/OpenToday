package com.fazziclay.opentoday;

import android.os.Build;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.App;

/**
 * Collect debug info and other debug constants
 */
public class Debug {
    public static final boolean CUSTOM_ITEMSTABINCLUDE_BACKGROUND = App.debug(false);
    public static final boolean CUSTOM_MAINACTIVITY_BACKGROUND = App.debug(false);
    public static final boolean SHOW_PATH_TO_ITEM_ON_ITEMTEXT = App.debug(false);
    public static final boolean SHOW_ID_ON_ITEMTEXT = App.debug(false);
    public static final boolean SHOW_GEN_ID_ON_ITEMTEXT = App.debug(true);
    public static final boolean DESTROY_ANY_TEXTITEM_CHILD = App.debug(true);

    public static final int DEF = -1;

    public static long latestTick = DEF;
    public static int latestTickDuration = DEF;
    private static Object latestTickSession = null;
    public static long tickedItems = DEF;
    private static long latestPersonalTick = DEF;
    public static int latestPersonalTickDuration = DEF;
    private static Object latestPersonalTickSession = null;
    public static long latestSave = DEF;
    public static int latestSaveRequestsCount = DEF;
    public static long appStartupTime = DEF;
    public static long mainActivityStartupTime = DEF;
    public static Object itemsStorageToolbarContext = null;
    public static Object itemTextEditor = null;

    @NonNull
    public static String getDebugInfoText() {
        String androidReleaseOrCodename;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            androidReleaseOrCodename = Build.VERSION.RELEASE_OR_CODENAME;
        } else {
            androidReleaseOrCodename = "sdk<R(30)";
        }
        return String.format("""
                        OpenToday %s; Branch: %s
                        [Android] ((int)sdk)=%s relOrCod=%s
                        $[@bold;-#ffff00][Tick]$[||] %s ago %sms all=%s; Personal: %s ago %sms
                        $[-#ffa000]%s$[||]; personal=$[-fff000]%s$[||]
                        [Save] %s ago; req=%s
                        [App] init=%sms
                        [GUI] %sms; Toolbar-ctx=$[@italic;-#0055ff]%s$[||]
                        [ItemTextEditor] %s""",

                App.VERSION_NAME, App.VERSION_BRANCH,
                Build.VERSION.SDK_INT, androidReleaseOrCodename,
                ago(latestTick), latestTickDuration, tickedItems, ago(latestPersonalTick), latestPersonalTickDuration,
                latestTickSession, latestPersonalTickSession,
                ago(latestSave), latestSaveRequestsCount,
                appStartupTime,
                mainActivityStartupTime, itemsStorageToolbarContext,
                itemTextEditor);
    }

    public static void free() {
        final String D = "(Debug.free() called)";
        if (itemsStorageToolbarContext != null) itemsStorageToolbarContext = D;
        if (itemTextEditor != null) itemTextEditor = D;
        if (latestTickSession != null) latestTickSession = D;
        if (latestPersonalTickSession != null) latestPersonalTickSession = D;
    }

    public static long ago(long l) {
        if (l <= 0) return l;
        return (System.currentTimeMillis() - l) / 1000;
    }


    public static void saved() {
        latestSave = System.currentTimeMillis();
    }

    public static void ticked(Object tickSession) {
        latestTick = System.currentTimeMillis();
        latestTickSession = tickSession;
    }

    public static void tickedPersonal(Object tickSession) {
        latestPersonalTick = System.currentTimeMillis();
        latestPersonalTickSession = tickSession;
    }
}

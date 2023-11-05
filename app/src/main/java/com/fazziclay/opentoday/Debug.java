package com.fazziclay.opentoday;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.debug.TestItemViewGenerator;

/**
 * Collect debug info and other debug constants
 */
public class Debug {
    public static final boolean DEBUG_TICK_NOTIFICATION = App.debug(false);
    public static final int DEBUG_MAIN_ACTIVITY_START_SLEEP = App.debug(false) ? 6000 : 0;
    public static final int DEBUG_APP_START_SLEEP = App.debug(false) ? 8000 : 0;
    public static Class<? extends Activity> DEBUG_MAIN_ACTIVITY = App.debug(false) ? TestItemViewGenerator.class : null;
    public static final boolean DEBUG_TEST_EXCEPTION_ON_LAUNCH = false;
    public static final boolean DEBUG_IMPORTANT_NOTIFICATIONS = App.debug(false);
    public static final boolean DEBUG_ALWAYS_SHOW_UI_NOTIFICATIONS = App.debug(false);
    public static final boolean DEBUG_LOG_ALL_IN_MAINACTIVITY = App.debug(false);
    public static final boolean DEBUG_NETWORK_UTIL_SHADOWCONTENT = App.debug(false);

    public static final boolean CUSTOM_ITEMSTABINCLUDE_BACKGROUND = App.debug(false);
    public static final boolean CUSTOM_MAINACTIVITY_BACKGROUND = App.debug(false);
    public static final boolean SHOW_PATH_TO_ITEM_ON_ITEMTEXT = App.debug(false);
    public static final boolean SHOW_ID_ON_ITEMTEXT = App.debug(false);
    public static final boolean SHOW_GEN_ID_ON_ITEMTEXT = App.debug(false);
    public static final boolean DESTROY_ANY_TEXTITEM_CHILD = App.debug(false);
    public static final boolean SHOW_DEBUG_MESSAGE_TOAST_IN_ITEMSTICKRECEIVER = App.debug(false);

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
                        $[@bold;-#33ff33]OpenToday$[@reset] %s; $[-#ff33ff]Branch: %s
                        $[-#00ffff][Android] ((int)sdk)=%s relOrCod=%s
                        $[@bold;-#ffff00][Tick]$[||] %s ago %sms all=%s; Personal: %s ago %sms
                        $[-#ffa000]%s$[||]; personal=$[-fff000]%s$[||]
                        [Save] %s ago; req=%s
                        [App] init=%sms
                        [GUI] %sms; Toolbar-ctx=$[@italic;-#00bbff;=#000000]%s$[||]
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

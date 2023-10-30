package com.fazziclay.opentoday.gui;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.util.profiler.Profiler;

public class GUILauncher {
    private static boolean backInitialized = false;
    private static boolean backInitializeInProcess = false;

    public static boolean isWaitGuiForBack() {
        return !backInitialized && backInitializeInProcess;
    }

    public static void startBackInitializerThread() {
        backInitializeInProcess = true;
        new BackInitializerThread().start();
    }

    private static class BackInitializerThread extends Thread {
        private static final Profiler PROFILER = App.createProfiler("GUILauncher:BackInitializerThread");

        public BackInitializerThread() {
            setName("BackInitializerThread");
        }

        @Override
        public void run() {
            PROFILER.push("run");
            PROFILER.push("app.get");
            App app = App.get();
            PROFILER.swap("settings");
            app.getSettingsManager();
            PROFILER.swap("tabs");
            app.getTabsManager();
            PROFILER.swap("beatify_color");
            app.getBeautifyColorManager();
            PROFILER.swap("item_notification_handler");
            app.getItemNotificationHandler();
            PROFILER.swap("color_history");
            app.getColorHistoryManager();
            PROFILER.swap("important_debug_callbacks");
            app.getImportantDebugCallbacks();
            PROFILER.swap("pin_code");
            app.getPinCodeManager();
            PROFILER.swap("selection_manager");
            app.getSelectionManager();
            backInitialized = true;
            backInitializeInProcess = false;
            PROFILER.pop2();
        }
    }
}

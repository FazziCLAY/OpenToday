package com.betterbrainmemory.opentoday.gui;

import android.util.ArraySet;

import androidx.annotation.NonNull;

import com.betterbrainmemory.opentoday.app.App;
import com.betterbrainmemory.opentoday.app.CrashReport;
import com.betterbrainmemory.opentoday.app.CrashReportContext;
import com.betterbrainmemory.opentoday.app.items.item.ItemsRegistry;
import com.betterbrainmemory.opentoday.gui.item.registry.ItemsGuiRegistry;
import com.betterbrainmemory.opentoday.util.StreamUtil;
import com.betterbrainmemory.opentoday.util.ThrowableRunnable;
import com.betterbrainmemory.opentoday.util.profiler.Profiler;

import java.util.Set;

public class BackendInitializer {
    private static boolean backInitialized = false;
    private static boolean backInitializeInProcess = false;
    private static final Set<Module> modulesInitialized = new ArraySet<>();

    public static boolean isWaitGuiForBack() {
        return !backInitialized && backInitializeInProcess; // if not initializED and initializING
    }

    public static boolean isWaitForModule(Module module) {
        return isWaitGuiForBack() && !modulesInitialized.contains(module);
    }

    public static void startBackInitializerThread() {
        if (!backInitialized && !backInitializeInProcess) {
            backInitializeInProcess = true;
            new BackInitializerThread().start();
        }
    }

    private static class BackInitializerThread extends Thread {
        private static final Profiler PROFILER = App.createProfiler("GUILauncher:BackInitializerThread");

        public BackInitializerThread() {
            setName("BackInitializerThread");
            setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                App.crash(null, CrashReport.create(thread, throwable), false);
                backInitializeInProcess = false;
            });
        }

        public void init(@NonNull Module m, @NonNull ThrowableRunnable r) {
            CrashReportContext.setBackendInitializerState("Module="+m.name());
            try {
                PROFILER.push(m.name());
                r.run();
                PROFILER.swap("update_modules_set");
                modulesInitialized.add(m);
                PROFILER.pop();
            } catch (Exception e) {
                throw new RuntimeException("Exception while init module " + m, e);
            }
        }

        @Override
        public void run() {
            PROFILER.push("run");
            PROFILER.push("app.get");
            App app = App.get();
            PROFILER.pop();

            init(Module.REGISTRIES, () -> {
                // Backend items
                ItemsRegistry.REGISTRY.initializeDefault(() -> {
                    final String jsonBackendItems = StreamUtil.read(App.get().getAssets().open("base/items.json"));
                    ItemsRegistry.REGISTRY.appendRegistryFromJson(jsonBackendItems);
                });

                // GUI Items
                ItemsGuiRegistry.REGISTRY.initializeDefault(() -> {
                    final String jsonGuiItems = StreamUtil.read(App.get().getAssets().open("base/gui/items_guis.json"));
                    ItemsGuiRegistry.REGISTRY.appendFromJson(jsonGuiItems);
                });
            });
            init(Module.SETTINGS_MANAGER, app::getSettingsManager);
            init(Module.PLUGINS, app::initPlugins);
            init(Module.TABS_MANAGER, app::getTabsManager);
            init(Module.ITEM_NOTIFICATION_HANDLER, app::getItemNotificationHandler);
            init(Module.IMPORTANT_DEBUG_CALLBACKS, app::getImportantDebugCallbacks);
            init(Module.SELECTION_MANAGER, app::getSelectionManager);
            init(Module.BEATIFY_COLOR_MANAGER, app::getBeautifyColorManager);
            init(Module.COLOR_HISTORY_MANAGER, app::getColorHistoryManager);

            CrashReportContext.setBackendInitializerState("Done");
            backInitialized = true;
            backInitializeInProcess = false;
            PROFILER.pop();
        }
    }

    public enum Module {
        REGISTRIES,
        PLUGINS,
        TABS_MANAGER,
        SETTINGS_MANAGER,
        COLOR_HISTORY_MANAGER,
        BEATIFY_COLOR_MANAGER,
        ITEM_NOTIFICATION_HANDLER,
        IMPORTANT_DEBUG_CALLBACKS,
        PIN_CODE_MANAGER,
        SELECTION_MANAGER
    }
}

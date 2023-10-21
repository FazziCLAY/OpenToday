package com.fazziclay.opentoday.api;

import java.util.HashMap;

public class PluginManager {
    private static final HashMap<String, OpenTodayPlugin> activePlugins = new HashMap<>();

    public static void loadPlugin(String packageId, OpenTodayPlugin plugin) {
        disablePlugin(packageId);
        plugin.onEnable();
        activePlugins.put(packageId, plugin);
    }

    public static void disablePlugin(String packageId) {
        OpenTodayPlugin openTodayPlugin = activePlugins.get(packageId);
        if (openTodayPlugin != null) {
            openTodayPlugin.onDisable();
            activePlugins.remove(packageId);
        }
    }

    public static void callEvent(Event event) {
        activePlugins.forEach((s, plugin) -> {
            for (EventHandler eventHandler : plugin.getEventHandlers()) {
                eventHandler.handle(event);
            }
        });
    }
}

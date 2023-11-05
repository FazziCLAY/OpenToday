package com.fazziclay.opentoday.api;

import java.util.HashMap;

public class PluginManager {
    private static final HashMap<String, OpenTodayPlugin> activePlugins = new HashMap<>();

    public static void loadPlugin(String packageId, OpenTodayPlugin plugin) {
        disablePlugin(packageId);
        activePlugins.put(packageId, plugin);
        plugin.onEnable();
    }

    public static void disablePlugin(String packageId) {
        OpenTodayPlugin openTodayPlugin = activePlugins.get(packageId);
        if (openTodayPlugin != null) {
            openTodayPlugin.onDisable();
            activePlugins.remove(packageId);
        }
    }

    public static void disableAllPlugins() {
        if (activePlugins.isEmpty()) return;
        for (String s : activePlugins.keySet()) {
            disablePlugin(s);
        }
    }

    public static void callEvent(Event event) {
        activePlugins.forEach((s, plugin) -> {
            try {
                for (EventHandler eventHandler : plugin.getEventHandlers()) {
                    eventHandler.handle(event);
                }
            } catch (Exception e) {
                throw new PluginException("Exception while process eventHandlers in \"" + s + "\"(" + plugin + ") plugin", e, plugin);
            }
        });
    }

    public static OpenTodayPlugin getActivePlugin(String key) {
        return activePlugins.get(key);
    }

    public static <T extends OpenTodayPlugin> T getActivePlugin(Class<T> clazz) {
        for (OpenTodayPlugin value : activePlugins.values()) {
            if (value.getClass() == clazz) {
                return (T) value;
            }
        }
        return null;
    }

    public static boolean isPluginActive(String key) {
        return getActivePlugin(key) != null;
    }
}

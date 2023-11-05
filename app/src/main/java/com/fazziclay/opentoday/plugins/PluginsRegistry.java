package com.fazziclay.opentoday.plugins;

import com.fazziclay.opentoday.api.OpenTodayPlugin;
import com.fazziclay.opentoday.plugins.gcp.GlobalChangesPlugin;
import com.fazziclay.opentoday.plugins.islp.ItemsSupportLibraryPlugin;

public class PluginsRegistry {
    public static final PluginsRegistry REGISTRY = new PluginsRegistry();

    private final PluginInfo[] PLUGINS = new PluginInfo[]{
            new PluginInfo("gcp", "fazziclay://opentoday/plugins/global_changes_plugin", GlobalChangesPlugin.class, "FazziCLAY", "1.0", new String[]{"islp"}),
            new PluginInfo("islp", "fazziclay://opentoday/plugins/items_support_library_plugin", ItemsSupportLibraryPlugin.class, "FazziCLAY", "1.0", new String[]{}),
    };

    private PluginsRegistry() {
    }

    public static class PluginInfo {
        private final String shortName;
        private final String packageId;
        private final Class<? extends OpenTodayPlugin> clazz;
        private final String author;
        private final String version;
        private final String[] depends;

        public PluginInfo(String shortName, String packageId, Class<? extends OpenTodayPlugin> clazz, String author, String version, String[] depends) {
            this.shortName = shortName;
            this.packageId = packageId;
            this.clazz = clazz;
            this.author = author;
            this.version = version;
            this.depends = depends;
        }

        public String getShortName() {
            return shortName;
        }

        public String getPackageId() {
            return packageId;
        }

        public Class<? extends OpenTodayPlugin> getClazz() {
            return clazz;
        }

        public String getAuthor() {
            return author;
        }

        public String getVersion() {
            return version;
        }

        public String[] getDepends() {
            return depends;
        }
    }

    public PluginInfo getByShortName(String name) {
        for (PluginInfo plugin : PLUGINS) {
            if (plugin.getShortName().equalsIgnoreCase(name)) return plugin;
        }
        return null;
    }
}

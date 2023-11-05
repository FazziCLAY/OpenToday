package com.fazziclay.opentoday.plugins.gcp;

import com.fazziclay.opentoday.api.EventHandler;
import com.fazziclay.opentoday.api.OpenTodayPlugin;
import com.fazziclay.opentoday.api.PluginManager;
import com.fazziclay.opentoday.plugins.PluginsRegistry;
import com.fazziclay.opentoday.plugins.islp.ItemsSupportLibraryPlugin;
import com.fazziclay.opentoday.util.Logger;

public class GlobalChangesPlugin extends OpenTodayPlugin {
    private static final String TAG = "plugin://GlobalChangesPlugin";

    private ItemsSupportLibraryPlugin islp;
    private final EventHandler[] handlers = new EventHandler[]{new GcpEventHandler(this)};

    @Override
    public void onEnable() {
        super.onEnable();
        islp = (ItemsSupportLibraryPlugin) PluginManager.getActivePlugin(PluginsRegistry.REGISTRY.getByShortName("islp").getPackageId());
        Logger.d(TAG, "gcp plugin is enabled!");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Logger.d(TAG, "gcp plugin is disabled!");
    }

    @Override
    public EventHandler[] getEventHandlers() {
        return handlers;
    }

    public ItemsSupportLibraryPlugin getIslp() {
        return islp;
    }
}

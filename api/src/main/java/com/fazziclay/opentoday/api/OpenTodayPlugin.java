package com.fazziclay.opentoday.api;

public abstract class OpenTodayPlugin {
    public void onEnable() {}
    public void onDisable() {}

    public EventHandler[] getEventHandlers() {
        return new EventHandler[0];
    }
}

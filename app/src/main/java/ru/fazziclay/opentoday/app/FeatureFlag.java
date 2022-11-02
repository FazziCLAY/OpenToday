package ru.fazziclay.opentoday.app;

public enum FeatureFlag {
    ITEM_DEBUG_TICK_COUNTER("Available new item: DebugTickCounterItem in items lists"),
    ITEM_EDITOR_SHOW_COPY_ID_BUTTON("New button 'Copy item id' in editor"),
    AVAILABLE_LOGS_OVERLAY("New button in (Toolbar -> OpenToday): 'toggle logs overlay'"),
    NONE("NONE flag"),
    SHOW_APP_STARTUP_TIME_IN_PREMAIN_ACTIVITY("PreMainActivity: show toast: App.getAppStartupTime"),
    SHOW_MAINACTIVITY_STARTUP_TIME("MainActivity: startup time show in Toast"),
    ALWAYS_SHOW_SAVE_STATUS("ItemManager: always show save status in toast");

    private final String description;

    FeatureFlag(String s) {
        description = s;
    }

    public String getDescription() {
        return description;
    }
}

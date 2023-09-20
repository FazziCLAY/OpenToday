package com.fazziclay.opentoday.app;

public enum FeatureFlag {
    ITEM_DEBUG_TICK_COUNTER("Show hidden debug item: DebugTickCounter"),
    ALWAYS_SHOW_SAVE_STATUS("Always show save status on TabsManager using Toast"),
    DISABLE_AUTOMATIC_TICK("Disable auto-tick calling by MainActivity"),
    DISABLE_DEBUG_MODE_NOTIFICATION("Disable debug-app-warning in MainActivity"),
    TOOLBAR_DEBUG("Show hidden DEBUG section in Toolbar"),
    ITEM_SLEEP_TIME("Show hidden item: SleepTime");

    private final String description;

    FeatureFlag(String s) {
        description = s;
    }

    public String getDescription() {
        return description;
    }
}

package ru.fazziclay.opentoday.app;

public enum FeatureFlag {
    ITEM_DEBUG_TICK_COUNTER("Available new item: DebugTickCounterItem in items lists"),
    ITEM_EDITOR_SHOW_COPY_ID_BUTTON("New button 'Copy item id' in editor"),
    AVAILABLE_LOGS_OVERLAY("New button in (Toolbar -> OpenToday): 'toggle logs overlay'"),
    NONE("NONE flag");

    private final String description;

    FeatureFlag(String s) {
        description = s;
    }
}

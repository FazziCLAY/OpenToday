package com.fazziclay.opentoday.gui.item;

import com.fazziclay.opentoday.app.SettingsManager;

public abstract class SettingsItemsStorageDrawerBehavior implements ItemsStorageDrawerBehavior {
    private final SettingsManager settingsManager;


    public SettingsItemsStorageDrawerBehavior(final SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @Override
    public SettingsManager.ItemAction getItemOnClickAction() {
        return settingsManager.getItemOnClickAction();
    }

    @Override
    public boolean isScrollToAddedItem() {
        return settingsManager.isScrollToAddedItem();
    }

    @Override
    public SettingsManager.ItemAction getItemOnLeftAction() {
        return settingsManager.getItemOnLeftAction();
    }
}

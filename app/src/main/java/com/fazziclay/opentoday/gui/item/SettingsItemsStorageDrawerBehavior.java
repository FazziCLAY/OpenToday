package com.fazziclay.opentoday.gui.item;

import com.fazziclay.opentoday.app.settings.enums.ItemAction;
import com.fazziclay.opentoday.app.settings.SettingsManager;

public abstract class SettingsItemsStorageDrawerBehavior implements ItemsStorageDrawerBehavior {
    private final SettingsManager settingsManager;


    public SettingsItemsStorageDrawerBehavior(final SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @Override
    public ItemAction getItemOnClickAction() {
        return settingsManager.getItemOnClickAction();
    }

    @Override
    public boolean isScrollToAddedItem() {
        return settingsManager.isScrollToAddedItem();
    }

    @Override
    public ItemAction getItemOnLeftAction() {
        return settingsManager.getItemOnLeftAction();
    }
}

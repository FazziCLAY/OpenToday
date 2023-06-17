package com.fazziclay.opentoday.gui.item;

import com.fazziclay.opentoday.app.SettingsManager;

public interface ItemsStorageDrawerBehavior {
    SettingsManager.ItemAction getItemOnClickAction();

    boolean isScrollToAddedItem();

    SettingsManager.ItemAction getItemOnLeftAction();
}

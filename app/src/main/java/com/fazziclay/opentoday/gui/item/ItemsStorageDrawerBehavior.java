package com.fazziclay.opentoday.gui.item;

import com.fazziclay.opentoday.app.SettingsManager;
import com.fazziclay.opentoday.app.items.item.Item;

public interface ItemsStorageDrawerBehavior {
    SettingsManager.ItemAction getItemOnClickAction();

    boolean isScrollToAddedItem();

    SettingsManager.ItemAction getItemOnLeftAction();

    void onItemOpenEditor(Item item);

    void onItemOpenTextEditor(Item item);

    boolean ignoreFilterGroup();
}

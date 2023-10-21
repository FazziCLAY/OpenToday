package com.fazziclay.opentoday.app.events.gui;

import com.fazziclay.opentoday.api.Event;
import com.fazziclay.opentoday.app.items.ItemsStorage;

public class CurrentItemsStorageContextChanged implements Event {
    private final ItemsStorage currentItemsStorage;

    public CurrentItemsStorageContextChanged(ItemsStorage currentItemsStorage) {
        this.currentItemsStorage = currentItemsStorage;
    }

    public ItemsStorage getCurrentItemsStorage() {
        return currentItemsStorage;
    }
}

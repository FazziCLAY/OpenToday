package com.betterbrainmemory.opentoday.app.events.gui;

import com.betterbrainmemory.opentoday.api.Event;
import com.betterbrainmemory.opentoday.app.items.ItemsStorage;

public class CurrentItemsStorageContextChanged implements Event {
    private final ItemsStorage currentItemsStorage;

    public CurrentItemsStorageContextChanged(ItemsStorage currentItemsStorage) {
        this.currentItemsStorage = currentItemsStorage;
    }

    public ItemsStorage getCurrentItemsStorage() {
        return currentItemsStorage;
    }
}

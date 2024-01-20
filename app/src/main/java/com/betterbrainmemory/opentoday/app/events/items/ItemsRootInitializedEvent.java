package com.betterbrainmemory.opentoday.app.events.items;

import com.betterbrainmemory.opentoday.api.Event;
import com.betterbrainmemory.opentoday.app.items.ItemsRoot;

public class ItemsRootInitializedEvent implements Event {
    private final ItemsRoot itemsRoot;

    public ItemsRootInitializedEvent(ItemsRoot itemsRoot) {
        this.itemsRoot = itemsRoot;
    }

    public ItemsRoot getItemsRoot() {
        return itemsRoot;
    }
}

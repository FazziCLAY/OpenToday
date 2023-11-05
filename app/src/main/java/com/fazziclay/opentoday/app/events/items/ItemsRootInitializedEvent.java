package com.fazziclay.opentoday.app.events.items;

import com.fazziclay.opentoday.api.Event;
import com.fazziclay.opentoday.app.items.ItemsRoot;

public class ItemsRootInitializedEvent implements Event {
    private final ItemsRoot itemsRoot;

    public ItemsRootInitializedEvent(ItemsRoot itemsRoot) {
        this.itemsRoot = itemsRoot;
    }

    public ItemsRoot getItemsRoot() {
        return itemsRoot;
    }
}

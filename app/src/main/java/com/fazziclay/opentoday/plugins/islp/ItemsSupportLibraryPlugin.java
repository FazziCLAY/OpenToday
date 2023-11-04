package com.fazziclay.opentoday.plugins.islp;

import com.fazziclay.opentoday.api.Event;
import com.fazziclay.opentoday.api.EventHandler;
import com.fazziclay.opentoday.api.OpenTodayPlugin;
import com.fazziclay.opentoday.app.events.gui.CurrentItemsStorageContextChanged;
import com.fazziclay.opentoday.app.events.items.ItemsRootDestroyedEvent;
import com.fazziclay.opentoday.app.events.items.ItemsRootInitializedEvent;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.ItemsStorage;

public class ItemsSupportLibraryPlugin extends OpenTodayPlugin {
    private ItemsRoot itemsRoot = null;
    private ItemsStorage currentItemsStorage = null;


    private final EventHandler eventHandler = new EventHandler() {
        @Override
        public void handle(Event event) {
            if (event instanceof ItemsRootInitializedEvent initializedEvent) {
                ItemsSupportLibraryPlugin.this.itemsRoot = initializedEvent.getItemsRoot();

            } else if (event instanceof ItemsRootDestroyedEvent) {
                ItemsSupportLibraryPlugin.this.itemsRoot = null;

            } else if (event instanceof CurrentItemsStorageContextChanged e) {
                currentItemsStorage = e.getCurrentItemsStorage();
            }
        }
    };
    private final EventHandler[] handlers = new EventHandler[]{eventHandler};

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public EventHandler[] getEventHandlers() {
        return handlers;
    }

    public ItemsRoot getItemsRoot() {
        return itemsRoot;
    }

    public ItemsStorage getCurrentItemsStorage() {
        return currentItemsStorage;
    }
}

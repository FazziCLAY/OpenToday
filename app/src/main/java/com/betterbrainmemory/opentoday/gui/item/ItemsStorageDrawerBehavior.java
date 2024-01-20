package com.betterbrainmemory.opentoday.gui.item;

import com.betterbrainmemory.opentoday.app.items.item.Item;
import com.betterbrainmemory.opentoday.app.settings.enums.ItemAction;

public interface ItemsStorageDrawerBehavior {
    ItemAction getItemOnClickAction();

    boolean isScrollToAddedItem();

    ItemAction getItemOnLeftAction();

    void onItemOpenEditor(Item item);

    void onItemOpenTextEditor(Item item);

    boolean ignoreFilterGroup();

    void onItemDeleteRequest(Item item);
}

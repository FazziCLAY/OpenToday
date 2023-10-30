package com.fazziclay.opentoday.gui.item;

import com.fazziclay.opentoday.app.settings.enums.ItemAction;
import com.fazziclay.opentoday.app.items.item.Item;

public interface ItemsStorageDrawerBehavior {
    ItemAction getItemOnClickAction();

    boolean isScrollToAddedItem();

    ItemAction getItemOnLeftAction();

    void onItemOpenEditor(Item item);

    void onItemOpenTextEditor(Item item);

    boolean ignoreFilterGroup();

    void onItemDeleteRequest(Item item);
}

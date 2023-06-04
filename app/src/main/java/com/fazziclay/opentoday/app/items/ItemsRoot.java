package com.fazziclay.opentoday.app.items;

import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.tab.Tab;

import java.util.UUID;

public interface ItemsRoot {
    /**
     * Getting item by ItemID
     * @param id id
     * @return item is exist, is not exist: null
     */
    Item getItemById(UUID id);

    /**
     * Getting tab by TabID
     * @param id id
     * @return tab is exist, is not exist: null
     */
    Tab getTabById(UUID id);

    boolean isExistById(UUID id);

    Type getTypeById(UUID id);

    enum Type {
        TAB,
        ITEM
    }
}

package com.fazziclay.opentoday.app.items.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ItemType {
    MISSING_NO,
    TEXT,
    DEBUG_TICK_COUNTER(TEXT),
    LONG_TEXT(TEXT),
    CHECKBOX(TEXT),
    CHECKBOX_DAY_REPEATABLE(TEXT, CHECKBOX),
    COUNTER(TEXT),
    CYCLE_LIST(TEXT),
    GROUP(TEXT),
    FILTER_GROUP(TEXT),
    MATH_GAME(TEXT),
    SLEEP_TIME(TEXT);

    private final List<ItemType> parents = new ArrayList<>();

    ItemType(ItemType... parents) {
        this.parents.addAll(Arrays.asList(parents));
    }

    public boolean isInherit(ItemType type) {
        if (type == this) return true;
        return this.parents.contains(type);
    }

    public static ItemType byClass(Class<? extends Item> c) {
        ItemsRegistry.ItemInfo itemInfo = ItemsRegistry.REGISTRY.get(c);
        if (itemInfo == null) return null;
        return itemInfo.getItemType();
    }
}

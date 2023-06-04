package com.fazziclay.opentoday.app.items.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ItemType {
    TEXT,
    DEBUG_TICK_COUNTER(TEXT),
    LONG_TEXT(TEXT),
    CHECKBOX(TEXT),
    CHECKBOX_DAY_REPEATABLE(TEXT, CHECKBOX),
    COUNTER(TEXT),
    CYCLE_LIST(TEXT),
    GROUP(TEXT),
    FILTER_GROUP(TEXT),
    MATH_GAME(TEXT);

    private final List<ItemType> parents = new ArrayList<>();

    ItemType(ItemType... parents) {
        this.parents.addAll(Arrays.asList(parents));
    }

    public boolean isInherit(ItemType type) {
        if (type == this) return true;
        return this.parents.contains(type);
    }
}

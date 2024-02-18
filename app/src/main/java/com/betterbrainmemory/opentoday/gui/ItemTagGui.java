package com.betterbrainmemory.opentoday.gui;

import com.betterbrainmemory.opentoday.app.items.tag.ItemTag;

public class ItemTagGui {
    public static String textInChip(ItemTag tag) {
        if (tag.getValue() == null) {
            return tag.getName();
        }
        return tag.getName() + ": " + tag.getValue();
    }
}

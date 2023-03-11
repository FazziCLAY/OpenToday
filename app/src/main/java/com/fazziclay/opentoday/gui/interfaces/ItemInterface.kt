package com.fazziclay.opentoday.gui.interfaces;

import com.fazziclay.opentoday.app.items.item.Item;

@FunctionalInterface
public interface ItemInterface {
    void run(Item item);
}

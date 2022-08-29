package ru.fazziclay.opentoday.ui.other.item;

import ru.fazziclay.opentoday.app.items.item.Item;

@FunctionalInterface
public interface OnItemClick {
    void run(Item item);
}

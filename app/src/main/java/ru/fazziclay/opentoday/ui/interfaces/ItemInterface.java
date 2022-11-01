package ru.fazziclay.opentoday.ui.interfaces;

import ru.fazziclay.opentoday.app.items.item.Item;

@FunctionalInterface
public interface ItemInterface {
    void run(Item item);
}

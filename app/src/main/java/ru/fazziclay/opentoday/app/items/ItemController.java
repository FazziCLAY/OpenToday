package ru.fazziclay.opentoday.app.items;

import ru.fazziclay.opentoday.app.items.item.Item;

public abstract class ItemController {
    public abstract void delete(Item item);
    public abstract void save(Item item);
    public abstract void updateUi(Item item);
}

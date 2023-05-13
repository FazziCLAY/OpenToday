package com.fazziclay.opentoday.app.items.item;

import com.fazziclay.opentoday.app.items.ItemsStorage;

public abstract class ItemController {
    public abstract void delete(Item item);
    public abstract void save(Item item);
    public abstract void updateUi(Item item);
    public abstract ItemsStorage getParentItemsStorage(Item item);
}

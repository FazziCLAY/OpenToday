package com.fazziclay.opentoday.app.items.item;

import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.ItemsStorage;

import java.util.UUID;

public abstract class ItemController {
    public abstract void delete(Item item);
    public abstract void save(Item item);
    public abstract void updateUi(Item item);
    public abstract ItemsStorage getParentItemsStorage(Item item);
    public abstract UUID generateId(Item item);
    public abstract ItemsRoot getRoot();
}

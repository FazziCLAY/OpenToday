package ru.fazziclay.opentoday.app.items;

import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.item.ItemsRegistry;

public class Selection {
    private ItemsStorage itemsStorage;
    private final Item item;

    public Selection(ItemsStorage itemsStorage, Item item) {
        this.itemsStorage = itemsStorage;
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public ItemsStorage getItemStorage() {
        return itemsStorage;
    }

    public void moveToStorage(ItemsStorage l) {
        this.itemsStorage.deleteItem(this.item);
        this.itemsStorage = l;
        this.itemsStorage.addItem(this.item);
    }

    public void copyToStorage(ItemsStorage l) {
        l.addItem(item.copy());
    }
}

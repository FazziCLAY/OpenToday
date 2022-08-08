package ru.fazziclay.opentoday.app.items;

import ru.fazziclay.opentoday.app.items.item.Item;

public class Selection {
    private ItemStorage itemStorage;
    private final Item item;

    public Selection(ItemStorage itemStorage, Item item) {
        this.itemStorage = itemStorage;
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public ItemStorage getItemStorage() {
        return itemStorage;
    }

    public void moveToStorage(ItemStorage l) {
        this.itemStorage.deleteItem(this.item);
        this.itemStorage = l;
        this.itemStorage.addItem(this.item);
    }
}

package ru.fazziclay.opentoday.app.items;

public class AbsoluteItemContainer {
    private final ItemStorage itemStorage;
    private final Item item;

    public AbsoluteItemContainer(ItemStorage itemStorage, Item item) {
        this.itemStorage = itemStorage;
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public ItemStorage getItemStorage() {
        return itemStorage;
    }
}

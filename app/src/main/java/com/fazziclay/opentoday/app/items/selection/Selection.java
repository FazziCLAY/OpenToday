package com.fazziclay.opentoday.app.items.selection;

import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.callback.ItemCallback;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;

public class Selection {
    private final SelectionController selectionController;
    private ItemsStorage itemsStorage;
    private final Item item;
    private final ItemCallback itemCallback = new ItemCallback() {
        @Override
        public Status detached(Item item) {
            selectionController.detached(Selection.this);
            return Status.NONE;
        }
    };

    public Selection(SelectionController selectionController, ItemsStorage itemsStorage, Item item) {
        this.selectionController = selectionController;
        this.itemsStorage = itemsStorage;
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public ItemsStorage getItemsStorage() {
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

    protected void selected() {
        item.getItemCallbacks().addCallback(CallbackImportance.MIN, itemCallback);
    }

    protected void deselect() {
        item.getItemCallbacks().deleteCallback(itemCallback);
    }
}

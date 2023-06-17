package com.fazziclay.opentoday.app.items.selection;

import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.callback.ItemCallback;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemUtil;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;

public class Selection {
    private final SelectionController selectionController;
    private final Item item;
    private final ItemCallback itemCallback = new ItemCallback() {
        @Override
        public Status detached(Item item) {
            selectionController.detached(Selection.this);
            return Status.NONE;
        }
    };

    public Selection(SelectionController selectionController, Item item) {
        this.selectionController = selectionController;
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public void moveTo(ItemsStorage l) {
        item.delete();
        l.addItem(item);
    }

    public void copyTo(ItemsStorage l) {
        l.addItem(ItemUtil.copyItem(item));
    }

    protected void selected() {
        item.getItemCallbacks().addCallback(CallbackImportance.MIN, itemCallback);
    }

    protected void deselect() {
        item.getItemCallbacks().removeCallback(itemCallback);
    }
}

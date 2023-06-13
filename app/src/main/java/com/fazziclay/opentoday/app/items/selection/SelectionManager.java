package com.fazziclay.opentoday.app.items.selection;

import com.fazziclay.opentoday.app.CrashReportContext;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.callback.SelectionCallback;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.util.callback.CallbackStorage;

import java.util.ArrayList;
import java.util.List;

public class SelectionManager {
    // Selection
    private final List<Selection> selections = new ArrayList<>();
    private final CallbackStorage<SelectionCallback> onSelectionUpdated = new CallbackStorage<>();
    private final SelectionController selectionController = this::deselectItem;

    public SelectionManager() {

    }


    public CallbackStorage<SelectionCallback> getOnSelectionUpdated() {
        return onSelectionUpdated;
    }

    public boolean isSelectionEmpty() {
        return selections.isEmpty();
    }

    public Selection[] getSelections() {
        return selections.toArray(new Selection[0]);
    }

    public void selectItem(Item item) {
        CrashReportContext.BACK.push("SelectionManager.selectItem");
        if (!item.isAttached()) {
            throw new IllegalArgumentException("Item to select is not attached!");
        }
        if (isSelected(item)) return;

        Selection selection = new Selection(selectionController, item);
        selection.selected();
        this.selections.add(selection);
        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.onSelectionChanged(getSelections());
            return callback.selected(selection);
        });
        item.visibleChanged();
        CrashReportContext.BACK.pop();
    }

    public void deselectItem(Item item) {
        CrashReportContext.BACK.push("SelectionManager.deselectItem");
        if (!isSelected(item)) return;
        Selection toDelete = null;
        for (Selection selection : this.selections) {
            if (selection.getItem() == item) toDelete = selection;
        }
        selections.remove(toDelete);
        toDelete.deselect();

        final Selection finalToDelete = toDelete;
        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.onSelectionChanged(getSelections());
            return callback.unselected(finalToDelete);
        });
        item.visibleChanged();
        CrashReportContext.BACK.pop();
    }

    public void deselectItem(Selection se) {
        CrashReportContext.BACK.push("SelectionManager.deselectItem");
        if (!isSelected(se.getItem())) return;
        selections.remove(se);
        se.deselect();

        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.onSelectionChanged(getSelections());
            return callback.unselected(se);
        });
        se.getItem().visibleChanged();
        CrashReportContext.BACK.pop();
    }

    public void deselectAll() {
        selections.clear();

        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.onSelectionChanged(getSelections());
            return callback.unselectedAll();
        });
    }

    public boolean isSelected(Item item) {
        for (Selection selection : selections) {
            if (selection.getItem() == item) return true;
        }
        return false;
    }

    public void copyAllSelectedTo(ItemsStorage itemsStorage) {
        for (Selection selection : getSelections()) {
            selection.copyTo(itemsStorage);
        }
    }

    public Item[] getItems() {
        List<Item> items = new ArrayList<>();
        for (Selection selection : getSelections()) {
            items.add(selection.getItem());
        }
        return items.toArray(new Item[0]);
    }

    public void moveAllSelectedTo(ItemsStorage itemsStorage) {
        for (Selection selection : getSelections()) {
            selection.moveTo(itemsStorage);
        }
    }
}

package com.fazziclay.opentoday.app.items;

import com.fazziclay.opentoday.app.items.callback.SelectionCallback;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.util.callback.CallbackStorage;
import com.fazziclay.opentoday.util.callback.Status;

import java.util.ArrayList;
import java.util.List;

public class SelectionManager {
    // Selection
    private final List<Selection> selections = new ArrayList<>();
    private final CallbackStorage<SelectionCallback> onSelectionUpdated = new CallbackStorage<>();
    private final SelectionController selectionController = new SelectionController() {
        @Override
        public void detached(Selection selection) {
            deselectItem(selection);
        }
    };

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
        if (isSelected(item)) return;

        ItemsStorage parentItemsStorage = item.getParentItemsStorage();
        Selection selection = new Selection(selectionController, parentItemsStorage, item);
        selection.selected();
        this.selections.add(selection);
        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.onSelectionChanged(this.selections);
            return Status.NONE;
        });
    }

    public void deselectItem(Item item) {
        if (!isSelected(item)) return;
        Selection toDelete = null;
        for (Selection selection : this.selections) {
            if (selection.getItem() == item) toDelete = selection;
        }
        selections.remove(toDelete);
        toDelete.deselect();

        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.onSelectionChanged(this.selections);
            return new Status.Builder().build();
        });
    }

    public void deselectItem(Selection se) {
        if (!isSelected(se.getItem())) return;
        Selection toDelete = null;
        for (Selection selection : this.selections) {
            if (selection == se) toDelete = selection;
        }
        selections.remove(toDelete);
        toDelete.deselect();


        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.onSelectionChanged(this.selections);
            return Status.NONE;
        });
    }

    public void deselectAll() {
        selections.clear();

        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.onSelectionChanged(this.selections);
            return Status.NONE;
        });
    }

    public boolean isSelected(Item item) {
        for (Selection selection : selections) {
            if (selection.getItem() == item) return true;
        }
        return false;
    }
}

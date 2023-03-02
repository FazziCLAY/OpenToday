package com.fazziclay.opentoday.app.items.callback;

import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.util.callback.Status;

/**
 * @see OnItemsStorageUpdate
 */
public class OnItemsStorageUpdateImpl implements OnItemsStorageUpdate {
    private final Runnable runnable;

    public OnItemsStorageUpdateImpl(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public Status onAdded(Item item, int pos) {
        runnable.run();
        return Status.NONE;
    }

    @Override
    public Status onDeleted(Item item, int pos) {
        runnable.run();
        return Status.NONE;
    }

    @Override
    public Status onMoved(Item item, int from, int to) {
        runnable.run();
        return Status.NONE;
    }

    @Override
    public Status onUpdated(Item item, int pos) {
        runnable.run();
        return Status.NONE;
    }
}

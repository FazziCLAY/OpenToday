package com.betterbrainmemory.opentoday.app.items.callback;

import com.betterbrainmemory.opentoday.app.items.ItemsStorage;
import com.betterbrainmemory.opentoday.app.items.item.Item;
import com.betterbrainmemory.opentoday.util.callback.Callback;
import com.betterbrainmemory.opentoday.util.callback.Status;

/**
 * @see ItemsStorage
 * @see Callback
 */
public abstract class OnItemsStorageUpdate implements Callback {
    public Status onAdded(Item item, int position) {
        return Status.NONE;
    }

    public Status onPreDeleted(Item item, int position) {
        return Status.NONE;
    }

    public Status onPostDeleted(Item item, int position) {
        return Status.NONE;
    }

    public Status onMoved(Item item, int from, int to) {
        return Status.NONE;
    }

    public Status onUpdated(Item item, int position) {
        return Status.NONE;
    }
}

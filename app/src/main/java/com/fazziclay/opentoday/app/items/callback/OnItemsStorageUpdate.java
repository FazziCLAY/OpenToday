package com.fazziclay.opentoday.app.items.callback;

import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.util.callback.Callback;
import com.fazziclay.opentoday.util.callback.Status;

/**
 * @see ItemsStorage
 * @see Callback
 */
public interface OnItemsStorageUpdate extends Callback {
    Status onAdded(Item item, int position);
    Status onDeleted(Item item, int position);
    Status onMoved(Item item, int from, int to);
    Status onUpdated(Item item, int position);
}

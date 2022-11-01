package ru.fazziclay.opentoday.app.items.callback;

import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.callback.Callback;
import ru.fazziclay.opentoday.callback.Status;

/**
 * @see ru.fazziclay.opentoday.app.items.ItemsStorage
 * @see ru.fazziclay.opentoday.callback.Callback
 */
public interface OnItemsStorageUpdate extends Callback {
    Status onAdded(Item item, int position);
    Status onDeleted(Item item, int position);
    Status onMoved(Item item, int from, int to);
    Status onUpdated(Item item, int position);
}

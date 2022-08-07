package ru.fazziclay.opentoday.app.items.callback;

import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.callback.Callback;
import ru.fazziclay.opentoday.callback.Status;

public interface OnItemStorageUpdate extends Callback {
    Status onAdded(Item item);
    Status onDeleted(Item item);
    Status onMoved(Item item, int from);
    Status onUpdated(Item item);
}

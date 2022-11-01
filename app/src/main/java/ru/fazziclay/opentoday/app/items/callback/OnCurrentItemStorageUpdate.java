package ru.fazziclay.opentoday.app.items.callback;

import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.callback.Callback;
import ru.fazziclay.opentoday.callback.Status;

/**
 * @see ru.fazziclay.opentoday.app.items.CurrentItemStorage
 * @see Callback
 */
@FunctionalInterface
public interface OnCurrentItemStorageUpdate extends Callback {
    Status onCurrentChanged(Item item);
}

package com.fazziclay.opentoday.app.items.callback;

import com.fazziclay.opentoday.app.items.CurrentItemStorage;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.callback.Callback;
import com.fazziclay.opentoday.callback.Status;

/**
 * @see CurrentItemStorage
 * @see Callback
 */
@FunctionalInterface
public interface OnCurrentItemStorageUpdate extends Callback {
    Status onCurrentChanged(Item item);
}

package com.fazziclay.opentoday.app.items.callback;

import com.fazziclay.opentoday.app.items.CurrentItemStorage;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.util.callback.Callback;
import com.fazziclay.opentoday.util.callback.Status;

/**
 * @see CurrentItemStorage
 * @see Callback
 */
public interface OnCurrentItemStorageUpdate extends Callback {
    Status onCurrentChanged(Item item);
}

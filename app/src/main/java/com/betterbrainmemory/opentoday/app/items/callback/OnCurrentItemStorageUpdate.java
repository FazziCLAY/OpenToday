package com.betterbrainmemory.opentoday.app.items.callback;

import com.betterbrainmemory.opentoday.app.items.CurrentItemStorage;
import com.betterbrainmemory.opentoday.app.items.item.Item;
import com.betterbrainmemory.opentoday.util.callback.Callback;
import com.betterbrainmemory.opentoday.util.callback.Status;

/**
 * @see CurrentItemStorage
 * @see Callback
 */
public interface OnCurrentItemStorageUpdate extends Callback {
    Status onCurrentChanged(Item item);
}

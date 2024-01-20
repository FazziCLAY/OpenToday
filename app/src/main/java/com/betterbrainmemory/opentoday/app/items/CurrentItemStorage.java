package com.betterbrainmemory.opentoday.app.items;

import com.betterbrainmemory.opentoday.app.items.callback.OnCurrentItemStorageUpdate;
import com.betterbrainmemory.opentoday.app.items.item.Item;
import com.betterbrainmemory.opentoday.util.callback.CallbackStorage;

public interface CurrentItemStorage {
    Item getCurrentItem();
    CallbackStorage<OnCurrentItemStorageUpdate> getOnCurrentItemStorageUpdateCallbacks();
}

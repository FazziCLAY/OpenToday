package com.fazziclay.opentoday.app.items;

import com.fazziclay.opentoday.app.items.callback.OnCurrentItemStorageUpdate;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.util.callback.CallbackStorage;

public interface CurrentItemStorage {
    Item getCurrentItem();
    CallbackStorage<OnCurrentItemStorageUpdate> getOnCurrentItemStorageUpdateCallbacks();
}

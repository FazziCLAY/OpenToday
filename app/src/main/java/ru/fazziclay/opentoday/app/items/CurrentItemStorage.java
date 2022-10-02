package ru.fazziclay.opentoday.app.items;

import ru.fazziclay.opentoday.app.items.callback.OnCurrentItemStorageUpdate;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.callback.CallbackStorage;

public interface CurrentItemStorage {
    Item getCurrentItem();
    CallbackStorage<OnCurrentItemStorageUpdate> getOnCurrentItemStorageUpdateCallbacks();
}

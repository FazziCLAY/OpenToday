package ru.fazziclay.opentoday.app.items;

import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.callback.OnItemStorageUpdate;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.callback.CallbackStorage;

public interface ItemStorage {
    Item[] getAllItems();
    int size();
    void addItem(Item item);
    void deleteItem(Item item);
    void move(int positionFrom, int positionTo);
    void tick(TickSession tickSession);
    void save();
    int getItemPosition(Item item);
    CallbackStorage<OnItemStorageUpdate> getOnUpdateCallbacks();
}

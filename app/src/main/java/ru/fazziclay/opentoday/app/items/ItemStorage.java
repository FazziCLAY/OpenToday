package ru.fazziclay.opentoday.app.items;

import ru.fazziclay.opentoday.app.items.callback.OnItemAdded;
import ru.fazziclay.opentoday.app.items.callback.OnItemDeleted;
import ru.fazziclay.opentoday.app.items.callback.OnItemMoved;
import ru.fazziclay.opentoday.app.items.callback.OnItemUpdated;
import ru.fazziclay.opentoday.callback.CallbackStorage;

public interface ItemStorage {
    Item[] getItems();
    int size();
    void addItem(Item item);
    void deleteItem(Item item);
    void move(int positionFrom, int positionTo);
    void tick();
    void save();
    int getItemPosition(Item item);
    CallbackStorage<OnItemDeleted> getOnItemDeletedCallbackStorage();
    CallbackStorage<OnItemUpdated> getOnItemUpdatedCallbackStorage();
    CallbackStorage<OnItemAdded> getOnItemAddedCallbackStorage();
    CallbackStorage<OnItemMoved> getOnItemMovedCallbackStorage();
}

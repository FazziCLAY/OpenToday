package com.fazziclay.opentoday.app.items;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.app.items.tick.Tickable;
import com.fazziclay.opentoday.util.callback.CallbackStorage;

import java.util.List;
import java.util.UUID;

/**
 * Interface items container
 * @author fazziclay
 * @see ItemsStorage#addItem(Item)
 * @see ItemsStorage#deleteItem(Item)
 * @see ItemsStorage#copyItem(Item)
 * @see ItemsStorage#getItemPosition(Item)
 * @see ItemsStorage#getItemById(UUID)
 * @see ItemsStorage#move(int, int)
 * @see ItemsStorage#getAllItems()
 * @see ItemsStorage#tick(TickSession)
 * @see ItemsStorage#save()
 * @see ItemsStorage#size()
 * @see ItemsStorage#getOnItemsStorageCallbacks()
 */
public interface ItemsStorage extends Tickable {
    /**
     * Add item to this ItemStorage
     * @param item item to add
     * @see Item
     */
    void addItem(Item item);

    /**
     * Add item to this ItemStorage
     * @param item item to add
     * @param position position to add
     * @see Item
     */
    void addItem(Item item, int position);

    /**
     * Delete item from ItemStorage
     * @param item item to delete
     */
    void deleteItem(Item item);

    /**
     * Copy item and automatically add to this ItemStorage
     * @param item item to copy
     * @return new item (copy)
     * @see Item
     */
    @NonNull Item copyItem(Item item);

    /**
     * Get item position in itemStorage
     * @param item item to find
     * @return position in ItemStorage, -1 if not found (default by List#indexOf)
     * @see Item
     * @see List#indexOf(Object)
     */
    int getItemPosition(Item item);

    /**
     * Get item by itemId
     * @param itemId itemId to find
     * @return item, if not found null
     * @see UUID
     * @see Item
     */
    @Nullable Item getItemById(UUID itemId);

    /**
     * Move items
     * @param positionFrom from
     * @param positionTo to
     */
    void move(int positionFrom, int positionTo);

    /**
     * Get all items in this ItemStorage (only root)
     * @return item in user-position
     * @see Item
     */
    @NonNull Item[] getAllItems();

    /**
     * Tick function
     * Call every seconds for user-like
     * @param tickSession see tickSession javaDoc
     * @see TickSession
     */
    void tick(TickSession tickSession);

    /**
     * Save data
     */
    void save();

    /**
     * Get items count in this ItemStorage (only root)
     * @return count of items
     */
    int size();

    /**
     * get items count include children item
     * @return count of total items
     */
    int totalSize();

    /**
     * Get OnItemStorageUpdate CallbackStorage
     * @return callbackStorage
     * @see OnItemsStorageUpdate
     * @see CallbackStorage
     */
    @NonNull CallbackStorage<OnItemsStorageUpdate> getOnItemsStorageCallbacks();

    boolean isEmpty();

    Item getItemAt(int position);
}

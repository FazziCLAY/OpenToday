package com.fazziclay.opentoday.app.items;

import com.fazziclay.opentoday.app.Translation;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.tab.Tab;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ItemsRoot {
    /**
     * Getting item by ItemID
     * @param id id
     * @return item is exist, is not exist: null
     */
    @Nullable
    Item getItemById(UUID id);

    /**
     * Getting tab by TabID
     * @param id id
     * @return tab is exist, is not exist: null
     */
    @Nullable
    Tab getTabById(UUID id);

    /**
     * Getting {@link ItemsStorage} by id
     * @param id id
     * @return Tab or Item extends of ItemsStorage
     */
    @Nullable
    ItemsStorage getItemsStorageById(UUID id);

    boolean isExistById(UUID id);

    @Nullable
    Type getTypeById(UUID id);

    @Nullable
    Object getById(UUID id);

    ItemPath getPathTo(Object o);

    /**
     * Generate not-exists id
     * @return UUID, no exist already
     */
    @NotNull
    UUID generateUniqueId();

    @NotNull
    Translation getTranslation();

    enum Type {
        TAB,
        ITEM
    }
}

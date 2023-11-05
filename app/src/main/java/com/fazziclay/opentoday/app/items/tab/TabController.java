package com.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.items.ItemsRoot;

import java.util.UUID;

/**
 * The tab controller.
 */
public interface TabController {
    void save(@NonNull final Tab tab);
    void nameChanged(@NonNull final Tab tab);
    UUID generateId();
    ItemsRoot getRoot();
    void iconChanged(Tab tab);
}

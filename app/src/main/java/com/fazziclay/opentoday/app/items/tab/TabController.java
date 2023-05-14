package com.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;

import java.util.UUID;

/**
 * The tab controller.
 */
public interface TabController {
    void save(@NonNull final Tab tab);
    void nameChanged(@NonNull final Tab tab);
    UUID generateId();
}

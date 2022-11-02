package ru.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;

public interface ItemsTabController {
    void save(@NonNull final Tab tab);
    void nameChanged(@NonNull final Tab tab);
}

package com.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;

public interface TabController {
    void save(@NonNull final Tab tab);
    void nameChanged(@NonNull final Tab tab);
}

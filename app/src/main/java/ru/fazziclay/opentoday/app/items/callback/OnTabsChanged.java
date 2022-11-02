package ru.fazziclay.opentoday.app.items.callback;

import androidx.annotation.NonNull;

import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.tab.Tab;
import ru.fazziclay.opentoday.callback.Callback;
import ru.fazziclay.opentoday.callback.Status;

/**
 * @see ItemManager#getOnTabsChanged()
 * @see ru.fazziclay.opentoday.callback.Callback
 */
@FunctionalInterface
public interface OnTabsChanged extends Callback {
    Status onTabsChanged(@NonNull final Tab[] tabs);
}

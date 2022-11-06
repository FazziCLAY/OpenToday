package com.fazziclay.opentoday.app.items.callback;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.items.ItemManager;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.callback.Callback;
import com.fazziclay.opentoday.callback.Status;

/**
 * @see ItemManager#getOnTabsChanged()
 * @see Callback
 */
@FunctionalInterface
public interface OnTabsChanged extends Callback {
    Status onTabsChanged(@NonNull final Tab[] tabs);
}

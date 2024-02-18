package com.betterbrainmemory.opentoday.app.items.callback;

import androidx.annotation.NonNull;

import com.betterbrainmemory.opentoday.app.items.tab.Tab;
import com.betterbrainmemory.opentoday.app.items.tab.TabsManager;
import com.betterbrainmemory.opentoday.util.callback.Callback;
import com.betterbrainmemory.opentoday.util.callback.Status;

/**
 * @see TabsManager#getOnTabsChangedCallbacks()
 * @see Callback
 */
public abstract class OnTabsChanged implements Callback {
    public Status onTabsChanged(@NonNull final Tab[] tabs) {
        return Status.NONE;
    }

    public Status onTabAdded(Tab tab, int position) {
        return Status.NONE;
    }

    public Status onTabPreDeleted(Tab tab, int position) {
        return Status.NONE;
    }

    public Status onTabPostDeleted(Tab tab, int position) {
        return Status.NONE;
    }

    public Status onTabMoved(Tab tab, int fromPos, int toPos) {
        return Status.NONE;
    }

    public Status onTabRenamed(Tab tab, int position) {
        return Status.NONE;
    }

    public Status onTabIconChanged(Tab tab, int position) {
        return Status.NONE;
    }
}

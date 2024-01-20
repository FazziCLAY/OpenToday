package com.betterbrainmemory.opentoday.app.items.callback;

import com.betterbrainmemory.opentoday.app.items.item.CountDownCheckmarkItem;
import com.betterbrainmemory.opentoday.app.items.item.Item;
import com.betterbrainmemory.opentoday.util.callback.Callback;
import com.betterbrainmemory.opentoday.util.callback.Status;

public abstract class ItemCallback implements Callback {
    public Status updateUi(Item item) {
        return Status.NONE;
    }

    public Status save(Item item) {
        return Status.NONE;
    }

    public Status delete(Item item) {
        return Status.NONE;
    }

    public Status attached(Item item) {
        return Status.NONE;
    }

    public Status detached(Item item) {
        return Status.NONE;
    }

    public Status tick(Item item) {
        return Status.NONE;
    }

    // debug function...
    public Status click(Item item) {
        return Status.NONE;
    }

    public Status cachedNotificationStatusChanged(Item item, boolean isUpdateNotifications) {
        return Status.NONE;
    }

    public Status countDownCheckmarkStopped(CountDownCheckmarkItem item, boolean changedTo) {
        return Status.NONE;
    }
}

package com.fazziclay.opentoday.app.items.callback;

import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.util.callback.Callback;
import com.fazziclay.opentoday.util.callback.Status;

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
}

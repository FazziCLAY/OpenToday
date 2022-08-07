package ru.fazziclay.opentoday.app.items.callback;

import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.callback.Status;

public class OnItemStorageUpdateRunnable implements OnItemStorageUpdate {
    private final Runnable runnable;

    public OnItemStorageUpdateRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public Status onAdded(Item item) {
        runnable.run();
        return Status.NONE;
    }

    @Override
    public Status onDeleted(Item item) {
        runnable.run();
        return Status.NONE;
    }

    @Override
    public Status onMoved(Item item, int from) {
        runnable.run();
        return Status.NONE;
    }

    @Override
    public Status onUpdated(Item item) {
        runnable.run();
        return Status.NONE;
    }
}

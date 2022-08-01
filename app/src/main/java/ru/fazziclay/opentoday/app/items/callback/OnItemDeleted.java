package ru.fazziclay.opentoday.app.items.callback;

import ru.fazziclay.opentoday.app.items.Item;
import ru.fazziclay.opentoday.callback.Callback;
import ru.fazziclay.opentoday.callback.Status;

@FunctionalInterface
public interface OnItemDeleted extends Callback {
    Status run(Item item, int position);
}

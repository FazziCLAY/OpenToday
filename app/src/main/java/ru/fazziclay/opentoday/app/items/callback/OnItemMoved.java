package ru.fazziclay.opentoday.app.items.callback;

import ru.fazziclay.opentoday.callback.Callback;
import ru.fazziclay.opentoday.callback.Status;

@FunctionalInterface
public interface OnItemMoved extends Callback {
    Status run(int positionFrom, int positionTo);
}

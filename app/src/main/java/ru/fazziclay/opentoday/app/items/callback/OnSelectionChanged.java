package ru.fazziclay.opentoday.app.items.callback;

import java.util.List;

import ru.fazziclay.opentoday.app.items.Selection;
import ru.fazziclay.opentoday.callback.Callback;

@FunctionalInterface
public interface OnSelectionChanged extends Callback {
    void run(List<Selection> selections);
}

package ru.fazziclay.opentoday.app.items.callback;

import ru.fazziclay.opentoday.app.items.AbsoluteItemContainer;
import ru.fazziclay.opentoday.callback.Callback;

public interface OnSelectionChanged extends Callback {
    void run(AbsoluteItemContainer selection);
}

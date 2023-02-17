package com.fazziclay.opentoday.app.items.callback;

import com.fazziclay.opentoday.app.items.Selection;
import com.fazziclay.opentoday.util.callback.Callback;

import java.util.List;

@FunctionalInterface
public interface OnSelectionChanged extends Callback {
    void onSelectionChanged(List<Selection> selections);
}

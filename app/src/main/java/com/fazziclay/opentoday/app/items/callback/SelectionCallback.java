package com.fazziclay.opentoday.app.items.callback;

import com.fazziclay.opentoday.app.items.Selection;
import com.fazziclay.opentoday.util.callback.Callback;
import com.fazziclay.opentoday.util.callback.Status;

import java.util.List;

public abstract class SelectionCallback implements Callback {
    public void onSelectionChanged(List<Selection> selections) {

    }

    public Status selected(Selection selection) {
        return null;
    }

    public Status unselected(Selection selection) {
        return null;
    }
}

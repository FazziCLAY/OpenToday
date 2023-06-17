package com.fazziclay.opentoday.app.items.callback;

import com.fazziclay.opentoday.app.items.selection.Selection;
import com.fazziclay.opentoday.util.callback.Callback;
import com.fazziclay.opentoday.util.callback.Status;

public abstract class SelectionCallback implements Callback {
    public void onSelectionChanged(Selection[] selections) {

    }

    public Status selected(Selection selection) {
        return Status.NONE;
    }

    public Status unselected(Selection selection) {
        return Status.NONE;
    }

    public Status unselectedAll() {
        return Status.NONE;
    }
}

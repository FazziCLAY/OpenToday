package com.betterbrainmemory.opentoday.app.items.callback;

import com.betterbrainmemory.opentoday.app.items.selection.Selection;
import com.betterbrainmemory.opentoday.util.callback.Callback;
import com.betterbrainmemory.opentoday.util.callback.Status;

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

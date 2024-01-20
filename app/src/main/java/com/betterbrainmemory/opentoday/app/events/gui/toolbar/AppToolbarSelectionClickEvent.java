package com.betterbrainmemory.opentoday.app.events.gui.toolbar;

import android.app.Activity;

import com.betterbrainmemory.opentoday.api.Event;
import com.betterbrainmemory.opentoday.app.items.selection.SelectionManager;
import com.betterbrainmemory.opentoday.databinding.ToolbarMoreSelectionBinding;

public class AppToolbarSelectionClickEvent implements Event {
    private final Activity activity;
    private final com.betterbrainmemory.opentoday.databinding.ToolbarMoreSelectionBinding localBinding;
    private final SelectionManager selectionManager;

    public AppToolbarSelectionClickEvent(Activity activity, ToolbarMoreSelectionBinding localBinding, SelectionManager selectionManager) {
        this.activity = activity;
        this.localBinding = localBinding;
        this.selectionManager = selectionManager;
    }

    public Activity getActivity() {
        return activity;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public ToolbarMoreSelectionBinding getLocalBinding() {
        return localBinding;
    }
}

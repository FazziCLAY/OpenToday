package com.fazziclay.opentoday.gui.item;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.items.CurrentItemStorage;
import com.fazziclay.opentoday.app.items.callback.OnCurrentItemStorageUpdate;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;

public class CurrentItemStorageDrawer {
    private final Activity activity;
    private final LinearLayout view;
    private final ItemViewGenerator itemViewGenerator;
    private final CurrentItemStorage currentItemStorage;
    private final OnUpdateListener listener = new OnUpdateListener();
    private OnCurrentItemStorageUpdate userListener = null;

    public CurrentItemStorageDrawer(Activity activity, ItemViewGenerator itemViewGenerator, CurrentItemStorage currentItemStorage) {
        this.activity = activity;
        this.view = new LinearLayout(activity);
        this.view.setOrientation(LinearLayout.VERTICAL);
        this.itemViewGenerator = itemViewGenerator;
        this.currentItemStorage = currentItemStorage;
    }

    public void create() {
        currentItemStorage.getOnCurrentItemStorageUpdateCallbacks().addCallback(CallbackImportance.DEFAULT, listener);
        updateView(currentItemStorage.getCurrentItem());
    }

    public void destroy() {
        currentItemStorage.getOnCurrentItemStorageUpdateCallbacks().removeCallback(listener);
        view.removeAllViews();
    }

    @NonNull
    public View getView() {
        return view;
    }

    private void updateView(Item currentItem) {
        view.removeAllViews();
        if (userListener != null) {
            userListener.onCurrentChanged(currentItem);
        }
        if (currentItem != null) view.addView(itemViewGenerator.generate(currentItem, view));
    }

    public void setOnUpdateListener(OnCurrentItemStorageUpdate listener) {
        userListener = listener;
    }

    private class OnUpdateListener implements OnCurrentItemStorageUpdate {
        @Override
        public Status onCurrentChanged(Item item) {
            activity.runOnUiThread(() -> updateView(item));
            return Status.NONE;
        }
    }
}

package com.fazziclay.opentoday.gui.item;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.items.CurrentItemStorage;
import com.fazziclay.opentoday.app.items.ItemManager;
import com.fazziclay.opentoday.app.items.callback.OnCurrentItemStorageUpdate;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.SettingsManager;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.gui.interfaces.StorageEditsActions;
import com.fazziclay.opentoday.util.ResUtil;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;

public class CurrentItemStorageDrawer {
    private final Activity activity;
    private final ItemManager itemManager;
    private final LinearLayout view;
    private final CurrentItemStorage currentItemStorage;
    private final ItemViewGenerator itemViewGenerator;
    private final OnUpdateListener listener = new OnUpdateListener();
    private OnCurrentItemStorageUpdate userListener = null;

    public CurrentItemStorageDrawer(Activity activity, ItemManager itemManager, SettingsManager settingsManager, CurrentItemStorage currentItemStorage, boolean previewMode, ItemInterface onItemClick, ItemInterface onItemEditor, StorageEditsActions storageEdits) {
        this.activity = activity;
        this.view = new LinearLayout(activity);
        this.itemManager = itemManager;
        this.view.setOrientation(LinearLayout.VERTICAL);
        this.currentItemStorage = currentItemStorage;
        this.itemViewGenerator = new ItemViewGenerator(activity, itemManager, settingsManager, previewMode, onItemClick, onItemEditor, storageEdits);
    }

    public void create() {
        currentItemStorage.getOnCurrentItemStorageUpdateCallbacks().addCallback(CallbackImportance.DEFAULT, listener);
        updateView(currentItemStorage.getCurrentItem());
    }

    public void destroy() {
        currentItemStorage.getOnCurrentItemStorageUpdateCallbacks().deleteCallback(listener);
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
        if (itemManager.isSelected(currentItem)) {
            view.setForeground(new ColorDrawable(ResUtil.getAttrColor(activity, R.attr.item_selectionForegroundColor)));
        } else {
            view.setForeground(null);
        }
    }

    public void setOnUpdateListener(OnCurrentItemStorageUpdate listener) {
        userListener = listener;
    }

    private class OnUpdateListener implements OnCurrentItemStorageUpdate {
        @Override
        public Status onCurrentChanged(Item item) {
            updateView(item);
            return Status.NONE;
        }
    }
}

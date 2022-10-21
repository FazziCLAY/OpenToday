package ru.fazziclay.opentoday.ui.item;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.CurrentItemStorage;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.callback.OnCurrentItemStorageUpdate;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.callback.CallbackImportance;
import ru.fazziclay.opentoday.callback.Status;
import ru.fazziclay.opentoday.ui.interfaces.IVGEditButtonInterface;
import ru.fazziclay.opentoday.ui.interfaces.OnItemClick;
import ru.fazziclay.opentoday.util.ResUtil;

public class CurrentItemStorageDrawer {
    private final Activity activity;
    private final ItemManager itemManager;
    private final LinearLayout view;
    private final CurrentItemStorage currentItemStorage;
    private final ItemViewGenerator itemViewGenerator;
    private final OnUpdateListener listener = new OnUpdateListener();
    private OnCurrentItemStorageUpdate userListener = null;

    public CurrentItemStorageDrawer(Activity activity, ItemManager itemManager, CurrentItemStorage currentItemStorage, boolean previewMode, OnItemClick onItemClick, IVGEditButtonInterface storageEdits) {
        this.activity = activity;
        this.view = new LinearLayout(activity);
        this.itemManager = itemManager;
        this.view.setOrientation(LinearLayout.VERTICAL);
        this.currentItemStorage = currentItemStorage;
        this.itemViewGenerator = new ItemViewGenerator(activity, App.get(activity).getItemManager(), onItemClick, previewMode, storageEdits);
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

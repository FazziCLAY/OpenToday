package ru.fazziclay.opentoday.ui.dialog;

import android.app.Activity;
import android.app.Dialog;

import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.databinding.DialogItemstorageEditorBinding;
import ru.fazziclay.opentoday.ui.other.AppToolbar;
import ru.fazziclay.opentoday.ui.other.item.ItemStorageDrawer;

public class DialogItemStorageEditor {
    private final Activity activity;
    private final Dialog dialog;
    private final ItemStorageDrawer itemStorageDrawer;
    private final AppToolbar appToolbar;

    public DialogItemStorageEditor(Activity activity, ItemManager itemManager, ItemStorage itemStorage, ItemStorageDrawer.OnItemClick onItemClick) {
        this.activity = activity;
        this.dialog = new Dialog(activity, android.R.style.ThemeOverlay_Material);
        this.itemStorageDrawer = new ItemStorageDrawer(activity, itemManager, itemStorage, onItemClick, false);
        this.itemStorageDrawer.create();
        this.appToolbar = new AppToolbar(activity, itemStorage);

        DialogItemstorageEditorBinding binding = DialogItemstorageEditorBinding.inflate(activity.getLayoutInflater());
        binding.canvas.addView(itemStorageDrawer.getView());
        binding.toolbar.addView(appToolbar.getToolbarView());
        binding.toolbarMore.addView(appToolbar.getToolbarMoreView());

        dialog.setContentView(binding.getRoot());
        dialog.setOnCancelListener(dialog -> {
            itemStorageDrawer.destroy();
            appToolbar.destroy();
        });
    }

    public void show() {
        dialog.show();
    }

    public void cancel() {
        dialog.cancel();
    }
}

package ru.fazziclay.opentoday.ui.dialog;

import android.app.Activity;
import android.app.Dialog;

import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.ui.other.ItemsEditor;
import ru.fazziclay.opentoday.ui.other.item.ItemStorageDrawer;
import ru.fazziclay.opentoday.ui.other.item.OnItemClick;

public class DialogItemStorageEditor {
    private final Dialog dialog;
    private final ItemsEditor itemsEditor;

    public DialogItemStorageEditor(Activity activity, ItemManager itemManager, ItemStorage itemStorage, OnItemClick onItemClick, String path) {
        this.dialog = new Dialog(activity, android.R.style.ThemeOverlay_Material);

        this.itemsEditor = new ItemsEditor(activity, null, itemManager, itemStorage, path, onItemClick, false);
        this.itemsEditor.create();

        dialog.setContentView(itemsEditor.getView());
        dialog.setOnCancelListener(dialog -> itemsEditor.destroy());
    }

    public void show() {
        dialog.show();
    }

    public void cancel() {
        dialog.cancel();
    }
}

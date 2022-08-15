package ru.fazziclay.opentoday.ui.dialog;

import android.app.Activity;
import android.app.Dialog;

import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.databinding.DialogItemstorageEditorBinding;
import ru.fazziclay.opentoday.ui.other.ItemsEditor;
import ru.fazziclay.opentoday.ui.other.item.ItemStorageDrawer;

public class DialogItemStorageEditor {
    private final Dialog dialog;
    private final ItemsEditor itemsEditor;

    public DialogItemStorageEditor(Activity activity, ItemManager itemManager, ItemStorage itemStorage, ItemStorageDrawer.OnItemClick onItemClick) {
        this.dialog = new Dialog(activity, android.R.style.ThemeOverlay_Material);

        DialogItemstorageEditorBinding binding = DialogItemstorageEditorBinding.inflate(activity.getLayoutInflater());

        this.itemsEditor = new ItemsEditor(activity, binding.itemsEditor, itemManager, itemStorage);
        this.itemsEditor.create();
        binding.itemsEditor.addView(itemsEditor.getView());

        dialog.setContentView(binding.getRoot());
        dialog.setOnCancelListener(dialog -> itemsEditor.destroy());
    }

    public void show() {
        dialog.show();
    }

    public void cancel() {
        dialog.cancel();
    }
}

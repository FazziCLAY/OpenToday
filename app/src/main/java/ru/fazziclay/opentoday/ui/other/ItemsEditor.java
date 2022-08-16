package ru.fazziclay.opentoday.ui.other;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.databinding.ItemsEditorBinding;
import ru.fazziclay.opentoday.ui.other.item.ItemStorageDrawer;

// layout/items_editor.xml
public class ItemsEditor {
    private final ItemsEditorBinding binding;
    private final ItemStorageDrawer itemStorageDrawer;
    private final AppToolbar toolbar;

    public ItemsEditor(@NonNull Activity activity, @Nullable ViewGroup parent, ItemManager itemManager, ItemStorage itemStorage) {
        if (parent == null) {
            this.binding = ItemsEditorBinding.inflate(activity.getLayoutInflater());
        } else {
            this.binding = ItemsEditorBinding.inflate(activity.getLayoutInflater(), parent, false);
        }

        this.itemStorageDrawer = new ItemStorageDrawer(activity, itemManager, itemStorage);
        this.binding.itemsEditor.addView(this.itemStorageDrawer.getView());

        // toolbar
        this.toolbar = new AppToolbar(activity, itemManager, itemStorage);
        this.binding.toolbar.addView(this.toolbar.getToolbarView());
        this.binding.toolbarMore.addView(this.toolbar.getToolbarMoreView());
    }

    public View getView() {
        return binding.getRoot();
    }

    public void destroy() {
        itemStorageDrawer.destroy();
        toolbar.destroy();
    }

    public void create() {
        itemStorageDrawer.create();
        toolbar.create();
    }
}

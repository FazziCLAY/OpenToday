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
    private final Activity activity;
    private final ViewGroup parent;
    private final ItemsEditorBinding binding;
    private final ItemStorageDrawer itemStorageDrawer;
    private final AppToolbar toolbar;

    public ItemsEditor(@NonNull Activity activity, @Nullable ViewGroup parent, ItemManager itemManager, ItemStorage itemStorage) {
        this.activity = activity;
        this.parent = parent;
        if (parent == null) {
            this.binding = ItemsEditorBinding.inflate(activity.getLayoutInflater());
        } else {
            this.binding = ItemsEditorBinding.inflate(activity.getLayoutInflater(), parent, false);
        }

        itemStorageDrawer = new ItemStorageDrawer(activity, itemManager, itemStorage);
        itemStorageDrawer.create();
        binding.itemsEditor.addView(itemStorageDrawer.getView());

        // toolbar
        toolbar = new AppToolbar(activity, itemStorage);
        binding.toolbar.addView(toolbar.getToolbarView());
        binding.toolbarMore.addView(toolbar.getToolbarMoreView());
    }

    public View getView() {
        return binding.getRoot();
    }

    public void destroy() {
        itemStorageDrawer.destroy();
        toolbar.destroy();
    }

    public void create() {
        // TODO: 15.08.2022 make create method
    }
}

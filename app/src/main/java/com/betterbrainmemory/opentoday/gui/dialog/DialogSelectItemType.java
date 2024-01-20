package com.betterbrainmemory.opentoday.gui.dialog;

import android.app.Dialog;
import android.content.Context;

import com.betterbrainmemory.opentoday.R;
import com.betterbrainmemory.opentoday.app.App;
import com.betterbrainmemory.opentoday.app.items.item.ItemsRegistry;
import com.betterbrainmemory.opentoday.gui.item.registry.ItemsGuiRegistry;
import com.betterbrainmemory.opentoday.util.Identifier;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class DialogSelectItemType {
    private final CharSequence[] itemsNames;
    private final ItemsRegistry.ItemInfo[] itemsInfos;

    private final Context context;
    private final App app;
    private String title;
    private String message;
    private final OnSelected onSelected;
    private Dialog dialog;
    private final ItemTypeValidator itemTypeValidator;
    private int selectedItem = -1;

    public DialogSelectItemType(Context context, OnSelected onSelected) {
        this(context, onSelected, (Identifier) null);
    }

    public DialogSelectItemType(Context context, OnSelected onSelected, Identifier selectedItem) {
        this(context, onSelected, type -> type.isCompatibility(App.get(context).getFeatureFlags()), selectedItem);
    }

    public DialogSelectItemType(Context context, OnSelected onSelected, ItemTypeValidator itemTypeValidator) {
        this(context, onSelected, itemTypeValidator, null);
    }

    public DialogSelectItemType(Context context, OnSelected onSelected, ItemTypeValidator itemTypeValidator, Identifier selectedItem) {
        this.context = context;
        this.app = App.get(context);
        this.itemTypeValidator = itemTypeValidator;
        this.onSelected = onSelected;

        final List<ItemsRegistry.ItemInfo> tempInfos = new ArrayList<>();
        final List<CharSequence> tempNames = new ArrayList<>();

        int i = 0;
        for (final ItemsRegistry.ItemInfo item : ItemsRegistry.REGISTRY.getAllItems()) {
            boolean validate = itemTypeValidator.validate(item);
            if (validate) {
                tempInfos.add(item);
                tempNames.add(ItemsGuiRegistry.REGISTRY.nameOf(context, item));
                if (item.getIdentifier().equals(selectedItem)) {
                    this.selectedItem = i;
                }
                i++;
            }
        }

        itemsInfos = tempInfos.toArray(new ItemsRegistry.ItemInfo[0]);
        itemsNames = tempNames.toArray(new CharSequence[0]);
    }

    public DialogSelectItemType setTitle(String m) {
        this.title = m;
        return this;
    }


    public DialogSelectItemType setMessage(String m) {
        this.message = m;
        return this;
    }

    public void show() {
        this.dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(this.title)
                .setMessage(this.message)
                .setSingleChoiceItems(itemsNames, selectedItem, (dialogInterface, i) -> {
                    if (i < 0) return;
                    onSelected.onSelected(itemsInfos[i]);
                    cancel();
                })
                .setPositiveButton(context.getString(R.string.dialog_selectItemAction_cancel), null)
                .create();
        dialog.show();
    }

    public void cancel() {
        dialog.cancel();
    }

    @FunctionalInterface
    public interface OnSelected {
        void onSelected(ItemsRegistry.ItemInfo type);
    }

    @FunctionalInterface
    public interface ItemTypeValidator {
        boolean validate(ItemsRegistry.ItemInfo type);
    }
}

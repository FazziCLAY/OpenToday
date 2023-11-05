package com.fazziclay.opentoday.gui.dialog;

import android.app.Dialog;
import android.content.Context;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.items.item.ItemType;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.gui.EnumsRegistry;
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
        this(context, onSelected, (ItemType) null);
    }

    public DialogSelectItemType(Context context, OnSelected onSelected, ItemType selectedItem) {
        this(context, onSelected, type -> ItemsRegistry.REGISTRY.get(type).isCompatibility(App.get(context).getFeatureFlags()), selectedItem);
    }

    public DialogSelectItemType(Context context, OnSelected onSelected, ItemTypeValidator itemTypeValidator) {
        this(context, onSelected, itemTypeValidator, null);
    }

    public DialogSelectItemType(Context context, OnSelected onSelected, ItemTypeValidator itemTypeValidator, ItemType selectedItem) {
        this.context = context;
        this.app = App.get(context);
        this.itemTypeValidator = itemTypeValidator;
        this.onSelected = onSelected;

        final List<ItemsRegistry.ItemInfo> tempInfos = new ArrayList<>();
        final List<CharSequence> tempNames = new ArrayList<>();

        int i = 0;
        for (final ItemsRegistry.ItemInfo item : ItemsRegistry.REGISTRY.getAllItems()) {
            boolean validate = itemTypeValidator.validate(item.getItemType());
            if (validate) {
                tempInfos.add(item);
                tempNames.add(EnumsRegistry.INSTANCE.name(item.getItemType(), context));
                if (item.getItemType() == selectedItem) {
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
                    onSelected.onSelected(itemsInfos[i].getItemType());
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
        void onSelected(ItemType type);
    }

    @FunctionalInterface
    public interface ItemTypeValidator {
        boolean validate(ItemType type);
    }
}

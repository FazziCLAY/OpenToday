package ru.fazziclay.opentoday.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.widget.Spinner;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.items.ItemsRegistry;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.util.SimpleSpinnerAdapter;

public class DialogSelectItemType {
    private final String selectButtonText;
    private final OnSelected onSelected;
    private final Spinner spinner;
    private final SimpleSpinnerAdapter<Class<? extends Item>> simpleSpinnerAdapter;
    private final Dialog dialog;

    public DialogSelectItemType(Activity activity, OnSelected onSelected) {
        this(activity, null, onSelected);
    }

    public DialogSelectItemType(Activity activity, int resId, OnSelected onSelected) {
        this(activity, activity.getString(resId), onSelected);
    }

    public DialogSelectItemType(Activity activity, String selectButtonText, OnSelected onSelected) {
        if (selectButtonText == null) {
            this.selectButtonText = activity.getString(R.string.dialog_selectItemType_select);
        } else {
            this.selectButtonText = selectButtonText;
        }
        this.onSelected = onSelected;

        this.simpleSpinnerAdapter = new SimpleSpinnerAdapter<>(activity);
        for (ItemsRegistry.ItemInfo itemInfo : ItemsRegistry.REGISTRY.getAllItems()) {
            this.simpleSpinnerAdapter.add(activity.getString(itemInfo.getNameResId()), itemInfo.getClassType());
        }

        this.spinner = new Spinner(activity);
        this.spinner.setAdapter(simpleSpinnerAdapter);

        this.dialog = new AlertDialog.Builder(activity)
                .setView(spinner)
                .setNegativeButton(activity.getString(R.string.dialog_selectItemAction_cancel), null)
                .setPositiveButton(this.selectButtonText, (i2, i1) -> {
                    Class<? extends Item> itemType = simpleSpinnerAdapter.getItem(spinner.getSelectedItemPosition());
                    onSelected.onSelected(itemType);
                })
                .create();
    }

    public void show() {
        dialog.show();
    }

    public void cancel() {
        dialog.cancel();
    }

    @FunctionalInterface
    public interface OnSelected {
        void onSelected(Class<? extends Item> type);
    }
}

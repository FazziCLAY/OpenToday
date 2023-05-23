package com.fazziclay.opentoday.gui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.Spinner;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.gui.EnumsRegistry;
import com.fazziclay.opentoday.util.SimpleSpinnerAdapter;

public class DialogSelectItemType {
    private final String selectButtonText;
    private String title;
    private String message;
    private final OnSelected onSelected;
    private View view;
    private final SimpleSpinnerAdapter<Class<? extends Item>> simpleSpinnerAdapter;
    private final Dialog dialog;

    public DialogSelectItemType(Context context, OnSelected onSelected) {
        this(context, null, onSelected);
    }

    public DialogSelectItemType(Context context, int resId, OnSelected onSelected) {
        this(context, context.getString(resId), onSelected);
    }

    public DialogSelectItemType(Context context, String selectButtonText, OnSelected onSelected) {
        if (selectButtonText == null) {
            this.selectButtonText = context.getString(R.string.dialog_selectItemType_select);
        } else {
            this.selectButtonText = selectButtonText;
        }
        this.onSelected = onSelected;

        this.simpleSpinnerAdapter = new SimpleSpinnerAdapter<>(context);
        for (ItemsRegistry.ItemInfo itemInfo : ItemsRegistry.REGISTRY.getAllItems()) {
            this.simpleSpinnerAdapter.add(context.getString(EnumsRegistry.INSTANCE.nameResId(itemInfo.getItemType())), itemInfo.getClassType());
        }

        final byte MODE = 2; // 1 - spinner; 2 - list
        if (MODE == 1) {
            Spinner spinner = new Spinner(context);
            this.view = spinner;
            spinner.setAdapter(simpleSpinnerAdapter);
        }
        if (MODE == 2) {
            ListView listView = new ListView(context);
            this.view = listView;
            listView.setAdapter(simpleSpinnerAdapter);
            listView.setOnItemClickListener((parent, ignoreView, position, id) -> {
                Class<? extends Item> itemType = simpleSpinnerAdapter.getItem(position);
                onSelected.onSelected(itemType);
                cancel();
            });
        }

        this.dialog = new AlertDialog.Builder(context)
                .setTitle(this.title)
                .setMessage(this.message)
                .setView(this.view)
                .setNegativeButton(context.getString(R.string.dialog_selectItemAction_cancel), null)
                .setPositiveButton(this.selectButtonText, (i2, i1) -> {
                    if (MODE == 1) {
                        Class<? extends Item> itemType = simpleSpinnerAdapter.getItem(((Spinner) view).getSelectedItemPosition());
                        onSelected.onSelected(itemType);
                    }
                })
                .create();
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

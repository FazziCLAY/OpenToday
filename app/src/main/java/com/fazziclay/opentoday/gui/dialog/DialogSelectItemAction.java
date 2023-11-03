package com.fazziclay.opentoday.gui.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.settings.enums.ItemAction;
import com.fazziclay.opentoday.gui.EnumsRegistry;
import com.fazziclay.opentoday.util.MinBaseAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DialogSelectItemAction {
    private final Activity activity;
    @Nullable private final ItemAction selected;
    private final OnSelected onSelected;
    private final String message;
    private Dialog dialog;
    private final View view;
    private List<ItemAction> excludeList = new ArrayList<>();

    public DialogSelectItemAction(Activity activity, @Nullable ItemAction selected, OnSelected onSelected) {
        this(activity, selected, onSelected, null);
    }

    public DialogSelectItemAction(Activity activity, ItemAction selected, OnSelected onSelected, String message) {
        this.activity = activity;
        this.selected = selected;
        this.onSelected = onSelected;
        if (message == null) {
            message = activity.getString(R.string.dialog_selectItemAction_message);
        }
        this.message = message;


        ListView listView = new ListView(activity);
        listView.setPadding(10, 10, 10, 10);
        this.view = listView;

        dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.dialog_selectItemAction_title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_selectItemAction_cancel, null)
                .setView(view)
                .create();

        listView.setAdapter(new MinBaseAdapter() {
            @Override
            public int getCount() {
                return ItemAction.values().length;
            }

            @SuppressLint("SetTextI18n")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ItemAction itemAction = ItemAction.values()[position];
                if (excludeList.contains(itemAction)) {
                    return new FrameLayout(activity);
                }

                TextView textView = new TextView(DialogSelectItemAction.this.activity);
                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setText((selected == itemAction ? " > " : "") + EnumsRegistry.INSTANCE.name(itemAction, activity));
                textView.setTextSize(20);
                if (itemAction == DialogSelectItemAction.this.selected) textView.setTextColor(Color.RED);
                return textView;
            }
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ItemAction itemAction = ItemAction.values()[position];
            DialogSelectItemAction.this.onSelected.run(itemAction);
            dialog.cancel();
        });
    }

    public DialogSelectItemAction excludeFromList(ItemAction... actions) {
        excludeList.addAll(Arrays.asList(actions));
        return this;
    }

    public void show() {
        dialog.show();
    }

    public interface OnSelected {
        void run(ItemAction itemAction);
    }
}

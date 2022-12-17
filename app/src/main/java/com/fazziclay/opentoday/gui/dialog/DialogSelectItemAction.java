package com.fazziclay.opentoday.gui.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.util.MinBaseAdapter;

public class DialogSelectItemAction {
    private final Activity activity;
    private final SettingsManager.ItemAction selected;
    private final OnSelected onSelected;
    private final String message;
    private Dialog dialog;
    private final View view;

    public DialogSelectItemAction(Activity activity, SettingsManager.ItemAction selected, OnSelected onSelected) {
        this(activity, selected, onSelected, null);
    }

    public DialogSelectItemAction(Activity activity, SettingsManager.ItemAction selected, OnSelected onSelected, String message) {
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

        dialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_selectItemAction_title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_selectItemAction_cancel, null)
                .setView(view)
                .create();

        listView.setAdapter(new MinBaseAdapter() {
            @Override
            public int getCount() {
                return SettingsManager.ItemAction.values().length;
            }

            @SuppressLint("SetTextI18n")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                SettingsManager.ItemAction itemAction = SettingsManager.ItemAction.values()[position];

                TextView textView = new TextView(DialogSelectItemAction.this.activity);
                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setText((selected == itemAction ? " > " : "") + activity.getString(itemAction.nameResId()));
                textView.setTextSize(20);
                if (itemAction == DialogSelectItemAction.this.selected) textView.setTextColor(Color.RED);
                return textView;
            }
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            SettingsManager.ItemAction itemAction = SettingsManager.ItemAction.values()[position];
            DialogSelectItemAction.this.onSelected.run(itemAction);
            dialog.cancel();
        });
    }

    public void show() {
        dialog.show();
    }

    public interface OnSelected {
        void run(SettingsManager.ItemAction itemAction);
    }
}

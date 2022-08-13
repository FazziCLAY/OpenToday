package ru.fazziclay.opentoday.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.util.MinBaseAdapter;

public class DialogSelectItemAction {
    private final Activity activity;
    private final Dialog dialog;
    private final ItemManager.ItemAction selected;
    private final OnSelected onSelected;
    private final View view;

    public DialogSelectItemAction(Activity activity, ItemManager.ItemAction selected, OnSelected onSelected) {
        this.activity = activity;
        this.selected = selected;
        this.onSelected = onSelected;

        this.dialog = new Dialog(activity);

        ListView listView = new ListView(activity);
        listView.setPadding(10, 10, 10, 10);
        view = listView;

        listView.setAdapter(new MinBaseAdapter() {
            @Override
            public int getCount() {
                return ItemManager.ItemAction.values().length;
            }

            @SuppressLint("SetTextI18n")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ItemManager.ItemAction itemAction = ItemManager.ItemAction.values()[position];

                TextView textView = new TextView(DialogSelectItemAction.this.activity);
                textView.setText((selected == itemAction ? " > " : "") + activity.getString(itemAction.nameResId()));
                textView.setTextSize(20);
                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                if (itemAction == DialogSelectItemAction.this.selected) textView.setTextColor(Color.RED);
                return textView;
            }
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ItemManager.ItemAction itemAction = ItemManager.ItemAction.values()[position];
            DialogSelectItemAction.this.onSelected.run(itemAction);
            dialog.cancel();
        });
    }

    public void show() {
        dialog.setContentView(view);
        dialog.show();
    }

    public interface OnSelected {
        void run(ItemManager.ItemAction itemAction);
    }
}

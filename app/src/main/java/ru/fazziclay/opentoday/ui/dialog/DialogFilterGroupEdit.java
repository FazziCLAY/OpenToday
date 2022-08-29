package ru.fazziclay.opentoday.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.app.items.item.FilterGroupItem;
import ru.fazziclay.opentoday.ui.other.ItemsEditor;

public class DialogFilterGroupEdit {
    private final FilterGroupItem editObject;
    private final ItemsEditor itemsEditor;
    private final Dialog dialog;
    private final ItemStorage itemStorage;

    public DialogFilterGroupEdit(Activity activity, FilterGroupItem editObject, String path) {
        this.editObject = editObject;
        this.itemStorage = editObject;
        this.itemsEditor = new ItemsEditor(activity, null, App.get(activity).getItemManager(), itemStorage, path, null, false);
        this.itemsEditor.create();

        itemsEditor.getItemStorageDrawer().setItemViewWrapper((item, view) -> {
            LinearLayout layout = new LinearLayout(view.getContext());

            layout.addView(view);
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            ImageButton filter = new ImageButton(view.getContext());
            filter.setImageResource(android.R.drawable.stat_notify_voicemail);
            filter.setOnClickListener(v -> {
                new DialogEditItemFilter(activity, editObject.getItemFilter(item), editObject::save).show();
            });
            layout.addView(filter);
            filter.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 70, 0));

            return layout;
        });

        this.dialog = new Dialog(activity, android.R.style.ThemeOverlay_Material);
        dialog.setContentView(itemsEditor.getView());
        dialog.setOnCancelListener(dialog -> itemsEditor.destroy());
    }

    public void show() {
        dialog.show();
    }
}

package com.fazziclay.opentoday.plugins.gcp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.fazziclay.opentoday.api.Event;
import com.fazziclay.opentoday.api.EventHandler;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.events.gui.CurrentItemsStorageContextChanged;
import com.fazziclay.opentoday.app.events.gui.toolbar.AppToolbarSelectionClickEvent;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.items.selection.SelectionManager;
import com.fazziclay.opentoday.databinding.ToolbarMoreSelectionBinding;
import com.fazziclay.opentoday.gui.ColorPicker;
import com.fazziclay.opentoday.gui.item.HolderDestroyer;
import com.fazziclay.opentoday.gui.item.ItemViewGenerator;
import com.fazziclay.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.fazziclay.opentoday.gui.item.ItemsStorageDrawerBehavior;

public class GcpEventHandler extends EventHandler {
    private ItemsStorage currentItemsStorage = null;
    private final GlobalChangesPlugin globalChangesPlugin;

    public GcpEventHandler(GlobalChangesPlugin globalChangesPlugin) {
        this.globalChangesPlugin = globalChangesPlugin;
    }

    @Override
    public void handle(Event event) {
        super.handle(event);

        if (event instanceof AppToolbarSelectionClickEvent e) {
            injectSelections(e);
        }

        if (event instanceof CurrentItemsStorageContextChanged e) {
            injectCurrentContext(e);
        }
    }

    private void injectCurrentContext(CurrentItemsStorageContextChanged e) {
        currentItemsStorage = e.getCurrentItemsStorage();
    }

    private void injectSelections(AppToolbarSelectionClickEvent e) {
        ToolbarMoreSelectionBinding localBinding = e.getLocalBinding();
        Context context = localBinding.getRoot().getContext();
        SelectionManager selectionManager = e.getSelectionManager();

        Button massBgColor = new Button(context);
        massBgColor.setText("Mass BG color");
        massBgColor.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        localBinding.getRoot().addView(massBgColor);

        massBgColor.setOnClickListener(i -> new ColorPicker(context, 0xffff00)
                .setting(true, true, true)
                .setColorHistoryManager(App.get(context).getColorHistoryManager())
                .setNeutralDialogButton("Reset ALL", () -> {
                    for (Item item : selectionManager.getItems()) {
                        item.setViewCustomBackgroundColor(false);
                    }
                })
                .showDialog("Mass background", "Cancel", "Apply", color -> {
                    for (Item item : selectionManager.getItems()) {
                        item.setViewBackgroundColor(color);
                        item.setViewCustomBackgroundColor(true);
                    }
                }));






        Button selectAll = new Button(context);
        selectAll.setText("Select all");
        selectAll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        localBinding.getRoot().addView(selectAll);

        selectAll.setOnClickListener(i -> {
            if (currentItemsStorage != null) {
                for (Item item : currentItemsStorage.getAllItems()) {
                    selectionManager.deselectItem(item);
                    selectionManager.selectItem(item);
                }
            }
        });



        Button massTextColor = new Button(context);
        massTextColor.setText("Mass TeXT color");
        massTextColor.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        localBinding.getRoot().addView(massTextColor);

        massTextColor.setOnClickListener(i -> new ColorPicker(context, 0xffff00)
                .setting(true, true, true)
                .setColorHistoryManager(App.get(context).getColorHistoryManager())
                .setNeutralDialogButton("Reset ALL", () -> {
                    for (Item item : selectionManager.getItems()) {
                        if (item instanceof TextItem textItem) {
                            textItem.setCustomTextColor(false);
                        }
                    }
                })
                .showDialog("Mass text color", "Cancel", "Apply", color -> {
                    for (Item item : selectionManager.getItems()) {
                        if (item instanceof TextItem textItem) {
                            textItem.setCustomTextColor(true);
                            textItem.setTextColor(color);
                        }
                    }
                }));





        Button massRemoveNotification = new Button(context);
        massRemoveNotification.setText("Mass remove notifications");
        massRemoveNotification.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        localBinding.getRoot().addView(massRemoveNotification);

        massRemoveNotification.setOnClickListener(i -> {
            var view = new LinearLayout(context);
            view.setOrientation(LinearLayout.VERTICAL);

            ItemViewGenerator itemViewGenerator = ItemViewGenerator.builder(e.getActivity())
                    .setPreviewMode(true)
                    .build();

            ItemViewGeneratorBehavior behavior = new ItemViewGeneratorBehavior() {
                @Override
                public boolean isConfirmFastChanges() {
                    return false;
                }

                @Override
                public void setConfirmFastChanges(boolean b) {

                }

                @Override
                public Drawable getForeground(Item item) {
                    return null;
                }

                @Override
                public void onGroupEdit(GroupItem groupItem) {

                }

                @Override
                public void onCycleListEdit(CycleListItem cycleListItem) {

                }

                @Override
                public void onFilterGroupEdit(FilterGroupItem filterGroupItem) {

                }

                @Override
                public ItemsStorageDrawerBehavior getItemsStorageDrawerBehavior(Item item) {
                    return null;
                }

                @Override
                public boolean isRenderMinimized(Item item) {
                    return true;
                }
            };
            HolderDestroyer destroyer = new HolderDestroyer();
            for (Item item : selectionManager.getItems()) {
                View iv = itemViewGenerator.generate(item, view, behavior, destroyer, item1 -> {});

                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.HORIZONTAL);

                int size = item.getNotifications().size();
                TextView t = new TextView(context);
                t.setText(String.valueOf(size));
                if (size > 0) {
                    t.setTextColor(Color.RED);
                }
                t.setTextSize(size > 0 ? 27 : 25);
                layout.addView(t);
                layout.addView(iv);
                var p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                p.setMargins(0, 5, 0, 5);
                layout.setLayoutParams(p);
                view.addView(layout);
            }

            var scroll = new ScrollView(context);
            scroll.addView(view);

            new AlertDialog.Builder(context)
                    .setTitle("Preview")
                    .setView(scroll)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Apply", (dialogInterface, i1) -> {
                        for (Item item : selectionManager.getItems()) {
                            item.getNotifications().clear();
                        }
                    })
                    .show();
        });
    }
}
package ru.fazziclay.opentoday.ui.other;

import static ru.fazziclay.opentoday.util.InlineUtil.fcu_viewOnClick;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.app.items.ItemsRegistry;
import ru.fazziclay.opentoday.app.items.Selection;
import ru.fazziclay.opentoday.app.items.callback.OnSelectionChanged;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.callback.CallbackImportance;
import ru.fazziclay.opentoday.databinding.ToolbarBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreFileBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreItemsBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreItemsItemBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreOpentodayBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreSelectionBinding;
import ru.fazziclay.opentoday.ui.dialog.DialogAppAbout;
import ru.fazziclay.opentoday.ui.dialog.DialogAppSettings;
import ru.fazziclay.opentoday.ui.dialog.DialogItem;
import ru.fazziclay.opentoday.ui.dialog.DialogPreviewDeleteItems;
import ru.fazziclay.opentoday.ui.dialog.DialogSelectItemAction;
import ru.fazziclay.opentoday.util.SimpleSpinnerAdapter;

public class AppToolbar {
    private final Activity activity;
    private final ToolbarBinding binding;
    private final View toolbarView;
    private final LinearLayout toolbarMoreView;
    private final App app;
    private final ItemStorage itemStorage;

    private View currentToolbarButton = null;
    private boolean destroyed = false;

    private View itemsSectionCacheView = null;
    private final SimpleSpinnerAdapter.ViewStyle<Class<? extends Item>> viewStyle;

    private OnSelectionChanged onSelectionChanged = null;

    public AppToolbar(Activity activity) {
        this(activity, App.get(activity).getItemManager());
    }

    public AppToolbar(Activity activity, ItemStorage itemStorage) {
        this.activity = activity;
        this.app = App.get(activity);
        this.binding = ToolbarBinding.inflate(activity.getLayoutInflater());
        this.toolbarMoreView = new LinearLayout(activity);
        this.itemStorage = itemStorage;
        this.toolbarMoreView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.toolbarMoreView.setOrientation(LinearLayout.VERTICAL);
        this.toolbarView = binding.getRoot();

        this.viewStyle = (string, value, convertView, parent) -> {
            ToolbarMoreItemsItemBinding b = ToolbarMoreItemsItemBinding.inflate(activity.getLayoutInflater(), parent, false);
            if (convertView != null) return convertView;

            b.name.setText(string);
            b.create.setOnClickListener(v -> {
                DialogItem dialogItem = new DialogItem(activity, app.getItemManager());
                dialogItem.create(value, itemStorage::addItem);
            });
            b.add.setOnClickListener(v -> {
                Item item = ItemsRegistry.REGISTRY.getItemInfoByClass(value).create();
                itemStorage.addItem(item);
            });

            return b.getRoot();
        };

        setup();
    }

    private void resetMoreView() {
        toolbarMoreView.removeAllViews();
        toolbarMoreView.setBackground(null);
        toolbarMoreView.setOnClickListener(null);
        if (currentToolbarButton != null) {
            currentToolbarButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#585858")));
        }
    }

    private boolean preOnClick(View buttonView) {
        boolean ret;
        resetMoreView();
        if (currentToolbarButton == buttonView) {
            currentToolbarButton = null;
            ret = false;
        } else {
            currentToolbarButton = buttonView;
            currentToolbarButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0000ff")));
            ret = true;
        }
        return ret;
    }

    private void setup() {
        fcu_viewOnClick(binding.selection, () -> {
            if (preOnClick(binding.selection)) onSelectionClick();
        });
        fcu_viewOnClick(binding.opentoday, () -> {
            if (preOnClick(binding.opentoday)) onOpenTodayClick();
        });
        fcu_viewOnClick(binding.items, () -> {
            if (preOnClick(binding.items)) onItemsClick();
        });
        fcu_viewOnClick(binding.file, () -> {
            if (preOnClick(binding.file)) onFileClick();
        });
    }

    private void onFileClick() {
        ToolbarMoreFileBinding lBinding = ToolbarMoreFileBinding.inflate(activity.getLayoutInflater());

        lBinding.saveAll.setOnClickListener(v -> {
            ItemManager itemManager = app.getItemManager();
            itemManager.saveAllDirect();
            Toast.makeText(activity, R.string.success, Toast.LENGTH_SHORT).show();
        });

        lBinding.getRoot().setOnClickListener(null);
        toolbarMoreView.addView(lBinding.getRoot());
    }

    private void onItemsClick() {
        if (itemsSectionCacheView != null) {
            if (itemsSectionCacheView.getParent() != null) {
                ((ViewGroup)itemsSectionCacheView.getParent()).removeView(itemsSectionCacheView);
                toolbarMoreView.addView(itemsSectionCacheView);
                return;
            }
        }
        ToolbarMoreItemsBinding lBinding = ToolbarMoreItemsBinding.inflate(activity.getLayoutInflater());

        for (ItemsRegistry.ItemInfo i : ItemsRegistry.REGISTRY.getAllItems()) {
            lBinding.items.addView(viewStyle.create(activity.getString(i.getNameResId()), i.getClassType(), null, lBinding.items));
        }

        ItemManager itemManager = app.getItemManager();

        lBinding.changeOnClick.setOnClickListener(v -> new DialogSelectItemAction(activity, itemManager.getItemOnClickAction(), itemManager::setItemOnClickAction, activity.getString(R.string.toolbar_more_items_action_click)).show());
        lBinding.changeOnLeftSwipe.setOnClickListener(v -> new DialogSelectItemAction(activity, itemManager.getItemOnLeftAction(), itemManager::setItemOnLeftAction, activity.getString(R.string.toolbar_more_items_action_leftSwipe)).show());

        lBinding.getRoot().setBackground(new ColorDrawable(Color.parseColor("#99000000")));

        lBinding.getRoot().setOnClickListener(null);
        toolbarMoreView.addView(itemsSectionCacheView = lBinding.getRoot());
    }

    private void onOpenTodayClick() {
        ToolbarMoreOpentodayBinding lBinding = ToolbarMoreOpentodayBinding.inflate(activity.getLayoutInflater());

        fcu_viewOnClick(lBinding.about, () -> new DialogAppAbout(activity).show());
        fcu_viewOnClick(lBinding.settings, () -> new DialogAppSettings(activity).show());

        lBinding.getRoot().setOnClickListener(null);
        toolbarMoreView.addView(lBinding.getRoot());
    }

    private void onSelectionClick() {
        ToolbarMoreSelectionBinding lBinding = ToolbarMoreSelectionBinding.inflate(activity.getLayoutInflater());
        fcu_viewOnClick(lBinding.moveSelectedToRoot, () -> {
            ItemManager itemManager = app.getItemManager();
            if (itemManager.getSelections().length == 0) {
                Toast.makeText(app, R.string.nothing_selected, Toast.LENGTH_SHORT).show();
            } else {
                for (Selection selection : itemManager.getSelections()) {
                    selection.moveToStorage(itemStorage);
                    itemManager.deselectItem(selection);
                }
            }
        });

        fcu_viewOnClick(lBinding.deselectAll, () -> {
            ItemManager itemManager = app.getItemManager();
            itemManager.deselectAll();
        });

        onSelectionChanged = selections -> {
            lBinding.selectedInfo.setText(activity.getString(R.string.toolbar_more_selection_info, String.valueOf(selections.size())));
            lBinding.delete.setEnabled(!selections.isEmpty());
            lBinding.moveSelectedToRoot.setEnabled(!selections.isEmpty());
        };
        onSelectionChanged.run(Arrays.asList(app.getItemManager().getSelections()));
        app.getItemManager().getOnSelectionUpdated().addCallback(CallbackImportance.DEFAULT, onSelectionChanged);

        fcu_viewOnClick(lBinding.delete, () -> {
            ItemManager itemManager = app.getItemManager();
            if (itemManager.getSelections().length == 0) {
                Toast.makeText(activity, R.string.nothing_selected, Toast.LENGTH_SHORT).show();
                return;
            }
            List<Item> items = new ArrayList<>();
            for (Selection selection : itemManager.getSelections()) {
                items.add(selection.getItem());
            }
            new DialogPreviewDeleteItems(activity, items.toArray(new Item[0])).show();
        });
        lBinding.getRoot().setOnClickListener(null);
        toolbarMoreView.addView(lBinding.getRoot());
    }

    public View getToolbarView() {
        return toolbarView;
    }

    public View getToolbarMoreView() {
        return toolbarMoreView;
    }

    public void destroy() {
        if (destroyed) {
            throw new RuntimeException("destroyed");
        }
        destroyed = true;
        if (onSelectionChanged != null) app.getItemManager().getOnSelectionUpdated().deleteCallback(onSelectionChanged);
    }
}

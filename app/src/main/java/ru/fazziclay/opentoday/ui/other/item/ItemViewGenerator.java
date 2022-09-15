package ru.fazziclay.opentoday.ui.other.item;

import static ru.fazziclay.opentoday.util.InlineUtil.fcu_viewOnClick;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.app.items.item.CheckboxItem;
import ru.fazziclay.opentoday.app.items.item.CounterItem;
import ru.fazziclay.opentoday.app.items.item.CycleListItem;
import ru.fazziclay.opentoday.app.items.item.DayRepeatableCheckboxItem;
import ru.fazziclay.opentoday.app.items.item.FilterGroupItem;
import ru.fazziclay.opentoday.app.items.item.GroupItem;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.item.TextItem;
import ru.fazziclay.opentoday.app.settings.SettingsManager;
import ru.fazziclay.opentoday.databinding.ItemCheckboxBinding;
import ru.fazziclay.opentoday.databinding.ItemCounterBinding;
import ru.fazziclay.opentoday.databinding.ItemCycleListBinding;
import ru.fazziclay.opentoday.databinding.ItemDayRepeatableCheckboxBinding;
import ru.fazziclay.opentoday.databinding.ItemFilterGroupBinding;
import ru.fazziclay.opentoday.databinding.ItemGroupBinding;
import ru.fazziclay.opentoday.databinding.ItemTextBinding;
import ru.fazziclay.opentoday.ui.dialog.DialogFilterGroupEdit;
import ru.fazziclay.opentoday.ui.dialog.DialogItemStorageEditor;
import ru.fazziclay.opentoday.ui.other.ItemViewHolder;
import ru.fazziclay.opentoday.util.DebugUtil;
import ru.fazziclay.opentoday.util.ResUtil;

public class ItemViewGenerator {
    @NotNull private final Activity activity;
    @NotNull private final ItemManager itemManager;
    @NotNull private final SettingsManager settingsManager;
    @Nullable private final String path; // Path (/item1/item3/item32/item3)
    @Nullable private final OnItemClick onItemClick; // Action when view click
    private final boolean previewMode; // Disable items minimize view patch & disable buttons

    public ItemViewGenerator(@NotNull Activity activity, @NotNull ItemManager itemManager, @Nullable String path, @Nullable OnItemClick onItemClick, boolean previewMode) {
        this.activity = activity;
        this.itemManager = itemManager;
        this.settingsManager = App.get(activity).getSettingsManager();
        this.path = path;
        this.onItemClick = onItemClick;
        this.previewMode = previewMode;
    }

    public View generate(Item item, ViewGroup view) {
        Class<? extends Item> type = item.getClass();

        View ret;
        if (type == Item.class) {
            throw new RuntimeException("Illegal itemType. Use Object extends Item");

        } else if (type == TextItem.class) {
            ret = generateTextItemView(activity, (TextItem) item, view);

        } else if (type == CheckboxItem.class) {
            ret = generateCheckboxItemView(activity, (CheckboxItem) item, view);

        } else if (type == DayRepeatableCheckboxItem.class) {
            ret = generateDayRepeatableCheckboxItemView(activity, (DayRepeatableCheckboxItem) item, view);

        } else if (type == CycleListItem.class) {
            ret = generateCycleListItemView(activity, (CycleListItem) item, view);

        } else if (type == CounterItem.class) {
            ret = generateCounterItemView(activity, (CounterItem) item, view);

        } else if (type == GroupItem.class) {
            ret = generateGroupItemView(activity, (GroupItem) item, view);

        } else if (type == FilterGroupItem.class) {
            ret = generateFilterGroupItemView(activity, (FilterGroupItem) item, view);

        } else {
            Log.e("Unknown item type", "Throw exception for 3 seconds...");
            DebugUtil.sleep(3000);
            throw new RuntimeException("Unknown item type '" + type.getName() + "'! check ItemViewGenerator!");
        }

        // Minimal height
        if (!item.isMinimize()) ret.setMinimumHeight(item.getViewMinHeight());

        // BackgroundColor
        if (item.isViewCustomBackgroundColor()) {
            ret.setBackgroundTintList(ColorStateList.valueOf(item.getViewBackgroundColor()));
        }

        // Minimize view patch
        if (!previewMode && item.isMinimize()) {
            if (settingsManager.isMinimizeGrayColor()) {
                ret.setForeground(AppCompatResources.getDrawable(activity, R.drawable.shape));
                ret.setForegroundTintList(ColorStateList.valueOf(Color.parseColor("#44f0fff0")));
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 0, 0, 0);
            ret.setLayoutParams(layoutParams);
        }
        if (onItemClick != null) fcu_viewOnClick(ret, () -> onItemClick.run(item));
        return ret;
    }

    private View generateFilterGroupItemView(Activity activity, FilterGroupItem item, ViewGroup view) {
        ItemFilterGroupBinding binding = ItemFilterGroupBinding.inflate(activity.getLayoutInflater(), view, false);

        applyTextItemToTextView(item, binding.title);
        // group
        if (!item.isMinimize()) {
            for (Item activeItem : item.getActiveItems()) {
                ItemViewHolder holder = new ItemViewHolder(activity);
                ItemViewGenerator itemViewGenerator = new ItemViewGenerator(activity, itemManager, appendPath(item.getText()), onItemClick, previewMode);
                holder.layout.addView(itemViewGenerator.generate(activeItem, binding.content));

                if (itemManager.isSelected(activeItem)) {
                    holder.layout.setForeground(new ColorDrawable(ResUtil.getAttrColor(activity, R.attr.item_selectionForegroundColor)));
                } else {
                    holder.layout.setForeground(null);
                }

                binding.content.addView(holder.layout);
            }
        }

        binding.externalEditor.setOnClickListener(v -> new DialogFilterGroupEdit(activity, item, appendPath(item.getText())).show());

        return binding.getRoot();
    }

    private View generateGroupItemView(Activity activity, GroupItem item, ViewGroup view) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(activity.getLayoutInflater(), view, false);

        // text
        applyTextItemToTextView(item, binding.title);

        // group
        if (!item.isMinimize()) {
            ItemStorageDrawer itemStorageDrawer = new ItemStorageDrawer(activity, App.get().getItemManager(), item.getItemStorage(), appendPath(item.getText()), onItemClick, previewMode);
            itemStorageDrawer.create();
            binding.content.addView(itemStorageDrawer.getView());
        }
        applyExternalEditorButton(item.getItemStorage(), binding.externalEditor, item.getText());

        return binding.getRoot();
    }

    private View generateCounterItemView(Activity activity, CounterItem item, ViewGroup view) {
        ItemCounterBinding binding = ItemCounterBinding.inflate(activity.getLayoutInflater(), view, false);

        // Title
        applyTextItemToTextView(item, binding.title);

        // counter
        if (previewMode) {
            binding.up.setEnabled(false);
            binding.down.setEnabled(false);
        }
        fcu_viewOnClick(binding.up, item::up);
        fcu_viewOnClick(binding.down, item::down);


        binding.counter.setText(String.valueOf(item.getCounter()));

        return binding.getRoot();
    }

    private View generateCycleListItemView(Activity activity, CycleListItem item, ViewGroup view) {
        ItemCycleListBinding binding = ItemCycleListBinding.inflate(activity.getLayoutInflater(), view, false);

        // Text
        applyTextItemToTextView(item, binding.title);

        // Cycle list
        binding.next.setOnClickListener(v -> item.next());
        binding.previous.setOnClickListener(v -> item.previous());


        if (previewMode) {
            binding.next.setEnabled(false);
            binding.previous.setEnabled(false);
        }

        applyExternalEditorButton(item.getItemsCycleStorage(), binding.externalEditor, item.getText());

        Item current = item.getCurrentItem();
        if (!item.isMinimize()) {
            if (current != null) {
                ItemViewHolder holder = new ItemViewHolder(activity);
                ItemViewGenerator itemViewGenerator = new ItemViewGenerator(activity, itemManager, appendPath(item.getText()), onItemClick, previewMode);
                holder.layout.addView(itemViewGenerator.generate(current, binding.getRoot()));

                if (itemManager.isSelected(item)) {
                    holder.layout.setForeground(new ColorDrawable(ResUtil.getAttrColor(activity, R.attr.item_selectionForegroundColor)));
                } else {
                    holder.layout.setForeground(null);
                }

                binding.content.addView(holder.layout);
            } else {
                TextView textView = new TextView(activity);
                textView.setText(R.string.empty);
                binding.content.addView(textView);
            }
        }
        return binding.getRoot();
    }

    private View generateDayRepeatableCheckboxItemView(Activity activity, DayRepeatableCheckboxItem item, ViewGroup viewGroup) {
        ItemDayRepeatableCheckboxBinding binding = ItemDayRepeatableCheckboxBinding.inflate(activity.getLayoutInflater(), viewGroup, false);

        // Text
        applyTextItemToTextView(item, binding.text);
        // Checkbox
        applyCheckItemToCheckBoxView(item, binding.checkbox);

        return binding.getRoot();
    }

    private View generateCheckboxItemView(Activity activity, CheckboxItem item, ViewGroup viewGroup) {
        ItemCheckboxBinding binding = ItemCheckboxBinding.inflate(activity.getLayoutInflater(), viewGroup, false);

        // Text
        applyTextItemToTextView(item, binding.text);
        // Checkbox
        applyCheckItemToCheckBoxView(item, binding.checkbox);

        return binding.getRoot();
    }

    private View generateTextItemView(Activity activity, TextItem item, ViewGroup viewGroup) {
        ItemTextBinding binding = ItemTextBinding.inflate(activity.getLayoutInflater(), viewGroup, false);

        // Text
        applyTextItemToTextView(item, binding.title);

        return binding.getRoot();
    }

    //
    private void applyTextItemToTextView(TextItem item, TextView view) {
        if (!previewMode && item.isMinimize()) {
            view.setText(item.getText().split("\n")[0]);
        } else {
            view.setText(item.getText());
        }
        if (item.isCustomTextColor()) {
            view.setTextColor(ColorStateList.valueOf(item.getTextColor()));
        }
        if (item.isClickableUrls()) Linkify.addLinks(view, Linkify.ALL);
    }

    private void applyCheckItemToCheckBoxView(CheckboxItem item, CheckBox view) {
        view.setChecked(item.isChecked());
        if (previewMode) view.setEnabled(false);
        fcu_viewOnClick(view, () -> {
            item.setChecked(view.isChecked());
            item.visibleChanged();
            item.save();
        });
    }

    private void applyExternalEditorButton(ItemStorage itemStorage, View view, String localPath) {
        if (previewMode) view.setEnabled(false);
        view.setOnClickListener(v -> new DialogItemStorageEditor(activity, itemManager, itemStorage, onItemClick, appendPath(localPath)).show());
    }

    private String appendPath(String localPath) {
        return appendPath(this.path, localPath);
    }

    public static String appendPath(String path, String localPath) {
        if (path == null) return null;
        if (localPath == null) localPath = "";
        localPath = localPath.split("\n")[0];
        if (localPath.length() > 60) {
            localPath = localPath.substring(0, 57) + "...";
        }
        return path + (path.endsWith("/") ? "" : "/") + "(" + localPath + ")";
    }
}

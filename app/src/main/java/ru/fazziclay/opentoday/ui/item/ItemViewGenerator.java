package ru.fazziclay.opentoday.ui.item;

import static ru.fazziclay.opentoday.util.InlineUtil.fcu_viewOnClick;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.annotation.ForItem;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.item.CheckboxItem;
import ru.fazziclay.opentoday.app.items.item.CounterItem;
import ru.fazziclay.opentoday.app.items.item.CycleListItem;
import ru.fazziclay.opentoday.app.items.item.DayRepeatableCheckboxItem;
import ru.fazziclay.opentoday.app.items.item.FilterGroupItem;
import ru.fazziclay.opentoday.app.items.item.GroupItem;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.item.TextItem;
import ru.fazziclay.opentoday.app.settings.SettingsManager;
import ru.fazziclay.opentoday.callback.Status;
import ru.fazziclay.opentoday.databinding.ItemCheckboxBinding;
import ru.fazziclay.opentoday.databinding.ItemCounterBinding;
import ru.fazziclay.opentoday.databinding.ItemCycleListBinding;
import ru.fazziclay.opentoday.databinding.ItemDayRepeatableCheckboxBinding;
import ru.fazziclay.opentoday.databinding.ItemFilterGroupBinding;
import ru.fazziclay.opentoday.databinding.ItemGroupBinding;
import ru.fazziclay.opentoday.databinding.ItemTextBinding;
import ru.fazziclay.opentoday.ui.interfaces.ContentInterface;
import ru.fazziclay.opentoday.ui.interfaces.IVGEditButtonInterface;
import ru.fazziclay.opentoday.ui.interfaces.OnItemClick;
import ru.fazziclay.opentoday.util.DebugUtil;
import ru.fazziclay.opentoday.util.ResUtil;

public class ItemViewGenerator {
    @NonNull private final Activity activity;
    @NonNull private final LayoutInflater layoutInflater;
    @NonNull private final ItemManager itemManager;
    @NonNull private final SettingsManager settingsManager;
    @Nullable private final OnItemClick onItemClick; // Action when view click
    private final IVGEditButtonInterface storageEdits;
    private final boolean previewMode; // Disable items minimize view patch & disable buttons

    public ItemViewGenerator(@NonNull Activity activity, @NonNull ItemManager itemManager, @Nullable OnItemClick onItemClick, boolean previewMode, IVGEditButtonInterface storageEdits) {
        this.activity = activity;
        this.layoutInflater = activity.getLayoutInflater();
        this.itemManager = itemManager;
        this.settingsManager = App.get(activity).getSettingsManager();
        this.onItemClick = onItemClick;
        this.previewMode = previewMode;
        this.storageEdits = storageEdits;
    }

    public View generate(Item item, ViewGroup view) {
        Class<? extends Item> type = item.getClass();

        View ret;
        if (type == Item.class) {
            throw new RuntimeException("Illegal itemType. Use Object extends Item");

        } else if (type == TextItem.class) {
            ret = generateTextItemView((TextItem) item, view);

        } else if (type == CheckboxItem.class) {
            ret = generateCheckboxItemView((CheckboxItem) item, view);

        } else if (type == DayRepeatableCheckboxItem.class) {
            ret = generateDayRepeatableCheckboxItemView((DayRepeatableCheckboxItem) item, view);

        } else if (type == CycleListItem.class) {
            ret = generateCycleListItemView((CycleListItem) item, view, i -> storageEdits.onCycleListEdit((CycleListItem) item),
                    (linearLayout, empty) -> {
                        CurrentItemStorageDrawer currentItemStorageDrawer = new CurrentItemStorageDrawer(this.activity, itemManager, (CycleListItem) item, previewMode, onItemClick, storageEdits);
                        linearLayout.addView(currentItemStorageDrawer.getView());
                        currentItemStorageDrawer.setOnUpdateListener(currentItem -> {
                            empty.setVisibility(currentItem == null ? View.VISIBLE : View.GONE);
                            return Status.NONE;
                        });
                        currentItemStorageDrawer.create();
                    });

        } else if (type == CounterItem.class) {
            ret = generateCounterItemView((CounterItem) item, view);

        } else if (type == GroupItem.class) {
            ret = generateGroupItemView((GroupItem) item, view, v -> storageEdits.onGroupEdit((GroupItem) item), linearLayout -> {
                ItemStorageDrawer itemStorageDrawer = new ItemStorageDrawer(activity, itemManager, ((GroupItem) item).getItemStorage(), onItemClick, previewMode, storageEdits);
                itemStorageDrawer.create();
                linearLayout.addView(itemStorageDrawer.getView());
            });

        } else if (type == FilterGroupItem.class) {
            ret = generateFilterGroupItemView((FilterGroupItem) item, view, v -> storageEdits.onFilterGroupEdit((FilterGroupItem) item), linearLayout -> {
                for (Item activeItem : ((FilterGroupItem) item).getActiveItems()) {
                    ItemViewHolder holder = new ItemViewHolder(activity);
                    ItemViewGenerator itemViewGenerator = new ItemViewGenerator(activity, itemManager, onItemClick, previewMode, storageEdits);
                    holder.layout.addView(itemViewGenerator.generate(activeItem, linearLayout));

                    if (itemManager.isSelected(activeItem)) {
                        holder.layout.setForeground(new ColorDrawable(ResUtil.getAttrColor(activity, R.attr.item_selectionForegroundColor)));
                    } else {
                        holder.layout.setForeground(null);
                    }

                    linearLayout.addView(holder.layout);
                }
            });

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
            layoutParams.setMargins(0, 0, 15, 0);
            ret.setLayoutParams(layoutParams);
        }
        if (onItemClick != null) fcu_viewOnClick(ret, () -> onItemClick.run(item));
        return ret;
    }

    @ForItem(key = FilterGroupItem.class)
    private View generateFilterGroupItemView(FilterGroupItem item, ViewGroup view, View.OnClickListener editButtonClick, ContentInterface contentInterface) {
        ItemFilterGroupBinding binding = ItemFilterGroupBinding.inflate(this.layoutInflater, view, false);

        // Text
        applyTextItemToTextView(item, binding.title);

        // FilterGroup
        if (!item.isMinimize()) {
            contentInterface.run(binding.content);
        }

        binding.externalEditor.setEnabled(!previewMode);
        binding.externalEditor.setOnClickListener(editButtonClick);

        return binding.getRoot();
    }

    @ForItem(key = GroupItem.class)
    private View generateGroupItemView(GroupItem item, ViewGroup view, View.OnClickListener editButtonClick, ContentInterface contentInterface) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(this.layoutInflater, view, false);

        // Text
        applyTextItemToTextView(item, binding.title);

        // Group
        if (!item.isMinimize()) {
            contentInterface.run(binding.content);
        }
        binding.externalEditor.setEnabled(!previewMode);
        binding.externalEditor.setOnClickListener(editButtonClick);

        return binding.getRoot();
    }

    @ForItem(key = CounterItem.class)
    public View generateCounterItemView(CounterItem item, ViewGroup view) {
        ItemCounterBinding binding = ItemCounterBinding.inflate(this.layoutInflater, view, false);

        // Title
        applyTextItemToTextView(item, binding.title);

        // Counter
        fcu_viewOnClick(binding.up, item::up);
        fcu_viewOnClick(binding.down, item::down);
        binding.up.setEnabled(!previewMode);
        binding.down.setEnabled(!previewMode);

        binding.counter.setText(String.valueOf(item.getCounter()));

        return binding.getRoot();
    }

    @ForItem(key = CycleListItem.class)
    public View generateCycleListItemView(CycleListItem item, ViewGroup view, View.OnClickListener editButtonClick, ContentInterfaceE contentInterface) {
        ItemCycleListBinding binding = ItemCycleListBinding.inflate(this.layoutInflater, view, false);

        // Text
        applyTextItemToTextView(item, binding.title);

        // CycleList
        binding.next.setEnabled(!previewMode);
        binding.next.setOnClickListener(v -> item.next());
        binding.previous.setEnabled(!previewMode);
        binding.previous.setOnClickListener(v -> item.previous());

        binding.externalEditor.setEnabled(!previewMode);
        binding.externalEditor.setOnClickListener(editButtonClick);

        if (!item.isMinimize()) {
            contentInterface.run(binding.content, binding.empty);
        } else {
            binding.empty.setVisibility(View.GONE);
        }

        return binding.getRoot();
    }

    @ForItem(key = DayRepeatableCheckboxItem.class)
    public View generateDayRepeatableCheckboxItemView(DayRepeatableCheckboxItem item, ViewGroup viewGroup) {
        ItemDayRepeatableCheckboxBinding binding = ItemDayRepeatableCheckboxBinding.inflate(this.layoutInflater, viewGroup, false);

        applyTextItemToTextView(item, binding.text);
        applyCheckItemToCheckBoxView(item, binding.checkbox);

        return binding.getRoot();
    }

    @ForItem(key = CheckboxItem.class)
    public View generateCheckboxItemView(CheckboxItem item, ViewGroup viewGroup) {
        ItemCheckboxBinding binding = ItemCheckboxBinding.inflate(this.layoutInflater, viewGroup, false);

        applyTextItemToTextView(item, binding.text);
        applyCheckItemToCheckBoxView(item, binding.checkbox);

        return binding.getRoot();
    }

    @ForItem(key = TextItem.class)
    public View generateTextItemView(TextItem item, ViewGroup viewGroup) {
        ItemTextBinding binding = ItemTextBinding.inflate(this.layoutInflater, viewGroup, false);

        // Text
        applyTextItemToTextView(item, binding.title);

        return binding.getRoot();
    }

    //
    private void applyTextItemToTextView(TextItem item, TextView view) {
        if (!previewMode && item.isMinimize()) {
            String text = item.getText().split("\n")[0];
            if (text.length() > 60) {
                text = text.substring(0, 57) + "...";
            }
            view.setText(text);
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
        view.setEnabled(!previewMode);
        fcu_viewOnClick(view, () -> {
            item.setChecked(view.isChecked());
            item.visibleChanged();
            item.save();
        });
    }

    private interface ContentInterfaceE {
        void run(LinearLayout linearLayout, View empty);
    }
}

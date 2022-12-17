package com.fazziclay.opentoday.gui.item;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;
import static com.fazziclay.opentoday.util.InlineUtil.viewVisible;

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

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.annotation.ForItem;
import com.fazziclay.opentoday.app.items.ItemManager;
import com.fazziclay.opentoday.app.items.item.CheckboxItem;
import com.fazziclay.opentoday.app.items.item.CounterItem;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.DayRepeatableCheckboxItem;
import com.fazziclay.opentoday.app.items.item.DebugTickCounterItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.LongTextItem;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.callback.Status;
import com.fazziclay.opentoday.databinding.ItemCheckboxBinding;
import com.fazziclay.opentoday.databinding.ItemCounterBinding;
import com.fazziclay.opentoday.databinding.ItemCycleListBinding;
import com.fazziclay.opentoday.databinding.ItemDayRepeatableCheckboxBinding;
import com.fazziclay.opentoday.databinding.ItemFilterGroupBinding;
import com.fazziclay.opentoday.databinding.ItemGroupBinding;
import com.fazziclay.opentoday.databinding.ItemLongtextBinding;
import com.fazziclay.opentoday.databinding.ItemTextBinding;
import com.fazziclay.opentoday.gui.interfaces.ContentInterface;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.gui.interfaces.StorageEditsActions;
import com.fazziclay.opentoday.util.DebugUtil;
import com.fazziclay.opentoday.util.ResUtil;

public class ItemViewGenerator {
    @NonNull private final Activity activity;
    @NonNull private final LayoutInflater layoutInflater;
    @NonNull private final ItemManager itemManager;
    @NonNull private final SettingsManager settingsManager;
    private final boolean previewMode; // Disable items minimize view patch & disable buttons
    @Nullable private final ItemInterface itemOnClick; // Action when view click
    @NonNull private final ItemInterface onItemEditor;
    private final StorageEditsActions storageEdits;

    public ItemViewGenerator(@NonNull final Activity activity, @NonNull final ItemManager itemManager, @NonNull final SettingsManager settingsManager, final boolean previewMode, @Nullable final ItemInterface itemOnClick, @NonNull final ItemInterface onItemEditor, @NonNull final StorageEditsActions storageEdits) {
        this.activity = activity;
        this.layoutInflater = activity.getLayoutInflater();
        this.itemManager = itemManager;
        this.settingsManager = settingsManager;
        this.previewMode = previewMode;
        this.itemOnClick = itemOnClick;
        this.onItemEditor = onItemEditor;
        this.storageEdits = storageEdits;
    }

    public static CreateBuilder builder(final Activity activity, final ItemManager itemManager, final SettingsManager settingsManager) {
        return new CreateBuilder(activity, itemManager, settingsManager);
    }

    public View generate(final Item item, final ViewGroup parent) {
        final Class<? extends Item> type = item.getClass();
        final View resultView;

        if (type == Item.class) {
            throw new RuntimeException("Illegal itemType. Use Object extends Item");

        } else if (type == TextItem.class) {
            resultView = generateTextItemView((TextItem) item, parent);

        } else if (type == CheckboxItem.class) {
            resultView = generateCheckboxItemView((CheckboxItem) item, parent);

        } else if (type == DebugTickCounterItem.class) {
            resultView = generateDebugTickCounterItemView((DebugTickCounterItem) item, parent);

        } else if (type == DayRepeatableCheckboxItem.class) {
            resultView = generateDayRepeatableCheckboxItemView((DayRepeatableCheckboxItem) item, parent);

        } else if (type == CycleListItem.class) {
            final CycleListItem cycleListItem = (CycleListItem) item;
            resultView = generateCycleListItemView((CycleListItem) item, parent, i -> storageEdits.onCycleListEdit(cycleListItem),
                    (linearLayout, empty) -> {
                        final CurrentItemStorageDrawer currentItemStorageDrawer = new CurrentItemStorageDrawer(this.activity, itemManager, settingsManager, cycleListItem, previewMode, itemOnClick, onItemEditor, storageEdits);
                        linearLayout.addView(currentItemStorageDrawer.getView());
                        currentItemStorageDrawer.setOnUpdateListener(currentItem -> {
                            viewVisible(empty, currentItem == null, View.GONE);
                            return Status.NONE;
                        });
                        currentItemStorageDrawer.create();
                    });

        } else if (type == CounterItem.class) {
            resultView = generateCounterItemView((CounterItem) item, parent);

        } else if (type == GroupItem.class) {
            final GroupItem groupItem = (GroupItem) item;
            resultView = generateGroupItemView(groupItem, parent, v -> storageEdits.onGroupEdit(groupItem), linearLayout -> {
                final ItemStorageDrawer itemStorageDrawer = new ItemStorageDrawer(activity, itemManager, settingsManager, groupItem, itemOnClick, onItemEditor, previewMode, storageEdits);
                itemStorageDrawer.create();
                linearLayout.addView(itemStorageDrawer.getView());
            });

        } else if (type == FilterGroupItem.class) {
            final FilterGroupItem filterGroupItem = (FilterGroupItem) item;
            resultView = generateFilterGroupItemView(filterGroupItem, parent, v -> storageEdits.onFilterGroupEdit(filterGroupItem), linearLayout -> {
                for (Item activeItem : filterGroupItem.getActiveItems()) {
                    final ItemViewHolder holder = new ItemViewHolder(activity);
                    holder.layout.addView(generate(activeItem, linearLayout));

                    if (itemManager.isSelected(activeItem)) {
                        holder.layout.setForeground(new ColorDrawable(ResUtil.getAttrColor(activity, R.attr.item_selectionForegroundColor)));
                    } else {
                        holder.layout.setForeground(null);
                    }

                    linearLayout.addView(holder.layout);
                }
            });

        } else if (type == LongTextItem.class) {
            resultView = generateLongTextItemView((LongTextItem) item, parent);

        } else {
            Log.e("Unknown item type", "Throw exception for 3 seconds...");
            DebugUtil.sleep(3000);
            throw new RuntimeException("Unknown item type '" + type.getName() + "'! check ItemViewGenerator!");
        }

        // Minimal height
        if (!item.isMinimize() && !previewMode) resultView.setMinimumHeight(item.getViewMinHeight());

        // BackgroundColor
        if (item.isViewCustomBackgroundColor()) {
            resultView.setBackgroundTintList(ColorStateList.valueOf(item.getViewBackgroundColor()));
        }

        // Minimize view patch
        if (!previewMode && item.isMinimize()) {
            if (settingsManager.isMinimizeGrayColor()) {
                resultView.setForeground(AppCompatResources.getDrawable(activity, R.drawable.shape));
                resultView.setForegroundTintList(ColorStateList.valueOf(Color.parseColor("#44f0fff0")));
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 15, 0);
            resultView.setLayoutParams(layoutParams);
        }
        if (itemOnClick != null) viewClick(resultView, () -> itemOnClick.run(item));
        return resultView;
    }

    @ForItem(key = LongTextItem.class)
    public View generateLongTextItemView(final LongTextItem item, final  ViewGroup parent) {
        final ItemLongtextBinding binding = ItemLongtextBinding.inflate(this.layoutInflater, parent, false);

        // Text
        applyTextItemToTextView(item, binding.title);
        applyLongTextItemToLongTextView(item, binding.longText);

        return binding.getRoot();
    }

    @ForItem(key = DebugTickCounterItem.class)
    private View generateDebugTickCounterItemView(final DebugTickCounterItem item, final ViewGroup parent) {
        final ItemCounterBinding binding = ItemCounterBinding.inflate(this.layoutInflater, parent, false);

        // Title
        applyTextItemToTextView(item, binding.title);

        // Counter
        viewVisible(binding.up, false, View.GONE);
        viewVisible(binding.down, false, View.GONE);
        binding.counter.setText(String.valueOf(item.getCounter()));

        return binding.getRoot();
    }

    @ForItem(key = FilterGroupItem.class)
    private View generateFilterGroupItemView(final FilterGroupItem item, final ViewGroup parent, View.OnClickListener editButtonClick, ContentInterface contentInterface) {
        final ItemFilterGroupBinding binding = ItemFilterGroupBinding.inflate(this.layoutInflater, parent, false);

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
    private View generateGroupItemView(GroupItem item, ViewGroup parent, View.OnClickListener editButtonClick, ContentInterface contentInterface) {
        final ItemGroupBinding binding = ItemGroupBinding.inflate(this.layoutInflater, parent, false);

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
    public View generateCounterItemView(CounterItem item, ViewGroup parent) {
        final ItemCounterBinding binding = ItemCounterBinding.inflate(this.layoutInflater, parent, false);

        // Title
        applyTextItemToTextView(item, binding.title);

        // Counter
        viewClick(binding.up, item::up);
        viewClick(binding.down, item::down);
        binding.up.setEnabled(!previewMode);
        binding.down.setEnabled(!previewMode);

        binding.counter.setText(String.valueOf(item.getCounter()));

        return binding.getRoot();
    }

    @ForItem(key = CycleListItem.class)
    public View generateCycleListItemView(CycleListItem item, ViewGroup parent, View.OnClickListener editButtonClick, ContentInterfaceE contentInterface) {
        final ItemCycleListBinding binding = ItemCycleListBinding.inflate(this.layoutInflater, parent, false);

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
    public View generateDayRepeatableCheckboxItemView(DayRepeatableCheckboxItem item, ViewGroup parent) {
        final ItemDayRepeatableCheckboxBinding binding = ItemDayRepeatableCheckboxBinding.inflate(this.layoutInflater, parent, false);

        applyTextItemToTextView(item, binding.text);
        applyCheckItemToCheckBoxView(item, binding.checkbox);

        return binding.getRoot();
    }

    @ForItem(key = CheckboxItem.class)
    public View generateCheckboxItemView(CheckboxItem item, ViewGroup parent) {
        final ItemCheckboxBinding binding = ItemCheckboxBinding.inflate(this.layoutInflater, parent, false);

        applyTextItemToTextView(item, binding.text);
        applyCheckItemToCheckBoxView(item, binding.checkbox);

        return binding.getRoot();
    }

    @ForItem(key = TextItem.class)
    public View generateTextItemView(TextItem item, ViewGroup parent) {
        final ItemTextBinding binding = ItemTextBinding.inflate(this.layoutInflater, parent, false);

        // Text
        applyTextItemToTextView(item, binding.title);

        return binding.getRoot();
    }

    //
    private void applyTextItemToTextView(final TextItem item, final TextView view) {
        final int MAX = 60;
        if (!previewMode && item.isMinimize()) {
            final String text = item.getText().split("\n")[0];
            if (text.length() > MAX) {
                view.setText(text.substring(0, MAX-3).concat("..."));
            } else {
                view.setText(text);
            }
        } else {
            view.setText(item.getText());
        }
        if (item.isCustomTextColor()) {
            view.setTextColor(ColorStateList.valueOf(item.getTextColor()));
        }
        if (item.isClickableUrls()) Linkify.addLinks(view, Linkify.ALL);
    }

    private void applyLongTextItemToLongTextView(final LongTextItem item, final TextView view) {
        final int MAX = 150;
        if (!previewMode && item.isMinimize()) {
            final String text = item.getLongText();
            if (text.length() > MAX) {
                view.setText(text.substring(0, MAX-3).concat("..."));
            } else {
                view.setText(text);
            }
        } else {
            view.setText(item.getLongText());
        }
        if (item.isCustomLongTextSize()) view.setTextSize(item.getLongTextSize());
        if (item.isCustomLongTextColor()) {
            view.setTextColor(ColorStateList.valueOf(item.getLongTextColor()));
        }
        if (item.isLongTextClickableUrls()) Linkify.addLinks(view, Linkify.ALL);
    }

    private void applyCheckItemToCheckBoxView(final CheckboxItem item, final CheckBox view) {
        view.setChecked(item.isChecked());
        view.setEnabled(!previewMode);
        viewClick(view, () -> {
            item.setChecked(view.isChecked());
            item.visibleChanged();
            item.save();
        });
    }

    private interface ContentInterfaceE {
        void run(final LinearLayout linearLayout, final View empty);
    }

    public static class CreateBuilder {
        private final Activity activity;
        private final ItemManager itemManager;
        private final SettingsManager settingsManager;
        private boolean previewMode = false;
        private ItemInterface onItemClick = null;
        private ItemInterface onItemOpenEditor = null;
        private StorageEditsActions storageEditsAction = null;

        public CreateBuilder(final Activity activity, final ItemManager itemManager, final SettingsManager settingsManager) {
            this.activity = activity;
            this.itemManager = itemManager;
            this.settingsManager = settingsManager;
        }

        public CreateBuilder setPreviewMode() {
            this.previewMode = true;
            return this;
        }

        public CreateBuilder setOnItemClick(ItemInterface i) {
            this.onItemClick = i;
            return this;
        }


        public CreateBuilder setOnItemOpenEditor(ItemInterface i) {
            this.onItemOpenEditor = i;
            return this;
        }


        public CreateBuilder setStorageEditsActions(StorageEditsActions i) {
            this.storageEditsAction = i;
            return this;
        }

        public ItemViewGenerator build() {
            return new ItemViewGenerator(activity, itemManager, settingsManager, previewMode, onItemClick, onItemOpenEditor, storageEditsAction);
        }
    }
}

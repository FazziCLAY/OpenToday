package ru.fazziclay.opentoday.ui.other.item;

import static ru.fazziclay.opentoday.util.InlineUtil.fcu_viewOnClick;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.CheckboxItem;
import ru.fazziclay.opentoday.app.items.CounterItem;
import ru.fazziclay.opentoday.app.items.CycleListItem;
import ru.fazziclay.opentoday.app.items.DayRepeatableCheckboxItem;
import ru.fazziclay.opentoday.app.items.GroupItem;
import ru.fazziclay.opentoday.app.items.Item;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.TextItem;
import ru.fazziclay.opentoday.databinding.ItemCheckboxBinding;
import ru.fazziclay.opentoday.databinding.ItemCounterBinding;
import ru.fazziclay.opentoday.databinding.ItemCycleListBinding;
import ru.fazziclay.opentoday.databinding.ItemDayRepeatableCheckboxBinding;
import ru.fazziclay.opentoday.databinding.ItemGroupBinding;
import ru.fazziclay.opentoday.databinding.ItemTextBinding;
import ru.fazziclay.opentoday.ui.dialog.DialogItem;
import ru.fazziclay.opentoday.util.ResUtil;

public class ItemViewGenerator {
    private final Activity activity;
    private final ItemManager itemManager;
    private final DialogItem dialogItem;

    public ItemViewGenerator(Activity activity, ItemManager itemManager) {
        this.activity = activity;
        this.itemManager = itemManager;
        this.dialogItem = new DialogItem(activity, itemManager);
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

        } else {
            throw new RuntimeException("Unknown item type '" + type.getName() + "'! check ItemViewGenerator!");
        }

        ret.setMinimumHeight(item.getViewMinHeight());
        if (item.isViewCustomBackgroundColor()) {
            ret.setBackgroundTintList(ColorStateList.valueOf(item.getViewBackgroundColor()));
        }
        fcu_viewOnClick(ret, () -> dialogItem.edit(item));
        if (item.isMinimize()) ret.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 60));

        if (itemManager.isSelected(item)) {
            ret.setForeground(new ColorDrawable(ResUtil.getAttrColor(activity, R.attr.item_selectionForegroundColor)));
        }
        return ret;
    }

    private View generateGroupItemView(Activity activity, GroupItem item, ViewGroup view) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(activity.getLayoutInflater(), view, false);

        // text
        applyTextItemToTextView(item, binding.title);

        // group
        ItemUIDrawer itemUIDrawer = new ItemUIDrawer(activity, item.getItemStorage(), App.get().getItemManager());
        itemUIDrawer.create();
        binding.content.addView(itemUIDrawer.getView());

        return binding.getRoot();
    }

    private View generateCounterItemView(Activity activity, CounterItem item, ViewGroup view) {
        ItemCounterBinding binding = ItemCounterBinding.inflate(activity.getLayoutInflater(), view, false);

        // Title
        applyTextItemToTextView(item, binding.title);

        // counter
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

        Item current = item.getCurrentItem();
        if (current != null) {
            binding.content.addView(generate(current, binding.getRoot()));
        } else {
            TextView textView = new TextView(activity);
            textView.setText(R.string.empty);
            binding.content.addView(textView);
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
        if (item.isMinimize()) {
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
        fcu_viewOnClick(view, () -> {
            item.setChecked(view.isChecked());
            item.save();
        });
    }
}

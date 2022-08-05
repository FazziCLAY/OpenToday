package ru.fazziclay.opentoday.ui.other.item;

import static ru.fazziclay.opentoday.util.InlineUtil.fcu_viewOnClick;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.items.CheckboxItem;
import ru.fazziclay.opentoday.app.items.CounterItem;
import ru.fazziclay.opentoday.app.items.CycleListItem;
import ru.fazziclay.opentoday.app.items.DayRepeatableCheckboxItem;
import ru.fazziclay.opentoday.app.items.Item;
import ru.fazziclay.opentoday.app.items.TextItem;
import ru.fazziclay.opentoday.databinding.ItemCheckboxBinding;
import ru.fazziclay.opentoday.databinding.ItemCounterBinding;
import ru.fazziclay.opentoday.databinding.ItemCycleListBinding;
import ru.fazziclay.opentoday.databinding.ItemDayRepeatableCheckboxBinding;
import ru.fazziclay.opentoday.databinding.ItemTextBinding;
import ru.fazziclay.opentoday.databinding.ItemUnknownBinding;
import ru.fazziclay.opentoday.ui.dialog.DialogItem;

public class ItemViewGenerator {
    private final Activity activity;
    private final DialogItem dialogItem;

    public ItemViewGenerator(Activity activity) {
        this.activity = activity;
        dialogItem = new DialogItem(activity);
    }

    public View generate(Item item, ViewGroup view) {
        Class<? extends Item> type = item.getClass();

        View ret;
        if (type == Item.class) {
            ret = generateItemView(activity, item, view);

        } else if (type == TextItem.class) {
            ret = generateTextItemView(activity, (TextItem) item, view);

        } else if (type == CheckboxItem.class) {
            ret = generateCheckboxItemView(activity, (CheckboxItem) item, view);

        } else if (type == DayRepeatableCheckboxItem.class) {
            ret = generateDayRepeatableChebkboxItemView(activity, (DayRepeatableCheckboxItem) item, view);

        } else if (type == CycleListItem.class) {
            ret = generateCycleListItemView(activity, (CycleListItem) item, view);

        } else if (type == CounterItem.class) {
            ret = generateCounterItemView(activity, (CounterItem) item, view);

        } else {
            throw new RuntimeException("Unknown item type '" + type.getName() + "'! check ItemViewGenerator!");
        }

        ret.setMinimumHeight(item.getViewMinHeight());
        if (item.isViewCustomBackgroundColor()) {
            ret.setBackgroundTintList(ColorStateList.valueOf(item.getViewBackgroundColor()));
        }
        fcu_viewOnClick(ret, () -> dialogItem.edit(item));
        if (item.isMinimize()) ret.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 60));
        return ret;
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

    private View generateDayRepeatableChebkboxItemView(Activity activity, DayRepeatableCheckboxItem item, ViewGroup viewGroup) {
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
        applyTextItemToTextView(item, binding.getRoot());

        return binding.getRoot();
    }

    private View generateItemView(Activity activity, Item item, ViewGroup viewGroup) {
        ItemUnknownBinding binding = ItemUnknownBinding.inflate(activity.getLayoutInflater(), viewGroup, false);

        binding.getRoot().setText(String.format("Error: Unknown '%s'", item.getClass().getSimpleName()));

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

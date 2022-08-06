package ru.fazziclay.opentoday.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.items.CheckboxItem;
import ru.fazziclay.opentoday.app.items.CounterItem;
import ru.fazziclay.opentoday.app.items.CycleListItem;
import ru.fazziclay.opentoday.app.items.DayRepeatableCheckboxItem;
import ru.fazziclay.opentoday.app.items.GroupItem;
import ru.fazziclay.opentoday.app.items.Item;
import ru.fazziclay.opentoday.app.items.TextItem;
import ru.fazziclay.opentoday.util.SpinnerHelper;

public class DialogSelectItemType {
    private final Activity activity;
    private Dialog dialog;
    private final Spinner spinner;
    private final SpinnerHelper<Class<? extends Item>> spinnerHelper;

    private String selectButtonText;
    private OnSelected onSelected = (i) -> {};

    public DialogSelectItemType(Activity activity) {
        this(activity, null);
    }

    public DialogSelectItemType(Activity activity, int resId) {
        this(activity, activity.getString(resId));
    }

    public DialogSelectItemType(Activity activity, String selectButtonText) {
        this.activity = activity;
        this.spinnerHelper = new SpinnerHelper<Class<? extends Item>>(
                new String[]{
                        activity.getString(R.string.item_text),
                        activity.getString(R.string.item_checkbox),
                        activity.getString(R.string.item_checkboxDayRepeatable),
                        activity.getString(R.string.item_cycleList),
                        activity.getString(R.string.item_counter),
                        activity.getString(R.string.item_group)
                },
                new Class[]{
                        TextItem.class,
                        CheckboxItem.class,
                        DayRepeatableCheckboxItem.class,
                        CycleListItem.class,
                        CounterItem.class,
                        GroupItem.class
                }
        );


        spinner = new Spinner(activity);
        spinner.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_expandable_list_item_1, spinnerHelper.getNames()));

        this.selectButtonText = selectButtonText;

        if (this.selectButtonText == null) {
            this.selectButtonText = activity.getString(R.string.selectItemTypeDialog_select);
        }
    }

    public void show() {
        new AlertDialog.Builder(activity)
                .setView(spinner)
                .setNegativeButton(activity.getString(R.string.selectItemTypeDialog_cancel), null)
                .setPositiveButton(this.selectButtonText, (i2, i1) -> {
                    Class<? extends Item> itemType = spinnerHelper.getValues()[spinner.getSelectedItemPosition()];
                    onSelected.onSelected(itemType);
                })
                .show();
    }

    public DialogSelectItemType setOnSelected(OnSelected onSelected) {
        this.onSelected = onSelected;
        return this;
    }

    @FunctionalInterface
    public interface OnSelected {
        void onSelected(Class<? extends Item> type);
    }
}

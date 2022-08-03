package ru.fazziclay.opentoday.ui.dialog;


import static ru.fazziclay.opentoday.util.InlineUtil.fcu_viewOnClick;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.rarepebble.colorpicker.ColorPickerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.items.CheckboxItem;
import ru.fazziclay.opentoday.app.items.CounterItem;
import ru.fazziclay.opentoday.app.items.CycleListItem;
import ru.fazziclay.opentoday.app.items.DayRepeatableCheckboxItem;
import ru.fazziclay.opentoday.app.items.Item;
import ru.fazziclay.opentoday.app.items.TextItem;
import ru.fazziclay.opentoday.databinding.DialogItemFrameBinding;
import ru.fazziclay.opentoday.databinding.DialogItemModuleCheckboxBinding;
import ru.fazziclay.opentoday.databinding.DialogItemModuleCounterBinding;
import ru.fazziclay.opentoday.databinding.DialogItemModuleCyclelistBinding;
import ru.fazziclay.opentoday.databinding.DialogItemModuleDayrepeatablecheckboxBinding;
import ru.fazziclay.opentoday.databinding.DialogItemModuleItemBinding;
import ru.fazziclay.opentoday.databinding.DialogItemModuleTextBinding;
import ru.fazziclay.opentoday.ui.activity.MainActivity;
import ru.fazziclay.opentoday.ui.other.item.ItemUIDrawer;
import ru.fazziclay.opentoday.util.MinTextWatcher;
import ru.fazziclay.opentoday.util.SpinnerHelper;

public class DialogItem {
    private final Activity activity;

    // By session
    private Dialog dialog;
    private View view;
    private Item item;
    private boolean create;
    private OnEditDone onEditDone;
    private boolean canceled = false;
    private boolean unsavedChanges = false;

    // Edit
    private final List<BaseEditUiModule> editModules = new ArrayList<>();

    public DialogItem(Activity activity) {
        this.activity = activity;
    }

    public void create(Class<? extends Item> type, OnEditDone onEditDone) {
        Item item;
        if (type == TextItem.class) {
            item = new TextItem("");
        } else if (type == CheckboxItem.class) {
            item = new CheckboxItem("", false);
        } else if (type == DayRepeatableCheckboxItem.class) {
            item = new DayRepeatableCheckboxItem("", false, false, 0);
        } else if (type == CycleListItem.class) {
            item = new CycleListItem("");
        } else if (type == CounterItem.class) {
            item = new CounterItem("");
        } else {
            throw new RuntimeException("Illegal item type! (check DialogItem)");
        }
        show(item, true, onEditDone);
    }

    public void edit(Item item) {
        show(item, false, null);
    }

    private void show(Item item, boolean create, OnEditDone onEditDone) {
        cancel();
        this.item = item;
        this.create = create;
        this.onEditDone = onEditDone;
        this.canceled = false;
        this.unsavedChanges = false;
        editModules.clear();

        this.view = generateView();
        this.dialog = generateDialog();
        this.dialog.show();
    }

    private View generateView() {
        DialogItemFrameBinding binding = DialogItemFrameBinding.inflate(this.activity.getLayoutInflater());

        if (item instanceof Item) {
            binding.canvas.addView(addEditModule(new ItemEditModule()));
        }
        if (item instanceof TextItem) {
            binding.canvas.addView(addEditModule(new TextItemEditModule()));
        }
        if (item instanceof CheckboxItem) {
            binding.canvas.addView(addEditModule(new CheckboxItemEditModule()));
        }
        if (item instanceof DayRepeatableCheckboxItem) {
            binding.canvas.addView(addEditModule(new DayRepeatableCheckboxItemEditModule()));
        }
        if (item instanceof CycleListItem) {
            binding.canvas.addView(addEditModule(new CycleListItemEditModule()));
        }
        if (item instanceof CounterItem) {
            binding.canvas.addView(addEditModule(new CounterItemEditModule()));
        }

        fcu_viewOnClick(binding.applyButton, this::applyRequest);
        fcu_viewOnClick(binding.cancelButton, this::cancelRequest);
        fcu_viewOnClick(binding.deleteButton, this::deleteRequest);
        if (create) binding.deleteButton.setVisibility(View.GONE);

        return binding.getRoot();
    }

    private View addEditModule(BaseEditUiModule editUiModule) {
        editUiModule.setup(this.item, this.activity, this.view);
        editUiModule.setOnStartEditListener(() -> {
            new Exception().printStackTrace();
            unsavedChanges = true;
        });
        if (create) editUiModule.notifyCreateMode();
        editModules.add(editUiModule);

        View view = editUiModule.getView();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 5, 0, 5);
        view.setLayoutParams(layoutParams);

        return view;
    }

    private void applyRequest() {
        for (BaseEditUiModule editModule : editModules) {
            try {
                editModule.commit(item);
            } catch (Exception e) {
                if (e instanceof UserException) {
                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("DialogItem", "apply exception", e);
                    Toast.makeText(activity, "Error: " + e, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        item.updateUi();
        item.save();

        if (onEditDone != null) onEditDone.run(item);
        cancel();
    }

    private void cancelRequest() {
        if (!unsavedChanges) {
            cancel();
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle(R.string.dialogItem_cancel_unsaved_title)
                .setNegativeButton(R.string.dialogItem_cancel_unsaved_contunue, null)
                .setPositiveButton(R.string.dialogItem_cancel_unsaved_discard, ((dialog1, which) -> cancel()))
                .show();
    }


    private void deleteRequest() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.dialogItem_delete_title)
                .setNegativeButton(R.string.dialogItem_delete_cancel, null)
                .setPositiveButton(R.string.dialogItem_delete_apply, ((dialog1, which) -> {
                    item.delete();
                    cancel();
                }))
                .show();
    }


    private Dialog generateDialog() {
        Dialog dialog = new Dialog(this.activity, android.R.style.ThemeOverlay_Material);
        dialog.setContentView(this.view);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(dialog1 -> {
            if (!canceled && unsavedChanges) {
                dialog.show();
                cancelRequest();
            }
        });
        return dialog;
    }

    public void cancel() {
        canceled = true;
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.cancel();
            }
        }
    }

    /*

    ItemUIDrawer itemUIDrawer = new ItemUIDrawer(activity, ((CycleListItem)item).getItemsCycleStorage());
        itemUIDrawer.create();
        Dialog dialog = new Dialog(activity, android.R.style.ThemeOverlay_Material);
        dialog.setContentView(itemUIDrawer.getView());
        dialog.show();
        dialog.setOnCancelListener(dialog1 -> {
            itemUIDrawer.destroy();
        });
    */

    @FunctionalInterface
    public interface OnEditDone {
        void run(Item item);
    }

    public static class UserException extends Exception {
        public UserException(String m) {
            super(m);
        }
    }

    public abstract static class BaseEditUiModule {
        public abstract View getView();
        public abstract void setup(Item item, Activity activity, View view);
        public abstract void commit(Item item) throws Exception;
        public abstract void setOnStartEditListener(Runnable o);
        public void notifyCreateMode() {}
    }

    public static class ItemEditModule extends BaseEditUiModule {
        private DialogItemModuleItemBinding binding;
        private Runnable onEditStart;

        private int temp_backgroundColor;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            binding = DialogItemModuleItemBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);

            // equip
            binding.viewMinHeight.setText(String.valueOf(item.getViewMinHeight()));
            binding.defaultBackgroundColor.setChecked(!item.isViewCustomBackgroundColor());
            temp_backgroundColor = item.getViewBackgroundColor();
            binding.viewBackgroundColorIndicator.setBackgroundTintList(ColorStateList.valueOf(temp_backgroundColor));
            binding.viewBackgroundColorEdit.setOnClickListener(v -> {
                ColorPickerView cp = new ColorPickerView(activity);
                cp.setCurrentColor(temp_backgroundColor);
                cp.showHex(true); cp.showPreview(true); cp.showAlpha(true);
                cp.setOriginalColor(temp_backgroundColor); cp.setCurrentColor(temp_backgroundColor);

                new AlertDialog.Builder(activity)
                        .setTitle(R.string.dialogItem_module_item_backgroundColor_dialog_title)
                        .setView(cp)
                        .setNegativeButton(R.string.dialogItem_module_item_backgroundColor_dialog_cancel, null)
                        .setPositiveButton(R.string.dialogItem_module_item_backgroundColor_dialog_apply, ((dialog1, which) -> {
                            temp_backgroundColor = cp.getColor();
                            binding.viewBackgroundColorIndicator.setBackgroundTintList(ColorStateList.valueOf(temp_backgroundColor));
                            onEditStart.run();
                        }))
                        .show();
            });
            binding.minimize.setChecked(item.isMinimize());

            // On edit start
            binding.viewMinHeight.addTextChangedListener(new MinTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    onEditStart.run();
                }
            });
            binding.defaultBackgroundColor.setOnClickListener(v -> onEditStart.run());
            binding.minimize.setOnClickListener(v -> onEditStart.run());
            //
        }

        @Override
        public void commit(Item item) {
            if (binding.viewMinHeight.getText().toString().trim().isEmpty()) {
                binding.viewMinHeight.setText("0");
            }

            item.setViewMinHeight(Integer.parseInt(binding.viewMinHeight.getText().toString()));
            item.setViewBackgroundColor(temp_backgroundColor);
            item.setViewCustomBackgroundColor(!binding.defaultBackgroundColor.isChecked());
            item.setMinimize(binding.minimize.isChecked());
        }

        @Override
        public void setOnStartEditListener(Runnable o) {
            onEditStart = o;
        }
    }

    public static class TextItemEditModule extends BaseEditUiModule {
        private DialogItemModuleTextBinding binding;
        private Runnable onEditStart;

        private int temp_textColor;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            TextItem textItem = (TextItem) item;
            binding = DialogItemModuleTextBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);

            // equip
            binding.text.setText(textItem.getText());
            binding.defaultTextColor.setChecked(!textItem.isCustomTextColor());
            temp_textColor = textItem.getTextColor();
            binding.textColorIndicator.setBackgroundTintList(ColorStateList.valueOf(temp_textColor));
            binding.textColorEdit.setOnClickListener(v -> {
                ColorPickerView cp = new ColorPickerView(activity);
                cp.setCurrentColor(temp_textColor);
                cp.showHex(true); cp.showPreview(true); cp.showAlpha(true);
                cp.setOriginalColor(temp_textColor); cp.setCurrentColor(temp_textColor);

                new AlertDialog.Builder(activity)
                        .setTitle(R.string.dialogItem_module_text_textColor_dialog_title)
                        .setView(cp)
                        .setNegativeButton(R.string.dialogItem_module_text_textColor_dialog_cancel, null)
                        .setPositiveButton(R.string.dialogItem_module_text_textColor_dialog_apply, ((dialog1, which) -> {
                            temp_textColor = cp.getColor();
                            binding.textColorIndicator.setBackgroundTintList(ColorStateList.valueOf(temp_textColor));
                            onEditStart.run();
                        }))
                        .show();
            });

            binding.clickableUrls.setChecked(textItem.isClickableUrls());

            // On edit start
            binding.text.addTextChangedListener(new MinTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    onEditStart.run();
                }
            });
            binding.defaultTextColor.setOnClickListener(v -> onEditStart.run());
            binding.clickableUrls.setOnClickListener(v -> onEditStart.run());
            //
        }

        @Override
        public void commit(Item item) {
            TextItem textItem = (TextItem) item;

            textItem.setText(binding.text.getText().toString());
            textItem.setTextColor(temp_textColor);
            textItem.setCustomTextColor(!binding.defaultTextColor.isChecked());
            textItem.setClickableUrls(binding.clickableUrls.isChecked());
        }

        @Override
        public void setOnStartEditListener(Runnable o) {
            onEditStart = o;
        }
    }

    public static class CheckboxItemEditModule extends BaseEditUiModule {
        private DialogItemModuleCheckboxBinding binding;
        private Runnable onEditStart;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            CheckboxItem checkboxItem = (CheckboxItem) item;
            this.binding = DialogItemModuleCheckboxBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);
            this.binding.isChecked.setChecked(checkboxItem.isChecked());
            this.binding.isChecked.setOnClickListener(v -> onEditStart.run());
        }

        @Override
        public void commit(Item item) {
            CheckboxItem checkboxItem = (CheckboxItem) item;
            checkboxItem.setChecked(binding.isChecked.isChecked());
        }

        @Override
        public void setOnStartEditListener(Runnable o) {
            onEditStart = o;
        }
    }

    public static class DayRepeatableCheckboxItemEditModule extends BaseEditUiModule {
        private DialogItemModuleDayrepeatablecheckboxBinding binding;
        private Runnable onEditStart;
        private boolean create = false;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void setup(Item item, Activity activity, View view) {
            DayRepeatableCheckboxItem dayRepeatableCheckboxItem = (DayRepeatableCheckboxItem) item;
            binding = DialogItemModuleDayrepeatablecheckboxBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);
            binding.startValue.setChecked(dayRepeatableCheckboxItem.getStartValue());
            binding.startValue.setOnClickListener(v -> onEditStart.run());

            String date = "-";
            if (!create) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd EEEE", Locale.getDefault());
                GregorianCalendar g = new GregorianCalendar();
                g.set(Calendar.DAY_OF_YEAR, dayRepeatableCheckboxItem.getLatestDayOfYear());
                date = dateFormat.format(g.getTime());
            }
            binding.latestReset.setText(activity.getString(R.string.dayRepeatable_latestRegenerate, date));
        }

        @Override
        public void commit(Item item) {
            DayRepeatableCheckboxItem dayRepeatableCheckboxItem = (DayRepeatableCheckboxItem) item;
            dayRepeatableCheckboxItem.setStartValue(binding.startValue.isChecked());
        }

        @Override
        public void setOnStartEditListener(Runnable o) {
            onEditStart = o;
        }

        @Override
        public void notifyCreateMode() {
            super.notifyCreateMode();
            create = true;
        }
    }

    public static class CycleListItemEditModule extends BaseEditUiModule {
        private DialogItemModuleCyclelistBinding binding;
        private ItemUIDrawer itemUIDrawer;
        private SpinnerHelper<CycleListItem.CycleItemsBackgroundWork> itemsCycleBackgroundWorkSpinnerHelp;
        private Runnable onEditStart;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            CycleListItem cycleListItem = (CycleListItem) item;

            binding = DialogItemModuleCyclelistBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);
            itemUIDrawer = new ItemUIDrawer(activity, cycleListItem.getItemsCycleStorage());

            binding.canvas.addView(itemUIDrawer.getView());
            itemUIDrawer.create();

            itemsCycleBackgroundWorkSpinnerHelp = new SpinnerHelper<>(
                    new String[] {
                            activity.getString(R.string.cycleListItem_tick_all),
                            activity.getString(R.string.cycleListItem_tick_current)
                    },
                    new CycleListItem.CycleItemsBackgroundWork[] {
                            CycleListItem.CycleItemsBackgroundWork.ALL,
                            CycleListItem.CycleItemsBackgroundWork.CURRENT
                    }
            );

            binding.itemsCycleBackgroundWork.setOnClickListener(v -> onEditStart.run());
            binding.itemsCycleBackgroundWork.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_expandable_list_item_1, itemsCycleBackgroundWorkSpinnerHelp.getNames()));
            binding.itemsCycleBackgroundWork.setSelection(itemsCycleBackgroundWorkSpinnerHelp.getPosition(cycleListItem.getItemsCycleBackgroundWork()));
            binding.addNew.setOnClickListener(v -> MainActivity.showAddNewDialog(activity, cycleListItem.getItemsCycleStorage()));
        }

        @Override
        public void commit(Item item) {
            CycleListItem cycleListItem = (CycleListItem) item;
            cycleListItem.setItemsCycleBackgroundWork(itemsCycleBackgroundWorkSpinnerHelp.getValue(binding.itemsCycleBackgroundWork.getSelectedItemPosition()));
        }

        @Override
        public void setOnStartEditListener(Runnable o) {
            onEditStart = o;
        }
    }

    private static class CounterItemEditModule extends BaseEditUiModule {
        private DialogItemModuleCounterBinding binding;
        private Runnable onEditStart;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            CounterItem counterItem = (CounterItem) item;

            binding = DialogItemModuleCounterBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);
            binding.counterValue.setText(String.valueOf(counterItem.getCounter()));
            binding.counterStep.setText(String.valueOf(counterItem.getStep()));

            binding.counterValue.addTextChangedListener(new MinTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    onEditStart.run();
                }
            });
            binding.counterStep.addTextChangedListener(new MinTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    onEditStart.run();
                }
            });
        }

        @Override
        public void commit(Item item) {
            CounterItem counterItem = (CounterItem) item;
            counterItem.setCounter(Double.parseDouble(binding.counterValue.getText().toString()));
            counterItem.setStep(Double.parseDouble(binding.counterStep.getText().toString()));
        }

        @Override
        public void setOnStartEditListener(Runnable o) {
            onEditStart = o;
        }
    }
}

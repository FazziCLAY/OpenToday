package ru.fazziclay.opentoday.ui.dialog;


import static ru.fazziclay.opentoday.util.InlineUtil.fcu_viewOnClick;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;

import com.rarepebble.colorpicker.ColorPickerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.AbsoluteItemContainer;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.ItemsRegistry;
import ru.fazziclay.opentoday.app.items.item.CheckboxItem;
import ru.fazziclay.opentoday.app.items.item.CounterItem;
import ru.fazziclay.opentoday.app.items.item.CycleListItem;
import ru.fazziclay.opentoday.app.items.item.DayRepeatableCheckboxItem;
import ru.fazziclay.opentoday.app.items.item.GroupItem;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.item.TextItem;
import ru.fazziclay.opentoday.databinding.DialogItemFrameBinding;
import ru.fazziclay.opentoday.databinding.DialogItemModuleCheckboxBinding;
import ru.fazziclay.opentoday.databinding.DialogItemModuleCounterBinding;
import ru.fazziclay.opentoday.databinding.DialogItemModuleCyclelistBinding;
import ru.fazziclay.opentoday.databinding.DialogItemModuleDayrepeatablecheckboxBinding;
import ru.fazziclay.opentoday.databinding.DialogItemModuleGroupBinding;
import ru.fazziclay.opentoday.databinding.DialogItemModuleItemBinding;
import ru.fazziclay.opentoday.databinding.DialogItemModuleTextBinding;
import ru.fazziclay.opentoday.ui.other.item.ItemStorageDrawer;
import ru.fazziclay.opentoday.util.MinTextWatcher;
import ru.fazziclay.opentoday.util.ResUtil;
import ru.fazziclay.opentoday.util.SpinnerHelper;

public class DialogItem {
    private final Activity activity;
    private final ItemManager itemManager;
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

    public DialogItem(Activity activity, ItemManager itemManager) {
        this.activity = activity;
        this.itemManager = itemManager;
    }

    public void create(Class<? extends Item> type, OnEditDone onEditDone) {
        Item item = ItemsRegistry.REGISTRY.getItemInfoByClass(type).create();
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
        if (item instanceof GroupItem) {
            binding.canvas.addView(addEditModule(new GroupItemEditModule()));
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
            updateTextColorIndicator(activity);
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
                            binding.defaultBackgroundColor.setChecked(false);
                            updateTextColorIndicator(activity);

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
            binding.defaultBackgroundColor.setOnClickListener(v -> {
                updateTextColorIndicator(activity);
                onEditStart.run();
            });
            binding.minimize.setOnClickListener(v -> onEditStart.run());
            //
        }

        private void updateTextColorIndicator(Activity activity) {
            if (binding.defaultBackgroundColor.isChecked()) {
                binding.viewBackgroundColorIndicator.setBackgroundTintList(ColorStateList.valueOf(ResUtil.getAttrColor(activity, R.attr.item_backgroundColor)));
            } else {
                binding.viewBackgroundColorIndicator.setBackgroundTintList(ColorStateList.valueOf(temp_backgroundColor));
            }
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
            updateTextColorIndicator(activity);
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
                            binding.defaultTextColor.setChecked(false);
                            updateTextColorIndicator(activity);
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
            binding.defaultTextColor.setOnClickListener(v -> {
                updateTextColorIndicator(activity);
                onEditStart.run();
            });
            binding.clickableUrls.setOnClickListener(v -> onEditStart.run());
            //
        }

        private void updateTextColorIndicator(Activity activity) {
            if (binding.defaultTextColor.isChecked()) {
                binding.textColorIndicator.setBackgroundTintList(ColorStateList.valueOf(ResUtil.getAttrColor(activity, R.attr.item_textColor)));
            } else {
                binding.textColorIndicator.setBackgroundTintList(ColorStateList.valueOf(temp_textColor));
            }
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
        private ItemStorageDrawer itemStorageDrawer;
        private SpinnerHelper<CycleListItem.TickBehavior> itemsCycleBackgroundWorkSpinnerHelp;
        private Runnable onEditStart;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            ItemManager itemManager = App.get().getItemManager();
            CycleListItem cycleListItem = (CycleListItem) item;

            binding = DialogItemModuleCyclelistBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);
            itemStorageDrawer = new ItemStorageDrawer(activity, itemManager, cycleListItem.getItemsCycleStorage());

            binding.canvas.addView(itemStorageDrawer.getView());
            itemStorageDrawer.create();

            itemsCycleBackgroundWorkSpinnerHelp = new SpinnerHelper<>(
                    new String[] {
                            activity.getString(R.string.cycleListItem_tick_all),
                            activity.getString(R.string.cycleListItem_tick_current)
                    },
                    new CycleListItem.TickBehavior[] {
                            CycleListItem.TickBehavior.ALL,
                            CycleListItem.TickBehavior.CURRENT
                    }
            );

            binding.itemsCycleBackgroundWork.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_expandable_list_item_1, itemsCycleBackgroundWorkSpinnerHelp.getNames()));
            binding.itemsCycleBackgroundWork.setSelection(itemsCycleBackgroundWorkSpinnerHelp.getPosition(cycleListItem.getTickBehavior()));
            binding.addNew.setOnClickListener(v -> new DialogSelectItemType(activity, R.string.selectItemTypeDialog_create)
                    .setOnSelected((itemType) -> {
                        DialogItem dialogItem = new DialogItem(activity, itemManager);
                        dialogItem.create(itemType, cycleListItem.getItemsCycleStorage()::addItem);
                    })
                    .show());
            final boolean[] first = {true};
            binding.itemsCycleBackgroundWork.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!first[0]) {
                        onEditStart.run();
                    }
                    first[0] = false;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        @Override
        public void commit(Item item) {
            CycleListItem cycleListItem = (CycleListItem) item;
            cycleListItem.setTickBehavior(itemsCycleBackgroundWorkSpinnerHelp.getValue(binding.itemsCycleBackgroundWork.getSelectedItemPosition()));
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

    private static class GroupItemEditModule extends BaseEditUiModule {
        private DialogItemModuleGroupBinding binding;
        private Runnable onEditStart;
        private ItemStorageDrawer itemStorageDrawer;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            ItemManager itemManager = App.get().getItemManager();
            GroupItem groupItem = (GroupItem) item;
            binding = DialogItemModuleGroupBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);
            binding.addNew.setOnClickListener(v -> new DialogSelectItemType(activity, R.string.selectItemTypeDialog_create)
                    .setOnSelected(type -> groupItem.getItemStorage().addItem(ItemsRegistry.REGISTRY.getItemInfoByClass(type).create()))
                    .show());


            itemStorageDrawer = new ItemStorageDrawer(activity, itemManager, groupItem.getItemStorage());

            binding.canvas.addView(itemStorageDrawer.getView());
            itemStorageDrawer.create();

            if (itemManager.getSelection() == null) {
                binding.moveSelected.setForeground(AppCompatResources.getDrawable(activity, R.drawable.shape));
                binding.moveSelected.setForegroundTintList(ColorStateList.valueOf(Color.parseColor("#bb444444")));
            }
            binding.moveSelected.setOnClickListener(v -> {
                if (itemManager.getSelection() == null) {
                    Toast.makeText(activity, R.string.nothing_selected, Toast.LENGTH_SHORT).show();
                    return;
                }
                AbsoluteItemContainer selection = itemManager.getSelection();
                selection.getItemStorage().deleteItem(selection.getItem());
                groupItem.getItemStorage().addItem(selection.getItem());
                itemManager.deselect();
            });
        }

        @Override
        public void commit(Item item) {
            GroupItem groupItem = (GroupItem) item;

        }

        @Override
        public void setOnStartEditListener(Runnable o) {
            onEditStart = o;
        }
    }
}

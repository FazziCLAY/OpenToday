package com.fazziclay.opentoday.gui.fragment;


import static com.fazziclay.opentoday.util.InlineUtil.viewClick;
import static com.fazziclay.opentoday.util.InlineUtil.viewLong;
import static com.fazziclay.opentoday.util.InlineUtil.viewVisible;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.ColorHistoryManager;
import com.fazziclay.opentoday.app.ImportWrapper;
import com.fazziclay.opentoday.app.items.ItemManager;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.selection.SelectionManager;
import com.fazziclay.opentoday.app.items.item.CheckboxItem;
import com.fazziclay.opentoday.app.items.item.CounterItem;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.DayRepeatableCheckboxItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.item.LongTextItem;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.items.notification.DayItemNotification;
import com.fazziclay.opentoday.app.items.notification.ItemNotification;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.app.SettingsManager;
import com.fazziclay.opentoday.databinding.FragmentItemEditorBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleCheckboxBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleCounterBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleCyclelistBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleDayrepeatablecheckboxBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleItemBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleLongtextBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleTextBinding;
import com.fazziclay.opentoday.gui.EnumsRegistry;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.dialog.DialogItemNotificationsEditor;
import com.fazziclay.opentoday.gui.interfaces.BackStackMember;
import com.fazziclay.opentoday.util.MinTextWatcher;
import com.fazziclay.opentoday.util.ResUtil;
import com.fazziclay.opentoday.util.SimpleSpinnerAdapter;
import com.fazziclay.opentoday.util.time.ConvertMode;
import com.fazziclay.opentoday.util.time.TimeUtil;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.rarepebble.colorpicker.ColorPickerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ItemEditorFragment extends Fragment implements BackStackMember {
    private static final int MODE_UNKNOWN = 0x00;
    private static final int MODE_CREATE = 0x02;
    private static final int MODE_EDIT = 0x04;
    private static final String KEY_MODE = "mode";
    private static final String KEY_EDIT_TAB_ID = "edit:tabId";
    private static final String KEY_EDIT_ITEM_ID = "edit:itemId";
    private static final String KEY_CREATE_ITEM_STORAGE_ID = "create:itemStorageId";
    private static final String KEY_CREATE_ITEM_TYPE = "create:itemType";
    public static ItemEditorFragment create(UUID itemStorageId, Class<? extends Item> itemType) {
        ItemEditorFragment result = new ItemEditorFragment();
        Bundle a = new Bundle();

        a.putInt(KEY_MODE, MODE_CREATE);
        a.putString(KEY_CREATE_ITEM_STORAGE_ID, itemStorageId.toString());
        a.putString(KEY_CREATE_ITEM_TYPE, ItemsRegistry.REGISTRY.get(itemType).getStringType());

        result.setArguments(a);
        return result;
    }

    public static ItemEditorFragment edit(UUID tabId, UUID itemId) {
        ItemEditorFragment result = new ItemEditorFragment();
        Bundle a = new Bundle();

        a.putInt(KEY_MODE, MODE_EDIT);
        a.putString(KEY_EDIT_TAB_ID, tabId.toString());
        a.putString(KEY_EDIT_ITEM_ID, itemId.toString());

        result.setArguments(a);
        return result;
    }

    public static ItemEditorFragment edit(UUID itemId) {
        ItemEditorFragment result = new ItemEditorFragment();
        Bundle a = new Bundle();

        a.putInt(KEY_MODE, MODE_EDIT);
        a.putString(KEY_EDIT_ITEM_ID, itemId.toString());

        result.setArguments(a);
        return result;
    }


    private App app;
    private ItemManager itemManager;
    private SettingsManager settingsManager;
    private ColorHistoryManager colorHistoryManager;
    private SelectionManager selectionManager;
    private boolean unsavedChanges = false;
    private Item item;

    private int mode;
    
    // Edit
    // Create
    private ItemsStorage itemsStorage; // for create

    // Internal
    private final List<BaseEditUiModule> editModules = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null) {
            throw new NullPointerException("Arguments is null");
        }

        app = App.get(requireContext());
        itemManager = app.getItemManager();
        settingsManager = app.getSettingsManager();
        colorHistoryManager = app.getColorHistoryManager();
        selectionManager = app.getSelectionManager();
        mode = getArguments().getInt("mode", MODE_UNKNOWN);
        
        if (mode == MODE_EDIT) {
            if (getArguments().containsKey(KEY_EDIT_TAB_ID)) {
                Tab tab = itemManager.getTab(getArgTabId());
                item = tab.getItemById(getArgItemId());
            } else {
                item = itemManager.getItemById(getArgItemId());
            }

        } else if (mode == MODE_CREATE) {
            itemsStorage = itemManager.getItemStorageById(getArgItemStorageId());
            ItemsRegistry.ItemInfo itemInfo = ItemsRegistry.REGISTRY.get(getArgItemType());
            item = itemInfo.create();
            
        } else {
            throw new RuntimeException("Unknown mode: " + mode);
        }

        if (item == null) {
            throw new RuntimeException("Item is null!");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final FragmentItemEditorBinding binding = FragmentItemEditorBinding.inflate(inflater);

        if (item instanceof Item) {
            binding.modules.addView(addEditModule(new ItemEditModule()));
        }
        if (item instanceof TextItem) {
            binding.modules.addView(addEditModule(new TextItemEditModule()));
        }
        if (item instanceof LongTextItem) {
            binding.modules.addView(addEditModule(new LongTextItemEditModule()));
        }
        if (item instanceof CheckboxItem) {
            binding.modules.addView(addEditModule(new CheckboxItemEditModule()));
        }
        if (item instanceof DayRepeatableCheckboxItem) {
            binding.modules.addView(addEditModule(new DayRepeatableCheckboxItemEditModule()));
        }
        if (item instanceof CycleListItem) {
            binding.modules.addView(addEditModule(new CycleListItemEditModule()));
        }
        if (item instanceof CounterItem) {
            binding.modules.addView(addEditModule(new CounterItemEditModule()));
        }

        viewClick(binding.applyButton, this::applyRequest);
        viewClick(binding.cancelButton, this::cancelRequest);
        viewClick(binding.deleteButton, this::deleteRequest);
        viewVisible(binding.deleteButton, item.isAttached(), View.GONE);
        binding.itemTypeName.setText(EnumsRegistry.INSTANCE.nameResId(ItemsRegistry.REGISTRY.get(item.getClass()).getItemType()));

        return binding.getRoot();
    }

    public UUID getArgTabId() {
        return UUID.fromString(getArguments().getString(KEY_EDIT_TAB_ID));
    }

    public UUID getArgItemId() {
        return UUID.fromString(getArguments().getString(KEY_EDIT_ITEM_ID));
    }

    public UUID getArgItemStorageId() {
        return UUID.fromString(getArguments().getString(KEY_CREATE_ITEM_STORAGE_ID));
    }

    public String getArgItemType() {
        return getArguments().getString(KEY_CREATE_ITEM_TYPE);
    }

    public ItemEditorFragment() {
    }

    private View addEditModule(BaseEditUiModule editUiModule) {
        editUiModule.setup(this.item, requireActivity(), null);
        editUiModule.setOnStartEditListener(() -> unsavedChanges = true);
        editModules.add(editUiModule);

        View view = editUiModule.getView();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 10, 0, 10);
        view.setLayoutParams(layoutParams);

        return view;
    }

    private void applyRequest() {
        for (BaseEditUiModule editModule : editModules) {
            try {
                editModule.commit(item);
            } catch (Exception e) {
                if (e instanceof UserException) {
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("DialogItem", "apply exception", e);
                    Toast.makeText(requireContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        if (mode == MODE_CREATE) {
            itemsStorage.addItem(item);
        }

        item.visibleChanged();
        item.save();
        unsavedChanges = false;

        cancel();
    }

    private void cancelRequest() {
        if (!unsavedChanges) {
            cancel();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialogItem_cancel_unsaved_title)
                .setNegativeButton(R.string.dialogItem_cancel_unsaved_contunue, null)
                .setPositiveButton(R.string.dialogItem_cancel_unsaved_discard, ((dialog1, which) -> cancel()))
                .show();
    }

    private void deleteRequest() {
        deleteRequest(requireContext(), item, this::cancel);
    }

    public static void deleteRequest(Context context, Item item, Runnable onDelete) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.dialogItem_delete_title)
                .setNegativeButton(R.string.dialogItem_delete_cancel, null)
                .setPositiveButton(R.string.dialogItem_delete_apply, ((dialog1, which) -> {
                    item.delete();
                    if (onDelete != null) onDelete.run();
                }))
                .show();
    }

    private void cancel() {
        unsavedChanges = false;
        UI.rootBack(this);
    }

    @Override
    public boolean popBackStack() {
        if (unsavedChanges) {
            cancelRequest();
        }
        return unsavedChanges;
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

    public class ItemEditModule extends BaseEditUiModule {
        private FragmentItemEditorModuleItemBinding binding;
        private Runnable onEditStart;

        private int temp_backgroundColor;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            binding = FragmentItemEditorModuleItemBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);

            // equip
            binding.selected.setChecked(selectionManager.isSelected(item));
            binding.viewMinHeight.setText(String.valueOf(item.getViewMinHeight()));
            binding.defaultBackgroundColor.setChecked(!item.isViewCustomBackgroundColor());
            temp_backgroundColor = item.getViewBackgroundColor();
            updateTextColorIndicator(activity);
            viewClick(binding.viewBackgroundColorEdit, () -> {
                ColorPickerView cp = new ColorPickerView(activity);
                cp.setCurrentColor(temp_backgroundColor);
                cp.showHex(true); cp.showPreview(true); cp.showAlpha(true);
                cp.setOriginalColor(temp_backgroundColor); cp.setCurrentColor(temp_backgroundColor);

                ChipGroup history = new ChipGroup(requireContext());
                int[] colors = colorHistoryManager.getHistory(5);
                for (int color : colors) {
                    Chip chip = new Chip(requireContext());
                    chip.setChipBackgroundColor(ColorStateList.valueOf(color));
                    chip.setOnClickListener(v -> cp.setCurrentColor(color));
                    chip.setText(String.format("#%08x", color));
                    history.addView(chip);
                }
                HorizontalScrollView historyHorizontal = new HorizontalScrollView(requireContext());
                historyHorizontal.addView(history);

                LinearLayout dialogLayout = new LinearLayout(activity);
                dialogLayout.setOrientation(LinearLayout.VERTICAL);
                dialogLayout.addView(cp);
                dialogLayout.addView(historyHorizontal);

                new AlertDialog.Builder(activity)
                        .setTitle(R.string.dialogItem_module_item_backgroundColor_dialog_title)
                        .setView(dialogLayout)
                        .setNegativeButton(R.string.dialogItem_module_item_backgroundColor_dialog_cancel, null)
                        .setPositiveButton(R.string.dialogItem_module_item_backgroundColor_dialog_apply, ((dialog1, which) -> {
                            colorHistoryManager.addColor(cp.getColor());
                            temp_backgroundColor = cp.getColor();
                            binding.defaultBackgroundColor.setChecked(false);
                            updateTextColorIndicator(activity);

                            onEditStart.run();
                        }))
                        .show();
            });
            binding.minimize.setChecked(item.isMinimize());
            //viewVisible(binding.copyItemId, app.isFeatureFlag(FeatureFlag.ITEM_EDITOR_SHOW_COPY_ID_BUTTON), View.GONE);
            viewClick(binding.copyItemId, () -> {
                ClipboardManager clipboardManager = activity.getSystemService(ClipboardManager.class);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("Item id", item.getId() == null ? "null" : item.getId().toString()));
            });

            viewClick(binding.exportItem, () -> {
                ImportWrapper w = ImportWrapper.createImport(ImportWrapper.Permission.ADD_ITEMS_TO_CURRENT)
                        .addItem(item)
                        .build();
                ClipboardManager clipboardManager = activity.getSystemService(ClipboardManager.class);
                try {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("OpenToday export", w.finalExport()));
                    Toast.makeText(requireContext(), R.string.export_success, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // On edit start
            binding.viewMinHeight.addTextChangedListener(new MinTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    onEditStart.run();
                }
            });
            viewClick(binding.defaultBackgroundColor, () -> {
                updateTextColorIndicator(activity);
                onEditStart.run();
            });
            viewClick(binding.minimize, onEditStart);
            //

            viewClick(binding.editNotifications, () -> new DialogItemNotificationsEditor(activity, item, () -> updateNotificationPreview(item, activity)).show());
            updateNotificationPreview(item, activity);
        }

        private void updateNotificationPreview(Item item, Activity activity) {
            StringBuilder text = new StringBuilder();
            for (ItemNotification notification : item.getNotifications()) {
                if (notification instanceof DayItemNotification) {
                    DayItemNotification d = (DayItemNotification) notification;
                    text.append(String.format("#%s - %s - %s", d.getNotificationId(), activity.getString(R.string.itemNotification_day), TimeUtil.convertToHumanTime(d.getTime(), ConvertMode.HHMM))).append("\n");
                }
            }
            binding.notificationsPreview.setText(text.toString());
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
            if (binding.selected.isChecked()) {
                if (!selectionManager.isSelected(item)) {
                    selectionManager.selectItem(item);
                }
            } else {
                if (selectionManager.isSelected(item)) {
                    selectionManager.deselectItem(item);
                }
            }
        }

        @Override
        public void setOnStartEditListener(Runnable o) {
            onEditStart = o;
        }
    }

    public class TextItemEditModule extends BaseEditUiModule {
        private FragmentItemEditorModuleTextBinding binding;
        private Runnable onEditStart;

        private int temp_textColor;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            TextItem textItem = (TextItem) item;
            binding = FragmentItemEditorModuleTextBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);

            // equip
            viewLong(binding.titleOfText, () -> {
                ClipboardManager clipboardManager = activity.getSystemService(ClipboardManager.class);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("Item text", item.getText()));
                Toast.makeText(activity, R.string.abc_coped, Toast.LENGTH_SHORT).show();
            });
            binding.text.setText(textItem.getText());
            binding.defaultTextColor.setChecked(!textItem.isCustomTextColor());
            temp_textColor = textItem.getTextColor();
            updateTextColorIndicator(activity);
            binding.textColorEdit.setOnClickListener(v -> {
                ColorPickerView cp = new ColorPickerView(activity);
                cp.setCurrentColor(temp_textColor);
                cp.showHex(true); cp.showPreview(true); cp.showAlpha(true);
                cp.setOriginalColor(temp_textColor); cp.setCurrentColor(temp_textColor);

                ChipGroup history = new ChipGroup(requireContext());
                int[] colors = colorHistoryManager.getHistory(5);
                for (int color : colors) {
                    Chip chip = new Chip(requireContext());
                    chip.setChipBackgroundColor(ColorStateList.valueOf(color));
                    chip.setOnClickListener(vvv -> cp.setCurrentColor(color));
                    chip.setText(String.format("#%08x", color));
                    history.addView(chip);
                }
                HorizontalScrollView historyHorizontal = new HorizontalScrollView(requireContext());
                historyHorizontal.addView(history);

                LinearLayout dialogLayout = new LinearLayout(activity);
                dialogLayout.setOrientation(LinearLayout.VERTICAL);
                dialogLayout.addView(cp);
                dialogLayout.addView(historyHorizontal);

                new AlertDialog.Builder(activity)
                        .setTitle(R.string.dialogItem_module_text_textColor_dialog_title)
                        .setView(dialogLayout)
                        .setNegativeButton(R.string.dialogItem_module_text_textColor_dialog_cancel, null)
                        .setPositiveButton(R.string.dialogItem_module_text_textColor_dialog_apply, ((dialog1, which) -> {
                            colorHistoryManager.addColor(cp.getColor());
                            temp_textColor = cp.getColor();
                            binding.defaultTextColor.setChecked(false);
                            updateTextColorIndicator(activity);
                            onEditStart.run();
                        }))
                        .show();
            });

            binding.paragraphColorize.setChecked(textItem.isParagraphColorize());
            binding.paragraphColorize.setOnClickListener(v -> onEditStart.run());

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
            binding.clickableUrls.setChecked(textItem.isClickableUrls());
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

            String userInput = binding.text.getText().toString();
            if (settingsManager.isTrimItemNamesOnEdit()) {
                userInput = userInput.trim();
            }
            textItem.setText(userInput);
            textItem.setTextColor(temp_textColor);
            textItem.setCustomTextColor(!binding.defaultTextColor.isChecked());
            textItem.setClickableUrls(binding.clickableUrls.isChecked());
            textItem.setParagraphColorize(binding.paragraphColorize.isChecked());
        }

        @Override
        public void setOnStartEditListener(Runnable o) {
            onEditStart = o;
        }
    }

    public class LongTextItemEditModule extends BaseEditUiModule {
        private FragmentItemEditorModuleLongtextBinding binding;
        private Runnable onEditStart;

        private int temp_textColor;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            LongTextItem longTextItem = (LongTextItem) item;
            binding = FragmentItemEditorModuleLongtextBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);

            // equip
            viewLong(binding.titleOfText, () -> {
                ClipboardManager clipboardManager = activity.getSystemService(ClipboardManager.class);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("Item long text", ((LongTextItem) item).getLongText()));
                Toast.makeText(activity, R.string.abc_coped, Toast.LENGTH_SHORT).show();
            });
            binding.text.setText(longTextItem.getLongText());
            binding.defaultTextColor.setChecked(!longTextItem.isCustomLongTextColor());
            temp_textColor = longTextItem.getLongTextColor();
            updateTextColorIndicator(activity);
            binding.textColorEdit.setOnClickListener(v -> {
                ColorPickerView cp = new ColorPickerView(activity);
                cp.setCurrentColor(temp_textColor);
                cp.showHex(true); cp.showPreview(true); cp.showAlpha(true);
                cp.setOriginalColor(temp_textColor); cp.setCurrentColor(temp_textColor);

                ChipGroup history = new ChipGroup(requireContext());
                int[] colors = colorHistoryManager.getHistory(5);
                for (int color : colors) {
                    Chip chip = new Chip(requireContext());
                    chip.setChipBackgroundColor(ColorStateList.valueOf(color));
                    chip.setOnClickListener(vvv -> cp.setCurrentColor(color));
                    chip.setText(String.format("#%08x", color));
                    history.addView(chip);
                }
                HorizontalScrollView historyHorizontal = new HorizontalScrollView(requireContext());
                historyHorizontal.addView(history);

                LinearLayout dialogLayout = new LinearLayout(activity);
                dialogLayout.setOrientation(LinearLayout.VERTICAL);
                dialogLayout.addView(cp);
                dialogLayout.addView(historyHorizontal);

                new AlertDialog.Builder(activity)
                        .setTitle(R.string.dialogItem_module_text_textColor_dialog_title)
                        .setView(dialogLayout)
                        .setNegativeButton(R.string.dialogItem_module_text_textColor_dialog_cancel, null)
                        .setPositiveButton(R.string.dialogItem_module_text_textColor_dialog_apply, ((dialog1, which) -> {
                            colorHistoryManager.addColor(cp.getColor());
                            temp_textColor = cp.getColor();
                            binding.defaultTextColor.setChecked(false);
                            updateTextColorIndicator(activity);
                            onEditStart.run();
                        }))
                        .show();
            });

            binding.clickableUrls.setChecked(longTextItem.isLongTextClickableUrls());

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

            binding.defaultSize.setChecked(!longTextItem.isCustomLongTextSize());
            binding.size.setMax(30);
            binding.size.setMin(1);
            binding.size.setProgress(longTextItem.getLongTextSize());
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
            LongTextItem longTextItem = (LongTextItem) item;

            String userInput = binding.text.getText().toString();
            if (settingsManager.isTrimItemNamesOnEdit()) {
                userInput = userInput.trim();
            }
            longTextItem.setLongText(userInput);
            longTextItem.setLongTextSize(binding.size.getProgress());
            longTextItem.setLongTextColor(temp_textColor);
            longTextItem.setCustomLongTextColor(!binding.defaultTextColor.isChecked());
            longTextItem.setCustomLongTextSize(!binding.defaultSize.isChecked());
            longTextItem.setLongTextClickableUrls(binding.clickableUrls.isChecked());
        }

        @Override
        public void setOnStartEditListener(Runnable o) {
            onEditStart = o;
        }
    }

    public static class CheckboxItemEditModule extends BaseEditUiModule {
        private FragmentItemEditorModuleCheckboxBinding binding;
        private Runnable onEditStart;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            CheckboxItem checkboxItem = (CheckboxItem) item;
            this.binding = FragmentItemEditorModuleCheckboxBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);
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
        private FragmentItemEditorModuleDayrepeatablecheckboxBinding binding;
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
            binding = FragmentItemEditorModuleDayrepeatablecheckboxBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);
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
        private FragmentItemEditorModuleCyclelistBinding binding;
        private SimpleSpinnerAdapter<CycleListItem.TickBehavior> simpleSpinnerAdapter;
        private Runnable onEditStart;
        private boolean firstFlag = true;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            CycleListItem cycleListItem = (CycleListItem) item;

            binding = FragmentItemEditorModuleCyclelistBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);
            simpleSpinnerAdapter = new SimpleSpinnerAdapter<CycleListItem.TickBehavior>(activity)
                    .add(activity.getString(R.string.cycleListItem_tick_all), CycleListItem.TickBehavior.ALL)
                    .add(activity.getString(R.string.cycleListItem_tick_current), CycleListItem.TickBehavior.CURRENT);

            binding.itemsCycleBackgroundWork.setAdapter(simpleSpinnerAdapter);
            binding.itemsCycleBackgroundWork.setSelection(simpleSpinnerAdapter.getValuePosition(cycleListItem.getTickBehavior()));
            binding.itemsCycleBackgroundWork.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!firstFlag) {
                        onEditStart.run();
                    }
                    firstFlag = false;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        @Override
        public void commit(Item item) {
            CycleListItem cycleListItem = (CycleListItem) item;
            cycleListItem.setTickBehavior(simpleSpinnerAdapter.getItem(binding.itemsCycleBackgroundWork.getSelectedItemPosition()));
        }

        @Override
        public void setOnStartEditListener(Runnable o) {
            onEditStart = o;
        }
    }

    private static class CounterItemEditModule extends BaseEditUiModule {
        private FragmentItemEditorModuleCounterBinding binding;
        private Runnable onEditStart;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            CounterItem counterItem = (CounterItem) item;

            binding = FragmentItemEditorModuleCounterBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);
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
        @Override
        public View getView() {
            return null;
        }

        @Override
        public void setup(Item item, Activity activity, View view) {

        }

        @Override
        public void commit(Item item) {}

        @Override
        public void setOnStartEditListener(Runnable o) { }
    }

    private static class FilterGroupItemEditModule extends BaseEditUiModule {
        @Override
        public View getView() {
            return null;
        }

        @Override
        public void setup(Item item, Activity activity, View view) {

        }

        @Override
        public void commit(Item item) {}

        @Override
        public void setOnStartEditListener(Runnable o) { }
    }
}

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
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.ColorHistoryManager;
import com.fazziclay.opentoday.app.CrashReportContext;
import com.fazziclay.opentoday.app.ImportWrapper;
import com.fazziclay.opentoday.app.SettingsManager;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.item.CheckboxItem;
import com.fazziclay.opentoday.app.items.item.CounterItem;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.DayRepeatableCheckboxItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.item.LongTextItem;
import com.fazziclay.opentoday.app.items.item.MathGameItem;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.items.notification.DayItemNotification;
import com.fazziclay.opentoday.app.items.notification.ItemNotification;
import com.fazziclay.opentoday.app.items.selection.SelectionManager;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.databinding.FragmentItemEditorBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleCheckboxBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleCounterBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleCyclelistBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleDayrepeatablecheckboxBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleFiltergroupBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleItemBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleLongtextBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleMathgameBinding;
import com.fazziclay.opentoday.databinding.FragmentItemEditorModuleTextBinding;
import com.fazziclay.opentoday.gui.ActivitySettings;
import com.fazziclay.opentoday.gui.ColorPicker;
import com.fazziclay.opentoday.gui.EnumsRegistry;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.dialog.DialogItemNotificationsEditor;
import com.fazziclay.opentoday.gui.interfaces.BackStackMember;
import com.fazziclay.opentoday.gui.interfaces.NavigationHost;
import com.fazziclay.opentoday.util.EnumUtil;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.MinTextWatcher;
import com.fazziclay.opentoday.util.ResUtil;
import com.fazziclay.opentoday.util.SimpleSpinnerAdapter;
import com.fazziclay.opentoday.util.time.ConvertMode;
import com.fazziclay.opentoday.util.time.TimeUtil;

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
    private static final String KEY_ADD_ITEM_POSITION = "create:addItemPosition";
    public static final int VALUE_ADD_ITEM_POSITION_TOP = 0;
    public static final int VALUE_ADD_ITEM_POSITION_BOTTOM = -1;
    private static final boolean DEBUG_SHOW_EDIT_START = App.debug(false);

    public static ItemEditorFragment create(UUID itemStorageId, Class<? extends Item> itemType, int addItemPosition) {
        ItemEditorFragment result = new ItemEditorFragment();
        Bundle a = new Bundle();

        a.putInt(KEY_MODE, MODE_CREATE);
        a.putString(KEY_CREATE_ITEM_STORAGE_ID, itemStorageId.toString());
        a.putString(KEY_CREATE_ITEM_TYPE, ItemsRegistry.REGISTRY.get(itemType).getStringType());
        a.putInt(KEY_ADD_ITEM_POSITION, addItemPosition);

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




    private FragmentItemEditorBinding binding;
    private App app;
    private ItemsRoot itemsRoot;
    private SettingsManager settingsManager;
    private ColorHistoryManager colorHistoryManager;
    private SelectionManager selectionManager;
    private boolean unsavedChanges = false;
    private Item item;
    private int addItemPosition = VALUE_ADD_ITEM_POSITION_BOTTOM;

    private int mode;

    // Edit
    // Create
    private ItemsStorage itemsStorage; // for create

    // Internal
    private final List<BaseEditUiModule> editModules = new ArrayList<>();

    private NavigationHost navigationHost;

    private boolean disableTextUpdated;
    private boolean disableViewMinHeightUpdated;
    private boolean disableLongTextUpdated;
    private boolean disableMathGameBoundsEdits;

    private void disableStateRestoreOnEdits() {
        disableLongTextUpdated = true;
        disableTextUpdated = true;
        disableViewMinHeightUpdated = true;
        disableMathGameBoundsEdits = true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashReportContext.FRONT.push("ItemEditorFragment.onCreate");

        if (getArguments() == null) {
            throw new NullPointerException("Arguments is null");
        }

        binding = FragmentItemEditorBinding.inflate(getLayoutInflater());
        app = App.get(requireContext());
        itemsRoot = app.getItemsRoot();
        settingsManager = app.getSettingsManager();
        colorHistoryManager = app.getColorHistoryManager();
        selectionManager = app.getSelectionManager();
        mode = getArguments().getInt(KEY_MODE, MODE_UNKNOWN);
        
        if (mode == MODE_EDIT) {
            if (getArguments().containsKey(KEY_EDIT_TAB_ID)) {
                Tab tab = itemsRoot.getTabById(getArgTabId());
                item = tab.getItemById(getArgItemId());
            } else {
                item = itemsRoot.getItemById(getArgItemId());
            }

        } else if (mode == MODE_CREATE) {
            itemsStorage = itemsRoot.getItemsStorageById(getArgItemStorageId());
            addItemPosition = getArgAddItemPosition();
            ItemsRegistry.ItemInfo itemInfo = ItemsRegistry.REGISTRY.get(getArgItemType());
            item = itemInfo.create();
            
        } else {
            throw new RuntimeException("Unknown mode: " + mode);
        }

        if (item == null) {
            throw new RuntimeException("Item is null!");
        }

        navigationHost = UI.findFragmentInParents(this, MainRootFragment.class);

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
        if (item instanceof FilterGroupItem) {
            binding.modules.addView(addEditModule(new FilterGroupItemEditModule()));
        }
        if (item instanceof MathGameItem) {
            binding.modules.addView(addEditModule(new MathGameItemEditModule()));
        }

        UI.getUIRoot(this).pushActivitySettings(a -> {
            a.setNotificationsVisible(false);
            a.setClockVisible(false);
            a.setToolbarSettings(ActivitySettings.ToolbarSettings.createBack(EnumsRegistry.INSTANCE.nameResId(ItemsRegistry.REGISTRY.get(item.getClass()).getItemType()), this::cancelRequest));
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UI.getUIRoot(this).popActivitySettings();
        CrashReportContext.FRONT.pop();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewClick(binding.applyButton, this::applyRequest);
        viewClick(binding.cancelButton, this::cancelRequest);
        viewClick(binding.deleteButton, this::deleteRequest);
        viewVisible(binding.deleteButton, item.isAttached(), View.GONE);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.i("ItemEditorFragment", "onResume");
        for (BaseEditUiModule editModule : editModules) {
            editModule.onResume();
        }
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
    public int getArgAddItemPosition() {
        return getArguments().getInt(KEY_ADD_ITEM_POSITION);
    }

    public ItemEditorFragment() {
    }

    private View addEditModule(BaseEditUiModule editUiModule) {
        editUiModule.setOnStartEditListener(() -> {
            if (DEBUG_SHOW_EDIT_START) {
                Toast.makeText(app, "edit start", Toast.LENGTH_SHORT).show();
                new Exception().printStackTrace();
            }
            unsavedChanges = true;
        });
        editUiModule.setup(this.item, requireActivity(), null);
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
            switch (addItemPosition) {
                case VALUE_ADD_ITEM_POSITION_BOTTOM -> itemsStorage.addItem(item);
                case VALUE_ADD_ITEM_POSITION_TOP -> itemsStorage.addItem(item, 0);
                default -> itemsStorage.addItem(item, addItemPosition);
            }
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
                .setTitle(R.string.fragment_itemEditor_cancel_unsaved_title)
                .setNegativeButton(R.string.fragment_itemEditor_cancel_unsaved_continue, null)
                .setPositiveButton(R.string.fragment_itemEditor_cancel_unsaved_discard, ((dialog1, which) -> cancel()))
                .show();
    }

    private void deleteRequest() {
        deleteRequest(requireContext(), item, this::cancel);
    }

    public static void deleteRequest(Context context, Item item, Runnable onDelete) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.fragment_itemEditor_delete_title)
                .setNegativeButton(R.string.fragment_itemEditor_delete_cancel, null)
                .setPositiveButton(R.string.fragment_itemEditor_delete_apply, ((dialog1, which) -> {
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


    public static class UserException extends RuntimeException {
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
        public void onResume() {}
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
            viewVisible(binding.selected, mode == MODE_EDIT, View.GONE);

            binding.viewMinHeight.setText(String.valueOf(item.getViewMinHeight()));
            binding.defaultBackgroundColor.setChecked(!item.isViewCustomBackgroundColor());
            temp_backgroundColor = item.getViewBackgroundColor();
            updateTextColorIndicator(activity);
            viewClick(binding.viewBackgroundColorEdit, () -> new ColorPicker(activity, temp_backgroundColor)
                    .setting(true, true, true)
                    .setColorHistoryManager(colorHistoryManager)
                    .showDialog(R.string.fragment_itemEditor_module_item_backgroundColor_dialog_title,
                            R.string.fragment_itemEditor_module_item_backgroundColor_dialog_cancel,
                            R.string.fragment_itemEditor_module_item_backgroundColor_dialog_apply,
                            (color) -> {
                                temp_backgroundColor = color;
                                binding.defaultBackgroundColor.setChecked(false);
                                updateTextColorIndicator(activity);
                                onEditStart.run();
                            }));
            binding.minimize.setChecked(item.isMinimize());
            viewVisible(binding.copyItemId, mode == MODE_EDIT, View.GONE);
            viewClick(binding.copyItemId, () -> {
                ClipboardManager clipboardManager = activity.getSystemService(ClipboardManager.class);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("Item id", item.getId() == null ? "null" : item.getId().toString()));
            });

            viewVisible(binding.exportItem, mode == MODE_EDIT, View.GONE);
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
                    if (disableViewMinHeightUpdated) {
                        disableViewMinHeightUpdated = false;
                        return;
                    }
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
                if (notification instanceof DayItemNotification d) {
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
        private MinTextWatcher textWatcher;

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
            binding.textColorEdit.setOnClickListener(v -> new ColorPicker(activity, temp_textColor)
                    .setting(true, true, true)
                    .setColorHistoryManager(colorHistoryManager)
                    .showDialog(R.string.fragment_itemEditor_module_text_textColor_dialog_title,
                            R.string.fragment_itemEditor_module_text_textColor_dialog_cancel,
                            R.string.fragment_itemEditor_module_text_textColor_dialog_apply,
                            (color) -> {
                                temp_textColor = color;
                                binding.defaultTextColor.setChecked(false);
                                updateTextColorIndicator(activity);
                                onEditStart.run();
                            }));

            binding.paragraphColorize.setChecked(textItem.isParagraphColorize());
            binding.paragraphColorize.setOnClickListener(v -> onEditStart.run());

            // On edit start
            binding.text.addTextChangedListener(textWatcher = new MinTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (disableTextUpdated) {
                        disableTextUpdated = false;
                        return;
                    }
                    onEditStart.run();
                    viewVisible(binding.openTextEditor, false, View.INVISIBLE);
                }
            });
            binding.defaultTextColor.setOnClickListener(v -> {
                updateTextColorIndicator(activity);
                onEditStart.run();
            });
            binding.clickableUrls.setChecked(textItem.isClickableUrls());
            binding.clickableUrls.setOnClickListener(v -> onEditStart.run());
            //

            viewVisible(binding.openTextEditor, mode == MODE_EDIT, View.INVISIBLE);
            viewClick(binding.openTextEditor, () -> {
                disableStateRestoreOnEdits();
                navigationHost.navigate(ItemTextEditorFragment.create(item.getId(), ItemTextEditorFragment.EDITABLE_TYPE_TEXT), true);
            });
        }

        @Override
        public void onResume() {
            MinTextWatcher.runAtDisabled(binding.text, textWatcher, () -> binding.text.setText(item.getText()));
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
        private MinTextWatcher textWatcher;

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
            binding.textColorEdit.setOnClickListener(v -> new ColorPicker(activity, temp_textColor)
                    .setting(true, true, true)
                    .setColorHistoryManager(colorHistoryManager)
                    .showDialog(R.string.fragment_itemEditor_module_longtext_textColor_dialog_title,
                            R.string.fragment_itemEditor_module_longtext_textColor_dialog_cancel,
                            R.string.fragment_itemEditor_module_longtext_textColor_dialog_apply,
                            (color) -> {
                                temp_textColor = color;
                                binding.defaultTextColor.setChecked(false);
                                updateTextColorIndicator(activity);
                                onEditStart.run();
                            }));

            binding.clickableUrls.setChecked(longTextItem.isLongTextClickableUrls());

            // On edit start
            binding.text.addTextChangedListener(textWatcher = new MinTextWatcher(){
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (disableLongTextUpdated) {
                        disableLongTextUpdated = false;
                        return;
                    }
                    onEditStart.run();
                    viewVisible(binding.openTextEditor, false, View.INVISIBLE);
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

            viewVisible(binding.openTextEditor, mode == MODE_EDIT, View.INVISIBLE);
            viewClick(binding.openTextEditor, () -> {
                disableStateRestoreOnEdits();
                navigationHost.navigate(ItemTextEditorFragment.create(item.getId(), ItemTextEditorFragment.EDITABLE_TYPE_LONG_TEXT), true);
            });
        }

        @Override
        public void onResume() {
            MinTextWatcher.runAtDisabled(binding.text, textWatcher, () -> binding.text.setText(((LongTextItem)item).getLongText()));
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

    public class DayRepeatableCheckboxItemEditModule extends BaseEditUiModule {
        private FragmentItemEditorModuleDayrepeatablecheckboxBinding binding;
        private Runnable onEditStart;

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
            if (mode == MODE_EDIT) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd EEEE", Locale.getDefault());
                GregorianCalendar g = new GregorianCalendar();
                g.set(Calendar.DAY_OF_YEAR, dayRepeatableCheckboxItem.getLatestDayOfYear());
                date = dateFormat.format(g.getTime());
            }
            binding.latestReset.setText(activity.getString(R.string.item_dayRepeatableCheckbox_latestRegenerate, date));
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
            simpleSpinnerAdapter = new SimpleSpinnerAdapter<>(activity);
            EnumUtil.addToSimpleSpinnerAdapter(activity, simpleSpinnerAdapter, CycleListItem.TickBehavior.values());

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
        private FragmentItemEditorModuleFiltergroupBinding binding;
        private Runnable onEditStart = null;
        private SimpleSpinnerAdapter<FilterGroupItem.TickBehavior> simpleSpinnerAdapter;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            final FilterGroupItem filterGroupItem = (FilterGroupItem) item;
            binding = FragmentItemEditorModuleFiltergroupBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);

            simpleSpinnerAdapter = new SimpleSpinnerAdapter<>(activity);
            EnumUtil.addToSimpleSpinnerAdapter(activity, simpleSpinnerAdapter, FilterGroupItem.TickBehavior.values());
            binding.tickBehavior.setAdapter(simpleSpinnerAdapter);
            binding.tickBehavior.setSelection(simpleSpinnerAdapter.getValuePosition(filterGroupItem.getTickBehavior()));
            binding.tickBehavior.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                int counter = 0;
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (counter > 0) {
                        if (onEditStart != null) onEditStart.run();
                    }
                    counter++;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        }

        @Override
        public void commit(Item item) {
            final FilterGroupItem filterGroupItem = (FilterGroupItem) item;

            filterGroupItem.setTickBehavior(simpleSpinnerAdapter.getItem(binding.tickBehavior.getSelectedItemPosition()));
        }

        @Override
        public void setOnStartEditListener(Runnable o) {
            this.onEditStart = o;
        }
    }

    private class MathGameItemEditModule extends BaseEditUiModule {
        private FragmentItemEditorModuleMathgameBinding binding;
        private MathGameItem item;
        private Runnable onEditStart = null;

        @Override
        public View getView() {
            return binding.getRoot();
        }

        @Override
        public void setup(Item item, Activity activity, View view) {
            final MathGameItem mathGameItem = (MathGameItem) item;
            this.item = mathGameItem;
            binding = FragmentItemEditorModuleMathgameBinding.inflate(activity.getLayoutInflater(), (ViewGroup) view, false);
            viewClick(binding.primitiveAdd, this::editStart);
            viewClick(binding.primitiveSubtract, this::editStart);
            viewClick(binding.primitiveMultiply, this::editStart);
            viewClick(binding.primitiveDivide, this::editStart);

            binding.primitiveAdd.setChecked(mathGameItem.isOperationEnabled(MathGameItem.Operation.PLUS));
            binding.primitiveSubtract.setChecked(mathGameItem.isOperationEnabled(MathGameItem.Operation.SUBTRACT));
            binding.primitiveMultiply.setChecked(mathGameItem.isOperationEnabled(MathGameItem.Operation.MULTIPLY));
            binding.primitiveDivide.setChecked(mathGameItem.isOperationEnabled(MathGameItem.Operation.DIVIDE));

            binding.n1min.setText(String.valueOf(this.item.getPrimitiveNumber1Min()));
            binding.n1max.setText(String.valueOf(this.item.getPrimitiveNumber1Max()));
            binding.n2min.setText(String.valueOf(this.item.getPrimitiveNumber2Min()));
            binding.n2max.setText(String.valueOf(this.item.getPrimitiveNumber2Max()));

            MinTextWatcher.afterAll(this::boundsChanged, binding.n1min, binding.n1max, binding.n2min, binding.n2max);
        }

        // 4 EditText call this function at the same time when onStateRestored
        int boundsDisableCounter = 0;
        private void boundsChanged() {
            if (disableMathGameBoundsEdits) {
                boundsDisableCounter++;
                if (boundsDisableCounter >= 4)disableMathGameBoundsEdits = false;
                return;
            }
            boundsDisableCounter = 0;
            editStart();
        }

        private void operationChange(MathGameItem.Operation o, boolean b) {
            item.setOperationEnabled(o, b);
            editStart();
        }

        private void editStart() {
            if (!binding.primitiveDivide.isChecked() && !binding.primitiveSubtract.isChecked() && !binding.primitiveMultiply.isChecked() && !binding.primitiveAdd.isChecked()) {
                binding.primitiveAdd.setChecked(true);
            }
            if (onEditStart != null) {
                onEditStart.run();
            }
        }

        @Override
        public void commit(Item item) {
            this.item = (MathGameItem) item;
            operationChange(MathGameItem.Operation.PLUS, binding.primitiveAdd.isChecked());
            operationChange(MathGameItem.Operation.SUBTRACT, binding.primitiveSubtract.isChecked());
            operationChange(MathGameItem.Operation.MULTIPLY, binding.primitiveMultiply.isChecked());
            operationChange(MathGameItem.Operation.DIVIDE, binding.primitiveDivide.isChecked());
            try {
                this.item.setPrimitiveNumber1Min(Integer.parseInt(binding.n1min.getText().toString()));
            } catch (Exception ignored) {}
            try {
                this.item.setPrimitiveNumber1Max(Integer.parseInt(binding.n1max.getText().toString()));
            } catch (Exception ignored) {}
            try {
                this.item.setPrimitiveNumber2Min(Integer.parseInt(binding.n2min.getText().toString()));
            } catch (Exception ignored) {}
            try {
                this.item.setPrimitiveNumber2Max(Integer.parseInt(binding.n2max.getText().toString()));
            } catch (Exception ignored) {}
            if (mode == MODE_EDIT) this.item.generateQuest();
        }

        @Override
        public void setOnStartEditListener(Runnable o) {
            this.onEditStart = o;
        }
    }
}

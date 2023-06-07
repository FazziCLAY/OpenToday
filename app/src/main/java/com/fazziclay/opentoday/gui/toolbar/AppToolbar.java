package com.fazziclay.opentoday.gui.toolbar;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;
import static com.fazziclay.opentoday.util.InlineUtil.viewLong;
import static com.fazziclay.opentoday.util.InlineUtil.viewVisible;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.FeatureFlag;
import com.fazziclay.opentoday.app.ImportWrapper;
import com.fazziclay.opentoday.app.SettingsManager;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.callback.OnTabsChanged;
import com.fazziclay.opentoday.app.items.callback.SelectionCallback;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemUtil;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.selection.Selection;
import com.fazziclay.opentoday.app.items.selection.SelectionManager;
import com.fazziclay.opentoday.app.items.tab.Debug202305RandomTab;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.app.items.tab.TabsManager;
import com.fazziclay.opentoday.databinding.ToolbarBinding;
import com.fazziclay.opentoday.databinding.ToolbarMoreFileBinding;
import com.fazziclay.opentoday.databinding.ToolbarMoreItemsBinding;
import com.fazziclay.opentoday.databinding.ToolbarMoreItemsItemBinding;
import com.fazziclay.opentoday.databinding.ToolbarMoreOpentodayBinding;
import com.fazziclay.opentoday.databinding.ToolbarMoreSelectionBinding;
import com.fazziclay.opentoday.databinding.ToolbarMoreTabsBinding;
import com.fazziclay.opentoday.gui.EnumsRegistry;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.activity.MainActivity;
import com.fazziclay.opentoday.gui.activity.SetupActivity;
import com.fazziclay.opentoday.gui.callbacks.UIDebugCallback;
import com.fazziclay.opentoday.gui.dialog.DialogSelectItemAction;
import com.fazziclay.opentoday.gui.fragment.AboutFragment;
import com.fazziclay.opentoday.gui.fragment.DeleteItemsFragment;
import com.fazziclay.opentoday.gui.fragment.ImportFragment;
import com.fazziclay.opentoday.gui.fragment.ItemEditorFragment;
import com.fazziclay.opentoday.gui.fragment.SettingsFragment;
import com.fazziclay.opentoday.gui.interfaces.NavigationHost;
import com.fazziclay.opentoday.util.ClipboardUtil;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.ResUtil;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;
import com.google.android.material.button.MaterialButton;

import java.util.Random;
import java.util.UUID;

public class AppToolbar {
    private static final String TAG = "AppToolbar";

    private final Activity activity;
    private ToolbarBinding binding;
    private final ViewGroup toolbarView;
    private final ViewGroup toolbarMoreView;
    private boolean destroyed = false;
    private boolean created = false;
    private final App app;
    private final TabsManager tabsManager;
    private final SettingsManager settingsManager;
    private final SelectionManager selectionManager;
    private ItemsStorage itemsStorage; // For context toolbar work
    private View currentToolbarButton = null; // Current active button. If none: null
    private SelectionCallback selectionCallback = null; // (Selection TAB) On selection changed. For runtime update selection information
    private SelectionCallback selectionCallbackTab = null; // (Selection TAB) On selection changed. For runtime update selection information
    private OnTabsChanged onTabsChanged = null;
    private UIDebugCallback debugCallback = null;

    // Cache
    private View itemsSectionCacheView = null;
    private OnMoreVisibleChanged onMoreVisibleChangedListener = null;
    private final NavigationHost rootNavigationHost;
    private long lastTabReorder;



    public AppToolbar(final Activity activity,
                      final TabsManager tabsManager,
                      final SettingsManager settingsManager,
                      final SelectionManager selectionManager,
                      final ItemsStorage itemsStorage,
                      final NavigationHost rootNavigationHost,
                      final ViewGroup toolbarView,
                      final ViewGroup toolbarMoreView) {
        this.app = App.get(activity);
        this.activity = activity;
        this.tabsManager = tabsManager;
        this.settingsManager = settingsManager;
        this.selectionManager = selectionManager;
        this.itemsStorage = itemsStorage;
        this.rootNavigationHost = rootNavigationHost;
        this.toolbarView = toolbarView;
        this.toolbarMoreView = toolbarMoreView;
        Logger.d(TAG, "<init>");
    }

    public void create() {
        if (created) throw new RuntimeException("AppToolbar already created!");
        created = true;

        Logger.d(TAG, "create");
        this.binding = ToolbarBinding.inflate(activity.getLayoutInflater());
        this.toolbarView.addView(binding.getRoot());

        // setup buttons callbacks
        setupButtonCallback(binding.toolbarFile, this::onFileClick);
        setupButtonCallback(binding.toolbarItems, this::onItemsClick);
        setupButtonCallback(binding.toolbarSelection, this::onSelectionClick);
        setupButtonCallback(binding.toolbarTabs, this::onTabsClick);
        setupButtonCallback(binding.toolbarOpentoday, this::onOpenTodayClick);
        setupButtonCallback(binding.toolbarDebug, this::onDebugClick);

        // Selection button
        selectionCallbackTab = new SelectionCallback() {
            @Override
            public void onSelectionChanged(Selection[] selections) {
                boolean visible = selections.length != 0;
                viewVisible(binding.toolbarSelection, visible, View.GONE);
                if (!visible && currentToolbarButton == binding.toolbarSelection) {
                    closeMoreView();
                }
            }
        };
        selectionManager.getOnSelectionUpdated().addCallback(CallbackImportance.DEFAULT, selectionCallbackTab);
        viewVisible(binding.toolbarSelection, !selectionManager.isSelectionEmpty(), View.GONE);

        debugCallback = new UIDebugCallback() {
            @Override
            public void debugChange(boolean visible) {
                viewVisible(binding.toolbarDebug, visible, View.GONE);
                if (!visible && currentToolbarButton == binding.toolbarDebug) {
                    closeMoreView();
                }
            }
        };
        UI.getDebugCallbacks().addCallback(CallbackImportance.DEFAULT, debugCallback);
        viewVisible(binding.toolbarDebug, app.isFeatureFlag(FeatureFlag.TOOLBAR_DEBUG), View.GONE);
    }


    public void destroy() {
        if (!created) throw new RuntimeException("No created before");
        if (destroyed) {
            throw new RuntimeException("AppToolbar already destroyed");
        }
        Logger.d(TAG, "destroy");
        destroyed = true;
        if (selectionCallback != null) selectionManager.getOnSelectionUpdated().removeCallback(selectionCallback);
        if (selectionCallbackTab != null) selectionManager.getOnSelectionUpdated().removeCallback(selectionCallbackTab);
        if (onTabsChanged != null) tabsManager.getOnTabsChangedCallbacks().removeCallback(onTabsChanged);
        if (debugCallback != null) UI.getDebugCallbacks().removeCallback(debugCallback);
        toolbarView.removeAllViews();
        toolbarMoreView.removeAllViews();
        this.binding = null;
    }

    private void setupButtonCallback(final View view, final Runnable runnable) {
        viewClick(view, () -> preOnClick(view, runnable));
    }

    // Set view android:backgroundTint for value from style param
    private void backgroundTintFromStyle(int style, View view) {
        TypedArray typedArray = ResUtil.getStyleColor(activity, style, android.R.attr.backgroundTint);
        int color = typedArray.getColor(0, Color.RED);
        view.setBackgroundTintList(ColorStateList.valueOf(color));
        typedArray.recycle();
    }

    /**
     * clear toolbarMoreView
     * set currentToolbarButton default color
     */
    private void resetMoreView() {
        toolbarMoreView.removeAllViews();
        if (currentToolbarButton != null) {
            backgroundTintFromStyle(R.style.Theme_OpenToday_Toolbar_Button, currentToolbarButton);
        }
        if (selectionCallback != null) selectionManager.getOnSelectionUpdated().removeCallback(selectionCallback);
        if (onTabsChanged != null) tabsManager.getOnTabsChangedCallbacks().removeCallback(onTabsChanged);
    }

    private void preOnClick(final View buttonView, final Runnable runnable) {
        resetMoreView();
        if (currentToolbarButton == buttonView) {
            currentToolbarButton = null;
            if (onMoreVisibleChangedListener != null) {
                onMoreVisibleChangedListener.onChange(false);
            }
        } else {
            currentToolbarButton = buttonView;
            backgroundTintFromStyle(R.style.Theme_OpenToday_Toolbar_Button_Selected, currentToolbarButton);
            if (onMoreVisibleChangedListener != null) {
                onMoreVisibleChangedListener.onChange(true);
            }
            runnable.run();
        }
    }

    public boolean isMoreViewVisible() {
        return currentToolbarButton != null;
    }

    public void closeMoreView() {
        resetMoreView();
        currentToolbarButton = null;
        if (onMoreVisibleChangedListener != null) onMoreVisibleChangedListener.onChange(false);
    }

    private void registerMoreView(View root) {
        toolbarMoreView.addView(root);
    }


    @SuppressWarnings("SetTextI18n")
    private void onDebugClick() {
        LinearLayout layout = new LinearLayout(activity);
        layout.setPadding(10, 0, 10, 0);
        layout.setOrientation(LinearLayout.VERTICAL);
        registerMoreView(layout);
        layout.setBackgroundColor(new Random().nextInt());

        Button overlay = new Button(activity);
        overlay.setText("Logs Overlay");
        viewClick(overlay, () -> ((MainActivity) activity).toggleLogsOverlay());

        Button personalTick = new Button(activity);
        personalTick.setText("Personal tick");
        viewClick(personalTick, () -> UI.Debug.showPersonalTickDialog(activity));

        Button restartActivity = new Button(activity);
        restartActivity.setText("Restart activity");
        viewClick(restartActivity, () -> {
            activity.finish();
            Intent intent = new Intent(activity, activity.getClass());
            try {
                intent.replaceExtras(activity.getIntent().getExtras());
            } catch (Exception ignored) {}
            activity.startActivity(intent);
        });


        MaterialButton resetSetup = new MaterialButton(activity);
        resetSetup.setText("Reset setup");
        viewClick(resetSetup, () -> {
            activity.getSharedPreferences(App.SHARED_NAME, Context.MODE_PRIVATE).edit().putBoolean(App.SHARED_KEY_IS_SETUP_DONE, false).apply();
            activity.finish();
            activity.startActivity(new Intent(activity, SetupActivity.class));
        });

        MaterialButton debugTabCreate = new MaterialButton(activity);
        debugTabCreate.setText("Create Debug202305RandomTab tab");
        viewClick(debugTabCreate, () -> tabsManager.addTab(new Debug202305RandomTab()));

        MaterialButton featureFlags = new MaterialButton(activity);
        featureFlags.setText("Feature flags");
        viewClick(featureFlags, () -> UI.Debug.showFeatureFlagsDialog(app, activity));

        MaterialButton crash = new MaterialButton(activity);
        crash.setText("CRASH");
        viewClick(crash, () -> UI.Debug.showCrashWithMessageDialog(activity, "Toolbar->Debug->Crash: %s"));

        // Position
        layout.addView(overlay);
        layout.addView(personalTick);
        layout.addView(restartActivity);
        layout.addView(resetSetup);
        layout.addView(debugTabCreate);
        layout.addView(featureFlags);
        layout.addView(crash);

        TextView textView = new TextView(activity);
        layout.addView(textView);
        textView.setText("FazziCLAY is god.");
    }


    private void onFileClick() {
        final ToolbarMoreFileBinding localBinding = ToolbarMoreFileBinding.inflate(activity.getLayoutInflater(), toolbarMoreView, false);
        registerMoreView(localBinding.getRoot());

        // Import from clipboard button
        viewVisible(localBinding.importDataFromClipboard, ImportWrapper.isImportText(ClipboardUtil.getSelectedText(activity)), View.GONE);
        viewClick(localBinding.importDataFromClipboard, () -> {
            try {
                final String text = ClipboardUtil.getSelectedText(activity);
                if (ImportWrapper.isImportText(text)) {
                    UUID id = ItemUtil.getId(itemsStorage);
                    if (id == null) {
                        Toast.makeText(activity, R.string.toolbar_more_file_import_unsupported, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    rootNavigationHost.navigate(ImportFragment.create(id, text, true), true);
                } else {
                    Toast.makeText(activity, R.string.toolbar_more_file_importFromClipboard_notImportText, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Logger.e(TAG, "importFromClipboard button pressed", e);
                Toast.makeText(activity, R.string.toolbar_more_file_importFromClipboard_unknownError, Toast.LENGTH_SHORT).show();
            }
        });

        // Import button
        viewClick(localBinding.importData, () -> {
            UUID id = ItemUtil.getId(itemsStorage);
            if (id == null) {
                Toast.makeText(activity, R.string.toolbar_more_file_import_unsupported, Toast.LENGTH_SHORT).show();
                return;
            }
            rootNavigationHost.navigate(ImportFragment.create(id), true);
        });

        // Save button
        viewClick(localBinding.saveAll, () -> {
            boolean success = tabsManager.saveAllDirect();
            if (success) Toast.makeText(activity, R.string.toolbar_more_file_saveAll_success, Toast.LENGTH_LONG).show();
        });
    }

    private class TabHolder extends RecyclerView.ViewHolder {
        public TabHolder() {
            super(new TextView(activity));
            TextView textView = (TextView) itemView;
            textView.setPadding(5, 0, 5, 0);
            textView.setTextSize(20);
            textView.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            p.setMargins(5, 5, 5, 5);
            textView.setBackgroundResource(R.drawable.shape);
            textView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#555555")));
            textView.setLayoutParams(p);
        }

        public void setText(String text) {
            ((TextView) itemView).setText(text);
        }

        public void setDisableTick(boolean disableTick) {
            itemView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(disableTick ? "#ff3333" : "#555555")));
        }
    }

    private void onTabsClick() {
        ToolbarMoreTabsBinding localBinding = ToolbarMoreTabsBinding.inflate(activity.getLayoutInflater(), toolbarMoreView, false);
        registerMoreView(localBinding.getRoot());

        localBinding.tabsRecycleView.setLayoutManager(new LinearLayoutManager(activity));
        localBinding.tabsRecycleView.setAdapter(new RecyclerView.Adapter<TabHolder>() {
            @NonNull
            @Override
            public TabHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new TabHolder();
            }

            @Override
            public void onBindViewHolder(@NonNull TabHolder holder, int position) {
                Tab tab = tabsManager.getTabAt(position);
                holder.setText(tab.getName());
                holder.setDisableTick(tab.isDisableTick());
                viewClick(holder.itemView, () -> showEditTabDialog(tab));
            }

            @Override
            public int getItemCount() {
                return tabsManager.getTabsCount();
            }
        });


        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int positionFrom = viewHolder.getAdapterPosition();
                int positionTo = target.getAdapterPosition();

                recyclerView.getAdapter().notifyItemMoved(positionFrom, positionTo);
                lastTabReorder = System.currentTimeMillis();
                tabsManager.moveTabs(positionFrom, positionTo);
                return true;
            }


            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(localBinding.tabsRecycleView);


        onTabsChanged = new OnTabsChanged() {
            @Override
            public Status onTabsChanged(@NonNull Tab[] tabs) {
                if (System.currentTimeMillis() - lastTabReorder >= 1000) {
                    // TODO: 2023.05.23 add specific for OnTabsChanged
                    // TODO: 2023.05.23 add TabsManager
                    localBinding.tabsRecycleView.getAdapter().notifyDataSetChanged();
                }
                return Status.NONE;
            }
        };
        tabsManager.getOnTabsChangedCallbacks().addCallback(CallbackImportance.DEFAULT, onTabsChanged);



        viewClick(localBinding.addTab, () -> {
            EditText tabNameEditText = new EditText(activity);
            tabNameEditText.setHint(R.string.toolbar_tabs_addNew_name_hint);
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.toolbar_tabs_addNew_dialog_title)
                    .setView(tabNameEditText)
                    .setPositiveButton(R.string.toolbar_more_items_tab_add, (dialog, which) -> {
                        String text = tabNameEditText.getText().toString();
                        if (!text.trim().isEmpty()) {
                            tabsManager.createLocalTab(tabNameEditText.getText().toString());
                        } else {
                            Toast.makeText(activity, R.string.tab_noEmptyName, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.abc_cancel, null)
                    .show();
        });
    }

    private void showEditTabDialog(final Tab tab) {
        LinearLayout dialogView = new LinearLayout(activity);
        dialogView.setOrientation(LinearLayout.VERTICAL);

        EditText tabNameEditText = new EditText(activity);
        tabNameEditText.setHint(R.string.toolbar_tabs_edit_name_hint);
        tabNameEditText.setText(tab.getName());

        CheckBox disableTick = new CheckBox(activity);
        disableTick.setText(R.string.toolbar_tabs_edit_disableTick);
        disableTick.setChecked(tab.isDisableTick());

        dialogView.addView(tabNameEditText);
        dialogView.addView(disableTick);

        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.toolbar_tabs_edit_dialog_title, tab.getName()))
                .setView(dialogView)
                .setPositiveButton(R.string.toolbar_more_items_tab_apply, (dialog, which) -> {
                    String text = tabNameEditText.getText().toString();
                    if (!text.trim().isEmpty()) {
                        tab.setName(text);
                    } else {
                        Toast.makeText(activity, R.string.tab_noEmptyName, Toast.LENGTH_SHORT).show();
                    }
                    tab.setDisableTick(disableTick.isChecked());
                })
                .setNegativeButton(R.string.abc_cancel, null)
                .setNeutralButton(R.string.toolbar_more_items_tab_delete, (dialog, w) -> new AlertDialog.Builder(activity)
                        .setTitle(activity.getString(R.string.fragment_deleteItems_delete_title, String.valueOf(tab.size())))
                        .setNegativeButton(R.string.fragment_deleteItems_delete_cancel, null)
                        .setPositiveButton(R.string.fragment_deleteItems_delete_apply, ((dialog1, which) -> {
                            if (tabsManager.isOneTabMode()) {
                                Toast.makeText(activity, R.string.toolbar_more_tabs_delete_notAllowOneTabMode, Toast.LENGTH_LONG).show();
                                return;
                            }
                            try {
                                tabsManager.deleteTab(tab);
                            } catch (Exception e) {
                                Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }))
                        .show())
                .setNegativeButton(R.string.abc_cancel, null)
                .show();
    }

    private class H extends RecyclerView.ViewHolder {
        private final TextView name;
        private final Button create;
        private final Button add;
        private final Button description;

        public H(ViewGroup parent) {
            super(new FrameLayout(activity));
            ToolbarMoreItemsItemBinding b = ToolbarMoreItemsItemBinding.inflate(activity.getLayoutInflater(), parent, false);
            this.name = b.name;
            this.create = b.create;
            this.add = b.add;
            this.description = b.description;
            ((FrameLayout) itemView).addView(b.getRoot());
        }

        public void clear() {
            ((FrameLayout) itemView).removeAllViews();
        }
    }

    private void onItemsClick() {
        // Cache
        if (itemsSectionCacheView != null) {
            registerMoreView(itemsSectionCacheView);
            return;
        }

        // Non-cache
        ToolbarMoreItemsBinding localBinding = ToolbarMoreItemsBinding.inflate(activity.getLayoutInflater());
        registerMoreView(itemsSectionCacheView = localBinding.getRoot());

        localBinding.items.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.VERTICAL, false));
        localBinding.items.setAdapter(new RecyclerView.Adapter<H>() {
            private int getAddItemPos(SettingsManager.ItemAddPosition fromSettings) {
                return switch (fromSettings) {
                    case TOP -> ItemEditorFragment.VALUE_ADD_ITEM_POSITION_TOP;
                    case BOTTOM -> ItemEditorFragment.VALUE_ADD_ITEM_POSITION_BOTTOM;
                };
            }

            @NonNull
            @Override
            public H onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new H(parent);
            }

            @Override
            public void onBindViewHolder(@NonNull H holder, int position) {
                ItemsRegistry.ItemInfo itemInfo = ItemsRegistry.REGISTRY.getAllItems()[position];

                if (itemInfo.isCompatibility(app.getFeatureFlags())) {
                    holder.name.setText(EnumsRegistry.INSTANCE.nameResId(itemInfo.getItemType()));
                    viewClick(holder.create, () -> rootNavigationHost.navigate(ItemEditorFragment.create(ItemUtil.getId(itemsStorage), itemInfo.getClassType(), getAddItemPos(settingsManager.getItemAddPosition())), true));
                    viewClick(holder.add, () -> {
                        switch (settingsManager.getItemAddPosition()) {
                            case TOP -> itemsStorage.addItem(ItemsRegistry.REGISTRY.get(itemInfo.getClassType()).create(), 0);
                            case BOTTOM -> itemsStorage.addItem(ItemsRegistry.REGISTRY.get(itemInfo.getClassType()).create());
                        }
                    });
                    viewClick(holder.description, () -> showItemDescriptionDialog(itemInfo));
                } else {
                    holder.clear();
                }
            }

            @Override
            public int getItemCount() {
                return ItemsRegistry.REGISTRY.count();
            }
        });

        // Action: On click
        viewClick(localBinding.changeOnClick, () -> new DialogSelectItemAction(activity, settingsManager.getItemOnClickAction(), itemOnClickAction -> {
            settingsManager.setItemOnClickAction(itemOnClickAction);
            settingsManager.save();
        }, activity.getString(R.string.toolbar_more_items_action_click)).show());
        // Action: On left swipe
        viewClick(localBinding.changeOnLeftSwipe, () -> new DialogSelectItemAction(activity, settingsManager.getItemOnLeftAction(), itemOnLeftAction -> {
            settingsManager.setItemOnLeftAction(itemOnLeftAction);
            settingsManager.save();
        }, activity.getString(R.string.toolbar_more_items_action_leftSwipe)).show());
    }

    private void showItemDescriptionDialog(ItemsRegistry.ItemInfo itemInfo) {
        new AlertDialog.Builder(activity)
                .setTitle(EnumsRegistry.INSTANCE.nameResId(itemInfo.getItemType()))
                .setMessage(EnumsRegistry.INSTANCE.itemDescriptionResId(itemInfo.getItemType()))
                .setPositiveButton(R.string.abc_ok, null)
                .show();
    }

    private void onOpenTodayClick() {
        ToolbarMoreOpentodayBinding localBinding = ToolbarMoreOpentodayBinding.inflate(activity.getLayoutInflater());
        registerMoreView(localBinding.getRoot());

        viewClick(localBinding.about, () -> rootNavigationHost.navigate(AboutFragment.create(), true));
        viewClick(localBinding.settings, () -> rootNavigationHost.navigate(SettingsFragment.create(), true));
        viewClick(localBinding.calendar, () -> {
            DatePickerDialog dialog = new DatePickerDialog(activity);
            dialog.getDatePicker().setFirstDayOfWeek(settingsManager.getFirstDayOfWeek());
            dialog.show();
        });
    }

    private void onSelectionClick() {
        ToolbarMoreSelectionBinding localBinding = ToolbarMoreSelectionBinding.inflate(activity.getLayoutInflater());
        registerMoreView(localBinding.getRoot());

        viewClick(localBinding.exportSelected, this::exportSelected);
        viewLong(localBinding.exportSelected, this::showExportSelectedWithMessageDialog);

        viewClick(localBinding.deselectAll, selectionManager::deselectAll);
        viewClick(localBinding.moveSelectedHere, () -> selectionManager.moveAllSelectedTo(itemsStorage));
        viewClick(localBinding.copySelectedHere, () -> selectionManager.copyAllSelectedTo(itemsStorage));
        viewClick(localBinding.delete, () -> rootNavigationHost.navigate(DeleteItemsFragment.create(selectionManager.getItems()), true));

        viewClick(localBinding.editSelected, () -> {
            if (selectionManager.getSelections().length > 0) {
                Item item = selectionManager.getSelections()[0].getItem();
                rootNavigationHost.navigate(ItemEditorFragment.edit(item.getId()), true);
            }
        });
        // Add selection listener
        selectionCallback = new SelectionCallback() {
            @Override
            public void onSelectionChanged(Selection[] selections) {
                localBinding.selectedInfo.setText(activity.getString(R.string.toolbar_more_selection_info, String.valueOf(selections.length)));
                viewVisible(localBinding.editSelected, selections.length == 1, View.GONE);
            }
        };
        selectionCallback.onSelectionChanged(selectionManager.getSelections()); // First run
        selectionManager.getOnSelectionUpdated().addCallback(CallbackImportance.MIN, selectionCallback); // Add to callbackStorage
    }

    private void exportSelected() {
        try {
            ImportWrapper importWrapper = ImportWrapper.createImport(ImportWrapper.Permission.ADD_ITEMS_TO_CURRENT)
                    .addItemAll(selectionManager.getItems())
                    .build();

            ClipboardUtil.selectText(app, R.string.toolbar_more_selection_export_clipdata_label, importWrapper.finalExport());
            Toast.makeText(activity, R.string.toolbar_more_selection_export_success, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(activity, activity.getString(R.string.toolbar_more_selection_export_exception, e.toString()), Toast.LENGTH_SHORT).show();
        }
    }

    private void showExportSelectedWithMessageDialog() {
        EditText dialogMessage = new EditText(activity);

        new AlertDialog.Builder(activity)
                .setTitle(R.string.toolbar_more_selection_export_setMessage_title)
                .setView(dialogMessage)
                .setNeutralButton(R.string.toolbar_more_selection_export_setMessage_nomsg, (ignore0, ignore1) -> exportSelected())
                .setPositiveButton(R.string.toolbar_more_selection_export_setMessage_export, (ignore2, ignore3) -> {
                    String msg = dialogMessage.getText().toString();

                    try {
                        ImportWrapper importWrapper = ImportWrapper.createImport(ImportWrapper.Permission.ADD_ITEMS_TO_CURRENT, ImportWrapper.Permission.PRE_IMPORT_SHOW_DIALOG)
                                .addItemAll(selectionManager.getItems())
                                .setDialogMessage(msg)
                                .build();

                        ClipboardUtil.selectText(app, R.string.toolbar_more_selection_export_clipdata_label, importWrapper.finalExport());
                        Toast.makeText(activity, R.string.toolbar_more_selection_export_success, Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Toast.makeText(activity, activity.getString(R.string.toolbar_more_selection_export_exception, e.toString()), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.toolbar_more_selection_export_setMessage_cancel, null)
                .show();
    }

    public void setItemStorage(ItemsStorage itemsStorage) {
        Debug.itemsStorageToolbarContext = this.itemsStorage = itemsStorage;
    }

    public void setOnMoreVisibleChangedListener(OnMoreVisibleChanged l) {
        onMoreVisibleChangedListener = l;
    }

    public interface OnMoreVisibleChanged {
        void onChange(boolean visible);
    }
}

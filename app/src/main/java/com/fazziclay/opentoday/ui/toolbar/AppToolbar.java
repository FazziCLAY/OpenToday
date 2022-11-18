package com.fazziclay.opentoday.ui.toolbar;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;
import static com.fazziclay.opentoday.util.InlineUtil.viewLong;
import static com.fazziclay.opentoday.util.InlineUtil.viewVisible;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.FeatureFlag;
import com.fazziclay.opentoday.app.ImportWrapper;
import com.fazziclay.opentoday.app.items.ID;
import com.fazziclay.opentoday.app.items.ItemManager;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.Selection;
import com.fazziclay.opentoday.app.items.callback.OnSelectionChanged;
import com.fazziclay.opentoday.app.items.callback.OnTabsChanged;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.app.receiver.ItemsTickReceiver;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.callback.CallbackImportance;
import com.fazziclay.opentoday.callback.Status;
import com.fazziclay.opentoday.databinding.ToolbarBinding;
import com.fazziclay.opentoday.databinding.ToolbarMoreFileBinding;
import com.fazziclay.opentoday.databinding.ToolbarMoreItemsBinding;
import com.fazziclay.opentoday.databinding.ToolbarMoreItemsItemBinding;
import com.fazziclay.opentoday.databinding.ToolbarMoreOpentodayBinding;
import com.fazziclay.opentoday.databinding.ToolbarMoreSelectionBinding;
import com.fazziclay.opentoday.databinding.ToolbarMoreTabsBinding;
import com.fazziclay.opentoday.ui.activity.MainActivity;
import com.fazziclay.opentoday.ui.activity.SetupActivity;
import com.fazziclay.opentoday.ui.dialog.DialogSelectItemAction;
import com.fazziclay.opentoday.ui.fragment.AboutFragment;
import com.fazziclay.opentoday.ui.fragment.DeleteItemsFragment;
import com.fazziclay.opentoday.ui.fragment.ImportFragment;
import com.fazziclay.opentoday.ui.fragment.ItemEditorFragment;
import com.fazziclay.opentoday.ui.fragment.ItemsTabIncludeFragment;
import com.fazziclay.opentoday.ui.fragment.SettingsFragment;
import com.fazziclay.opentoday.ui.interfaces.NavigationHost;
import com.fazziclay.opentoday.util.ResUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AppToolbar {
    private final Activity activity;
    private ToolbarBinding binding;
    private final LinearLayout toolbarView;
    private final LinearLayout toolbarMoreView;
    private boolean destroyed = false;
    private final App app;
    private final ItemManager itemManager;
    private final SettingsManager settingsManager;
    private ItemsStorage itemsStorage; // For context toolbar work
    private Tab tab; // For context toolbar work
    private View currentToolbarButton = null; // Current active button. If none: null
    private OnSelectionChanged onSelectionChanged = null; // (Selection TAB) On selection changed. For runtime update selection information
    private OnSelectionChanged onSelectionChangedTab = null; // (Selection TAB) On selection changed. For runtime update selection information
    private OnTabsChanged onTabsChanged = null;

    // Cache
    private View itemsSectionCacheView = null;
    private OnMoreVisibleChanged onMoreVisibleChangedListener = null;
    private final NavigationHost rootNavigationHost;
    private final NavigationHost navigationHost;
    private long lastTabReorder;

    private View sectionItems;


    public AppToolbar(Activity activity, ItemManager itemManager, SettingsManager settingsManager, ItemsStorage itemsStorage, NavigationHost rootNavigationHost, ItemsTabIncludeFragment itemsTabIncludeFragment) {
        this.activity = activity;
        this.toolbarMoreView = new LinearLayout(activity);
        this.toolbarView = new LinearLayout(activity);
        this.app = App.get(activity);
        this.itemManager = itemManager;
        this.settingsManager = settingsManager;
        this.itemsStorage = itemsStorage;
        this.rootNavigationHost = rootNavigationHost;
        this.navigationHost = itemsTabIncludeFragment;
        this.toolbarMoreView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.toolbarMoreView.setOrientation(LinearLayout.VERTICAL);
        this.toolbarMoreView.setClickable(false);
        this.toolbarView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.toolbarView.setOrientation(LinearLayout.VERTICAL);
    }

    public void create() {
        this.binding = ToolbarBinding.inflate(activity.getLayoutInflater());
        this.toolbarView.addView(binding.getRoot());

        viewClick(binding.toolbarFile, () -> {
            if (preOnClick(binding.toolbarFile)) onFileClick();
        });
        viewClick(binding.toolbarItems, () -> {
            if (preOnClick(binding.toolbarItems)) onItemsClick();
        });
        viewClick(binding.toolbarSelection, () -> {
            if (preOnClick(binding.toolbarSelection)) onSelectionClick();
        });
        viewClick(binding.toolbarOpentoday, () -> {
            if (preOnClick(binding.toolbarOpentoday)) onOpenTodayClick();
        });
        viewClick(binding.toolbarTabs, () -> {
            if (preOnClick(binding.toolbarTabs)) onTabsClick();
        });
        onSelectionChangedTab = selections -> {
            if (selections.isEmpty()) {
                binding.toolbarSelection.setVisibility(View.GONE);
                if (currentToolbarButton == binding.toolbarSelection) {
                    closeMoreView();
                }
            } else {
                binding.toolbarSelection.setVisibility(View.VISIBLE);
            }
        };
        itemManager.getOnSelectionUpdated().addCallback(CallbackImportance.DEFAULT, onSelectionChangedTab);
        binding.toolbarSelection.setVisibility(itemManager.getSelections().length == 0 ? View.GONE : View.VISIBLE);
    }


    public View getToolbarView() {
        return toolbarView;
    }

    public View getToolbarMoreView() {
        return toolbarMoreView;
    }

    public void destroy() {
        if (destroyed) {
            throw new RuntimeException("destroyed");
        }
        destroyed = true;
        if (onSelectionChanged != null) itemManager.getOnSelectionUpdated().deleteCallback(onSelectionChanged);
        if (onSelectionChangedTab != null) itemManager.getOnSelectionUpdated().deleteCallback(onSelectionChangedTab);
        if (onTabsChanged != null) itemManager.getOnTabsChanged().deleteCallback(onTabsChanged);
    }

    // Set view android:backgroundTint for value from style param

    private void backgroundTintFromStyle(int style, View view) {
        TypedArray typedArray = ResUtil.getStyleColor(activity, style, android.R.attr.backgroundTint);
        int color = typedArray.getColor(0, Color.RED);
        view.setBackgroundTintList(ColorStateList.valueOf(color));
        typedArray.recycle();
    }
    private void resetMoreView() {
        toolbarMoreView.removeAllViews();
        if (currentToolbarButton != null) {
            backgroundTintFromStyle(R.style.Theme_OpenToday_Toolbar_Button, currentToolbarButton);
        }
        if (onSelectionChanged != null) itemManager.getOnSelectionUpdated().deleteCallback(onSelectionChanged);
        if (onTabsChanged != null) itemManager.getOnTabsChanged().deleteCallback(onTabsChanged);
    }

    private boolean preOnClick(View buttonView) {
        resetMoreView();
        if (currentToolbarButton == buttonView) {
            currentToolbarButton = null;
            if (onMoreVisibleChangedListener != null) {
                onMoreVisibleChangedListener.onChange(false);
            }
            return false;
        } else {
            currentToolbarButton = buttonView;
            backgroundTintFromStyle(R.style.Theme_OpenToday_Toolbar_Button_Selected, currentToolbarButton);
            if (onMoreVisibleChangedListener != null) {
                onMoreVisibleChangedListener.onChange(true);
            }
            return true;
        }
    }

    public boolean isMoreViewVisible() {
        return currentToolbarButton != null;
    }

    public void closeMoreView() {
        resetMoreView();
        currentToolbarButton = null;
        if (onMoreVisibleChangedListener != null) {
            onMoreVisibleChangedListener.onChange(false);
        }
    }

    private void onFileClick() {
        ToolbarMoreFileBinding b = ToolbarMoreFileBinding.inflate(activity.getLayoutInflater(), toolbarMoreView, false);

        // Save button
        viewClick(b.saveAll, () -> {
            boolean success = itemManager.saveAllDirect();
            if (success) Toast.makeText(activity, R.string.toolbar_more_file_saveAll_success, Toast.LENGTH_LONG).show();
        });

        viewClick(b.importData, () -> {
            UUID id = null;
            if (itemsStorage instanceof ID) {
                ID i = (ID) itemsStorage;
                id = i.getId();
            }

            if (id == null) {
                Toast.makeText(activity, R.string.toolbar_more_file_import_unsupported, Toast.LENGTH_SHORT).show();
                return;
            }
            rootNavigationHost.navigate(ImportFragment.create(id), true);
        });

        toolbarMoreView.addView(b.getRoot());
    }

    private class TabHolder extends RecyclerView.ViewHolder {
        public TabHolder() {
            super(new TextView(activity));
            TextView textView = (TextView) itemView;
            textView.setTextSize(20);
            textView.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            p.setMargins(0, 5, 0, 5);
            textView.setBackgroundResource(R.drawable.shape);
            textView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#555555")));
            textView.setLayoutParams(p);
        }

        public void setText(String text) {
            StringBuilder s = new StringBuilder(" > ");
            s.append(text);
            ((TextView) itemView).setText(s);
        }
    }

    private void onTabsClick() {
        ToolbarMoreTabsBinding b = ToolbarMoreTabsBinding.inflate(activity.getLayoutInflater(), toolbarMoreView, false);


        b.tabsRecycleView.setAdapter(new RecyclerView.Adapter<TabHolder>() {
            @NonNull
            @Override
            public TabHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new TabHolder();
            }

            @Override
            public void onBindViewHolder(@NonNull TabHolder holder, int position) {
                Tab tab = itemManager.getTabs().get(position);
                String s = tab.getName();
                holder.setText(s);
                viewClick(holder.itemView, () -> {
                    EditText editText = new EditText(activity);
                    editText.setHint(R.string.toolbar_tabs_edit_name_hint);
                    editText.setText(tab.getName());

                    new AlertDialog.Builder(activity)
                            .setTitle(activity.getString(R.string.toolbar_tabs_edit_dialog_title, tab.getName()))
                            .setView(editText)
                            .setPositiveButton(R.string.toolbar_more_items_tab_rename, (dialog, which) -> {
                                String text = editText.getText().toString();
                                if (!text.trim().isEmpty()) {
                                    tab.setName(text);
                                } else {
                                    Toast.makeText(activity, R.string.tab_noEmptyName, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton(R.string.abc_cancel, null)
                            .setNeutralButton(R.string.toolbar_more_items_tab_delete, (dialog, w) -> new AlertDialog.Builder(activity)
                                    .setTitle(activity.getString(R.string.dialog_previewDeleteItems_delete_title, String.valueOf(tab.size())))
                                    .setNegativeButton(R.string.dialog_previewDeleteItems_delete_cancel, null)
                                    .setPositiveButton(R.string.dialog_previewDeleteItems_delete_apply, ((dialog1, which) -> {
                                        try {
                                            itemManager.deleteTab(tab);
                                        } catch (Exception e) {
                                            Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }))
                                    .show())
                            .setNegativeButton(R.string.abc_cancel, null)
                            .show();
                });
            }

            @Override
            public int getItemCount() {
                return itemManager.getTabs().size();
            }
        });


        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int positionFrom = viewHolder.getAdapterPosition();
                int positionTo = target.getAdapterPosition();

                recyclerView.getAdapter().notifyItemMoved(positionFrom, positionTo);
                lastTabReorder = System.currentTimeMillis();
                itemManager.moveTabs(positionFrom, positionTo);
                return true;
            }


            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        };

        onTabsChanged = tabs -> {
            if (System.currentTimeMillis() - lastTabReorder >= 1000) {
                b.tabsRecycleView.getAdapter().notifyDataSetChanged();
            }
            return Status.NONE;
        };
        itemManager.getOnTabsChanged().addCallback(CallbackImportance.DEFAULT, onTabsChanged);


        ItemTouchHelper t = new ItemTouchHelper(simpleCallback);
        t.attachToRecyclerView(b.tabsRecycleView);

        b.tabsRecycleView.setLayoutManager(new LinearLayoutManager(activity));


        viewClick(b.addTab, () -> {
            EditText editText = new EditText(activity);
            editText.setHint(R.string.toolbar_tabs_addNew_name_hint);
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.toolbar_tabs_addNew_dialog_title)
                    .setView(editText)
                    .setPositiveButton(R.string.toolbar_more_items_tab_add, (dialog, which) -> {
                        String text = editText.getText().toString();
                        if (!text.trim().isEmpty()) {
                            itemManager.createTab(editText.getText().toString());
                        } else {
                            Toast.makeText(activity, R.string.tab_noEmptyName, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.abc_cancel, null)
                    .show();
        });

        toolbarMoreView.addView(b.getRoot());
    }

    private static class H extends RecyclerView.ViewHolder {
        private final TextView name;
        private final Button create;
        private final Button add;

        public H(@NonNull View itemView, TextView name, Button create, Button add) {
            super(itemView);
            this.name = name;
            this.create = create;
            this.add = add;
        }
    }

    private void onItemsClick() {
        // Cache
        if (itemsSectionCacheView != null) {
            if (itemsSectionCacheView.getParent() != null) {
                ((ViewGroup)itemsSectionCacheView.getParent()).removeView(itemsSectionCacheView);
                toolbarMoreView.addView(itemsSectionCacheView);
                return;
            }
        }

        // Non-cache
        ToolbarMoreItemsBinding b = ToolbarMoreItemsBinding.inflate(activity.getLayoutInflater());

        b.items.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.VERTICAL, false));
        b.items.setAdapter(new RecyclerView.Adapter<H>() {
            @NonNull
            @Override
            public H onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ToolbarMoreItemsItemBinding b = ToolbarMoreItemsItemBinding.inflate(activity.getLayoutInflater(), parent, false);
                return new H(b.getRoot(), b.name, b.create, b.add);
            }

            @Override
            public void onBindViewHolder(@NonNull H holder, int position) {
                ItemsRegistry.ItemInfo itemInfo = ItemsRegistry.REGISTRY.getAllItems()[position];

                viewVisible(holder.itemView, itemInfo.isCompatibility(app.getFeatureFlags()), View.GONE);

                holder.name.setText(itemInfo.getNameResId());
                viewClick(holder.create, () -> rootNavigationHost.navigate(ItemEditorFragment.create(tab.getId(), itemInfo.getClassType()), true));
                viewClick(holder.add, () -> itemsStorage.addItem(ItemsRegistry.REGISTRY.get(itemInfo.getClassType()).create()));
            }

            @Override
            public int getItemCount() {
                return ItemsRegistry.REGISTRY.count();
            }
        });

        // Action: On click
        viewClick(b.changeOnClick, () -> new DialogSelectItemAction(activity, settingsManager.getItemOnClickAction(), itemOnClickAction -> {
            settingsManager.setItemOnClickAction(itemOnClickAction);
            settingsManager.save();
        }, activity.getString(R.string.toolbar_more_items_action_click)).show());
        // Action: On left swipe
        viewClick(b.changeOnLeftSwipe, () -> new DialogSelectItemAction(activity, settingsManager.getItemOnLeftAction(), itemOnLeftAction -> {
            settingsManager.setItemOnLeftAction(itemOnLeftAction);
            settingsManager.save();
        }, activity.getString(R.string.toolbar_more_items_action_leftSwipe)).show());

        // Cache view & show
        toolbarMoreView.addView(itemsSectionCacheView = b.getRoot());
    }

    private void onOpenTodayClick() {
        ToolbarMoreOpentodayBinding b = ToolbarMoreOpentodayBinding.inflate(activity.getLayoutInflater());

        Runnable showPersonalTickDebug = () -> {
            EditText view = new EditText(activity);
            new AlertDialog.Builder(activity)
                    .setView(view)
                    .setPositiveButton("TICK", (dfsd, fdsg) -> {
                        try {
                            UUID id = UUID.fromString(view.getText().toString());
                            activity.sendBroadcast(new Intent(activity, ItemsTickReceiver.class).putExtra(ItemsTickReceiver.EXTRA_PERSONAL_TICK, new String[]{id.toString()}).putExtra("debugMessage", "Debug personal tick is work!"));
                        } catch (Exception e) {
                            Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        };

        viewVisible(b.debugToggleDebugOverlayText, app.isFeatureFlag(FeatureFlag.AVAILABLE_LOGS_OVERLAY), View.GONE);
        viewVisible(b.debugPersonalTick, app.isFeatureFlag(FeatureFlag.AVAILABLE_UI_PERSONAL_TICK), View.GONE);
        viewVisible(b.debugRestartActivity, app.isFeatureFlag(FeatureFlag.AVAILABLE_RESTART_ACTIVITY), View.GONE);
        viewVisible(b.debugResetSetup, app.isFeatureFlag(FeatureFlag.AVAILABLE_RESET_SETUP), View.GONE);
        viewClick(b.debugToggleDebugOverlayText, () -> ((MainActivity) activity).toggleLogsOverlay());
        viewClick(b.debugPersonalTick, showPersonalTickDebug);
        viewClick(b.debugRestartActivity, () -> {
            activity.finish();
            Intent intent = new Intent(activity, activity.getClass());
            try {
                intent.replaceExtras(activity.getIntent().getExtras());
            } catch (Exception ignored) {}
            activity.startActivity(intent);
        });
        viewClick(b.debugResetSetup, () -> {
            activity.getSharedPreferences(App.SHARED_NAME, Context.MODE_PRIVATE).edit().putBoolean(App.SHARED_KEY_IS_SETUP_DONE, false).apply();
            activity.finish();
            activity.startActivity(new Intent(activity, SetupActivity.class));
        });
        viewClick(b.about, () -> rootNavigationHost.navigate(AboutFragment.create(), true));
        viewClick(b.settings, () -> rootNavigationHost.navigate(SettingsFragment.create(), true));
        viewClick(b.calendar, () -> new DatePickerDialog(activity)
                .show());

        toolbarMoreView.addView(b.getRoot());
    }

    private void onSelectionClick() {
        ToolbarMoreSelectionBinding b = ToolbarMoreSelectionBinding.inflate(activity.getLayoutInflater());
        if (itemManager.getSelections().length == 0) {
            b.empty.setVisibility(View.VISIBLE);
            b.notEmpty.setVisibility(View.GONE);
        }

        Runnable export = () -> {
            try {
                ImportWrapper.Builder builder = ImportWrapper.createImport(ImportWrapper.Permission.ADD_ITEMS_TO_CURRENT);
                for (Selection selection : itemManager.getSelections()) {
                    builder.addItem(selection.getItem());
                }
                ImportWrapper importWrapper = builder.build();
                ClipboardManager clipboardManager = activity.getSystemService(ClipboardManager.class);
                clipboardManager.setPrimaryClip(ClipData.newPlainText(activity.getString(R.string.toolbar_more_selection_export_clipdata_label), importWrapper.finalExport()));
                Toast.makeText(activity, R.string.toolbar_more_selection_export_success, Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Toast.makeText(activity, activity.getString(R.string.toolbar_more_selection_export_exception, e.toString()), Toast.LENGTH_SHORT).show();
            }
        };

        viewClick(b.exportSelected, export);
        
        viewLong(b.exportSelected, () -> {
            EditText dialogMessage = new EditText(activity);

            new AlertDialog.Builder(activity)
                    .setTitle(R.string.toolbar_more_selection_export_setMessage_title)
                    .setView(dialogMessage)
                    .setNeutralButton(R.string.toolbar_more_selection_export_setMessage_nomsg, (ignore0, ignore1) -> export.run())
                    .setPositiveButton(R.string.toolbar_more_selection_export_setMessage_export, (ignore2, ignore3) -> {
                        String msg = dialogMessage.getText().toString();

                        try {
                            ImportWrapper.Builder builder = ImportWrapper.createImport(ImportWrapper.Permission.ADD_ITEMS_TO_CURRENT, ImportWrapper.Permission.PRE_IMPORT_SHOW_DIALOG);
                            for (Selection selection : itemManager.getSelections()) {
                                builder.addItem(selection.getItem());
                            }
                            builder.setDialogMessage(msg);
                            ImportWrapper importWrapper = builder.build();
                            ClipboardManager clipboardManager = activity.getSystemService(ClipboardManager.class);
                            clipboardManager.setPrimaryClip(ClipData.newPlainText(activity.getString(R.string.toolbar_more_selection_export_clipdata_label), importWrapper.finalExport()));
                            Toast.makeText(activity, R.string.toolbar_more_selection_export_success, Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            Toast.makeText(activity, activity.getString(R.string.toolbar_more_selection_export_exception, e.toString()), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.toolbar_more_selection_export_setMessage_cancel, null)
                    .show();
        });

        // Deselect all
        viewClick(b.deselectAll, itemManager::deselectAll);

        // Move selected to this
        viewClick(b.moveSelectedHere, () -> {
            // If nothing selected
            if (itemManager.getSelections().length == 0) {
                Toast.makeText(activity, R.string.toolbar_more_selection_nothingSelected, Toast.LENGTH_SHORT).show();
                return;
            }

            for (Selection selection : itemManager.getSelections()) {
                selection.moveToStorage(itemsStorage);
                itemManager.deselectItem(selection);
            }
        });

        // copy
        viewClick(b.copySelectedHere, () -> {
            // If nothing selected
            if (itemManager.getSelections().length == 0) {
                Toast.makeText(activity, R.string.toolbar_more_selection_nothingSelected, Toast.LENGTH_SHORT).show();
                return;
            }

            for (Selection selection : itemManager.getSelections()) {
                selection.copyToStorage(itemsStorage);
                itemManager.deselectItem(selection);
            }
        });

        // Delete selected
        viewClick(b.delete, () -> {
            // If nothing selected
            if (itemManager.getSelections().length == 0) {
                Toast.makeText(activity, R.string.toolbar_more_selection_nothingSelected, Toast.LENGTH_SHORT).show();
                return;
            }

            List<Item> items = new ArrayList<>();
            for (Selection selection : itemManager.getSelections()) {
                items.add(selection.getItem());
            }

            // Show delete dialog
            rootNavigationHost.navigate(DeleteItemsFragment.create(items.toArray(new Item[0])), true);
        });

        viewClick(b.editSelected, () -> {
            if (itemManager.getSelections().length > 0) {
                Item item = itemManager.getSelections()[0].getItem();
                rootNavigationHost.navigate(ItemEditorFragment.edit(item.getId()), true);
            }
        });
        // Add selection listener
        onSelectionChanged = (selections) -> {
            b.selectedInfo.setText(activity.getString(R.string.toolbar_more_selection_info, String.valueOf(selections.size())));
            viewVisible(b.empty, selections.isEmpty(), View.GONE);
            viewVisible(b.notEmpty, !selections.isEmpty(), View.GONE);
            viewVisible(b.editSelected, selections.size() == 1, View.GONE);
        };
        onSelectionChanged.onSelectionChanged(Arrays.asList(itemManager.getSelections())); // First run
        itemManager.getOnSelectionUpdated().addCallback(CallbackImportance.MIN, onSelectionChanged); // Add to callbackStorage

        toolbarMoreView.addView(b.getRoot());
    }

    public void setItemStorage(ItemsStorage itemsStorage) {
        this.itemsStorage = itemsStorage;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public void setOnMoreVisibleChangedListener(OnMoreVisibleChanged l) {
        onMoreVisibleChangedListener = l;
    }

    public interface OnMoreVisibleChanged {
        void onChange(boolean visible);
    }
}

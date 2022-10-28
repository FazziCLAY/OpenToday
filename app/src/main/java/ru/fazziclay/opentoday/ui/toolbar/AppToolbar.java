package ru.fazziclay.opentoday.ui.toolbar;

import static ru.fazziclay.opentoday.util.InlineUtil.fcu_viewOnClick;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.ImportWrapper;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.ItemsStorage;
import ru.fazziclay.opentoday.app.items.callback.OnTabsChanged;
import ru.fazziclay.opentoday.app.items.item.ItemsRegistry;
import ru.fazziclay.opentoday.app.items.Selection;
import ru.fazziclay.opentoday.app.items.callback.OnSelectionChanged;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.tab.Tab;
import ru.fazziclay.opentoday.app.settings.SettingsManager;
import ru.fazziclay.opentoday.callback.CallbackImportance;
import ru.fazziclay.opentoday.callback.Status;
import ru.fazziclay.opentoday.databinding.DialogImportBinding;
import ru.fazziclay.opentoday.databinding.ToolbarBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreFileBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreItemsBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreItemsItemBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreOpentodayBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreSelectionBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreTabsBinding;
import ru.fazziclay.opentoday.ui.activity.MainActivity;
import ru.fazziclay.opentoday.ui.fragment.ItemEditorFragment;
import ru.fazziclay.opentoday.ui.fragment.ItemsTabIncludeFragment;
import ru.fazziclay.opentoday.ui.fragment.AboutFragment;
import ru.fazziclay.opentoday.ui.fragment.SettingsFragment;
import ru.fazziclay.opentoday.ui.dialog.DeleteItemsFragment;
import ru.fazziclay.opentoday.ui.dialog.DialogSelectItemAction;
import ru.fazziclay.opentoday.ui.interfaces.NavigationHost;
import ru.fazziclay.opentoday.util.NetworkUtil;
import ru.fazziclay.opentoday.util.ResUtil;
import ru.fazziclay.opentoday.util.SimpleSpinnerAdapter;

public class AppToolbar {
    private final Activity activity;
    private ToolbarBinding binding;
    private final LinearLayout toolbarView;
    private final LinearLayout toolbarMoreView;
    private boolean destroyed = false;
    private final ItemManager itemManager;
    private final SettingsManager settingsManager;
    private ItemsStorage itemsStorage; // For context toolbar work
    private Tab tab; // For context toolbar work
    private View currentToolbarButton = null; // Current active button. If none: null
    private OnSelectionChanged onSelectionChanged = null; // (Selection TAB) On selection changed. For runtime update selection information
    private OnTabsChanged onTabsChanged = null;

    // Cache
    private View itemsSectionCacheView = null;
    private OnMoreVisibleChanged onMoreVisibleChangedListener = null;
    private final NavigationHost rootNavigationHost;
    private final NavigationHost navigationHost;
    private long lastTabReorder;


    public AppToolbar(Activity activity, ItemManager itemManager, SettingsManager settingsManager, ItemsStorage itemsStorage, NavigationHost rootNavigationHost, ItemsTabIncludeFragment itemsTabIncludeFragment) {
        this.activity = activity;
        this.toolbarMoreView = new LinearLayout(activity);
        this.toolbarView = new LinearLayout(activity);
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

        fcu_viewOnClick(binding.toolbarFile, () -> {
            if (preOnClick(binding.toolbarFile)) onFileClick();
        });
        fcu_viewOnClick(binding.toolbarItems, () -> {
            if (preOnClick(binding.toolbarItems)) onItemsClick();
        });
        fcu_viewOnClick(binding.toolbarSelection, () -> {
            if (preOnClick(binding.toolbarSelection)) onSelectionClick();
        });
        fcu_viewOnClick(binding.toolbarOpentoday, () -> {
            if (preOnClick(binding.toolbarOpentoday)) onOpenTodayClick();
        });
        fcu_viewOnClick(binding.toolbarTabs, () -> {
            if (preOnClick(binding.toolbarTabs)) onTabsClick();
        });
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
        fcu_viewOnClick(b.saveAll, () -> {
            boolean success = itemManager.saveAllDirect();
            if (success) Toast.makeText(activity, R.string.toolbar_more_file_saveAll_success, Toast.LENGTH_LONG).show();
        });

        fcu_viewOnClick(b.importData, () -> {
            DialogImportBinding binding = DialogImportBinding.inflate(activity.getLayoutInflater());
            View dView = binding.getRoot();
            EditText editText = binding.editText;

            Runnable importRun = () -> {
                Dialog loading = new Dialog(activity);
                loading.getWindow().setBackgroundDrawable(null);
                loading.setCancelable(false);
                loading.setCanceledOnTouchOutside(false);
                ProgressBar progressBar = new ProgressBar(activity);
                progressBar.setIndeterminate(true);
                loading.setContentView(progressBar);
                loading.show();

                try {
                    String text = editText.getText().toString();
                    new Thread(() -> {
                        try {
                            String content;
                            if (text.startsWith("https://") || text.startsWith("http://")) {
                                content = NetworkUtil.parseTextPage(text);
                            } else {
                                content = text;
                            }

                            ImportWrapper i = ImportWrapper.finalImport(content);

                            for (Item item : i.getItems()) {
                                itemsStorage.addItem(item);
                                itemManager.selectItem(new Selection(itemsStorage, item));
                            }
                            activity.runOnUiThread(() -> Toast.makeText(activity, R.string.toolbar_more_file_import_success, Toast.LENGTH_SHORT).show());
                        } catch (Exception e) {
                            e.printStackTrace();
                            activity.runOnUiThread(() -> Toast.makeText(activity, activity.getString(R.string.toolbar_more_file_import_exception, e.toString()), Toast.LENGTH_SHORT).show());
                        }
                        activity.runOnUiThread(loading::cancel);
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(activity, activity.getString(R.string.toolbar_more_file_import_exception, e.toString()), Toast.LENGTH_SHORT).show();
                    loading.cancel();
                }
            };


            Dialog d = new Dialog(activity, android.R.style.ThemeOverlay_Material);
            d.setContentView(dView);
            d.show();

            binding.runImport.setOnClickListener(ig -> {
                importRun.run();
                d.cancel();
            });

            binding.cancel.setOnClickListener(ig -> {
                d.cancel();
            });
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
            ((TextView) itemView).setText(" > " + text);
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
                holder.itemView.setOnClickListener(v -> {
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
                            .setNeutralButton(R.string.toolbar_more_items_tab_delete, (dialog, w) -> {
                                new AlertDialog.Builder(activity)
                                        .setTitle(activity.getString(R.string.dialog_previewDeleteItems_delete_title, String.valueOf(tab.size())))
                                        .setNegativeButton(R.string.dialog_previewDeleteItems_delete_cancel, null)
                                        .setPositiveButton(R.string.dialog_previewDeleteItems_delete_apply, ((dialog1, which) -> {
                                            try {
                                                itemManager.deleteTab(tab);
                                            } catch (Exception e) {
                                                Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        }))
                                        .show();


                            })
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

        onTabsChanged = new OnTabsChanged() {
            @Override
            public Status run(Tab[] tabs) {
                if (System.currentTimeMillis() - lastTabReorder >= 1000) {
                    b.tabsRecycleView.getAdapter().notifyDataSetChanged();
                }
                return Status.NONE;
            }
        };
        itemManager.getOnTabsChanged().addCallback(CallbackImportance.DEFAULT, onTabsChanged);


        ItemTouchHelper t = new ItemTouchHelper(simpleCallback);
        t.attachToRecyclerView(b.tabsRecycleView);

        b.tabsRecycleView.setLayoutManager(new LinearLayoutManager(activity));


        b.addTab.setOnClickListener(v -> {
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

        // Item view
        SimpleSpinnerAdapter.ViewStyle<Class<? extends Item>> viewStyle = (string, value, convertView, parent) -> {
            if (convertView != null) return convertView;

            ToolbarMoreItemsItemBinding itemBinding = ToolbarMoreItemsItemBinding.inflate(activity.getLayoutInflater(), parent, false);
            itemBinding.name.setText(string);

            // Create button (+)
            fcu_viewOnClick(itemBinding.create, () -> {
                Item item = ItemsRegistry.REGISTRY.getItemInfoByClass(value).create();
                itemsStorage.addItem(item);
                UUID id = item.getId();
                if (id == null || tab == null) {
                    Toast.makeText(activity, activity.getString(R.string.toolbar_more_items_add_exception, "NULL! itemId=" + id + "; tab=" + tab), Toast.LENGTH_SHORT).show();
                    return;
                }

                rootNavigationHost.navigate(ItemEditorFragment.edit(tab.getId(), id), true);

                final boolean UNAVAILABLE_WARN = false;
                if (UNAVAILABLE_WARN) Toast.makeText(activity, R.string.temporarly_unavailable, Toast.LENGTH_SHORT).show();
            });
            // Add button (!)
            fcu_viewOnClick(itemBinding.add, () -> itemsStorage.addItem(ItemsRegistry.REGISTRY.getItemInfoByClass(value).create()));

            return itemBinding.getRoot();
        };
        // Add all items from REGISTRY
        for (ItemsRegistry.ItemInfo itemInfo : ItemsRegistry.REGISTRY.getAllItems()) {
            b.items.addView(viewStyle.create(activity.getString(itemInfo.getNameResId()), itemInfo.getClassType(), null, b.items));
        }

        // Action: On click
        fcu_viewOnClick(b.changeOnClick, new DialogSelectItemAction(activity, settingsManager.getItemOnClickAction(), itemOnClickAction -> {
            settingsManager.setItemOnClickAction(itemOnClickAction);
            settingsManager.save();
        }, activity.getString(R.string.toolbar_more_items_action_click))::show);
        // Action: On left swipe
        fcu_viewOnClick(b.changeOnLeftSwipe, () -> new DialogSelectItemAction(activity, settingsManager.getItemOnLeftAction(), itemOnLeftAction -> {
            settingsManager.setItemOnLeftAction(itemOnLeftAction);
            settingsManager.save();
        }, activity.getString(R.string.toolbar_more_items_action_leftSwipe)).show());

        // Cache view & show
        toolbarMoreView.addView(itemsSectionCacheView = b.getRoot());
    }

    private void onOpenTodayClick() {
        ToolbarMoreOpentodayBinding b = ToolbarMoreOpentodayBinding.inflate(activity.getLayoutInflater());

        b.debug.setVisibility(App.DEBUG ? View.VISIBLE : View.GONE);
        b.debugToggleDebugOverlayText.setOnClickListener(v -> {
            ((MainActivity) activity).toggleDebugOverLogs();
        });

        fcu_viewOnClick(b.about, () -> {
            rootNavigationHost.navigate(AboutFragment.create(), true);
        });
        fcu_viewOnClick(b.settings, () -> {
            rootNavigationHost.navigate(SettingsFragment.create(), true);
        });

        toolbarMoreView.addView(b.getRoot());
    }

    private void onSelectionClick() {
        ToolbarMoreSelectionBinding b = ToolbarMoreSelectionBinding.inflate(activity.getLayoutInflater());
        if (itemManager.getSelections().length == 0) {
            b.empty.setVisibility(View.VISIBLE);
            b.notEmpty.setVisibility(View.GONE);
        }

        fcu_viewOnClick(b.exportSelected, () -> {
            try {
                ImportWrapper.Builder builder = ImportWrapper.createImport();
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
        });

        // Deselect all
        fcu_viewOnClick(b.deselectAll, itemManager::deselectAll);

        // Move selected to this
        fcu_viewOnClick(b.moveSelectedHere, () -> {
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
        fcu_viewOnClick(b.copySelectedHere, () -> {
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
        fcu_viewOnClick(b.delete, () -> {
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
            new DeleteItemsFragment(activity, items.toArray(new Item[0])).show();
        });

        // Add selection listener
        onSelectionChanged = (selections) -> {
            b.selectedInfo.setText(activity.getString(R.string.toolbar_more_selection_info, String.valueOf(selections.size())));


            if (selections.isEmpty()) {
                b.empty.setVisibility(View.VISIBLE);
                b.notEmpty.setVisibility(View.GONE);
            } else {
                b.empty.setVisibility(View.GONE);
                b.notEmpty.setVisibility(View.VISIBLE);
            }
        };
        onSelectionChanged.run(Arrays.asList(itemManager.getSelections())); // First run
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

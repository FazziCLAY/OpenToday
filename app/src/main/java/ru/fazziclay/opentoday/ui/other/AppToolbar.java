package ru.fazziclay.opentoday.ui.other;

import static ru.fazziclay.opentoday.util.InlineUtil.fcu_viewOnClick;

import android.app.Activity;
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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.ContentLoadingProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.items.ImportWrapper;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.app.items.ItemsRegistry;
import ru.fazziclay.opentoday.app.items.Selection;
import ru.fazziclay.opentoday.app.items.callback.OnSelectionChanged;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.callback.CallbackImportance;
import ru.fazziclay.opentoday.databinding.ToolbarBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreFileBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreItemsBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreItemsItemBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreOpentodayBinding;
import ru.fazziclay.opentoday.databinding.ToolbarMoreSelectionBinding;
import ru.fazziclay.opentoday.ui.dialog.DialogAppAbout;
import ru.fazziclay.opentoday.ui.dialog.DialogAppSettings;
import ru.fazziclay.opentoday.ui.dialog.DialogDeleteItems;
import ru.fazziclay.opentoday.ui.dialog.DialogItem;
import ru.fazziclay.opentoday.ui.dialog.DialogSelectItemAction;
import ru.fazziclay.opentoday.util.NetworkUtil;
import ru.fazziclay.opentoday.util.ResUtil;
import ru.fazziclay.opentoday.util.SimpleSpinnerAdapter;

public class AppToolbar {
    private final Activity activity;
    private final ToolbarBinding binding;
    private final View toolbarView;
    private final LinearLayout toolbarMoreView;
    private boolean destroyed = false;
    private final ItemManager itemManager;
    private final ItemStorage itemStorage; // For context toolbar work
    private View currentToolbarButton = null; // Current active button. If none: null
    private OnSelectionChanged onSelectionChanged = null; // (Selection TAB) On selection changed. For runtime update selection information

    // Cache
    private View itemsSectionCacheView = null;


    public AppToolbar(Activity activity, ItemManager itemManager, ItemStorage itemStorage) {
        this.activity = activity;
        this.binding = ToolbarBinding.inflate(activity.getLayoutInflater());
        this.toolbarMoreView = new LinearLayout(activity);
        this.itemManager = itemManager;
        this.itemStorage = itemStorage;
        this.toolbarMoreView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.toolbarMoreView.setOrientation(LinearLayout.VERTICAL);
        this.toolbarMoreView.setClickable(false);
        this.toolbarView = binding.getRoot();
    }

    public void create() {
        fcu_viewOnClick(binding.selection, () -> {
            if (preOnClick(binding.selection)) onSelectionClick();
        });
        fcu_viewOnClick(binding.opentoday, () -> {
            if (preOnClick(binding.opentoday)) onOpenTodayClick();
        });
        fcu_viewOnClick(binding.items, () -> {
            if (preOnClick(binding.items)) onItemsClick();
        });
        fcu_viewOnClick(binding.file, () -> {
            if (preOnClick(binding.file)) onFileClick();
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
    }

    private boolean preOnClick(View buttonView) {
        resetMoreView();
        if (currentToolbarButton == buttonView) {
            currentToolbarButton = null;
            return false;
        } else {
            currentToolbarButton = buttonView;
            backgroundTintFromStyle(R.style.Theme_OpenToday_Toolbar_Button_Selected, currentToolbarButton);
            return true;
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
            EditText editText = new EditText(activity);

            new AlertDialog.Builder(activity)
                    .setView(editText)
                    .setMessage(R.string.toolbar_more_file_import_hint)
                    .setPositiveButton(R.string.toolbar_more_file_import_import, (ignore123213, ignore342143) -> {
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
                                        itemStorage.addItem(item);
                                        itemManager.selectItem(new Selection(itemStorage, item));
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
                    })
                    .setNegativeButton(R.string.toolbar_more_file_import_cancel, null)
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
                DialogItem dialogItem = new DialogItem(activity, itemManager);
                dialogItem.create(value, itemStorage::addItem);
            });
            // Add button (!)
            fcu_viewOnClick(itemBinding.add, () -> itemStorage.addItem(ItemsRegistry.REGISTRY.getItemInfoByClass(value).create()));

            return itemBinding.getRoot();
        };
        // Add all items from REGISTRY
        for (ItemsRegistry.ItemInfo itemInfo : ItemsRegistry.REGISTRY.getAllItems()) {
            b.items.addView(viewStyle.create(activity.getString(itemInfo.getNameResId()), itemInfo.getClassType(), null, b.items));
        }

        // Action: On click
        fcu_viewOnClick(b.changeOnClick, () -> new DialogSelectItemAction(activity, itemManager.getItemOnClickAction(), itemManager::setItemOnClickAction, activity.getString(R.string.toolbar_more_items_action_click)).show());
        // Action: On left swipe
        fcu_viewOnClick(b.changeOnLeftSwipe, () -> new DialogSelectItemAction(activity, itemManager.getItemOnLeftAction(), itemManager::setItemOnLeftAction, activity.getString(R.string.toolbar_more_items_action_leftSwipe)).show());

        // Cache view & show
        toolbarMoreView.addView(itemsSectionCacheView = b.getRoot());
    }

    private void onOpenTodayClick() {
        ToolbarMoreOpentodayBinding b = ToolbarMoreOpentodayBinding.inflate(activity.getLayoutInflater());

        fcu_viewOnClick(b.about, () -> new DialogAppAbout(activity).show());
        fcu_viewOnClick(b.settings, () -> new DialogAppSettings(activity).show());

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
        fcu_viewOnClick(b.moveSelectedToRoot, () -> {
            // If nothing selected
            if (itemManager.getSelections().length == 0) {
                Toast.makeText(activity, R.string.toolbar_more_selection_nothingSelected, Toast.LENGTH_SHORT).show();
                return;
            }

            for (Selection selection : itemManager.getSelections()) {
                selection.moveToStorage(itemStorage);
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
            new DialogDeleteItems(activity, items.toArray(new Item[0])).show();
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
}

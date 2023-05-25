package com.fazziclay.opentoday.gui.item;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.SettingsManager;
import com.fazziclay.opentoday.app.items.ItemManager;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.callback.SelectionCallback;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.items.item.Transform;
import com.fazziclay.opentoday.app.items.selection.Selection;
import com.fazziclay.opentoday.app.items.selection.SelectionManager;
import com.fazziclay.opentoday.gui.dialog.DialogSelectItemType;
import com.fazziclay.opentoday.gui.dialog.DialogTextItemEditText;
import com.fazziclay.opentoday.gui.fragment.ItemEditorFragment;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.gui.interfaces.StorageEditsActions;
import com.fazziclay.opentoday.util.ResUtil;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;

import java.util.ArrayList;
import java.util.List;

public class ItemStorageDrawer {
    private static final String TAG = "ItemStorageDrawer";
    private final Activity activity;
    private final ItemManager itemManager;
    private final SettingsManager settingsManager;
    private final SelectionManager selectionManager;
    private final ItemsStorage itemsStorage;
    private final RecyclerView view;
    private StorageEditsActions storageEdits;
    private RecyclerView.Adapter<ItemViewHolder> adapter;
    private ItemViewGenerator itemViewGenerator;
    private final Thread originalThread;

    private boolean destroyed = false;
    private boolean created = false;

    private final List<Selection> visibleSelections = new ArrayList<>();
    private final OnItemsStorageUpdate onItemsStorageUpdate = new DrawerOnItemsStorageUpdated();
    private final SelectionCallback selectionCallback = new SelectionCallback() {
        @Override
        public void onSelectionChanged(List<Selection> selections) {
            activity.runOnUiThread(() -> runInternal(selections));
        }

        private void runInternal(List<Selection> selections) {
            List<Selection> toUpdate = new ArrayList<>();

            for (Selection visibleSelection : visibleSelections) {
                boolean contain = false;
                for (Selection selection : selections) {
                    if (visibleSelection.getItem() == selection.getItem()) {
                        contain = true;
                        break;
                    }
                }
                if (!contain) {
                    toUpdate.add(visibleSelection);
                }
            }

            for (Selection selection : selections) {
                boolean contain = false;
                for (Selection visibleSelection : visibleSelections) {
                    if (visibleSelection.getItem() == selection.getItem()) {
                        contain = true;
                        break;
                    }
                }
                if (!contain) {
                    toUpdate.add(selection);
                }
            }

            for (Selection selection : toUpdate) {
                int pos = itemsStorage.getItemPosition(selection.getItem());
                Runnable updateRunnable = () -> adapter.notifyItemChanged(pos);
                if (Thread.currentThread() == originalThread) {
                    updateRunnable.run();
                } else {
                    activity.runOnUiThread(updateRunnable);
                }
            }
            visibleSelections.clear();
            visibleSelections.addAll(selections);
        }
    };

    private final ItemInterface itemOnClick;
    private final boolean previewMode;
    private ItemViewWrapper itemViewWrapper = null;
    private final ItemInterface onItemEditor;

    // Public
    public ItemStorageDrawer(@NonNull Activity activity, @NonNull ItemManager itemManager, SettingsManager settingsManager, SelectionManager selectionManager, ItemsStorage itemsStorage, ItemInterface itemOnClick, @NonNull ItemInterface onItemEditor, boolean previewMode, StorageEditsActions storageEdits) {
        this.activity = activity;
        this.onItemEditor = onItemEditor;
        this.itemManager = itemManager;
        this.settingsManager = settingsManager;
        this.selectionManager = selectionManager;
        this.itemsStorage = itemsStorage;
        this.originalThread = Thread.currentThread();
        this.view = new RecyclerView(activity);
        this.itemOnClick = itemOnClick;
        this.previewMode = previewMode;
        this.view.setLayoutManager(new LinearLayoutManager(activity));
        this.view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        this.itemViewGenerator = new ItemViewGenerator(this.activity, this.itemManager, settingsManager, selectionManager, previewMode, (item) -> {
            if (this.itemOnClick == null) {
                if (!previewMode) actionItem(item, settingsManager.getItemOnClickAction());
            } else {
                this.itemOnClick.run(item);
            }
        }, onItemEditor, storageEdits);

    }

    public static CreateBuilder builder(Activity activity, ItemManager itemManager, SettingsManager settingsManager, SelectionManager selectionManager, ItemsStorage itemsStorage) {
        return new CreateBuilder(activity, itemManager, settingsManager, selectionManager, itemsStorage);
    }

    public void create() {
        if (destroyed) {
            throw new RuntimeException("ItemStorageDrawer destroyed!");
        }
        if (created) {
            throw new RuntimeException("ItemStorageDrawer created!");
        }
        this.created = true;
        this.adapter = new DrawerAdapter();
        this.view.setAdapter(adapter);
        this.itemsStorage.getOnUpdateCallbacks().addCallback(CallbackImportance.DEFAULT, onItemsStorageUpdate);
        this.selectionManager.getOnSelectionUpdated().addCallback(CallbackImportance.DEFAULT, selectionCallback);


        if (!previewMode) new ItemTouchHelper(new DrawerTouchCallback()).attachToRecyclerView(view);
    }

    public void destroy() {
        if (!created) {
            throw new RuntimeException("ItemStorageDrawer no created!");
        }
        if (destroyed) {
            throw new RuntimeException("ItemStorageDrawer destroyed!");
        }
        destroyed = true;
        this.itemsStorage.getOnUpdateCallbacks().deleteCallback(onItemsStorageUpdate);
        this.selectionManager.getOnSelectionUpdated().deleteCallback(selectionCallback);
        this.view.setAdapter(null);
        this.itemViewGenerator = null;
        this.adapter = null;
    }

    public View getView() {
        return this.view;
    }

    public void setItemViewWrapper(ItemViewWrapper itemViewWrapper) {
        this.itemViewWrapper = itemViewWrapper;
    }

    public void updateItemAt(int position) {
        adapter.notifyItemChanged(position);
    }

    public void _updateAllItems() {
        adapter.notifyDataSetChanged();
    }

    // Private
    private class DrawerOnItemsStorageUpdated extends OnItemsStorageUpdate {
        @Override
        public Status onAdded(Item item, int pos) {
            rou(() -> adapter.notifyItemInserted(pos));
            return Status.NONE;
        }

        @Override
        public Status onPreDeleted(Item item, int pos) {
            rou(() -> adapter.notifyItemRemoved(pos));
            return Status.NONE;
        }

        @Override
        public Status onMoved(Item item, int from, int to) {
            rou(() -> adapter.notifyItemMoved(from, to));
            return Status.NONE;
        }

        @Override
        public Status onUpdated(Item item, int pos) {
            rou(() -> adapter.notifyItemChanged(pos));
            return Status.NONE;
        }

        private int getItemPos(Item item) {
            return ItemStorageDrawer.this.itemsStorage.getItemPosition(item);
        }

        private void rou(Runnable runnable) {
            activity.runOnUiThread(runnable);
        }
    }

    private View generateViewForItem(Item item) {
        return itemViewGenerator.generate(item, view);
    }

    private class DrawerAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ItemViewHolder(activity);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            Item item = itemsStorage.getAllItems()[position];
            View view = generateViewForItem(item);

            holder.layout.removeAllViews();
            holder.layout.addView((itemViewWrapper != null) ? itemViewWrapper.wrap(item, view) : view);

            if (selectionManager.isSelected(item)) {
                holder.layout.setForeground(new ColorDrawable(ResUtil.getAttrColor(activity, R.attr.item_selectionForegroundColor)));
            } else {
                holder.layout.setForeground(null);
            }
        }

        @Override
        public int getItemCount() {
            return itemsStorage.getAllItems().length;
        }
    }

    private class DrawerTouchCallback extends ItemTouchHelper.SimpleCallback {
        private static final int DRAG_DIRS = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        private static final int SWIPE_DIRS = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.START | ItemTouchHelper.END;

        public DrawerTouchCallback() {
            super(DRAG_DIRS, SWIPE_DIRS);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int positionFrom = viewHolder.getAdapterPosition();
            int positionTo = target.getAdapterPosition();

            //! NOTE: Adapter receive notify signal from callbacks!
            //ItemUIDrawer.this.adapter.notifyItemMoved(positionFrom, positionTo);
            ItemStorageDrawer.this.itemsStorage.move(positionFrom, positionTo);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.LEFT) {
                int positionFrom = viewHolder.getAdapterPosition();
                Item item = ItemStorageDrawer.this.itemsStorage.getAllItems()[positionFrom];
                item.visibleChanged();
                actionItem(item, settingsManager.getItemOnLeftAction());

            } else if (direction == ItemTouchHelper.RIGHT) {
                int position = viewHolder.getAdapterPosition();
                Item item = ItemStorageDrawer.this.itemsStorage.getAllItems()[position];
                item.visibleChanged();
                ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
                showRightMenu(item, itemViewHolder.itemView);
            }
        }
    }

    private void actionItem(Item item, SettingsManager.ItemAction action) {
        switch (action) {
            case OPEN_EDITOR:
                onItemEditor.run(item);
                break;

            case SELECT_ON:
                selectionManager.selectItem(item);
                item.visibleChanged();
                break;

            case SELECT_OFF:
                selectionManager.deselectItem(item);
                item.visibleChanged();
                break;

            case MINIMIZE_REVERT:
                item.setMinimize(!item.isMinimize());
                item.visibleChanged();
                item.save();
                break;

            case MINIMIZE_OFF:
                item.setMinimize(false);
                item.visibleChanged();
                item.save();
                break;


            case MINIMIZE_ON:
                item.setMinimize(true);
                item.visibleChanged();
                item.save();
                break;

            case SELECT_REVERT:
                if (selectionManager.isSelected(item)) {
                    selectionManager.deselectItem(item);
                } else {
                    selectionManager.selectItem(item);
                }
                item.visibleChanged();
                break;

            case DELETE_REQUEST:
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.dialogItem_delete_title)
                        .setNegativeButton(R.string.dialogItem_delete_cancel, null)
                        .setPositiveButton(R.string.dialogItem_delete_apply, ((dialog1, which) -> item.delete()))
                        .show();

        }
    }

    private void showRightMenu(Item item, View itemView) {
        App app = App.get(activity);
        ItemManager itemManager = app.getItemManager();
        PopupMenu menu = new PopupMenu(activity, itemView);
        menu.setForceShowIcon(true);
        menu.inflate(R.menu.menu_item);
        menu.getMenu().findItem(R.id.minimize).setChecked(item.isMinimize());
        menu.getMenu().findItem(R.id.selected).setChecked(selectionManager.isSelected(item));
        menu.getMenu().setGroupEnabled(R.id.textItem, item instanceof TextItem);
        if (item instanceof TextItem) {
            TextItem textItem = (TextItem) item;
            menu.getMenu().findItem(R.id.textItem_clickableUrls).setChecked(textItem.isClickableUrls());
        }
        menu.getMenu().findItem(R.id.transform).setVisible(true);
        menu.setOnMenuItemClickListener(menuItem -> {
            boolean save = false;
            SettingsManager.ItemAction itemAction = null;
            switch (menuItem.getItemId()) {
                case R.id.delete:
                    ItemEditorFragment.deleteRequest(activity, item, null);
                    break;

                case R.id.edit:
                    itemAction = SettingsManager.ItemAction.OPEN_EDITOR;
                    break;

                case R.id.minimize:
                    itemAction = SettingsManager.ItemAction.MINIMIZE_REVERT;
                    break;

                case R.id.selected:
                    itemAction = SettingsManager.ItemAction.SELECT_REVERT;
                    break;

                case R.id.copy:
                    try {
                        Item copyItem = itemsStorage.copyItem(item);
                        onItemEditor.run(copyItem);
                    } catch (Exception e) {
                        Toast.makeText(activity, activity.getString(R.string.menuItem_copy_exception, e.toString()), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;

                case R.id.textItem_clickableUrls:
                    if (item instanceof TextItem) {
                        TextItem textItem = (TextItem) item;
                        textItem.setClickableUrls(!textItem.isClickableUrls());
                        save = true;
                    }
                    break;

                case R.id.textItem_editText:
                    if (item instanceof TextItem) {
                        TextItem textItem = (TextItem) item;
                        DialogTextItemEditText d = new DialogTextItemEditText(activity, textItem);
                        d.show();
                    }
                    break;

                case R.id.transform:
                    new DialogSelectItemType(activity, type -> {
                        Transform.Result result = Transform.transform(item, type);
                        if (result.isAllow()) {
                            int pos = itemsStorage.getItemPosition(item);
                            itemsStorage.addItem(result.getResult(), pos + 1);

                        } else {
                            Toast.makeText(activity, R.string.transform_not_allowed, Toast.LENGTH_SHORT).show();
                        }
                    }).setTitle(activity.getString(R.string.transform_selectTypeDialog_title))
                            .setMessage(activity.getString(R.string.transform_selectTypeDialog_message)).show();
                    break;
            }

            if (itemAction != null) actionItem(item, itemAction);
            if (save) item.save();
            item.visibleChanged();
            return true;
        });
        menu.setGravity(Gravity.END);
        menu.show();
    }

    @FunctionalInterface
    public interface ItemViewWrapper {
        View wrap(Item item, View view);
    }

    public static class CreateBuilder {
        private final Activity activity;
        private final ItemManager itemManager;
        private final SettingsManager settingsManager;
        private SelectionManager selectionManager;
        private final ItemsStorage itemsStorage;
        private boolean previewMode = false;
        private ItemInterface onItemClick = null;
        private ItemInterface onItemOpenEditor = null;
        private StorageEditsActions storageEditsAction = null;

        public CreateBuilder(Activity activity, ItemManager itemManager, SettingsManager settingsManager, SelectionManager selectionManager, ItemsStorage itemsStorage) {
            this.activity = activity;
            this.itemManager = itemManager;
            this.settingsManager = settingsManager;
            this.selectionManager = selectionManager;
            this.itemsStorage = itemsStorage;
        }

        public CreateBuilder setPreviewMode() {
            this.previewMode = true;
            return this;
        }

        public CreateBuilder setOnItemClick(ItemInterface i) {
            this.onItemClick = i;
            return this;
        }


        public CreateBuilder setOnItemOpenEditor(ItemInterface i) {
            this.onItemOpenEditor = i;
            return this;
        }


        public CreateBuilder setStorageEditsActions(StorageEditsActions i) {
            this.storageEditsAction = i;
            return this;
        }

        public ItemStorageDrawer build() {
            return new ItemStorageDrawer(activity, itemManager, settingsManager, selectionManager, itemsStorage, onItemClick, onItemOpenEditor, previewMode, storageEditsAction);
        }
    }
}

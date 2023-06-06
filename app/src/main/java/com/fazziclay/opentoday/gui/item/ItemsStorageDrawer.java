package com.fazziclay.opentoday.gui.item;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Looper;
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
import com.fazziclay.opentoday.app.SettingsManager;
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
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;

public class ItemsStorageDrawer {
    private static final String TAG = "ItemStorageDrawer";
    private final Activity activity;
    private final ItemViewGenerator itemViewGenerator;
    private final ItemViewGeneratorBehavior itemViewGeneratorBehavior;
    private final SelectionManager selectionManager;
    private final ItemsStorage itemsStorage;
    private final RecyclerView view;
    private RecyclerView.Adapter<ItemViewHolder> adapter;
    private final Thread originalThread; // always is main(UI) thread.

    private boolean destroyed = false;
    private boolean created = false;
    private final OnItemsStorageUpdate onItemsStorageUpdate = new DrawerOnItemsStorageUpdated();
    private final SelectionCallback selectionCallback = new DrawerSelectionCallback();
    private final ItemInterface itemOnClick;
    private final boolean previewMode;
    private ItemViewWrapper itemViewWrapper = null;
    private final ItemInterface onItemEditor;

    // Public
    public ItemsStorageDrawer(@NonNull Activity activity, ItemViewGeneratorBehavior itemViewGeneratorBehavior, SelectionManager selectionManager, ItemsStorage itemsStorage, ItemInterface itemOnClick, @NonNull ItemInterface onItemEditor, boolean previewMode) {
        this.originalThread = Thread.currentThread();
        if (this.originalThread != Looper.getMainLooper().getThread()) {
            throw new RuntimeException("Creating an ItemsStorageDrawer object is allowed only in the main thread");
        }

        this.activity = activity;
        this.itemViewGeneratorBehavior = itemViewGeneratorBehavior;
        this.selectionManager = selectionManager;
        this.itemsStorage = itemsStorage;
        this.onItemEditor = onItemEditor;
        this.view = new RecyclerView(activity);
        this.itemOnClick = itemOnClick;
        this.previewMode = previewMode;
        this.view.setLayoutManager(new LinearLayoutManager(activity));
        this.view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        this.itemViewGenerator = new ItemViewGenerator(this.activity, itemViewGeneratorBehavior, previewMode, this::onItemClick);
    }


    public static CreateBuilder builder(Activity activity, ItemViewGeneratorBehavior itemViewGeneratorBehavior, SelectionManager selectionManager, ItemsStorage itemsStorage) {
        return new CreateBuilder(activity, itemViewGeneratorBehavior, selectionManager, itemsStorage);
    }

    public void create() {
        throwIsBadThread();
        if (destroyed) {
            throw new RuntimeException("ItemsStorageDrawer already destroyed!");
        }
        if (created) {
            throw new RuntimeException("ItemsStorageDrawer already created!");
        }
        this.created = true;
        this.adapter = new DrawerAdapter();
        this.view.setAdapter(adapter);
        this.itemsStorage.getOnItemsStorageCallbacks().addCallback(CallbackImportance.DEFAULT, onItemsStorageUpdate);
        this.selectionManager.getOnSelectionUpdated().addCallback(CallbackImportance.DEFAULT, selectionCallback);
        this.itemViewGenerator.create();

        if (!previewMode) new ItemTouchHelper(new DrawerTouchCallback()).attachToRecyclerView(view);
    }

    public void destroy() {
        if (!created) {
            throw new RuntimeException("ItemsStorageDrawer no created!");
        }
        if (destroyed) {
            throw new RuntimeException("ItemsStorageDrawer already destroyed!");
        }
        destroyed = true;
        this.itemsStorage.getOnItemsStorageCallbacks().removeCallback(onItemsStorageUpdate);
        this.selectionManager.getOnSelectionUpdated().removeCallback(selectionCallback);
        this.view.setAdapter(null);
        this.itemViewGenerator.destroy();
        this.adapter = null;
    }

    private void onItemClick(Item item) {
        if (this.itemOnClick != null) {
            this.itemOnClick.run(item);
            return;
        }
        if (!previewMode) {
            actionItem(item, itemViewGeneratorBehavior.getItemOnClickAction());
        }
    }

    private void throwIsBadThread() {
        if (Thread.currentThread() != originalThread) throw new RuntimeException("Access from non-original thread.");
    }

    private void runOnUiThread(Runnable r) {
        if (Thread.currentThread() == originalThread) {
            r.run();
        } else {
            activity.runOnUiThread(r);
        }
    }

    public View getView() {
        return this.view;
    }

    public void setItemViewWrapper(ItemViewWrapper itemViewWrapper) {
        this.itemViewWrapper = itemViewWrapper;
    }

    // Private
    private class DrawerOnItemsStorageUpdated extends OnItemsStorageUpdate {
        @Override
        public Status onAdded(Item item, int pos) {
            runOnUiThread(() -> {
                runAdapter((adapter) -> adapter.notifyItemInserted(pos));
                if (itemViewGeneratorBehavior.isScrollToAddedItem()) view.smoothScrollToPosition(pos);
            });
            return Status.NONE;
        }

        @Override
        public Status onPreDeleted(Item item, int pos) {
            runOnUiThread(() -> runAdapter((adapter) -> adapter.notifyItemRemoved(pos)));
            return Status.NONE;
        }

        @Override
        public Status onMoved(Item item, int from, int to) {
            runOnUiThread(() -> runAdapter((adapter) -> adapter.notifyItemMoved(from, to)));
            return Status.NONE;
        }

        @Override
        public Status onUpdated(Item item, int pos) {
            runOnUiThread(() -> runAdapter(adapter -> adapter.notifyItemChanged(pos)));
            return Status.NONE;
        }
    }

    /**
     * Run AdapterInterface if adapter not null
     */
    private void runAdapter(AdapterInterface i) {
        if (adapter != null) i.run(adapter);
    }
    
    private View generateViewForItem(Item item) {
        boolean previewMode = this.previewMode || selectionManager.isSelected(item);
        return itemViewGenerator.generate(item, view, previewMode);
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
            ItemsStorageDrawer.this.itemsStorage.move(positionFrom, positionTo);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            final Item item = ItemsStorageDrawer.this.itemsStorage.getAllItems()[position];
            runAdapter(adapter1 -> adapter1.notifyItemChanged(position));

            if (direction == ItemTouchHelper.LEFT) {
                actionItem(item, itemViewGeneratorBehavior.getItemOnLeftAction());

            } else if (direction == ItemTouchHelper.RIGHT) {
                ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
                showRightMenu(item, itemViewHolder.layout.getChildAt(0));
            }
        }
    }

    private void actionItem(Item item, SettingsManager.ItemAction action) {
        switch (action) {
            case OPEN_EDITOR -> onItemEditor.run(item);
            case SELECT_ON -> {
                selectionManager.selectItem(item);
                item.visibleChanged();
            }
            case SELECT_OFF -> {
                selectionManager.deselectItem(item);
                item.visibleChanged();
            }
            case MINIMIZE_REVERT -> {
                item.setMinimize(!item.isMinimize());
                item.visibleChanged();
                item.save();
            }
            case MINIMIZE_OFF -> {
                item.setMinimize(false);
                item.visibleChanged();
                item.save();
            }
            case MINIMIZE_ON -> {
                item.setMinimize(true);
                item.visibleChanged();
                item.save();
            }
            case SELECT_REVERT -> {
                if (selectionManager.isSelected(item)) {
                    selectionManager.deselectItem(item);
                } else {
                    selectionManager.selectItem(item);
                }
                item.visibleChanged();
            }
            case DELETE_REQUEST -> new AlertDialog.Builder(activity)
                    .setTitle(R.string.dialogItem_delete_title)
                    .setNegativeButton(R.string.dialogItem_delete_cancel, null)
                    .setPositiveButton(R.string.dialogItem_delete_apply, ((dialog1, which) -> item.delete()))
                    .show();
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void showRightMenu(Item item, View itemView) {
        if (itemView == null) {
            Logger.w(TAG, "showRightMenu view is null...");
        }
        if (!item.isAttached()) {
            throw new RuntimeException("Item is not attached");
        }
        PopupMenu menu = new PopupMenu(activity, itemView);
        menu.setForceShowIcon(true);
        menu.inflate(R.menu.menu_item);
        menu.getMenu().findItem(R.id.minimize).setChecked(item.isMinimize());
        menu.getMenu().findItem(R.id.selected).setChecked(selectionManager.isSelected(item));
        if (item instanceof TextItem textItem) {
            menu.getMenu().findItem(R.id.textItem_clickableUrls).setChecked(textItem.isClickableUrls());
        }
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
                        Logger.e(TAG, "Copy error", e);
                        Toast.makeText(activity, activity.getString(R.string.menuItem_copy_exception, e.toString()), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;

                case R.id.textItem_clickableUrls:
                    if (item instanceof TextItem textItem) {
                        textItem.setClickableUrls(!textItem.isClickableUrls());
                        save = true;
                    }
                    break;

                case R.id.textItem_editText:
                    if (item instanceof TextItem textItem) {
                        DialogTextItemEditText d = new DialogTextItemEditText(activity, textItem);
                        d.show();
                    }
                    break;

                case R.id.transform:
                    new DialogSelectItemType(activity, type -> {
                        Transform.Result result = Transform.transform(item, type);
                        if (result.isAllow()) {
                            int pos = itemsStorage.getItemPosition(item);
                            itemsStorage.addItem(result.generate(), pos + 1);

                        } else {
                            Toast.makeText(activity, R.string.transform_not_allowed, Toast.LENGTH_SHORT).show();
                        }
                    }, (type -> Transform.isAllow(item, type)))
                            .setTitle(activity.getString(R.string.transform_selectTypeDialog_title))
                            .setMessage(activity.getString(R.string.transform_selectTypeDialog_message))
                            .show();
                    break;
            }

            if (itemAction != null) actionItem(item, itemAction);
            if (save) item.save();
            runAdapter(adapter1 -> adapter1.notifyItemChanged(itemsStorage.getItemPosition(item)));
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
        private final ItemViewGeneratorBehavior viewGeneratorBehavior;
        private final SelectionManager selectionManager;
        private final ItemsStorage itemsStorage;
        private boolean previewMode = false;
        private ItemInterface onItemClick = null;
        private ItemInterface onItemOpenEditor = null;

        public CreateBuilder(Activity activity, ItemViewGeneratorBehavior viewGeneratorBehavior, SelectionManager selectionManager, ItemsStorage itemsStorage) {
            this.activity = activity;
            this.viewGeneratorBehavior = viewGeneratorBehavior;
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

        public ItemsStorageDrawer build() {
            return new ItemsStorageDrawer(activity, viewGeneratorBehavior, selectionManager, itemsStorage, onItemClick, onItemOpenEditor, previewMode);
        }
    }

    private interface AdapterInterface {
        void run(RecyclerView.Adapter<ItemViewHolder> adapter);
    }

    private class DrawerSelectionCallback extends SelectionCallback {
        @Override
        public Status selected(Selection selection) {
            if (selection.getItem().getParentItemsStorage() == itemsStorage) {
                int pos = itemsStorage.getItemPosition(selection.getItem());
                adapter.notifyItemChanged(pos);
            }

            return Status.NONE;
        }

        @Override
        public Status unselected(Selection selection) {
            if (selection.getItem().getParentItemsStorage() == itemsStorage) {
                int pos = itemsStorage.getItemPosition(selection.getItem());
                adapter.notifyItemChanged(pos);
            }

            return Status.NONE;
        }

        @Override
        public Status unselectedAll() {
            runAdapter(RecyclerView.Adapter::notifyDataSetChanged);
            return Status.NONE;
        }
    }
}

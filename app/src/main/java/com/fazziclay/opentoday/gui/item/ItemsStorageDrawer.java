package com.fazziclay.opentoday.gui.item;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.CrashReportContext;
import com.fazziclay.opentoday.app.settings.enums.ItemAction;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.callback.SelectionCallback;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.items.item.Transform;
import com.fazziclay.opentoday.app.items.selection.Selection;
import com.fazziclay.opentoday.app.items.selection.SelectionManager;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.dialog.DialogSelectItemAction;
import com.fazziclay.opentoday.gui.dialog.DialogSelectItemType;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;

import java.util.function.Supplier;

/**
 * Drawer of ItemsStorage
 */
public class ItemsStorageDrawer extends AbstractItemsStorageDrawer {
    private static final String TAG = "ItemStorageDrawer";


    private final Activity activity;
    private final ItemViewGenerator itemViewGenerator;
    private final ItemViewGeneratorBehavior itemViewGeneratorBehavior;
    private final ItemsStorageDrawerBehavior behavior;
    private final SelectionManager selectionManager;
    private final ItemsStorage itemsStorage;
    private final Thread originalThread; // always is main(UI) thread.


    private final OnItemsStorageUpdate onItemsStorageUpdate = new DrawerOnItemsStorageUpdated();
    private final SelectionCallback selectionCallback = new DrawerSelectionCallback();
    private final ItemInterface itemOnClick;
    private final boolean previewMode;
    private ItemViewWrapper itemViewWrapper;
    private final Handler handler;

    // Private (available only with builder)
    private ItemsStorageDrawer(@NonNull Activity activity,
                               RecyclerView view,
                               boolean isDragsEnable,
                               boolean isSwipesEnable,

                               ItemsStorageDrawerBehavior itemsStorageDrawerBehavior,
                               ItemViewGeneratorBehavior itemViewGeneratorBehavior,

                               SelectionManager selectionManager,
                               ItemsStorage itemsStorage,
                               @Nullable ItemInterface itemOnClick,
                               boolean previewMode,
                               @Nullable ItemViewWrapper itemViewWrapper) {
        super(activity, view, isDragsEnable && !previewMode, isSwipesEnable && !previewMode);
        this.originalThread = Thread.currentThread();
        if (this.originalThread != Looper.getMainLooper().getThread()) {
            throw new RuntimeException("Creating an ItemsStorageDrawer object is allowed only in the main thread");
        }
        this.activity = activity;
        this.behavior = itemsStorageDrawerBehavior;
        this.selectionManager = selectionManager;
        this.itemsStorage = itemsStorage;
        this.itemOnClick = itemOnClick;
        this.previewMode = previewMode;
        this.itemViewWrapper = itemViewWrapper;
        this.itemViewGeneratorBehavior = itemViewGeneratorBehavior;
        this.itemViewGenerator = new ItemViewGenerator(this.activity, previewMode);
        this.handler = new Handler(Looper.getMainLooper());
    }


    public static CreateBuilder builder(Activity activity, ItemsStorageDrawerBehavior behavior, ItemViewGeneratorBehavior itemViewGeneratorBehavior, SelectionManager selectionManager, ItemsStorage itemsStorage) {
        return new CreateBuilder(activity, behavior, itemViewGeneratorBehavior, selectionManager, itemsStorage);
    }

    public void create() {
        throwIsBadThread();
        super.create();
        this.itemsStorage.getOnItemsStorageCallbacks().addCallback(CallbackImportance.DEFAULT, onItemsStorageUpdate);
        this.selectionManager.getOnSelectionUpdated().addCallback(CallbackImportance.DEFAULT, selectionCallback);
    }

    public void destroy() {
        super.destroy();
        this.itemsStorage.getOnItemsStorageCallbacks().removeCallback(onItemsStorageUpdate);
        this.selectionManager.getOnSelectionUpdated().removeCallback(selectionCallback);
    }

    @Override
    protected void runOnUiThread(Runnable r) {
        if (Thread.currentThread() == originalThread) {
            r.run();
        } else {
            handler.post(r);
        }
    }


    protected void onItemClicked(Item item) {
        if (this.itemOnClick != null) {
            this.itemOnClick.run(item);
            return;
        }
        if (!previewMode) {
            actionItem(item, behavior.getItemOnClickAction());
        }
    }

    private void throwIsBadThread() {
        if (Thread.currentThread() != originalThread) {
            throw new RuntimeException("Access from non-original thread.");
        }
    }

    public void setItemViewWrapper(ItemViewWrapper itemViewWrapper) {
        this.itemViewWrapper = itemViewWrapper;
    }

    // Private
    private class DrawerOnItemsStorageUpdated extends OnItemsStorageUpdate {
        @Override
        public Status onAdded(Item item, int pos) {
            runOnUiThread(() -> callWithNonNullAdapter((adapter) -> {
                adapter.notifyItemInserted(pos);
                adapter.notifyItemRangeChanged(adapter.getItemCount(), pos);
                if (behavior.isScrollToAddedItem()) {
                    smoothScrollToPosition(pos);
                }
            }));
            return Status.NONE;
        }

        @Override
        public Status onPreDeleted(Item item, int pos) {
            runOnUiThread(() -> callWithNonNullAdapter((adapter) -> adapter.notifyItemRemoved(pos)));
            return Status.NONE;
        }

        @Override
        public Status onMoved(Item item, int from, int to) {
            runOnUiThread(() -> callWithNonNullAdapter((adapter) -> adapter.notifyItemMoved(from, to)));
            return Status.NONE;
        }

        @Override
        public Status onUpdated(Item item, int pos) {
            runOnUiThread(() -> smoothUpdateItemAt(pos));
            return Status.NONE;
        }
    }


    @Nullable
    private View generateViewForItem(Item item, Destroyer destroyer) {
        boolean previewMode = this.previewMode || selectionManager.isSelected(item);
        return itemViewGenerator.generate(item, getView(), itemViewGeneratorBehavior, previewMode, destroyer, this::onItemClicked);
    }

    @Override
    protected void onBindItem(@NonNull ItemViewHolder holder, int position) {
        final Item item = itemsStorage.getItemAt(position);


        @Nullable View view;
        if (itemViewWrapper != null) {
            view = itemViewWrapper.wrap(item, () -> generateViewForItem(item, holder.destroyer), holder.destroyer);
        } else {
            view = generateViewForItem(item, holder.destroyer);
        }

        if (itemViewGeneratorBehavior.isRenderMinimized(item) && view != null) {
            final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 17, 0);
            view.setLayoutParams(layoutParams);
        }

        holder.bind(item, view);
    }

    @Override
    protected int getItemCount() {
        return itemsStorage.size();
    }

    @Override
    protected boolean onItemsMoved(int positionFrom, int positionTo) {
        //! NOTE: Adapter receive notify signal from callbacks!
        //ItemUIDrawer.this.adapter.notifyItemMoved(positionFrom, positionTo);

        Logger.d(TAG, "onItemsMoved from="+positionFrom + "; to="+ positionTo);
        ItemsStorageDrawer.this.itemsStorage.move(positionFrom, positionTo);
        return true;
    }

    @Override
    protected void onItemSwiped(ItemViewHolder viewHolder, int position, int direction) {
        final Item item = ItemsStorageDrawer.this.itemsStorage.getItemAt(position);

        if (direction == ItemTouchHelper.LEFT) {
            actionItem(item, behavior.getItemOnLeftAction());

        } else if (direction == ItemTouchHelper.RIGHT) {
            showRightMenu(item, viewHolder.layout.getChildAt(0));
        }
        roughUpdateItemAt(position);
    }

    private void actionItem(Item item, ItemAction action) {
        CrashReportContext.FRONT.push("ItemsStorageDrawer.actionItem");
        switch (action) {
            case OPEN_EDITOR -> behavior.onItemOpenEditor(item);
            case OPEN_TEXT_EDITOR -> behavior.onItemOpenTextEditor(item);
            case SELECT_ON -> selectionManager.selectItem(item);
            case SELECT_OFF -> selectionManager.deselectItem(item);
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
            }
            case DELETE_REQUEST -> behavior.onItemDeleteRequest(item);
            case SHOW_ACTION_DIALOG -> {
                new DialogSelectItemAction(activity, null, new DialogSelectItemAction.OnSelected() {
                    @Override
                    public void run(ItemAction dialogAction) {
                        actionItem(item, dialogAction);
                    }
                    // TODO: 21.10.2023 make translatable
                }, "Click to action now").excludeFromList(ItemAction.SHOW_ACTION_DIALOG).show();
            }
        }
        CrashReportContext.FRONT.pop();
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
            ItemAction itemAction = null;
            switch (menuItem.getItemId()) {
                case R.id.delete:
                    itemAction = ItemAction.DELETE_REQUEST;
                    break;

                case R.id.edit:
                    itemAction = ItemAction.OPEN_EDITOR;
                    break;

                case R.id.minimize:
                    itemAction = ItemAction.MINIMIZE_REVERT;
                    break;

                case R.id.selected:
                    itemAction = ItemAction.SELECT_REVERT;
                    break;

                case R.id.copy:
                    try {
                        Item copyItem = itemsStorage.copyItem(item);
                        behavior.onItemOpenEditor(copyItem);
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
                    behavior.onItemOpenTextEditor(item);
                    break;

                case R.id.transform:
                    new DialogSelectItemType(activity, type -> {
                        Transform.Result result = Transform.transform(item, type);
                        if (result.isAllow()) {
                            int pos = itemsStorage.getItemPosition(item);
                            Item newItem = result.generate();

                            UI.postDelayed(() -> itemsStorage.addItem(newItem, pos + 1), 500);

                        } else {
                            Toast.makeText(activity, R.string.transform_not_allowed, Toast.LENGTH_SHORT).show();
                        }
                    }, (type -> Transform.isAllow(item, type)))
                            .setTitle(activity.getString(R.string.transform_selectTypeDialog_title))
                            .show();
                    break;
            }

            if (itemAction != null) actionItem(item, itemAction);
            if (save) item.save();
            callWithNonNullAdapter(adapter1 -> adapter1.notifyItemChanged(itemsStorage.getItemPosition(item)));
            return true;
        });
        menu.setGravity(Gravity.END);
        menu.show();
    }

    @FunctionalInterface
    public interface ItemViewWrapper {
        @Nullable
        View wrap(Item item, Supplier<View> viewSupplier, Destroyer destroyer);
    }

    public static class CreateBuilder {
        private final Activity activity;
        private final ItemsStorageDrawerBehavior behavior;
        private final ItemViewGeneratorBehavior viewGeneratorBehavior;
        private final SelectionManager selectionManager;
        private final ItemsStorage itemsStorage;
        private boolean previewMode = false;
        private boolean isDragsEnable = true;
        private boolean isSwipesEnable = true;
        private RecyclerView view = null;
        private ItemViewWrapper itemViewWrapper = null;
        private ItemInterface onItemClick = null;

        public CreateBuilder(Activity activity, ItemsStorageDrawerBehavior behavior, ItemViewGeneratorBehavior viewGeneratorBehavior, SelectionManager selectionManager, ItemsStorage itemsStorage) {
            this.activity = activity;
            this.behavior = behavior;
            this.viewGeneratorBehavior = viewGeneratorBehavior;
            this.selectionManager = selectionManager;
            this.itemsStorage = itemsStorage;
        }

        public CreateBuilder setPreviewMode(boolean b) {
            this.previewMode = b;
            return this;
        }

        public CreateBuilder setPreviewMode() {
            this.previewMode = true;
            return this;
        }

        public ItemsStorageDrawer build() {
            if (view == null) {
                view = new RecyclerView(activity);
            }
            return new ItemsStorageDrawer(activity, view, isDragsEnable, isSwipesEnable, behavior, viewGeneratorBehavior, selectionManager, itemsStorage, onItemClick, previewMode, itemViewWrapper);
        }

        public CreateBuilder setDragsEnable(boolean b) {
            this.isDragsEnable = b;
            return this;
        }

        public CreateBuilder setView(RecyclerView view) {
            this.view = view;
            return this;
        }

        public CreateBuilder setItemViewWrapper(ItemViewWrapper itemViewWrapper) {
            this.itemViewWrapper = itemViewWrapper;
            return this;
        }

        public CreateBuilder setSwipesEnable(boolean b) {
            this.isSwipesEnable = b;
            return this;
        }

        public CreateBuilder setOnItemClick(ItemInterface onItemClick) {
            this.onItemClick = onItemClick;
            return this;
        }
    }

    private class DrawerSelectionCallback extends SelectionCallback {
        @Override
        public Status selected(Selection selection) {
            if (selection.getItem().getParentItemsStorage() == itemsStorage) {
                int pos = itemsStorage.getItemPosition(selection.getItem());
                roughUpdateItemAt(pos);
            }

            return Status.NONE;
        }

        @Override
        public Status unselected(Selection selection) {
            if (selection.getItem().getParentItemsStorage() == itemsStorage) {
                int pos = itemsStorage.getItemPosition(selection.getItem());
                roughUpdateItemAt(pos);
            }

            return Status.NONE;
        }

        @Override
        public Status unselectedAll() {
            callWithNonNullAdapter(RecyclerView.Adapter::notifyDataSetChanged);
            return Status.NONE;
        }
    }
}

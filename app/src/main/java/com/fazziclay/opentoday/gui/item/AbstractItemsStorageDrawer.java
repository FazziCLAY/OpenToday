package com.fazziclay.opentoday.gui.item;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fazziclay.opentoday.app.CrashReportContext;
import com.fazziclay.opentoday.util.Logger;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class AbstractItemsStorageDrawer {
    private static final String TAG = "AbstractItemsStorageDrawer";


    protected final Context context;
    protected final RecyclerView view;
    protected final DrawerTouchCallback drawerTouchCallback;
    private DrawerAdapter adapter;

    // internal states
    private boolean created = false;
    private boolean destroyed = false;


    public AbstractItemsStorageDrawer(Context context, RecyclerView view, boolean isDragsEnabled, boolean isSwipesEnabled) {
        this.context = context;
        this.view = view;
        this.drawerTouchCallback = new DrawerTouchCallback(isDragsEnabled, isSwipesEnabled);
    }

    public void create() {
        if (created) {
            throw new IllegalStateException("Already created!");
        }
        created = true;

        this.adapter = new DrawerAdapter();
        this.view.setLayoutManager(new LinearLayoutManager(context));
        this.view.setAdapter(adapter);
        new ItemTouchHelper(this.drawerTouchCallback).attachToRecyclerView(view);
    }

    public void destroy() {
        if (!created) {
            throw new IllegalStateException("Not created!");
        }
        if (destroyed) {
            throw new IllegalStateException("Already destroyed!");
        }
        destroyed = true;
        view.setAdapter(null);
    }

    protected abstract void runOnUiThread(Runnable r);

    protected abstract int getItemCount();

    protected abstract void onBindItem(@NotNull ItemViewHolder holder, int position);

    protected abstract boolean onItemsMoved(int positionFrom, int positionTo);

    protected abstract void onItemSwiped(ItemViewHolder viewHolder, int position, int direction);

    public RecyclerView getView() {
        return view;
    }

    /**
     * Run consumer if adapter not null
     */
    protected void callWithNonNullAdapter(Consumer<DrawerAdapter> consumer) {
        if (adapter != null) {
            consumer.accept(adapter);
        }
    }

    public void roughUpdateItemAt(int position) {
        runOnUiThread(() -> callWithNonNullAdapter(drawerAdapter -> drawerAdapter.notifyItemChanged(position)));
    }

    public void smoothUpdateItemAt(int position) {
        // if update viewHolder is null, it currently out of screen bounds
        var var = (ItemViewHolder) view.findViewHolderForAdapterPosition(position);
        if (var != null) {
            runOnUiThread(() -> onBindItem(var, position));
        } else {
            // test change: if smooth is unavailable => rough
            roughUpdateItemAt(position);
        }
    }

    public void smoothScrollToPosition(int position) {
        view.smoothScrollToPosition(position);
    }

    protected class DrawerAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ItemViewHolder(context);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            CrashReportContext.FRONT.push("bind ItemViewHolder in " + TAG);
            onBindItem(holder, position);
            CrashReportContext.FRONT.pop();
            Logger.d(TAG, "binded holder = " + holder);
        }

        @Override
        public void onViewRecycled(@NonNull ItemViewHolder holder) {
            Logger.d(TAG, "recycled holder = " + holder);
            holder.recycle();
        }

        @Override
        public int getItemCount() {
            return AbstractItemsStorageDrawer.this.getItemCount();
        }
    }


    protected class DrawerTouchCallback extends ItemTouchHelper.SimpleCallback {
        private static final int SWIPE_DIRS = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.START | ItemTouchHelper.END;
        private static final int DRAG_DIRS = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.START | ItemTouchHelper.END;

        private boolean isDragsEnable;
        private boolean isSwipesEnable;


        public DrawerTouchCallback(boolean isDragsEnable, boolean isSwipesEnable) {
            super(isDragsEnable ? DRAG_DIRS : 0, isSwipesEnable ? SWIPE_DIRS : 0);
            this.isDragsEnable = isDragsEnable;
            this.isSwipesEnable = isSwipesEnable;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            if (!isDragsEnable) return false;

            int positionFrom = viewHolder.getAdapterPosition();
            int positionTo = target.getAdapterPosition();

            return AbstractItemsStorageDrawer.this.onItemsMoved(positionFrom, positionTo);
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (!isSwipesEnable) return;

            final int position = viewHolder.getAdapterPosition();
            AbstractItemsStorageDrawer.this.onItemSwiped((ItemViewHolder) viewHolder, position, direction);
        }

        public boolean isDragsEnable() {
            return isDragsEnable;
        }

        public boolean isSwipesEnable() {
            return isSwipesEnable;
        }

        public void setDragsEnable(boolean dragsEnable) {
            isDragsEnable = dragsEnable;
            setDefaultDragDirs(dragsEnable ? DRAG_DIRS : 0);
        }

        public void setSwipesEnable(boolean swipesEnable) {
            isSwipesEnable = swipesEnable;
            setDefaultSwipeDirs(swipesEnable ? SWIPE_DIRS : 0);
        }
    }
}

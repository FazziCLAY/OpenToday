package com.fazziclay.opentoday.gui.item;

import android.content.Context;
import android.view.View;
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
    private static final String TAG = "SimpleItemsStorageDrawer";


    private final Context context;
    private final RecyclerView view;
    private DrawerAdapter adapter;
    private final boolean isDragsEnabled;
    private final boolean isSwipesEnabled;

    // internal states
    private boolean created = false;
    private boolean floatingCreated = false;
    private boolean destroyed = false;


    public AbstractItemsStorageDrawer(Context context, RecyclerView view, boolean isDragsEnabled, boolean isSwipesEnabled) {
        this.context = context;
        this.view = view;
        this.isDragsEnabled = isDragsEnabled;
        this.isSwipesEnabled = isSwipesEnabled;
    }

    public void create() {
        if (created) {
            throw new IllegalStateException("Already created!");
        }
        created = true;

        this.adapter = new DrawerAdapter();
        this.view.setLayoutManager(new LinearLayoutManager(context));
        this.view.setAdapter(adapter);
        new ItemTouchHelper(new DrawerTouchCallback(isDragsEnabled, isSwipesEnabled)).attachToRecyclerView(view);
        floatingCreate();
    }

    public void floatingCreate() {
        if (!floatingCreated) {
            doFloatCreate();
            floatingCreated = true;
        }
    }

    protected void doFloatCreate() {

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
        floatingDestroy();
    }

    public void floatingDestroy() {
        if (floatingCreated) {
            doFloatDestroy();
            floatingCreated = false;
        }
    }

    protected void doFloatDestroy() {

    }

    protected abstract int getItemCount();

    protected abstract void onBindItem(@NotNull ItemViewHolder holder, int position);

    protected abstract boolean onItemsMoved(int positionFrom, int positionTo);

    protected abstract void onItemSwiped(RecyclerView.ViewHolder viewHolder, int position, int direction);

    protected abstract void onItemClicked(View view, ItemViewHolder viewHolder, int position);

    public RecyclerView getView() {
        return view;
    }

    /**
     * Run AdapterInterface if adapter not null
     */
    protected void callWithNonNullAdapter(Consumer<DrawerAdapter> consumer) {
        if (adapter != null) {
            consumer.accept(adapter);
        }
    }

    public void updateItemAt(int position) {
        callWithNonNullAdapter(drawerAdapter -> drawerAdapter.notifyItemChanged(position));
    }

    public void smoothUpdateItemAt(int position) {
        // if update viewHolder is null, it currently out of screen bounds
        var var = (ItemViewHolder) view.findViewHolderForAdapterPosition(position);
        if (var != null) {
            onBindItem(var, position);
        }
    }

    public View.OnAttachStateChangeListener createSimplyFloatViewAttachListener() {
        return new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View view) {
                floatingCreate();
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View view) {
                floatingDestroy();
            }
        };
    }

    public void smoothScrollToPosition(int pos) {
        view.smoothScrollToPosition(pos);
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
            holder.bindOnClick(view -> onItemClicked(view, holder, position));
            CrashReportContext.FRONT.pop();
        }

        @Override
        public int getItemCount() {
            return AbstractItemsStorageDrawer.this.getItemCount();
        }
    }


    private class DrawerTouchCallback extends ItemTouchHelper.SimpleCallback {
        private static final int SWIPE_DIRS = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.START | ItemTouchHelper.END;
        private static final int DRAG_DIRS = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.START | ItemTouchHelper.END;

        private final boolean isDragsEnable;
        private final boolean isSwipesEnable;


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
            AbstractItemsStorageDrawer.this.onItemSwiped(viewHolder, position, direction);
        }
    }
}

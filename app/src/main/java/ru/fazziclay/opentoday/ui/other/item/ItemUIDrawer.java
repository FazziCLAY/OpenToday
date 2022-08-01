package ru.fazziclay.opentoday.ui.other.item;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ru.fazziclay.opentoday.app.items.Item;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.app.items.callback.OnItemAdded;
import ru.fazziclay.opentoday.app.items.callback.OnItemDeleted;
import ru.fazziclay.opentoday.app.items.callback.OnItemMoved;
import ru.fazziclay.opentoday.app.items.callback.OnItemUpdated;
import ru.fazziclay.opentoday.callback.CallbackImportance;
import ru.fazziclay.opentoday.callback.Status;

public class ItemUIDrawer {
    private final Activity activity;
    private final ItemStorage itemStorage;
    private final RecyclerView view;
    private RecyclerView.Adapter<ViewHolder> adapter;
    private ItemViewGenerator itemViewGenerator;

    private boolean destroyed = false;
    private boolean created = false;

    private final OnItemUpdated onItemUpdated = (item, position) -> {
        adapter.notifyItemChanged(position);
        return new Status.Builder().build();
    };
    private final OnItemDeleted onItemDeleted = (item, position) -> {
        adapter.notifyItemRemoved(position);
        return new Status.Builder().build();
    };
    private final OnItemAdded onItemAdded = (item, position) -> {
        adapter.notifyItemInserted(position);
        return new Status.Builder().build();
    };
    private final OnItemMoved onItemMoved = (posFrom, posTo) -> {
        adapter.notifyItemMoved(posFrom, posTo);
        return new Status.Builder().build();
    };

    public ItemUIDrawer(Activity activity, ItemStorage itemStorage) {
        this.activity = activity;
        this.itemStorage = itemStorage;
        this.view = new RecyclerView(activity);
        this.view.setLayoutManager(new LinearLayoutManager(activity));
    }

    public void create() {
        if (destroyed) {
            throw new RuntimeException("This ItemUIDrawer is destroyed!");
        }
        created = true;
        this.adapter = new Adapter();
        this.itemViewGenerator = new ItemViewGenerator(activity);
        this.view.setAdapter(adapter);
        this.itemStorage.getOnItemDeletedCallbackStorage().addCallback(CallbackImportance.DEFAULT, onItemDeleted);
        this.itemStorage.getOnItemUpdatedCallbackStorage().addCallback(CallbackImportance.DEFAULT, onItemUpdated);
        this.itemStorage.getOnItemAddedCallbackStorage().addCallback(CallbackImportance.DEFAULT, onItemAdded);
        this.itemStorage.getOnItemMovedCallbackStorage().addCallback(CallbackImportance.DEFAULT, onItemMoved);

        // Drag&Drop reorder
        new ItemTouchHelper(new DragReorderCallback()).attachToRecyclerView(view);
    }

    public void destroy() {
        if (!created) {
            throw new RuntimeException("This ItemUIDrawer no created!");
        }
        destroyed = true;
        this.itemStorage.getOnItemDeletedCallbackStorage().deleteCallback(onItemDeleted);
        this.itemStorage.getOnItemUpdatedCallbackStorage().deleteCallback(onItemUpdated);
        this.itemStorage.getOnItemAddedCallbackStorage().deleteCallback(onItemAdded);
        this.itemStorage.getOnItemMovedCallbackStorage().deleteCallback(onItemMoved);

        this.view.setAdapter(null);
    }

    public View getView() {
        return this.view;
    }

    private View getViewForItem(Item item) {
        return generateView(item);
    }

    private View generateView(Item item) {
        return itemViewGenerator.generate(item, view);
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder();
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Item item = itemStorage.getItems()[position];
            LinearLayout layout = holder.layout;
            layout.removeAllViews();
            layout.addView(getViewForItem(item));
        }

        @Override
        public int getItemCount() {
            return itemStorage.getItems().length;
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layout;

        public ViewHolder() {
            super(new LinearLayout(activity));
            layout = (LinearLayout) itemView;
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(0, 5, 0, 5);
            layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private class DragReorderCallback extends ItemTouchHelper.SimpleCallback {
        public DragReorderCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int positionFrom = viewHolder.getAdapterPosition();
            int positionTo = target.getAdapterPosition();

            //~~NOTE: Adapter receive notify signal from callbacks!
            //~~ItemUIDrawer.this.adapter.notifyItemMoved(positionFrom, positionTo);
            ItemUIDrawer.this.itemStorage.move(positionFrom, positionTo);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
    }
}

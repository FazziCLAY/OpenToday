package ru.fazziclay.opentoday.ui.other;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.databinding.ItemsEditorBinding;
import ru.fazziclay.opentoday.ui.other.item.ItemStorageDrawer;
import ru.fazziclay.opentoday.ui.other.item.OnItemClick;

// layout/items_editor.xml
public class ItemsEditor {
    private final Activity activity;
    private final ItemsEditorBinding binding;
    private final ItemStorageDrawer itemStorageDrawer;
    private final AppToolbar toolbar;

    // date
    private Handler currentDateHandler;
    private Runnable currentDateRunnable;
    private GregorianCalendar currentDateCalendar;
    private boolean destroyed = false;

    public ItemsEditor(@NonNull Activity activity, @Nullable ViewGroup parent, ItemManager itemManager, ItemStorage itemStorage, String path) {
        this(activity, parent, itemManager, itemStorage, path, null, false);
    }

    public ItemsEditor(@NonNull Activity activity, @Nullable ViewGroup parent, ItemManager itemManager, ItemStorage itemStorage, String path, OnItemClick onItemClick, boolean previewMode) {
        this.activity = activity;
        if (parent == null) {
            this.binding = ItemsEditorBinding.inflate(activity.getLayoutInflater());
        } else {
            this.binding = ItemsEditorBinding.inflate(activity.getLayoutInflater(), parent, false);
        }

        this.itemStorageDrawer = new ItemStorageDrawer(activity, itemManager, itemStorage, path, onItemClick, previewMode);
        this.binding.itemsStorageDrawer.addView(this.itemStorageDrawer.getView());

        // toolbar
        this.toolbar = new AppToolbar(activity, itemManager, itemStorage);
        this.binding.toolbar.addView(this.toolbar.getToolbarView());
        this.binding.toolbarMore.addView(this.toolbar.getToolbarMoreView());

        // path
        this.binding.path.setText(path);
    }

    public ItemStorageDrawer getItemStorageDrawer() {
        return itemStorageDrawer;
    }

    public View getView() {
        return binding.getRoot();
    }

    public void destroy() {
        destroyed = true;
        itemStorageDrawer.destroy();
        toolbar.destroy();
        currentDateHandler.removeCallbacks(currentDateRunnable);
    }

    public void create() {
        itemStorageDrawer.create();
        toolbar.create();
        setupCurrentDate();
    }

    private void setupCurrentDate() {
        currentDateCalendar = new GregorianCalendar();
        setCurrentDate();
        currentDateHandler = new Handler(activity.getMainLooper());
        currentDateRunnable = new Runnable() {
            @Override
            public void run() {
                if (destroyed) return;
                setCurrentDate();
                long millis = System.currentTimeMillis() % 1000;
                currentDateHandler.postDelayed(this, 1000 - millis);
            }
        };
        currentDateHandler.post(currentDateRunnable);
    }


    private void setCurrentDate() {
        currentDateCalendar.setTimeInMillis(System.currentTimeMillis());
        Date time = currentDateCalendar.getTime();

        // Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd EEEE", Locale.getDefault());
        binding.currentDate.setText(dateFormat.format(time));

        // Time
        dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        binding.currentTime.setText(dateFormat.format(time));
    }
}

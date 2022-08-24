package ru.fazziclay.opentoday.app.items;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.StringRes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.callback.OnSelectionChanged;
import ru.fazziclay.opentoday.app.items.item.CheckboxItem;
import ru.fazziclay.opentoday.app.items.item.DayRepeatableCheckboxItem;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.item.TextItem;
import ru.fazziclay.opentoday.callback.CallbackStorage;
import ru.fazziclay.opentoday.callback.Status;

public class ItemManager extends SimpleItemStorage {
    private static final boolean DEBUG_ITEMS_SET = (App.DEBUG && false);

    private final ItemIEManager itemIEManager;

    private final List<Selection> selections = new ArrayList<>();
    private final CallbackStorage<OnSelectionChanged> onSelectionUpdated = new CallbackStorage<>();

    private final SaveThread saveThread = new SaveThread();

    private ItemAction itemOnClickAction = ItemAction.OPEN_EDIT_DIALOG;
    private ItemAction itemOnLeftAction = ItemAction.MINIMIZE_REVERT;

    public ItemManager(File saveFile) {
        this.itemIEManager = new ItemIEManager(saveFile);
        load();
        saveThread.start();
    }

    private void load() {
        DataTransferPacket load = DEBUG_ITEMS_SET ? generateDebugData() : itemIEManager.loadFromFile();
        if (load != null) {
            importData(load);
        }
    }

    public Selection[] getSelections() {
        return selections.toArray(new Selection[0]);
    }

    public void selectItem(Selection selection) {
        if (isSelected(selection.getItem())) return;
        this.selections.add(selection);
        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.run(this.selections);
            return new Status.Builder().build();
        });
    }

    public void deselectItem(Item item) {
        if (!isSelected(item)) return;
        Selection toDelete = null;
        for (Selection selection : this.selections) {
            if (selection.getItem() == item) toDelete = selection;
        }
        selections.remove(toDelete);
        Log.e("deselect", "owo");

        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.run(this.selections);
            return new Status.Builder().build();
        });
    }

    public void deselectItem(Selection se) {
        if (!isSelected(se.getItem())) return;
        Selection toDelete = null;
        for (Selection selection : this.selections) {
            if (selection == se) toDelete = selection;
        }
        selections.remove(toDelete);

        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.run(this.selections);
            return new Status.Builder().build();
        });
    }

    public void deselectAll() {
        selections.clear();

        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.run(this.selections);
            return new Status.Builder().build();
        });
    }

    public boolean isSelected(Item item) {
        for (Selection selection : selections) {
            if (selection.getItem() == item) return true;
        }
        return false;
    }

    public CallbackStorage<OnSelectionChanged> getOnSelectionUpdated() {
        return onSelectionUpdated;
    }

    public ItemAction getItemOnClickAction() {
        return itemOnClickAction;
    }

    public ItemAction getItemOnLeftAction() {
        return itemOnLeftAction;
    }

    public void setItemOnClickAction(ItemAction itemOnClickAction) {
        this.itemOnClickAction = itemOnClickAction;
    }

    public void setItemOnLeftAction(ItemAction itemOnLeftAction) {
        this.itemOnLeftAction = itemOnLeftAction;
    }

    @Override
    public void deleteItem(Item item) {
        deselectItem(item);
        super.deleteItem(item);
    }

    @Override
    public void tick(TickSession tickSession) {
        Log.d("ItemManager", "tick");
        super.tick(tickSession);
    }

    @Override
    public void save() {
        saveThread.request();
    }

    public boolean saveAllDirect() {
        try {
            ItemManager.this.itemIEManager.saveToFile(exportData());
            return true;
        } catch (Exception e) {
            Log.e("ItemManager", "SaveThread exception", e);
            try {
                new Handler(App.get().getMainLooper()).post(() -> Toast.makeText(App.get(), "Error: Save exception: " + e + "; cause: " + e.getCause(), Toast.LENGTH_LONG).show());
            } catch (Exception ignored) {}
            return false;
        }
    }

    private class SaveThread extends Thread {
        public boolean request = false;
        public int requestsCount = 0;
        public long firstRequestTime = 0;
        public long latestRequestTime = 0;

        public SaveThread() {
            super.setName("ItemManager-SaveThread");
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                if (request) {
                    request = false;
                    requestsCount = 0;
                    firstRequestTime = 0;
                    latestRequestTime = 0;

                    // Save
                    internalSave();
                    Log.i("SaveThread", String.format("requestCount=%s\nfirstTime=%s\nlatestTime=%s", requestsCount, firstRequestTime, latestRequestTime));
                }

            }
        }

        private void internalSave() {
            try {
                ItemManager.this.itemIEManager.saveToFile(exportData());
            } catch (Exception e) {
                Log.e("ItemManager", "SaveThread exception", e);
                try {
                    new Handler(App.get().getMainLooper()).post(() -> Toast.makeText(App.get(), "Error: Save exception: " + e + "; cause: " + e.getCause(), Toast.LENGTH_LONG).show());
                } catch (Exception ignored) {}
            }
        }

        public void request() {
            if (!request) {
                firstRequestTime = System.currentTimeMillis();
            }
            request = true;
            latestRequestTime = System.currentTimeMillis();
            requestsCount = requestsCount + 1;
        }
    }

    private DataTransferPacket generateDebugData() {
        DataTransferPacket debug = new DataTransferPacket();

        debug.items.add(new TextItem("first TextItem"));
        debug.items.add(new CheckboxItem("first CheckboxItem", false));
        debug.items.add(new DayRepeatableCheckboxItem("first DayRepeatableCheckboxItem", false, false, 0));

        int i = 0;
        while (i < 50) {
            Random r = new Random(999);
            if (r.nextBoolean()) debug.items.add(new TextItem("i=" + i));
            if (r.nextBoolean()) debug.items.add(new CheckboxItem("i=" + i, (i % 2 == 0)));
            i++;
        }
        return debug;
    }

    public enum ItemAction {
        OPEN_EDIT_DIALOG(R.string.itemAction_OPEN_EDIT_DIALOG),
        SELECT_REVERT(R.string.itemAction_SELECT_REVERT),
        SELECT_ON(R.string.itemAction_SELECT_ON),
        SELECT_OFF(R.string.itemAction_SELECT_OFF),
        DELETE_REQUEST(R.string.itemAction_DELETE_REQUEST),
        MINIMIZE_REVERT(R.string.itemAction_MINIMIZE_REVERT),
        MINIMIZE_ON(R.string.itemAction_MINIMIZE_ON),
        MINIMIZE_OFF(R.string.itemAction_MINIMIZE_OFF);

        @StringRes
        private final int n;

        ItemAction(@StringRes int n) {
            this.n = n;
        }

        public int nameResId() {
            return n;
        }
    }
}

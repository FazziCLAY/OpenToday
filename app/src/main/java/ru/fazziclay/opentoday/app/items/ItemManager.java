package ru.fazziclay.opentoday.app.items;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.fazziclay.opentoday.app.App;
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
        this.selections.add(selection);
        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.run(this.selections);
            return new Status.Builder().build();
        });
    }

    public void deselectItem(Item item) {
        Selection toDelete = null;
        for (Selection selection : this.selections) {
            if (selection.getItem() == item) toDelete = selection;
        }
        selections.remove(toDelete);

        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.run(this.selections);
            return new Status.Builder().build();
        });
    }

    public void deselectItem(Selection se) {
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
        if (selections == null) return false;
        for (Selection selection : selections) {
            if (selection.getItem() == item) return true;
        }
        return false;
    }

    public CallbackStorage<OnSelectionChanged> getOnSelectionUpdated() {
        return onSelectionUpdated;
    }

    @Override
    public void save() {
        saveThread.request();
    }

    public void saveAllDirect() {
        try {
            ItemManager.this.itemIEManager.saveToFile(exportData());
        } catch (Exception e) {
            Log.e("ItemManager", "SaveThread exception", e);
            try {
                new Handler(App.get().getMainLooper()).post(() -> Toast.makeText(App.get(), "Error: Save exception: " + e + "; cause: " + e.getCause(), Toast.LENGTH_LONG).show());
            } catch (Exception ignored) {}
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
                    internalSave();
                    Log.i("SaveThread", String.format("requestCount=%s\nfirstTime=%s\nlatestTime=%s", requestsCount, firstRequestTime, latestRequestTime));

                    request = false;
                    requestsCount = 0;
                    firstRequestTime = 0;
                    latestRequestTime = 0;
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
}

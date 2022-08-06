package ru.fazziclay.opentoday.app.items;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Random;

import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.callback.OnSelectedChanged;
import ru.fazziclay.opentoday.callback.CallbackStorage;
import ru.fazziclay.opentoday.callback.Status;

public class ItemManager extends SimpleItemStorage {
    private static final boolean DEBUG_ITEMS_SET = (App.DEBUG && false);

    private final ItemIEManager itemIEManager;

    private AbsoluteItemContainer selection;
    private final CallbackStorage<OnSelectedChanged> onSelectionUpdated = new CallbackStorage<>();

    public ItemManager(File saveFile) {
        this.itemIEManager = new ItemIEManager(saveFile);
        load();
    }

    private void load() {
        DataTransferPacket load = DEBUG_ITEMS_SET ? generateDebugData() : itemIEManager.loadFromFile();
        if (load != null) {
            importData(load);
        }
    }

    public AbsoluteItemContainer getSelection() {
        return selection;
    }

    public void selectItem(AbsoluteItemContainer selection) {
        this.selection = selection;
        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.run(this.selection);
            return new Status.Builder().build();
        });
    }

    public void deselect() {
        this.selection = null;
        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.run(null);
            return new Status.Builder().build();
        });
    }

    public boolean isSelected(Item item) {
        if (selection == null) return false;
        return item == selection.getItem();
    }

    public CallbackStorage<OnSelectedChanged> getOnSelectionUpdated() {
        return onSelectionUpdated;
    }

    @Override
    public void save() {
        new SaveThread().start();
    }

    private class SaveThread extends Thread {
        public SaveThread() {
            super.setName("ItemManager-SaveThread");
        }

        @Override
        public void run() {
            try {
                ItemManager.this.itemIEManager.saveToFile(exportData());
            } catch (Exception e) {
                Log.e("ItemManager", "SaveThread exception", e);
                try {
                    new Handler(App.get().getMainLooper()).post(() -> Toast.makeText(App.get(), "Error: Save exception: " + e + "; cause: " + e.getCause(), Toast.LENGTH_LONG).show());
                } catch (Exception ignored) {}
            }
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

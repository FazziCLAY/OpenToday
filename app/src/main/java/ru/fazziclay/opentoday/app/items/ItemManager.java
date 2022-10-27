package ru.fazziclay.opentoday.app.items;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import ru.fazziclay.javaneoutil.FileUtil;
import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.annotation.SaveKey;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.callback.OnSelectionChanged;
import ru.fazziclay.opentoday.app.items.callback.OnTabsChanged;
import ru.fazziclay.opentoday.app.items.item.CheckboxItem;
import ru.fazziclay.opentoday.app.items.item.CounterItem;
import ru.fazziclay.opentoday.app.items.item.CycleListItem;
import ru.fazziclay.opentoday.app.items.item.DayRepeatableCheckboxItem;
import ru.fazziclay.opentoday.app.items.item.FilterGroupItem;
import ru.fazziclay.opentoday.app.items.item.GroupItem;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.item.TextItem;
import ru.fazziclay.opentoday.app.items.tab.LocalItemsTab;
import ru.fazziclay.opentoday.app.items.tab.ItemsTabController;
import ru.fazziclay.opentoday.app.items.tab.Tab;
import ru.fazziclay.opentoday.app.items.tab.TabIEUtil;
import ru.fazziclay.opentoday.callback.CallbackStorage;
import ru.fazziclay.opentoday.callback.Status;
import ru.fazziclay.opentoday.util.Profiler;

public class ItemManager {
    private static final boolean DEBUG_ITEMS_SET = (App.DEBUG && false);

    // Selection
    private final List<Selection> selections = new ArrayList<>();
    private final CallbackStorage<OnSelectionChanged> onSelectionUpdated = new CallbackStorage<>();

    @NonNull private final File dataFile;
    @NonNull private final SaveThread saveThread = new SaveThread();
    @NonNull @RequireSave @SaveKey(key = "tabs") private final List<Tab> tabs = new ArrayList<>();
    @NonNull private final ItemsTabController itemsTabController = new LocalItemTabsController();
    @NonNull private final CallbackStorage<OnTabsChanged> onTabsChangedCallbacks = new CallbackStorage<>();

    public ItemManager(@NonNull File dataFile) {
        this.dataFile = dataFile;
        load();
        saveThread.start();
    }

    private void load() {
        Profiler profiler = new Profiler("ItemManager load");
        if (DEBUG_ITEMS_SET) {
            tabs.addAll(generateDebugDataSet());

        } else {
            try {
                JSONObject jsonRoot = new JSONObject(FileUtil.getText(dataFile, "{}"));
                JSONArray jsonTabs;
                if (jsonRoot.has("tabs")) {
                    jsonTabs = jsonRoot.getJSONArray("tabs");
                } else {
                    jsonTabs = new JSONArray();
                }
                List<Tab> tabs = TabIEUtil.importTabs(jsonTabs);
                for (Tab tab : tabs) {
                    tab.setController(itemsTabController);
                }
                this.tabs.addAll(tabs);
            } catch (Exception e) {
                throw new RuntimeException("Load error! Data is break", e);
            }
        }
        if (tabs.isEmpty()) {
            createTab("My Items");
        }
        profiler.end();
    }

    // ==== TABS ====
    @NonNull
    public List<Tab> getTabs() {
        return tabs;
    }

    public Tab getTab(UUID uuid) {
        for (Tab tab : getTabs()) {
            if (tab.getId().equals(uuid)) {
                return tab;
            }
        }
        return null;
    }

    public Tab getMainTab() {
        return getTabs().get(0);
    }

    public void createTab(String name) {
        if (name.trim().isEmpty()) {
            throw new RuntimeException("Empty name for tab is not allowed!");
        }
        addTab(new LocalItemsTab(UUID.randomUUID(), name));
        internalOnTabChanged();
        save();
    }

    private void addTab(Tab tab) {
        if (tab.getId() == null) tab.setId(UUID.randomUUID());
        tab.setController(itemsTabController);
        this.tabs.add(tab);
        internalOnTabChanged();
        save();
    }

    public void deleteTab(Tab tab) {
        if (this.tabs.size() == 1) {
            throw new SecurityException("Not allowed one tab (after delete tabs count == 0)");
        }
        this.tabs.remove(tab);
        internalOnTabChanged();
        save();
    }

    public void moveTabs(int positionFrom, int positionTo) {
        Collections.swap(this.tabs, positionFrom, positionTo);
        internalOnTabChanged();
        save();
    }

    private void internalOnTabChanged() {
        onTabsChangedCallbacks.run((callbackStorage, callback) -> callback.run(this.tabs.toArray(new Tab[0])));
    }

    private class LocalItemTabsController implements ItemsTabController {
        @Override
        public void save(Tab tab) {
            ItemManager.this.save();
        }
        @Override
        public void nameChanged(Tab tab) {
            ItemManager.this.internalOnTabChanged();
            ItemManager.this.save();
        }
    }

    // ==== Tick ====
    public void tick(TickSession tickSession, List<UUID> uuids) {
        for (UUID uuid : uuids) {
            for (Tab tab : tabs) {
                Item i = tab.getItemById(uuid);
                if (i != null) {
                    i.tick(tickSession);
                }
            }
        }
    }

    public void tick(TickSession tickSession) {
        for (Tab tab : tabs) {
            if (tab == null) continue;
            tab.tick(tickSession);
        }
    }

    public CallbackStorage<OnSelectionChanged> getOnSelectionUpdated() {
        return onSelectionUpdated;
    }

    public CallbackStorage<OnTabsChanged> getOnTabsChanged() {
        return onTabsChangedCallbacks;
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

    public void save() {
        saveThread.request();
    }

    public boolean saveAllDirect() {
        try {
            // Save
            {
                JSONObject jRoot = new JSONObject();
                JSONArray jTabs = TabIEUtil.exportTabs(this.tabs);
                jRoot.put("tabs", jTabs);

                FileUtil.setText(dataFile, jRoot.toString(2));
            }
            return true;
        } catch (Exception e) {
            Log.e("ItemManager", "SaveThread exception", e);
            App.exception(App.get(), e);
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
                ItemManager.this.saveAllDirect();
            } catch (Exception e) {
                Log.e("ItemManager", "SaveThread exception", e);
                try {
                    new Handler(App.get().getMainLooper()).post(() -> Toast.makeText(App.get(), "Error: Save exception: " + e + "; cause: " + e.getCause(), Toast.LENGTH_LONG).show());
                } catch (Exception ignored) {}
                try {
                    App.exception(App.get(), e);
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

    private List<Tab> generateDebugDataSet() {
        List<Item> items = new ArrayList<>();
        items.add(new TextItem("first TextItem"));
        items.add(new CheckboxItem("first CheckboxItem", false));
        items.add(new DayRepeatableCheckboxItem("first DayRepeatableCheckboxItem", false, false, 0));
        items.add(new CycleListItem("first CycleListItem"));
        items.add(new GroupItem("first GroupItem"));
        items.add(new FilterGroupItem("first FilterGroupItem"));
        items.add(new CounterItem("first CounterItem"));

        int i = 0;
        while (i < 50) {
            Random r = new Random(999);
            if (r.nextBoolean()) items.add(new TextItem("i=" + i));
            if (r.nextBoolean()) items.add(new CheckboxItem("i=" + i, (i % 2 == 0)));
            i++;
        }

        List<Tab> tabs = new ArrayList<>();
        addTab(new LocalItemsTab(UUID.randomUUID(), "Debug1"));
        return tabs;
    }
}

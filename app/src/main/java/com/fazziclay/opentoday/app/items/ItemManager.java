package com.fazziclay.opentoday.app.items;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.annotation.RequireSave;
import com.fazziclay.opentoday.annotation.SaveKey;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.TickSession;
import com.fazziclay.opentoday.app.items.callback.OnSelectionChanged;
import com.fazziclay.opentoday.app.items.callback.OnTabsChanged;
import com.fazziclay.opentoday.app.items.item.CheckboxItem;
import com.fazziclay.opentoday.app.items.item.CounterItem;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.DayRepeatableCheckboxItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.items.tab.ItemsTabController;
import com.fazziclay.opentoday.app.items.tab.LocalItemsTab;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.app.items.tab.TabIEUtil;
import com.fazziclay.opentoday.callback.CallbackStorage;
import com.fazziclay.opentoday.callback.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import ru.fazziclay.javaneoutil.FileUtil;

public class ItemManager {
    private static final boolean DEBUG_ITEMS_SET = (App.DEBUG && false);

    // Selection
    private final List<Selection> selections = new ArrayList<>();
    private final CallbackStorage<OnSelectionChanged> onSelectionUpdated = new CallbackStorage<>();

    @NonNull private final File dataOriginalFile;
    @NonNull private final File dataCompressFile;
    private boolean debugPrintSaveStatusAlways = false;
    private SaveThread saveThread = null;
    @NonNull @RequireSave
    @SaveKey(key = "tabs") private final List<Tab> tabs = new ArrayList<>();
    @NonNull private final ItemsTabController itemsTabController = new LocalItemTabsController();
    @NonNull private final CallbackStorage<OnTabsChanged> onTabsChangedCallbacks = new CallbackStorage<>();

    public ItemManager(@NonNull final File dataOriginalFile, @NonNull final File dataCompressFile) {
        this.dataOriginalFile = dataOriginalFile;
        this.dataCompressFile = dataCompressFile;
        load();
    }

    public void setDebugPrintSaveStatusAlways(boolean b) {
        debugPrintSaveStatusAlways = b;
    }

    private void load() {
        if (DEBUG_ITEMS_SET) {
            tabs.addAll(generateDebugDataSet());

        } else {
            boolean isOriginal = FileUtil.isExist(dataOriginalFile);
            boolean isCompress = FileUtil.isExist(dataCompressFile);
            if (isCompress || isOriginal) {
                try {
                    JSONObject jsonRoot = null;
                    try {
                        FileInputStream fis = new FileInputStream(dataCompressFile);
                        GZIPInputStream gz = new GZIPInputStream(fis);
                        Reader reader = new InputStreamReader(gz);

                        final StringBuilder result = new StringBuilder();

                        final char[] buff = new char[1024];
                        int i;
                        while ((i = reader.read(buff)) > 0) {
                            result.append(new String(buff, 0, i));
                        }

                        reader.close();
                        jsonRoot  = new JSONObject(result.toString());

                    } catch (Exception ignored) {}

                    if (jsonRoot == null) {
                        jsonRoot  = new JSONObject(FileUtil.getText(dataOriginalFile, "{}"));
                    }

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
        }
        if (tabs.isEmpty()) {
            createTab("My Items");
        }
    }

    @Nullable
    public ItemsStorage getItemStorageById(UUID id) {
        Tab tab = getTab(id);
        if (tab != null) return tab;
        Item item = null;
        for (Tab t : tabs) {
            Item i = t.getItemById(id);
            if (i != null) {
                item = i;
                break;
            }
        }
        if (item instanceof ItemsStorage) {
            return (ItemsStorage) item;
        }
        return null;
    }

    // ==== Path ====
    public Item getItemByPath(ItemPath itemPath) {
        UUID tabId = itemPath.getTabId();
        Tab tab = getTab(tabId);
        if (tab == null) {
            return null;
        }

        Object o = tab;
        for (UUID item : itemPath.getItems()) {
            if (o instanceof ItemsStorage) {
                ItemsStorage itemsStorage = (ItemsStorage) o;
                o = itemsStorage.getItemById(item);
            } else {
                break;
            }
        }
        if (o == null) {
            return null;
        }
        if (o instanceof Item) {
            return (Item) o;
        } else {
            throw new RuntimeException("founded not extend Item object: " + o);
        }
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

    @NonNull
    public Tab getMainTab() {
        return getTabs().get(0);
    }

    public void createTab(@NonNull String name) {
        if (name.trim().isEmpty()) {
            throw new RuntimeException("Empty name for tab is not allowed!");
        }
        addTab(new LocalItemsTab(UUID.randomUUID(), name));
        internalOnTabChanged();
        queueSave();
    }

    private void addTab(@NonNull Tab tab) {
        if (tab.getId() == null) tab.setId(UUID.randomUUID());
        tab.setController(itemsTabController);
        this.tabs.add(tab);
        internalOnTabChanged();
        queueSave();
    }

    public void deleteTab(Tab tab) {
        if (this.tabs.size() == 1) {
            throw new SecurityException("Not allowed one tab (after delete tabs count == 0)");
        }
        this.tabs.remove(tab);
        internalOnTabChanged();
        queueSave();
    }

    public void moveTabs(int positionFrom, int positionTo) {
        Tab from = tabs.get(positionFrom);
        tabs.remove(from);
        tabs.add(positionTo, from);
        //Collections.rotate(this.tabs, positionFrom, positionTo);
        // TODO: 27.10.2022 EXPERIMENTAL CHANGES
        internalOnTabChanged();
        queueSave();
    }

    private void internalOnTabChanged() {
        onTabsChangedCallbacks.run((callbackStorage, callback) -> callback.onTabsChanged(this.tabs.toArray(new Tab[0])));
    }

    public CallbackStorage<OnTabsChanged> getOnTabsChanged() {
        return onTabsChangedCallbacks;
    }

    @Nullable
    public Item getItemById(@NonNull UUID id) {
        for (Tab tab : getTabs()) {
            Item i = tab.getItemById(id);
            if (i != null) return i;
        }
        return null;
    }

    private class LocalItemTabsController implements ItemsTabController {
        @Override
        public void save(@NonNull Tab tab) {
            ItemManager.this.queueSave();
        }
        @Override
        public void nameChanged(@NonNull final Tab tab) {
            ItemManager.this.internalOnTabChanged();
            ItemManager.this.queueSave();
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

    public Selection[] getSelections() {
        return selections.toArray(new Selection[0]);
    }

    public void selectItem(Selection selection) {
        if (isSelected(selection.getItem())) return;
        this.selections.add(selection);
        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.onSelectionChanged(this.selections);
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
            callback.onSelectionChanged(this.selections);
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
            callback.onSelectionChanged(this.selections);
            return new Status.Builder().build();
        });
    }

    public void deselectAll() {
        selections.clear();

        this.onSelectionUpdated.run((callbackStorage, callback) -> {
            callback.onSelectionChanged(this.selections);
            return new Status.Builder().build();
        });
    }

    public boolean isSelected(Item item) {
        for (Selection selection : selections) {
            if (selection.getItem() == item) return true;
        }
        return false;
    }

    public void queueSave() {
        if (saveThread == null) {
            saveThread = new SaveThread();
            saveThread.start();
        }
        saveThread.request();
    }

    public boolean saveAllDirect() {
        if (DEBUG_ITEMS_SET) return false;
        try {
            // Save
            {
                JSONObject jRoot = new JSONObject();
                JSONArray jTabs = TabIEUtil.exportTabs(this.tabs);
                jRoot.put("tabs", jTabs);
                String originalData = jRoot.toString();

                FileUtil.setText(dataOriginalFile, originalData);

                GZIPOutputStream gz = new GZIPOutputStream(new FileOutputStream(dataCompressFile));
                Writer writer = new OutputStreamWriter(gz);
                writer.write(originalData);
                writer.flush();
                writer.close();
            }

            if (debugPrintSaveStatusAlways) {
                try {
                    new Handler(App.get().getMainLooper()).post(() -> Toast.makeText(App.get(), "Success save", Toast.LENGTH_SHORT).show());
                } catch (Exception ignored) {}
            }
            return true;
        } catch (Exception e) {
            Log.e("ItemManager", "SaveThread exception", e);
            App.exception(null, e);
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
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
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
        List<Item> tab1items = new ArrayList<>();
        tab1items.add(new TextItem("first TextItem"));
        tab1items.add(new CheckboxItem("first CheckboxItem", false));
        tab1items.add(new DayRepeatableCheckboxItem("first DayRepeatableCheckboxItem", false, false, 0));
        tab1items.add(new CycleListItem("first CycleListItem"));
        tab1items.add(new GroupItem("first GroupItem"));
        tab1items.add(new FilterGroupItem("first FilterGroupItem"));
        tab1items.add(new CounterItem("first CounterItem"));

        List<Item> tab2items = new ArrayList<>();
        int i = 0;
        while (i < 50) {
            Random r = new Random(999);
            if (r.nextBoolean()) tab2items.add(new TextItem("i=" + i));
            if (r.nextBoolean()) tab2items.add(new CheckboxItem("i=" + i, (i % 2 == 0)));
            i++;
        }

        List<Tab> tabs = new ArrayList<>();
        addTab(new LocalItemsTab(UUID.randomUUID(), "Debug1", tab1items.toArray(new Item[0])));
        addTab(new LocalItemsTab(UUID.randomUUID(), "Debug2", tab2items.toArray(new Item[0])));
        return tabs;
    }
}
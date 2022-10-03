package ru.fazziclay.opentoday.app.items;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.StringRes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
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
import ru.fazziclay.opentoday.callback.CallbackStorage;
import ru.fazziclay.opentoday.callback.Status;
import ru.fazziclay.opentoday.util.Profiler;

public class ItemManager {
    private static final boolean DEBUG_ITEMS_SET = (App.DEBUG && false);

    private final File dataFile;
    private final SaveThread saveThread = new SaveThread();

    private final List<Selection> selections = new ArrayList<>();
    private final CallbackStorage<OnSelectionChanged> onSelectionUpdated = new CallbackStorage<>();

    private ItemAction itemOnClickAction = ItemAction.OPEN_EDIT_DIALOG;
    private ItemAction itemOnLeftAction = ItemAction.MINIMIZE_REVERT;

    @RequireSave @SaveKey(key = "tabs") private final List<ItemsTab> tabs = new ArrayList<>();
    private final ItemsTabController itemsTabController = new LocalItemTabsController();
    private final CallbackStorage<OnTabsChanged> onTabsChangedCallbacks = new CallbackStorage<>();

    public ItemManager(File dataFile) {
        this.dataFile = dataFile;
        load();
        saveThread.start();
    }

    private void load() {
        Profiler profiler = new Profiler("ItemManager load");
        if (DEBUG_ITEMS_SET) {
            tabs.addAll(generateDebugData());

        } else {
            try {
                JSONObject jRoot = new JSONObject(FileUtil.getText(dataFile, "{}"));
                JSONArray jTabs;
                if (jRoot.has("tabs")) {
                    jTabs = jRoot.getJSONArray("tabs");
                } else {
                    jTabs = new JSONArray();
                }

                int i = 0;
                while (i < jTabs.length()) {
                    JSONObject jTab = jTabs.getJSONObject(i);
                    ItemsTab tab = new ItemsTab(UUID.fromString(jTab.getString("id")), jTab.getString("name"), itemsTabController);
                    DataTransferPacket d = new DataTransferPacket();
                    d.items = ItemIEUtil.importItemList(jTab.getJSONArray("items"));
                    tab.importData(d);
                    i++;
                    this.tabs.add(tab);
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("ItemManager load exception", e);
            }
        }
        if (tabs.isEmpty()) {
            createTab("My Items");
        }
        profiler.end();
    }

    // ==== TABS ====
    public List<ItemsTab> getTabs() {
        return tabs;
    }

    public ItemsTab getTab(UUID uuid) {
        for (ItemsTab itemsTab : getTabs()) {
            if (itemsTab.getId().equals(uuid)) {
                return itemsTab;
            }
        }
        return null;
    }

    public ItemsTab getMainTab() {
        return getTabs().get(0);
    }

    public void deleteTab(ItemsTab tab) {
        if (this.tabs.size() == 1) {
            throw new SecurityException("Not allowed one tab (after delete tabs count == 0)");
        }
        this.tabs.remove(tab);
        internalOnTabChanged();
        save();
    }

    public void createTab(String name) {
        if (name.trim().isEmpty()) {
            throw new RuntimeException("Empty name for tab is not allowed!");
        }
        this.tabs.add(new ItemsTab(UUID.randomUUID(), name, itemsTabController));
        internalOnTabChanged();
        save();
    }

    private void internalOnTabChanged() {
        onTabsChangedCallbacks.run((callbackStorage, callback) -> callback.run(this.tabs.toArray(new ItemsTab[0])));
    }

    private class LocalItemTabsController implements ItemsTabController {
        @Override
        public void save(ItemsTab itemsTab) {
            ItemManager.this.save();
        }
        @Override
        public void nameChanged(ItemsTab itemsTab) {
            ItemManager.this.internalOnTabChanged();
        }
    }

    // ==== Tick ====
    public void tick(TickSession tickSession, List<UUID> uuids) {
        for (UUID uuid : uuids) {
            for (ItemsTab tab : tabs) {
                Item i = tab.getItemById(uuid);
                if (i != null) {
                    i.tick(tickSession);
                }
            }
        }
    }

    public void tick(TickSession tickSession) {
        for (ItemsTab tab : tabs) {
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
                JSONArray jTabs = new JSONArray();

                for (ItemsTab tab : this.tabs) {
                    JSONObject jTab = new JSONObject();
                    jTab.put("id", tab.getId().toString());
                    jTab.put("name", tab.getName());
                    jTab.put("items", ItemIEUtil.exportItemList(tab.exportData().items));
                    jTabs.put(jTab);
                }
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

    private List<ItemsTab> generateDebugData() {
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

        ItemsTab tab = new ItemsTab(UUID.randomUUID(), "Debug Set", itemsTabController);
        for (Item item : items) {
            tab.addItem(item);
        }
        List<ItemsTab> tabs = new ArrayList<>();
        tabs.add(tab);
        tabs.add(new ItemsTab(UUID.randomUUID(), "DebutSetNone", itemsTabController));
        return tabs;
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

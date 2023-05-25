package com.fazziclay.opentoday.app.items;

import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.data.CherryOrchard;
import com.fazziclay.opentoday.app.items.callback.OnTabsChanged;
import com.fazziclay.opentoday.app.items.item.CheckboxItem;
import com.fazziclay.opentoday.app.items.item.CounterItem;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.DayRepeatableCheckboxItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.items.selection.SelectionManager;
import com.fazziclay.opentoday.app.items.tab.LocalItemsTab;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.app.items.tab.TabCodecUtil;
import com.fazziclay.opentoday.app.items.tab.TabController;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;
import com.fazziclay.opentoday.util.callback.CallbackStorage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ItemManager {
    private static final boolean DEBUG_ITEMS_SET = App.debug(false);
    private static final String TAG = "ItemManager";

    private boolean destroyed = false;
    @NonNull private final File dataOriginalFile;
    @NonNull private final File dataCompressFile;
    private boolean debugPrintSaveStatusAlways = false;
    private SaveThread saveThread = null;
    @NonNull @RequireSave
    @SaveKey(key = "tabs") private final List<Tab> tabs = new ArrayList<>();
    @NonNull private final TabController tabController = new LocalItemTabsController();
    @NonNull private final CallbackStorage<OnTabsChanged> onTabsChangedCallbacks = new CallbackStorage<>();
    @NonNull private final SelectionManager selectionManager;

    public ItemManager(@NonNull final File dataOriginalFile, @NonNull final File dataCompressFile) {
        this.dataOriginalFile = dataOriginalFile;
        this.dataCompressFile = dataCompressFile;
        this.selectionManager = new SelectionManager();
        load();
    }

    @NonNull
    public SelectionManager getSelectionManager() {
        return selectionManager;
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
                    List<Tab> tabs = TabCodecUtil.importTabList(CherryOrchard.of(jsonTabs));
                    for (Tab tab : tabs) {
                        tab.setController(tabController);
                        tab.validateId();
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
        checkDestroy();

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
        checkDestroy();

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
        checkDestroy();

        return tabs;
    }

    public boolean isOneTabMode() {
        checkDestroy();

        return getTabsCount() == 1;
    }

    public int getTabsCount() {
        checkDestroy();

        return tabs.size();
    }

    public Tab getTabAt(int i) {
        checkDestroy();

        return tabs.get(i);
    }

    public int getTabPosition(UUID tabId) {
        checkDestroy();

        Tab tab = getTab(tabId);
        if (tab == null) {
            return -1;
        }
        return tabs.indexOf(tab);
    }

    public Tab getTab(UUID uuid) {
        checkDestroy();

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
        checkDestroy();

        if (name.trim().isEmpty()) {
            throw new RuntimeException("Empty name for tab is not allowed!");
        }
        addTab(new LocalItemsTab(name));
    }

    public void addTab(@NonNull Tab tab) {
        checkDestroy();

        tab.attach(tabController);
        this.tabs.add(tab);
        internalOnTabChanged();
        queueSave();
    }

    public void deleteTab(Tab tab) {
        checkDestroy();

        if (getTabsCount() == 1) {
            throw new SecurityException("Not allowed one tab (after delete tabs count == 0)");
        }
        this.tabs.remove(tab);
        internalOnTabChanged();
        queueSave();
    }

    public void moveTabs(int positionFrom, int positionTo) {
        checkDestroy();

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
        checkDestroy();

        for (Tab tab : getTabs()) {
            Item i = tab.getItemById(id);
            if (i != null) return i;
        }
        return null;
    }

    public void destroy() {
        saveAllDirect();
        saveThread.disable();

        for (Tab tab : tabs) {
            tab.detach();
        }

        destroyed = true;
    }

    private void checkDestroy() {
        if (destroyed) {
            throw new RuntimeException("This ItemManager destroyed!");
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    private class LocalItemTabsController implements TabController {
        @Override
        public void save(@NonNull Tab tab) {
            ItemManager.this.queueSave();
        }
        @Override
        public void nameChanged(@NonNull final Tab tab) {
            ItemManager.this.internalOnTabChanged();
            ItemManager.this.queueSave();
        }

        @Override
        public UUID generateId() {
            return UUID.randomUUID();
        }
    }

    // ==== Tick ====
    public void tick(TickSession tickSession, List<UUID> uuids) {
        checkDestroy();

        Debug.latestPersonalTickDuration = Logger.countOnlyDur(() -> {
            for (UUID uuid : uuids) {
                for (Tab tab : tabs.toArray(new Tab[0])) {
                    Item i = tab.getItemById(uuid);
                    if (i != null) {
                        i.tick(tickSession);
                    }
                }
            }
        });
    }

    public void tick(TickSession tickSession) {
        checkDestroy();

        Debug.latestTickDuration = Logger.countOnlyDur(() -> {
            for (Tab tab : tabs.toArray(new Tab[0])) {
                if (tab == null) continue;
                tab.tick(tickSession);
            }
        });
    }

    public void queueSave() {
        queueSave(SaveInitiator.USER);
    }

    public void queueSave(SaveInitiator initiator) {
        if (saveThread == null) {
            saveThread = new SaveThread();
            saveThread.start();
        }
        saveThread.request(initiator);
    }

    public boolean saveAllDirect() {
        if (DEBUG_ITEMS_SET) return false;
        try {
            // Save
            {
                JSONObject jRoot = new JSONObject();
                JSONArray jTabs = TabCodecUtil.exportTabList(this.tabs).toJSONArray();
                jRoot.put("tabs", jTabs);
                String originalData = jRoot.toString(2);

                FileUtil.setText(dataOriginalFile, originalData);

                GZIPOutputStream gz = new GZIPOutputStream(Files.newOutputStream(dataCompressFile.toPath()));
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
            Debug.saved();
            return true;
        } catch (Exception e) {
            Logger.e(TAG, "saveAllDirect", e);
            App.exception(null, e);
            try {
                new Handler(App.get().getMainLooper()).post(() -> Toast.makeText(App.get(), "Error: Save exception: " + e + "; cause: " + e.getCause(), Toast.LENGTH_LONG).show());
            } catch (Exception ignored) {}
            return false;
        }
    }

    private class SaveThread extends Thread {
        public boolean enabled = true;
        public boolean request = false;
        private byte requestImportance = 0;
        public int requestsCount = 0;
        public long firstRequestTime = 0;
        public long latestRequestTime = 0;

        public SaveThread() {
            super.setName("ItemManager-SaveThread");
        }

        @Override
        public void run() {
            while (!isInterrupted() && enabled) {
                if (request && (requestImportance > 0 || ((System.currentTimeMillis() - firstRequestTime) > 1000 * 10))) {
                    request = false;
                    requestsCount = Debug.latestSaveRequestsCount = 0;
                    firstRequestTime = 0;
                    latestRequestTime = 0;
                    requestImportance = 0;

                    // Save
                    internalSave();
                    if (App.LOG) Logger.i(TAG, String.format("SaveThread: requestCount=%s\nfirstTime=%s\nlatestTime=%s", requestsCount, firstRequestTime, latestRequestTime));
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
            interrupt();
        }

        private void internalSave() {
            try {
                ItemManager.this.saveAllDirect();
            } catch (Exception e) {
                Logger.e(TAG, "SaveThread internalSave", e);
                try {
                    new Handler(App.get().getMainLooper()).post(() -> Toast.makeText(App.get(), "Error: Save exception: " + e + "; cause: " + e.getCause(), Toast.LENGTH_LONG).show());
                } catch (Exception ignored) {}
                try {
                    App.exception(null, e);
                } catch (Exception ignored) {}
            }
        }

        public void request(SaveInitiator initiator) {
            if (!request) {
                firstRequestTime = System.currentTimeMillis();
            }
            if (requestImportance == 0 && initiator == SaveInitiator.USER) requestImportance = 1;
            request = true;
            latestRequestTime = System.currentTimeMillis();
            requestsCount = Debug.latestSaveRequestsCount = requestsCount + 1;
        }

        public void disable() {
            this.enabled = false;
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
        //addTab(new LocalItemsTab("Debug1", tab1items.toArray(new Item[0])));
        //addTab(new LocalItemsTab("Debug2", tab2items.toArray(new Item[0])));
        return tabs;
    }
}

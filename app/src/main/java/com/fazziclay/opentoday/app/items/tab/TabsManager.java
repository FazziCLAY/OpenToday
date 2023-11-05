package com.fazziclay.opentoday.app.items.tab;

import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.api.EventHandler;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.CrashReportContext;
import com.fazziclay.opentoday.app.ImportantDebugCallback;
import com.fazziclay.opentoday.app.Translation;
import com.fazziclay.opentoday.app.data.CherryOrchard;
import com.fazziclay.opentoday.app.events.items.ItemsRootDestroyedEvent;
import com.fazziclay.opentoday.app.events.items.ItemsRootInitializedEvent;
import com.fazziclay.opentoday.app.items.ItemPath;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.SaveInitiator;
import com.fazziclay.opentoday.app.items.callback.OnTabsChanged;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemUtil;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.app.items.tick.Tickable;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.SafeFileUtil;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;
import com.fazziclay.opentoday.util.callback.CallbackStorage;
import com.fazziclay.opentoday.util.time.TimeUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class TabsManager implements ItemsRoot, Tickable {
    public static final int TAB_NAME_MAX_LENGTH = 35;

    private static final boolean DEBUG_ITEMS_SET = false; // Default value is FALSE!!!!!!!!!
    private static final String TAG = "TabsManager";

    private boolean destroyed;
    @NotNull private final File dataOriginalFile;
    @NotNull private final File dataCompressFile;
    @NotNull private final File dataCompressBakFile;
    private boolean debugPrintSaveStatusAlways = false;
    private SaveThread saveThread = null;
    @NotNull @RequireSave
    @SaveKey(key = "tabs") private final List<Tab> tabs = new ArrayList<>();
    @NotNull private final TabController tabController = new LocalItemTabsController();
    @NotNull private final CallbackStorage<OnTabsChanged> onTabsChangedCallbacks = new CallbackStorage<>();
    @NotNull private final Translation translation;

    public TabsManager(@NotNull final File dataOriginalFile, @NotNull final File dataCompressFile, @NotNull final File dataCompressBakFile, @NotNull final Translation translation) {
        this.dataCompressBakFile = dataCompressBakFile;
        this.destroyed = false;
        this.dataOriginalFile = dataOriginalFile;
        this.dataCompressFile = dataCompressFile;
        this.translation = translation;
        load();
        EventHandler.call(new ItemsRootInitializedEvent(this));
    }

    @Nullable
    @Override
    public ItemsStorage getItemsStorageById(final UUID id) {
        checkDestroy();

        final Object find = getById(id);
        if (find instanceof ItemsStorage itemsStorageFind) return itemsStorageFind;
        return null;
    }

    @Override
    @Nullable
    public Tab getTabById(UUID uuid) {
        checkDestroy();
        if (uuid == null) return null;

        UUID find = null;
        Tab findTab = null;
        for (Tab tab : getAllTabs()) {
            if (Objects.equals(uuid, tab.getId())) {
                if (find != null) {
                    ImportantDebugCallback.pushStatic(TAG + " getTabById id duplicate: findTab="+findTab+" find="+find + "tab="+tab);
                }

                find = uuid;
                findTab = tab;
            }
        }
        return findTab;
    }

    @Override
    @Nullable
    public Item getItemById(@NotNull UUID id) {
        checkDestroy();

        for (Tab tab : getAllTabs()) {
            Item item = tab.getItemById(id);
            if (item != null) return item;
        }
        return null;
    }

    @Override
    public boolean isExistById(UUID id) {
        checkDestroy();

        final Tab tab = getTabById(id);
        if (tab != null) return true;

        final Item item = getItemById(id);
        return item != null;
    }

    @Nullable
    @Override
    public Type getTypeById(UUID id) {
        checkDestroy();

        final Tab tab = getTabById(id);
        if (tab != null) return Type.TAB;

        final Item item = getItemById(id);
        if (item != null) return Type.ITEM;

        return null;
    }

    @Nullable
    @Override
    public Object getById(UUID id) {
        checkDestroy();

        final Tab tab = getTabById(id);
        if (tab != null) return tab;

        return getItemById(id);
    }

    @NonNull
    @Override
    public Translation getTranslation() {
        return translation;
    }

    @NotNull
    @Override
    public UUID generateUniqueId() {
        final int MAX_ITER = 1000;
        int i = 0;
        UUID uuid = UUID.randomUUID();
        while (i < MAX_ITER) {
            if (isExistById(uuid)) {
                uuid = UUID.randomUUID();
            } else {
                return uuid;
            }
            i++;
        }
        throw new RuntimeException("The maximum number of iterations (MAX_ITER="+MAX_ITER+") when generating a unique ID has been reached!");
    }

    public boolean isTabAttached(Tab tab) {
        return this.tabs.contains(tab);
    }

    // ==== TABS ====
    @NotNull
    public Tab[] getAllTabs() {
        checkDestroy();
        return tabs.toArray(new Tab[0]);
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
        if (i < 0 || i >= tabs.size()) throw new IndexOutOfBoundsException("getTabAt: index " + i + " index out of bounds tabs list.");
        return tabs.get(i);
    }

    public int getTabPosition(Tab tab) {
        checkDestroy();
        return tabs.indexOf(tab);
    }

    @NotNull
    public Tab getFirstTab() {
        return getAllTabs()[0];
    }

    @SuppressWarnings("UnusedReturnValue")
    public Tab createLocalTab(@NotNull String name) {
        checkDestroy();
        name = name.trim();

        if (name.isEmpty()) {
            throw new RuntimeException("Tab can't be create with empty name!");
        }
        if (name.length() > TAB_NAME_MAX_LENGTH) {
            throw new RuntimeException("Maximum length exceeded: " + TAB_NAME_MAX_LENGTH);
        }
        LocalItemsTab tab = new LocalItemsTab(name);
        addTab(tab);
        return tab;
    }

    public void addTab(@NotNull Tab tab) {
        checkDestroy();
        TabUtil.throwIsAttached(tab);

        this.tabs.add(tab);
        tab.attach(tabController);
        onTabsChangedCallbacks.run((callbackStorage, callback) -> callback.onTabAdded(tab, getTabPosition(tab)));
        internalOnTabChanged();
        queueSave(SaveInitiator.USER);
    }

    public void deleteTab(Tab tab) {
        checkDestroy();
        if (isOneTabMode()) throw new SecurityException("A single tab cannot be deleted!");
        if (!isTabAttached(tab)) throw new RuntimeException("The tab is not attached so that is can be deleted!");

        int position = getTabPosition(tab);
        onTabsChangedCallbacks.run((callbackStorage, callback) -> callback.onTabPreDeleted(tab, position));
        this.tabs.remove(tab);
        tab.detach();
        onTabsChangedCallbacks.run((callbackStorage, callback) -> callback.onTabPostDeleted(tab, position));
        internalOnTabChanged();
        queueSave(SaveInitiator.USER);
    }

    public void moveTabs(int positionFrom, int positionTo) {
        checkDestroy();

        Tab from = getTabAt(positionFrom);
        // move trick BEGIN
        tabs.remove(from);
        tabs.add(positionTo, from);
        // move trick END
        onTabsChangedCallbacks.run((callbackStorage, callback) -> callback.onTabMoved(from, positionFrom, positionTo));
        internalOnTabChanged();
        queueSave(SaveInitiator.USER);
    }

    private void internalOnTabChanged() {
        onTabsChangedCallbacks.run((callbackStorage, callback) -> callback.onTabsChanged(getAllTabs()));
    }

    @NonNull
    public CallbackStorage<OnTabsChanged> getOnTabsChangedCallbacks() {
        return onTabsChangedCallbacks;
    }


    public void destroy() {
        saveAllDirect();
        if (saveThread != null) saveThread.disable();

        for (Tab tab : tabs) {
            tab.detach();
        }

        destroyed = true;
        EventHandler.call(new ItemsRootDestroyedEvent(this));
    }

    private void checkDestroy() {
        if (isDestroyed()) {
            throw new RuntimeException("This TabManager is destroyed!");
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }


    private class LocalItemTabsController implements TabController {
        @Override
        public void save(@NonNull Tab tab) {
            // What is USER mode?
            // A direct save call means somethings important,
            // unimportant changes will be processed in TickSession.saveNeeded()
            TabsManager.this.queueSave(SaveInitiator.USER);
        }
        @Override
        public void nameChanged(@NonNull final Tab tab) {
            onTabsChangedCallbacks.run((callbackStorage, callback) -> callback.onTabRenamed(tab, getTabPosition(tab)));
            TabsManager.this.internalOnTabChanged();
            TabsManager.this.queueSave(SaveInitiator.USER);
        }

        @Override
        public UUID generateId() {
            return TabsManager.this.generateUniqueId();
        }

        @Override
        public ItemsRoot getRoot() {
            return TabsManager.this;
        }

        @Override
        public void iconChanged(Tab tab) {
            onTabsChangedCallbacks.run((callbackStorage, callback) -> callback.onTabIconChanged(tab, getTabPosition(tab)));
            TabsManager.this.internalOnTabChanged();
            TabsManager.this.queueSave(SaveInitiator.USER);
        }
    }

    // ==== Tick ====
    public void tick(TickSession tickSession, List<UUID> uuids) {
        CrashReportContext.BACK.push("TabsManager tick personal");
        checkDestroy();

        Debug.tickedPersonal(tickSession);
        Debug.latestPersonalTickDuration = Logger.countOnlyDur(() -> {
            for (UUID uuid : uuids) {
                Object find = getById(uuid);
                if (find instanceof Tickable tickableFind) {
                    tickableFind.tick(tickSession);
                }
            }
        });
        CrashReportContext.BACK.pop();
    }

    @Override
    public void tick(TickSession tickSession) {
        CrashReportContext.BACK.push("TabsManager tick");
        checkDestroy();

        Debug.tickedItems = 0;
        Debug.ticked(tickSession);
        Debug.latestTickDuration = Logger.countOnlyDur(() -> {
            for (Tab tab : getAllTabs()) {
                if (tab == null || tab.isDisableTick()) continue;
                CrashReportContext.BACK.push("TickTab_"+tab.getId());
                tab.tick(tickSession);
                CrashReportContext.BACK.pop();
            }
        });
        CrashReportContext.BACK.pop();
    }


    public void queueSave(SaveInitiator initiator) {
        if (saveThread == null || !saveThread.isEnabled()) {
            saveThread = new SaveThread();
            saveThread.start();
        }
        saveThread.request(initiator);
    }

    private void load() {
        if (DEBUG_ITEMS_SET) {
            tabs.addAll(generateDebugDataSet());

        } else {
            load0();
        }
        if (tabs.isEmpty()) {
            createLocalTab(getTranslation().get(Translation.KEY_TABS_DEFAULT_MAIN_NAME));
        }

        // Check id duplication
        boolean detected = false;
        List<UUID> list = new ArrayList<>();
        for (Tab tab : tabs) {
            UUID tabId = ItemUtil.getId(tab);
            if (list.contains(tabId)) {
                Logger.i(TAG, "ID Duplication detected! tab="+tab+" tabId="+tabId+"; Regenerating...");
                tab.regenerateId();
                detected = true;
            }
            list.add(tabId);

            Item[] allItemsInTab = ItemUtil.getAllItemsInTree(tab.getAllItems());
            for (Item item : allItemsInTab) {
                UUID itemId = item.getId();
                if (list.contains(itemId)) {
                    Logger.i(TAG, "ID Duplication detected! tab="+tab+" tabId="+tabId+"; item="+item+" itemId="+itemId+"; Regenerating...");
                    ItemUtil.regenerateIdForItem(item);
                    detected = true;
                }
                list.add(itemId);
            }
        }
        if (detected) {
            saveAllDirect();
        }
    }

    @Nullable
    private JSONObject loadJsonObjectFromCompress() {
        try {
            if (SafeFileUtil.isExist(dataCompressFile)) {
                Reader reader = new InputStreamReader(new GZIPInputStream(SafeFileUtil.getFileInputSteam(dataCompressFile)));

                final StringBuilder result = new StringBuilder();
                final char[] buff = new char[1024];
                int i;
                while ((i = reader.read(buff)) > 0) {
                    result.append(new String(buff, 0, i));
                }
                reader.close();
                return new JSONObject(result.toString());
            } else {
                Logger.w(TAG, "Load from compress file impossible: file not exist. Trying load from .bak file...");
            }
        } catch (Exception e) {
            Logger.w(TAG, "Load from compress file failed. Trying load .bak file...");
            App.exception(null, e);
        }

        try {
            if (SafeFileUtil.isExist(dataCompressBakFile)) {
                Reader reader = new InputStreamReader(new GZIPInputStream(SafeFileUtil.getFileInputSteam(dataCompressBakFile)));

                final StringBuilder result = new StringBuilder();
                final char[] buff = new char[1024];
                int i;
                while ((i = reader.read(buff)) > 0) {
                    result.append(new String(buff, 0, i));
                }
                reader.close();
                return new JSONObject(result.toString());
            } else {
                Logger.w(TAG, "Load from .bak file impossible: file not exist.");
            }
        } catch (Exception e) {
            Logger.w(TAG, "Load from .bak file failed.");
            App.exception(null, e);
        }
        return null;
    }

    private void load0() {
        boolean isOriginal = FileUtil.isExist(dataOriginalFile);
        boolean isCompress = SafeFileUtil.isExistOrBack(dataCompressFile, dataCompressBakFile);
        if (isCompress || isOriginal) {
            try {
                JSONObject jsonRoot = null;
                try {
                    jsonRoot = loadJsonObjectFromCompress();

                } catch (Exception e) {
                    Logger.e(TAG, "Exception while load compress data", e);
                }

                if (jsonRoot == null) {
                    Logger.w(TAG, "Loading from original file...");
                    jsonRoot = new JSONObject(FileUtil.getText(dataOriginalFile, "{}"));
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
                    this.tabs.add(tab);
                }
            } catch (Exception e) {
                throw new RuntimeException("TabsManager load() error! Data maybe break.", e);
            }
        }
    }

    public boolean saveAllDirect() {
        if (DEBUG_ITEMS_SET) return false;
        try {
            // Save
            {
                JSONObject jRoot = new JSONObject();
                JSONArray jTabs = TabCodecUtil.exportTabList(this.tabs).toJSONArray();
                jRoot.put("tabs", jTabs);
                String originalData = jRoot.toString();

                FileUtil.setText(dataOriginalFile, originalData);
                SafeFileUtil.makeBackup(dataCompressFile, dataCompressBakFile);
                GZIPOutputStream gz = new GZIPOutputStream(SafeFileUtil.getFileOutputSteam(dataCompressFile));
                Writer writer = new OutputStreamWriter(gz);
                writer.write(originalData);
                writer.flush();
                writer.close();
                gz.close();
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
            super.setName("TabsManager-SaveThread");
        }

        @Override
        public void run() {
            while (!isInterrupted() && enabled) {
                if (request && (requestImportance > 0 || ((System.currentTimeMillis() - firstRequestTime) > 1000 * 10))) {
                    Logger.i(TAG, String.format("SaveThread: requestCount=%s\nfirstTime=%s\nlatestTime=%s", requestsCount, TimeUtil.getDebugDate(firstRequestTime), TimeUtil.getDebugDate(latestRequestTime)));

                    request = false;
                    requestsCount = Debug.latestSaveRequestsCount = 0;
                    firstRequestTime = 0;
                    latestRequestTime = 0;
                    requestImportance = 0;

                    // Save
                    internalSave();
                }
                try {
                    //noinspection BusyWait
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
            Logger.w(TAG, "SaveThread finished his work! enabled="+enabled+"; isInterrupted="+isInterrupted());
            enabled = false;
            interrupt();
        }

        private void internalSave() {
            try {
                TabsManager.this.saveAllDirect();
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
            if (initiator == SaveInitiator.TICK_PERSONAL) initiator = SaveInitiator.USER; // TODO: 2023.05.26 handle TICK_PERSONAL!

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

        public boolean isEnabled() {
            return enabled;
        }
    }

    private List<Tab> generateDebugDataSet() {
        List<Tab> tabs = new ArrayList<>();
        tabs.add(new Debug202305RandomTab());
        return tabs;
    }


    // ==== Path ====


    @Override
    public ItemPath getPathTo(Object o) {
        if (o instanceof Item item) {
            List<Object> path = new ArrayList<>(Arrays.asList(ItemUtil.getPathToItem(item)));
            path.add(o);
            return new ItemPath(path.toArray());
        }
        return new ItemPath();
    }

    public void setDebugPrintSaveStatusAlways(boolean b) {
        debugPrintSaveStatusAlways = b;
    }
}

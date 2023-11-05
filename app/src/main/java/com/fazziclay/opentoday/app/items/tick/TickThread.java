package com.fazziclay.opentoday.app.items.tick;

import android.content.Context;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.ImportantDebugCallback;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.SaveInitiator;
import com.fazziclay.opentoday.app.items.Unique;
import com.fazziclay.opentoday.app.items.item.CheckboxItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemUtil;
import com.fazziclay.opentoday.app.items.tab.TabsManager;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.profiler.Profiler;
import com.fazziclay.opentoday.util.time.TimeUtil;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class TickThread extends Thread {
    private static final String TAG = "TickThread";
    private static final Profiler PROFILER = App.createProfiler("TickThread");
    private static boolean exceptionOnceDebugFlag = true; // for once call App.exception(...)
    private static final boolean LOG = App.debug(true);
    private static final boolean LOG_ONLY_PERSONAL = true;

    private boolean enabled = true;
    private final Context context;
    private final TabsManager tabsManager;
    private final TickSession defaultTickSession;
    private boolean currentlyExecutingTickPersonal;
    private int requests = 0;
    private boolean requested = false;
    private boolean requestedAll = false;
    private boolean requestedPersonal = false;
    private final List<UUID> personalsNoPaths = new ArrayList<>();
    private final List<UUID> personalsPaths = new ArrayList<>();
    private long firstRequestTime = 0;
    private long lastRequestTime = 0;

    public TickThread(Context context, TabsManager tabsManager) {
        setName("TickThread");
        this.context = context;
        this.tabsManager = tabsManager;
        this.defaultTickSession = createTickSession(context);
        setUncaughtExceptionHandler((thread, throwable) -> {
            Logger.e(TAG, "Exception in thread!", throwable);
            ImportantDebugCallback.pushStatic("TickThread exception: " + throwable);
            if (exceptionOnceDebugFlag) {
                App.exception(null, new RuntimeException(throwable));
                exceptionOnceDebugFlag = false;
            }
        });
    }

    private void log(String s) {
        if (LOG) {
            if (LOG_ONLY_PERSONAL) {
                if (currentlyExecutingTickPersonal) {
                    Logger.d(TAG, s);
                }
            } else {
                Logger.d(TAG, s);
            }
        }
    }

    @Override
    public void run() {
        PROFILER.push("run");
        enabled = true;
        while (!isInterrupted() && enabled) {
            PROFILER.push("tick");
            long tickStart = System.currentTimeMillis();
            if (requested) {
                try {
                    log("Processing requests: " + requests + "; all: "+requestedAll+"; personal: " + requestedPersonal);

                    PROFILER.push("all");
                    if (requestedAll) {
                        recycle(false);
                        tickAll();
                    }

                    PROFILER.swap("personal");
                    if (requestedPersonal) {
                        recycle(true);
                        tickPersonal();
                    }

                    PROFILER.swap("save");
                    if (defaultTickSession.isSaveNeeded()) {
                        if (defaultTickSession.isImportantSaveNeeded()) {
                            tabsManager.queueSave(SaveInitiator.USER);
                        } else {
                            tabsManager.queueSave(requestedPersonal ? SaveInitiator.TICK_PERSONAL : SaveInitiator.TICK);
                        }
                    }
                    PROFILER.pop();
                } catch (Exception e) {
                    PROFILER.push("exceptions");
                    Logger.e(TAG, "EXCEPTION IN TICK!!!!!!!", e);
                    if (exceptionOnceDebugFlag) {
                        App.exception(null, e);
                        exceptionOnceDebugFlag = false;
                    }
                    PROFILER.pop2();
                }

                // reset requests
                PROFILER.push("resets");
                personalsNoPaths.clear();
                personalsPaths.clear();
                requested = false;
                requestedPersonal = false;
                requestedAll = false;
                firstRequestTime = 0;
                lastRequestTime = 0;
                requests = 0;
                PROFILER.pop();
            }
            long tickEnd = System.currentTimeMillis();

            PROFILER.swap("sleep");
            try {
                int sleep = (int) (1000 - (tickEnd - tickStart));
                if (sleep <= 0) sleep = 1;
                //noinspection BusyWait
                Thread.sleep(sleep);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            PROFILER.pop();
        }
        log("Thread done.");
        interrupt();
        enabled = false;
        PROFILER.pop();
    }

    private void tickAll() {
        PROFILER.push("tickAll");
        tabsManager.tick(defaultTickSession);
        log("Tick all");
        PROFILER.pop();
    }

    private void tickPersonal() {
        PROFILER.push("tickPersonal");
        currentlyExecutingTickPersonal = true;
        if (!this.personalsNoPaths.isEmpty()) {
            List<UUID> personalsNoPaths = new ArrayList<>(this.personalsNoPaths);
            log("Tick personal(no-paths): "+ personalsNoPaths);
            tabsManager.tick(defaultTickSession, personalsNoPaths);
        }

        if (!this.personalsPaths.isEmpty()) {
            List<UUID> personalsPaths = new ArrayList<>(this.personalsPaths);
            log("Tick personal(paths): " + personalsPaths);
            for (UUID personal : personalsPaths) {
                Item item = tabsManager.getItemById(personal);
                if (item == null) {
                    log("Item by id '" + personal + "' not found. Maybe personalTick called for deleted item: 'continue' in for-each;");
                    continue;
                }
                List<UUID> pathWhitelisted = new ArrayList<>();
                pathWhitelisted.add(item.getId());
                for (ItemsStorage itemsStorage : ItemUtil.getPathToItemNoReverse(item)) {
                    if (itemsStorage instanceof Unique unique) {
                        pathWhitelisted.add(unique.getId());
                    }
                }
                log("Tick personal(paths): resultWhitelist for item=" + item + ": " + pathWhitelisted);
                defaultTickSession.recycleWhitelist(true, pathWhitelisted);
                tabsManager.tick(defaultTickSession);
            }
        }


        currentlyExecutingTickPersonal = false;
        PROFILER.pop();
    }

    private void recycle(boolean personal) {
        PROFILER.push("recycle");
        log("recycle. personal="+personal);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        GregorianCalendar noTimeCalendar = TimeUtil.noTimeCalendar(gregorianCalendar);
        int daySeconds = (int) ((gregorianCalendar.getTimeInMillis() - noTimeCalendar.getTimeInMillis()) / 1000);


        defaultTickSession.recycleGregorianCalendar(gregorianCalendar);
        defaultTickSession.recycleNoTimeCalendar(noTimeCalendar);
        defaultTickSession.recycleDaySeconds(daySeconds);
        defaultTickSession.recyclePersonal(personal);
        defaultTickSession.recycleSaveNeeded();
        defaultTickSession.recycleWhitelist(false);
        defaultTickSession.recycleSpecifiedTickTarget();
        PROFILER.pop();
    }


    public void instantlyCheckboxTick(CheckboxItem item) {
        TickSession tickSession = createTickSession(context);

        List<UUID> pathWhitelisted = new ArrayList<>();
        pathWhitelisted.add(item.getId());
        for (ItemsStorage itemsStorage : ItemUtil.getPathToItemNoReverse(item)) {
            if (itemsStorage instanceof Unique unique) {
                pathWhitelisted.add(unique.getId());
            }
        }

        tickSession.recycleWhitelist(true, pathWhitelisted);
        tickSession.recyclePersonal(true);
        tabsManager.tick(tickSession);
        log("Tick instantlyTick");
    }

    public void requestTick() {
        checkEnabled();
        requestUniversal();
        requestedAll = true;
        log("Requested no-personal");
    }

    public void requestPersonalTick(List<UUID> uuids, boolean usePaths) {
        checkEnabled();
        requestUniversal();
        requestedPersonal = true;
        if (usePaths) {
            personalsPaths.addAll(uuids);
        } else {
            personalsNoPaths.addAll(uuids);
        }
        log("Requested personal(paths="+usePaths+"): " + uuids);
    }

    private void requestUniversal() {
        final long currMs = System.currentTimeMillis();
        if (!requested) {
            firstRequestTime = currMs;
        }
        lastRequestTime = currMs;
        requests++;
        requested = true;
    }

    private void checkEnabled() {
        if (!enabled) throw new RuntimeException("TickThread is disabled!");
    }

    private TickSession createTickSession(Context context) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        GregorianCalendar noTimeCalendar = TimeUtil.noTimeCalendar(gregorianCalendar);
        int daySeconds = (int) ((gregorianCalendar.getTimeInMillis() - noTimeCalendar.getTimeInMillis()) / 1000);


        return new TickSession(App.get(context).getItemNotificationHandler(), gregorianCalendar, noTimeCalendar, daySeconds, false, PROFILER);
    }

    public void requestTerminate() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public GregorianCalendar getGregorianCalendar() {
        return defaultTickSession.getGregorianCalendar();
    }
}

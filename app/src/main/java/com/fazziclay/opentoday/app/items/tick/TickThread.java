package com.fazziclay.opentoday.app.items.tick;

import android.content.Context;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.items.tab.TabsManager;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.item.ItemUtil;
import com.fazziclay.opentoday.app.items.SaveInitiator;
import com.fazziclay.opentoday.app.items.Unique;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.util.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class TickThread extends Thread {
    private static final String TAG = "TickThread";
    private static final boolean LOG = App.debug(true);
    private static final boolean LOG_ONLY_PERSONAL = true;

    private boolean enabled = true;
    private final TabsManager tabsManager;
    private final TickSession defaultTickSession;
    private final List<UUID> personals = new ArrayList<>();
    private boolean currentlyExecutingTickPersonal;
    private int requests = 0;
    private boolean requested = false;
    private boolean personalOnly = true;
    private boolean personalUsePaths = false;
    private long firstRequestTime = 0;
    private long lastRequestTime = 0;

    public TickThread(Context context, TabsManager tabsManager) {
        setName("TickThread");
        this.tabsManager = tabsManager;
        this.defaultTickSession = createTickSession(context);
        setUncaughtExceptionHandler((thread, throwable) -> Logger.e(TAG, "Exception in thread!", throwable));
    }

    private void log(String s) {
        if (App.LOG && LOG) {
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
        while (!isInterrupted() && enabled) {
            long tickStart = System.currentTimeMillis();
            if (requested) {
                log("Processing requests: " + requests + "; personalOnly: "+personalOnly);

                if (personalOnly) {
                    recycle(true);
                    tickPersonal();
                } else {
                    recycle(false);
                    tickAll();
                    if (!personals.isEmpty()) {
                        defaultTickSession.recyclePersonal(true);
                        tickPersonal();
                    }
                }

                if (defaultTickSession.isSaveNeeded()) {
                    tabsManager.queueSave(!personals.isEmpty() ? SaveInitiator.TICK_PERSONAL : SaveInitiator.TICK);
                }

                // reset requests
                personals.clear();
                requested = false;
                firstRequestTime = 0;
                lastRequestTime = 0;
                requests = 0;
                personalOnly = true;
                personalUsePaths = false;
            }
            long tickEnd = System.currentTimeMillis();

            try {
                int sleep = (int) (1000 - (tickEnd - tickStart));
                if (sleep <= 0) sleep = 1;
                //noinspection BusyWait
                Thread.sleep(sleep);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        log("Thread done.");
        interrupt();
        enabled = false;
    }

    private void tickAll() {
        tabsManager.tick(defaultTickSession);
        log("Tick all");
    }

    private void tickPersonal() {
        currentlyExecutingTickPersonal = true;
        if (personalUsePaths) {
            log("Tick personal(paths): "+ personals);
            for (UUID personal : personals) {
                Item item = tabsManager.getItemById(personal);
                List<UUID> pathWhitelisted = new ArrayList<>();
                pathWhitelisted.add(item.getId());
                for (ItemsStorage itemsStorage : ItemUtil.getPathToItem(item)) {
                    if (itemsStorage instanceof Unique unique) {
                        pathWhitelisted.add(unique.getId());
                    }
                }
                log("Tick personal(paths): resultWhitelist for item="+item+": "+ pathWhitelisted);
                defaultTickSession.recycleWhitelist(true, pathWhitelisted);
                tabsManager.tick(defaultTickSession);
            }
        } else {
            log("Tick personal(no-paths): "+ personals);
            tabsManager.tick(defaultTickSession, personals);
        }
        currentlyExecutingTickPersonal = false;
    }

    private void recycle(boolean personal) {
        log("recycle. personal="+personal);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();

        // START - day seconds
        GregorianCalendar noTimeCalendar = new GregorianCalendar(
                gregorianCalendar.get(Calendar.YEAR),
                gregorianCalendar.get(Calendar.MONTH),
                gregorianCalendar.get(Calendar.DAY_OF_MONTH));
        int daySeconds = (int) ((gregorianCalendar.getTimeInMillis() - noTimeCalendar.getTimeInMillis()) / 1000);
        // END - day seconds


        defaultTickSession.recycleGregorianCalendar(gregorianCalendar);
        defaultTickSession.recycleNoTimeCalendar(noTimeCalendar);
        defaultTickSession.recycleDaySeconds(daySeconds);
        defaultTickSession.recyclePersonal(personal);
        defaultTickSession.recycleSaveNeeded();
        defaultTickSession.recycleWhitelist(false);
        defaultTickSession.recycleSpecifiedTickTarget();
    }

    public void requestTick() {
        if (!enabled) throw new RuntimeException("TickThread disabled!");
        if (!requested) {
            firstRequestTime = System.currentTimeMillis();
        }
        personalOnly = false;
        lastRequestTime = System.currentTimeMillis();
        requested = true;
        requests++;
        log("Requested no-personal");
    }

    public void requestPersonalTick(List<UUID> uuids, boolean usePaths) {
        if (!enabled) throw new RuntimeException("TickThread disabled!");

        if (!requested) {
            firstRequestTime = System.currentTimeMillis();
        }
        personals.addAll(uuids);
        lastRequestTime = System.currentTimeMillis();
        requested = true;
        requests++;
        personalUsePaths = usePaths;
        log("Requested personal: " + uuids);
    }

    private TickSession createTickSession(Context context) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();

        // START - day seconds
        GregorianCalendar noTimeCalendar = new GregorianCalendar(
                gregorianCalendar.get(Calendar.YEAR),
                gregorianCalendar.get(Calendar.MONTH),
                gregorianCalendar.get(Calendar.DAY_OF_MONTH));
        int daySeconds = (int) ((gregorianCalendar.getTimeInMillis() - noTimeCalendar.getTimeInMillis()) / 1000);
        // END - day seconds

        return new TickSession(context, gregorianCalendar, noTimeCalendar, daySeconds, false);
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

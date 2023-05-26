package com.fazziclay.opentoday.app.items.tick;

import android.content.Context;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.items.ItemManager;
import com.fazziclay.opentoday.app.items.SaveInitiator;
import com.fazziclay.opentoday.util.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class TickThread extends Thread {
    private static final String TAG = "TickThread";
    private static final boolean LOG = App.debug(true);


    private boolean enabled = true;
    private final ItemManager itemManager;
    private final TickSession defaultTickSession;
    private final List<UUID> personals = new ArrayList<>();
    private int requests = 0;
    private boolean requested = false;
    private boolean personalOnly = true;
    private long firstRequestTime = 0;
    private long lastRequestTime = 0;

    public TickThread(Context context, ItemManager itemManager) {
        setName("TickThread");
        this.itemManager = itemManager;
        this.defaultTickSession = createTickSession(context);
        setUncaughtExceptionHandler((thread, throwable) -> Logger.e(TAG, "Exception in thread!", throwable));
    }

    private void log(String s) {
        if (App.LOG && LOG) {
            Logger.d(TAG, s);
        }
    }

    @Override
    public void run() {
        while (!isInterrupted() && enabled) {
            if (requested) {
                log("Processing requests: " + requests + "; personalOnly: "+personalOnly);

                if (personalOnly) {
                    recycle(true);
                    itemManager.tick(defaultTickSession, personals);
                    log("Tick personal: "+ personals);
                } else {
                    recycle(false);
                    itemManager.tick(defaultTickSession);
                    log("Tick all");
                    if (!personals.isEmpty()) {
                        defaultTickSession.recyclePersonal(true);
                        itemManager.tick(defaultTickSession, personals);
                        log("Tick personal: "+ personals);
                    }
                }

                if (defaultTickSession.isSaveNeeded()) {
                    itemManager.queueSave(SaveInitiator.TICK);
                }

                // reset requests
                personals.clear();
                requested = false;
                firstRequestTime = 0;
                lastRequestTime = 0;
                requests = 0;
                personalOnly = true;
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        log("Thread done.");
        interrupt();
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

    public void requestTick(List<UUID> uuids) {
        if (!enabled) throw new RuntimeException("TickThread disabled!");

        if (!requested) {
            firstRequestTime = System.currentTimeMillis();
        }
        personals.addAll(uuids);
        lastRequestTime = System.currentTimeMillis();
        requested = true;
        requests++;
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

    public GregorianCalendar getGregorianCalendar() {
        return defaultTickSession.getGregorianCalendar();
    }
}

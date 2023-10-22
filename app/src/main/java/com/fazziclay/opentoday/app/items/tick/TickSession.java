package com.fazziclay.opentoday.app.items.tick;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.ItemNotificationHandler;
import com.fazziclay.opentoday.app.items.Unique;
import com.fazziclay.opentoday.util.Logger;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

public class TickSession {
    private static final String TAG = "TickSession";
    private static final boolean LOG_ISALLOWED = App.debug(false);

    // Life-hacks :)
    public static GregorianCalendar getLatestGregorianCalendar() {
        GregorianCalendar gregorianCalendar1 = App.get().getTickThread().getGregorianCalendar();
        if (gregorianCalendar1 ==  null) {
            Logger.w(TAG, "getLatestGregorianCalendar: null calendar!!!! C:98d78820");
            return new GregorianCalendar();
        }
        return gregorianCalendar1;
    }

    private final ItemNotificationHandler itemNotificationHandler;
    private GregorianCalendar gregorianCalendar;
    private GregorianCalendar noTimeCalendar;
    private int dayTime;
    private boolean isPersonalTick;
    private boolean saveNeeded = false;
    private boolean importantSaveNeeded = false;
    private final Stack<List<TickTarget>> specifiedTickTarget = new Stack<>();
    private final List<UUID> whitelist = new ArrayList<>();
    private boolean isWhitelist = false;

    public TickSession(ItemNotificationHandler itemNotificationHandler, GregorianCalendar gregorianCalendar, GregorianCalendar noTimeCalendar, int dayTime, boolean isPersonalTick) {
        this.itemNotificationHandler = itemNotificationHandler;
        this.gregorianCalendar = gregorianCalendar;
        this.noTimeCalendar = noTimeCalendar;
        this.dayTime = dayTime;
        this.isPersonalTick = isPersonalTick;
    }

    public boolean isAllowed(Unique unique) {
        if (LOG_ISALLOWED) Logger.d(TAG, "isAllowed(" + unique + "): id="+unique==null ? "(unique is null)" : unique.getId());
        if (isWhitelist) {
            if (unique == null) {
                Logger.w(TAG, "isAllowed: whitelist=true_, unique=null_. Return: true");
                return true;
            }
            UUID uuid = unique.getId();
            return whitelist.contains(uuid);
        }
        return true;
    }

    public boolean isTickTargetAllowed(TickTarget tickTarget) {
        if (specifiedTickTarget.isEmpty()) return true;
        return specifiedTickTarget.lastElement().contains(tickTarget);
    }

    public void runWithSpecifiedTickTargets(List<TickTarget> targets, Runnable r) {
        specifiedTickTarget.push(targets);
        r.run();
        specifiedTickTarget.pop();
    }

    public GregorianCalendar getGregorianCalendar() {
        return gregorianCalendar;
    }

    public GregorianCalendar getNoTimeCalendar() {
        return noTimeCalendar;
    }

    public int getDayTime() {
        return dayTime;
    }

    public boolean isPersonalTick() {
        return isPersonalTick;
    }

    public void saveNeeded() {
        saveNeeded = true;
    }

    public boolean isSaveNeeded() {
        return saveNeeded;
    }

    public boolean isImportantSaveNeeded() {
        return importantSaveNeeded;
    }

    public void recyclePersonal(boolean b) {
        this.isPersonalTick = b;
    }

    public void recycleDaySeconds(int t) {
        this.dayTime = t;
    }

    public void recycleNoTimeCalendar(GregorianCalendar c) {
        this.noTimeCalendar = c;
    }

    public void recycleGregorianCalendar(GregorianCalendar c) {
        this.gregorianCalendar = c;
    }

    public void recycleSaveNeeded() {
        this.saveNeeded = false;
        this.importantSaveNeeded = false;
    }

    public void recycleWhitelist(boolean isWhitelist, List<UUID> whitelist) {
        this.isWhitelist = isWhitelist;
        this.whitelist.clear();
        this.whitelist.addAll(whitelist);
    }

    public void recycleWhitelist(boolean isWhitelist) {
        this.isWhitelist = isWhitelist;
        this.whitelist.clear();
    }

    public void recycleSpecifiedTickTarget() {
        this.specifiedTickTarget.clear();
    }

    public void setStartSpecifiedTickTarget(List<TickTarget> start) {
        this.specifiedTickTarget.push(start);
    }

    public Object _getWhitelist() {
        return whitelist.toString();
    }

    public Object _isWhitelist() {
        return isWhitelist;
    }

    public void importantSaveNeeded() {
        this.importantSaveNeeded = true;
    }

    public ItemNotificationHandler getItemNotificationHandler() {
        return itemNotificationHandler;
    }

    @NotNull
    @Override
    public String toString() {
        return "TickSession{" +
                "isPersonalTick=" + isPersonalTick +
                ", specifiedTickTarget=" + specifiedTickTarget +
                ", whitelist=" + whitelist +
                '}';
    }
}

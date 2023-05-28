package com.fazziclay.opentoday.app.items.tick;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.items.item.Item;

import java.util.GregorianCalendar;

public class TickSession {
    // Life-hacks :)
    public static GregorianCalendar getLatestGregorianCalendar() {
        return App.get().getTickThread().getGregorianCalendar();
    }

    private final Context context;
    private GregorianCalendar gregorianCalendar;
    private GregorianCalendar noTimeCalendar;
    private int dayTime;
    private boolean isPersonalTick;
    private boolean saveNeeded = false;

    public TickSession(Context context, GregorianCalendar gregorianCalendar, GregorianCalendar noTimeCalendar, int dayTime, boolean isPersonalTick) {
        this.context = context;
        this.gregorianCalendar = gregorianCalendar;
        this.noTimeCalendar = noTimeCalendar;
        this.dayTime = dayTime;
        this.isPersonalTick = isPersonalTick;
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

    public Context getContext() {
        return context;
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

    public void setAlarmDayOfTimeInSeconds(int time, Item item) {
        final long shift = getDayTime() >= time ? (24*60*60*1000L) : 0; // IN MILLIS!!
        final AlarmManager alarmManager = getContext().getSystemService(AlarmManager.class);
        final int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        long triggerAtMs = getNoTimeCalendar().getTimeInMillis() + shift + (time * 1000L) + 599;
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, PendingIntent.getBroadcast(getContext(), item.getId().hashCode() + time, ItemsTickReceiver.createIntent(context, item.getId()).putExtra("debugMessage", "DayItemNotification is work :)\nItem:\n * id-hashCode: " + item.getId().hashCode() + "\n * Item: " + item + " time: " + time), flags));
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
    }
}

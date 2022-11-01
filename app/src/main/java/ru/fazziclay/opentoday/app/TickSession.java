package ru.fazziclay.opentoday.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.GregorianCalendar;

import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.receiver.ItemsTickReceiver;

public class TickSession {
    private final Context context;
    private final GregorianCalendar gregorianCalendar;
    private final GregorianCalendar noTimeCalendar;
    private final int dayTime;
    private boolean saveNeeded = false;

    public TickSession(Context context, GregorianCalendar gregorianCalendar, GregorianCalendar noTimeCalendar, int dayTime) {
        this.context = context;
        this.gregorianCalendar = gregorianCalendar;
        this.noTimeCalendar = noTimeCalendar;
        this.dayTime = dayTime;
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

    public void saveNeeded() {
        saveNeeded = true;
    }

    public boolean isSaveNeeded() {
        return saveNeeded;
    }

    public void setAlarmDayOfTimeInSeconds(int time, Item item) {
        // TODO: 30.10.2022 rewrite
        long shift = getDayTime() >= time ? (24*60*60*1000L) : 0;
        AlarmManager alarmManager = getContext().getSystemService(AlarmManager.class);
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = flags | PendingIntent.FLAG_IMMUTABLE;
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP, getNoTimeCalendar().getTimeInMillis() + shift + (time * 1000L) - 5000, PendingIntent.getBroadcast(getContext(), 0, new Intent(getContext(), ItemsTickReceiver.class).putExtra(ItemsTickReceiver.EXTRA_PERSONAL_TICK, new String[]{item.getId().toString()}).putExtra("debugMessage", "dayItemNotification is work :)"), flags));

    }
}

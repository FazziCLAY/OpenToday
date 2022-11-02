package ru.fazziclay.opentoday.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.GregorianCalendar;

import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.receiver.ItemsTickReceiver;
import ru.fazziclay.opentoday.util.time.ConvertMode;
import ru.fazziclay.opentoday.util.time.TimeUtil;

public class TickSession {
    private final Context context;
    private final GregorianCalendar gregorianCalendar;
    private final GregorianCalendar noTimeCalendar;
    private final int dayTime;
    private final boolean isPersonalTick;
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
        // TODO: 30.10.2022 rewrite
        final long shift = getDayTime() >= time ? (24*60*60*1000L) : 0;
        final AlarmManager alarmManager = getContext().getSystemService(AlarmManager.class);
        final int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP, getNoTimeCalendar().getTimeInMillis() + shift + (time * 1000L) - 5000, PendingIntent.getBroadcast(getContext(), 0, new Intent(getContext(), ItemsTickReceiver.class).putExtra(ItemsTickReceiver.EXTRA_PERSONAL_TICK, new String[]{item.getId().toString()}).putExtra("debugMessage", "dayItemNotification is work :)"), flags));

    }
}

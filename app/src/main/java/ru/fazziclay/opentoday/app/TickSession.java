package ru.fazziclay.opentoday.app;

import android.content.Context;

import java.util.GregorianCalendar;

public class TickSession {
    private final Context context;
    private final GregorianCalendar gregorianCalendar;
    private final int dayTime;
    private boolean saveNeeded = false;

    public TickSession(Context context, GregorianCalendar gregorianCalendar, int dayTime) {
        this.context = context;
        this.gregorianCalendar = gregorianCalendar;
        this.dayTime = dayTime;
    }

    public GregorianCalendar getGregorianCalendar() {
        return gregorianCalendar;
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
}

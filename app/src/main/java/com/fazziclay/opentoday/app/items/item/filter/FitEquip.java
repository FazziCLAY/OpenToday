package com.fazziclay.opentoday.app.items.item.filter;

import java.util.GregorianCalendar;

public class FitEquip {
    private GregorianCalendar calendar;
    private int recycle = 0; // Debug only

    public FitEquip(GregorianCalendar calendar) {
        this.calendar = calendar;
    }

    public FitEquip() {
        this.calendar = null;
    }

    public void recycle(GregorianCalendar calendar) {
        this.calendar = calendar;
        recycle++;
    }


    public GregorianCalendar getGregorianCalendar() {
        return calendar;
    }


    public int getRecycle() {
        return recycle;
    }
}

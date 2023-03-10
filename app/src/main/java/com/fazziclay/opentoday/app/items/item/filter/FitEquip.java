package com.fazziclay.opentoday.app.items.item.filter;

import java.util.GregorianCalendar;

public class FitEquip {
    private final GregorianCalendar calendar;

    public GregorianCalendar getGregorianCalendar() {
        return calendar;
    }

    public FitEquip(GregorianCalendar calendar) {
        this.calendar = calendar;
    }
}

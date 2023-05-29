package com.fazziclay.opentoday.app.items.item.filter;

import com.fazziclay.opentoday.app.items.item.Item;

import java.util.GregorianCalendar;

public class FitEquip {
    private GregorianCalendar calendar;
    private int recycle = 0; // Debug only
    private Item currentItem = null;

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

    public void setCurrentItem(Item item) {
        this.currentItem = item;
    }

    public void clearCurrentItem() {
        this.currentItem = null;
    }

    public Item getCurrentItem() {
        return currentItem;
    }
}

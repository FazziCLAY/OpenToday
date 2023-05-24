package com.fazziclay.opentoday.app.items.stat;

public class ItemStat {
    private int totalTicks = 0;
    private int containerItems;
    private int activeItems;

    public int getContainerItems() {
        return containerItems;
    }

    public void setContainerItems(int containerItems) {
        this.containerItems = containerItems;
    }

    public int getActiveItems() {
        return activeItems;
    }

    public void setActiveItems(int activeItems) {
        this.activeItems = activeItems;
    }

    public int getTotalTicks() {
        return totalTicks;
    }

    public void tick() {
        totalTicks++;
    }
}

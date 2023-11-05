package com.fazziclay.opentoday.app.items.item;

public class ItemStat {
    private int totalTicks = 0;
    private int containerItems;
    private int activeItems;
    private int notifications;
    private boolean isChecked;

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

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getTotalTicks() {
        return totalTicks;
    }

    public void setNotifications(int notifications) {
        this.notifications = notifications;
    }

    public int getNotifications() {
        return notifications;
    }

    public void tick() {
        totalTicks++;
    }
}

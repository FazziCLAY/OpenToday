package com.fazziclay.opentoday.app.items.item.filter;

public abstract class ItemFilter {
    public abstract boolean isFit(FitEquip fitEquip);

    public abstract ItemFilter copy();

    public abstract String getDescription();

    public abstract void setDescription(String s);
}

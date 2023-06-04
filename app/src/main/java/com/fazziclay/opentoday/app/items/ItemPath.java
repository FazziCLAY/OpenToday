package com.fazziclay.opentoday.app.items;

public class ItemPath {
    private final Object[] sections;

    public ItemPath(Object... sections) {
        this.sections = sections;
    }

    public int length() {
        return sections.length;
    }

    public Object[] getSections() {
        return sections;
    }
}

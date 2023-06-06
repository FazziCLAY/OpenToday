package com.fazziclay.opentoday.app.items;

public record ItemPath(Object... sections) {

    public int length() {
        return sections.length;
    }
}

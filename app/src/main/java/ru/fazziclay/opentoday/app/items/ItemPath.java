package ru.fazziclay.opentoday.app.items;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemPath {
    private final UUID tabId;
    private final UUID[] items;

    public static ItemPath from(String s) {
        s = s.replace("tab://", "");
        String[] l = s.split("/");
        boolean isTab = true;
        UUID tab = null;
        List<UUID> items = new ArrayList<>();
        for (String s1 : l) {
            UUID id = UUID.fromString(s1);
            if (isTab) {
                tab = id;
                isTab = false;
            } else {
                items.add(id);
            }
        }

        return new ItemPath(tab, items.toArray(new UUID[0]));
    }

    protected ItemPath(UUID tabId, UUID... items) {
        this.tabId = tabId;
        this.items = items;
    }

    public UUID getTabId() {
        return tabId;
    }

    public int length() {
        return items.length;
    }

    public UUID[] getItems() {
        return items;
    }

    public String toPath() {
        StringBuilder s = new StringBuilder("tab://" + tabId);
        for (UUID item : items) {
            s.append("/").append(item);
        }
        return s.toString();
    }
}

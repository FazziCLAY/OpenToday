package ru.fazziclay.opentoday.app.items;

import java.util.UUID;

import ru.fazziclay.opentoday.annotation.RequireSave;

public class ItemsTab extends SimpleItemStorage {
    @RequireSave private final UUID id;
    @RequireSave private String name;
    private final ItemsTabController controller;

    protected ItemsTab(UUID id, String name, ItemsTabController controller) {
        this.id = id;
        this.name = name;
        this.controller = controller;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        controller.nameChanged(this);
    }

    public UUID getId() {
        return id;
    }

    @Override
    public void save() {
        controller.save(this);
    }
}

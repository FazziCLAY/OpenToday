package com.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.Unique;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;

import java.util.UUID;

public abstract class Tab implements ItemsStorage, Unique {
    protected static class TabCodec extends AbstractTabCodec {
        @NonNull
        @Override
        public Cherry exportTab(@NonNull Tab tab) {
            return new Cherry()
                    .put("name", tab.name)
                    .put("id", tab.id == null ? null : tab.id.toString());
        }

        @NonNull
        @Override
        public Tab importTab(@NonNull Cherry cherry, @Nullable Tab tab) {
            if (cherry.has("id")) tab.id = UUID.fromString(cherry.getString("id"));
            tab.name = cherry.getString("name");
            return tab;
        }
    }

    @RequireSave @SaveKey(key = "id") private UUID id;
    @RequireSave @SaveKey(key = "name") private String name;
    private TabController controller;

    public Tab(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setController(TabController controller) {
        this.controller = controller;
    }

    protected Tab() {

    }

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String text) {
        this.name = text;
        if (controller != null) controller.nameChanged(this);
    }

    @Override
    public void save() {
        if (controller != null) controller.save(this);
    }
}

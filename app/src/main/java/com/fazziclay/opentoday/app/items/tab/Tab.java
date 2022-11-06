package com.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.annotation.RequireSave;
import com.fazziclay.opentoday.annotation.SaveKey;
import com.fazziclay.opentoday.app.items.ID;
import com.fazziclay.opentoday.app.items.ItemsStorage;

import org.json.JSONObject;

import java.util.UUID;

public abstract class Tab implements ItemsStorage, ID {
    public static final TabIETool IE_TOOL = new TabIETool();
    protected static class TabIETool extends TabImportExportTool {
        @NonNull
        @Override
        public JSONObject exportTab(@NonNull Tab tab) throws Exception {
            return new JSONObject()
                    .put("name", tab.name)
                    .put("id", tab.id == null ? null : tab.id.toString());
        }

        @NonNull
        @Override
        public Tab importTab(@NonNull JSONObject json, @Nullable Tab tab) throws Exception {
            if (json.has("id")) tab.id = UUID.fromString(json.getString("id"));
            tab.name = json.getString("name");
            return tab;
        }
    }

    @RequireSave @SaveKey(key = "id") private UUID id;
    @RequireSave @SaveKey(key = "name") private String name;
    private ItemsTabController controller;

    public Tab(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setController(ItemsTabController controller) {
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
        controller.nameChanged(this);
    }

    @Override
    public void save() {
        if (controller != null) controller.save(this);
    }
}

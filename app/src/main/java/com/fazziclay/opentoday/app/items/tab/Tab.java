package com.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.icons.IconsRegistry;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.Unique;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemUtil;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;

import java.util.UUID;

public abstract class Tab implements ItemsStorage, Unique {
    private static final String TAG = "Tab";

    protected static class TabCodec extends AbstractTabCodec {
        private static final String KEY_ID = "id";
        private static final String KEY_NAME = "name";
        private static final String KEY_DISABLE_TICK = "disableTick";
        private static final String KEY_ICON = "icon";

        @NonNull
        @Override
        public Cherry exportTab(@NonNull Tab tab) {
            return new Cherry()
                    .put(KEY_NAME, tab.name)
                    .put(KEY_ID, tab.id == null ? null : tab.id.toString())
                    .put(KEY_DISABLE_TICK, tab.disableTick)
                    .put(KEY_ICON, tab.icon.getId());

        }

        @NonNull
        @Override
        public Tab importTab(@NonNull Cherry cherry, @Nullable Tab tab) {
            if (cherry.has(KEY_ID)) tab.id = UUID.fromString(cherry.getString(KEY_ID));
            tab.name = cherry.optString(KEY_NAME, "");
            tab.disableTick = cherry.optBoolean(KEY_DISABLE_TICK, false);
            tab.icon = IconsRegistry.REGISTRY.getById(cherry.optString(KEY_ICON, IconsRegistry.REGISTRY.NONE.getId()));
            if (tab.id == null) Logger.w("Tab", "id is null while importing...");
            return tab;
        }
    }

    @RequireSave @SaveKey(key = "id") private UUID id = null;
    @RequireSave @SaveKey(key = "name") private String name = "";
    @RequireSave @SaveKey(key = "disableTick") private boolean disableTick = false;
    @RequireSave @SaveKey(key = "icon") private IconsRegistry.Icon icon = IconsRegistry.REGISTRY.NONE;
    private TabController controller;

    public Tab(String name) {
        this.name = name;
    }

    public void setController(TabController controller) {
        this.controller = controller;
    }

    public ItemsRoot getRoot() {
        if (isAttached()) {
            return controller.getRoot();
        }
        Logger.w(TAG, "Attempt to getRoot in unattached Tab.");
        return null;
    }

    public void validateId() {
        if (id == null && controller != null) id = controller.generateId();
    }

    public void attach(TabController controller) {
        this.controller = controller;
        regenerateId();
    }

    protected void regenerateId() {
        if (controller != null) {
            this.id = controller.generateId();
        } else {
            Logger.w(TAG, "Attempt to regenerateId in unattached Tab.");
        }
        for (Item item : getAllItems()) {
            ItemUtil.regenerateIdForItem(item);
        }
    }

    public void detach() {
        this.controller = null;
        this.id = null;
    }

    public boolean isAttached() {
        return controller != null;
    }

    protected Tab() {

    }

    @Override
    public UUID getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public void setName(String text) {
        this.name = text;
        if (controller != null) controller.nameChanged(this);
    }

    public IconsRegistry.Icon getIcon() {
        return icon;
    }

    public void setIcon(IconsRegistry.Icon icon) {
        this.icon = icon;
        if (controller != null) controller.iconChanged(this);
    }

    @Override
    public void save() {
        if (controller != null) {
            controller.save(this);
        } else {
            Logger.w(TAG, "Attempt to save in unattached Tab.");
        }
    }

    public boolean isDisableTick() {
        return disableTick;
    }

    public void setDisableTick(boolean b) {
        this.disableTick = b;
    }
}

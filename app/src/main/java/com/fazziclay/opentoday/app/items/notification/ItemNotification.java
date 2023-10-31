package com.fazziclay.opentoday.app.items.notification;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.icons.IconsRegistry;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.tick.TickSession;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class ItemNotification implements Cloneable {
    public static final int DEFAULT_COLOR = 0; // this is magic number


    public static class ItemNotificationCodec extends AbstractItemNotificationCodec {
        @Override
        public Cherry exportNotification(ItemNotification itemNotification) {
            return new Cherry()
                    .put("id", itemNotification.id == null ? null : itemNotification.id.toString())
                    .put("icon", itemNotification.icon.getId())
                    .put("color", itemNotification.color);
        }

        @Override
        public ItemNotification importNotification(Cherry cherry, ItemNotification notification) {
            if (cherry.has("id")) {
                try {
                    notification.id = UUID.fromString(cherry.getString("id"));
                } catch (Exception ignored) {}
            }
            notification.icon = IconsRegistry.REGISTRY.getById(cherry.optString("icon", "opentoday"));
            notification.color = cherry.optInt("color", DEFAULT_COLOR);
            return notification;
        }
    }


    private UUID id;
    private NotificationController controller;
    @NotNull private IconsRegistry.Icon icon = IconsRegistry.REGISTRY.OPENTODAY;
    private int color;

    public abstract boolean tick(TickSession tickSession);

    @NonNull
    public ItemNotification clone() {
        ItemNotification clone;
        try {
            clone = (ItemNotification) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone exception", e);
        }
        clone.controller = null;
        clone.id = null;
        return clone;
    }

    public void attach(@NotNull NotificationController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("controller is null :(");
        }
        this.controller = controller;
        this.id = controller.generateId(this);
    }

    public void detach() {
        this.controller = null;
        this.id = null;
    }

    public boolean isAttached() {
        return controller != null;
    }

    public UUID getId() {
        return id;
    }

    public NotificationController getController() {
        return controller;
    }

    public Item getParentItem() {
        if (!isAttached()) return null;
        return controller.getParentItem(this);
    }

    // DO NOT USE THIS!!!!!! (used only for importing)
    public void setController(NotificationController controller) {
        this.controller = controller;
    }


    public void setIcon(@NonNull IconsRegistry.Icon icon) {
        this.icon = icon;
    }

    @NonNull
    public IconsRegistry.Icon getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int c) {
        this.color = c;
    }

    @NonNull
    @Override
    public String toString() {
        return "ItemNotification{" +
                "id=" + id +
                ", controller=" + controller +
                '}';
    }
}

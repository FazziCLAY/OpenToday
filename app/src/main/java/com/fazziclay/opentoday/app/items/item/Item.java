package com.fazziclay.opentoday.app.items.item;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.Unique;
import com.fazziclay.opentoday.app.items.callback.ItemCallback;
import com.fazziclay.opentoday.app.items.notification.DayItemNotification;
import com.fazziclay.opentoday.app.items.notification.ItemNotification;
import com.fazziclay.opentoday.app.items.notification.ItemNotificationCodecUtil;
import com.fazziclay.opentoday.app.items.notification.ItemNotificationUtil;
import com.fazziclay.opentoday.app.items.notification.NotificationController;
import com.fazziclay.opentoday.app.items.tag.ItemTag;
import com.fazziclay.opentoday.app.items.tag.TagsCodecUtil;
import com.fazziclay.opentoday.app.items.tag.TagsUtil;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.app.items.tick.TickTarget;
import com.fazziclay.opentoday.app.items.tick.Tickable;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;
import com.fazziclay.opentoday.util.annotation.Setter;
import com.fazziclay.opentoday.util.callback.CallbackStorage;
import com.fazziclay.opentoday.util.callback.Status;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <h1>The main unit of the application</h1>
 * <p>This is the basic parent of all items. It is not possible to add it/render it, etc.</p>
 * <p>For all of the above actions, use the children of this object</p>
 *
 * <br><br>
 * <h1>Codecs</h1>
 * <p>Also this object contains the base codec, the codecs of children are also required to inherit the base codec.</p>
 *
 * <br><br>
 * <h1>Non-english comments</h1>
 * <p>Do not delete them, they do not carry important information, but this is the only thing that has remained since when Item was called Entry...</p>
 */
public abstract class Item implements Unique, Tickable {
    // START - Save
    public static class ItemCodec extends AbstractItemCodec {
        private static final String KEY_ID = "id";
        private static final String KEY_VIEW_MIN_HEIGHT = "viewMinHeight";
        private static final String KEY_VIEW_BACKGROUND_COLOR = "viewBackgroundColor";
        private static final String KEY_VIEW_CUSTOM_BACKGROUND_COLOR = "viewCustomBackgroundColor";
        private static final String KEY_NOTIFICATIONS = "notifications";
        private static final String KEY_MINIMIZE = "minimize";
        private static final String KEY_TAGS = "tags";

        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            return new Cherry()
                    .put(KEY_ID, item.id.toString())
                    .put(KEY_VIEW_MIN_HEIGHT, item.viewMinHeight)
                    .put(KEY_VIEW_BACKGROUND_COLOR, item.viewBackgroundColor)
                    .put(KEY_VIEW_CUSTOM_BACKGROUND_COLOR, item.viewCustomBackgroundColor)
                    .put(KEY_MINIMIZE, item.minimize)
                    .put(KEY_NOTIFICATIONS, ItemNotificationCodecUtil.exportNotificationList(item.notifications))
                    .put(KEY_TAGS, TagsCodecUtil.exportTagsList(item.tags));
        }

        private final Item defaultValues = new Item(){
            @Override
            public ItemType getItemType() {
                throw new UnsupportedOperationException("This method of this instance never executed!");
            }
        };
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            if (item == null) throw new NullPointerException("item is null");
            applyId(item, cherry);
            item.viewMinHeight = cherry.optInt(KEY_VIEW_MIN_HEIGHT, defaultValues.viewMinHeight);
            item.viewBackgroundColor = cherry.optInt(KEY_VIEW_BACKGROUND_COLOR, defaultValues.viewBackgroundColor);
            item.viewCustomBackgroundColor = cherry.optBoolean(KEY_VIEW_CUSTOM_BACKGROUND_COLOR, defaultValues.viewCustomBackgroundColor);
            item.minimize = cherry.optBoolean(KEY_MINIMIZE, defaultValues.minimize);
            item.notifications = ItemNotificationCodecUtil.importNotificationList(cherry.optOrchard(KEY_NOTIFICATIONS));
            for (ItemNotification notification : item.notifications) {
                if (notification.getId() == null) {
                    notification.attach(item.notificationController);
                } else {
                    notification.setController(item.notificationController);
                }
            }
            item.tags = TagsCodecUtil.importTagsList(cherry.optOrchard(KEY_TAGS));
            return item;
        }

        private void applyId(Item item, Cherry cherry) {
            String stringId = cherry.optString(KEY_ID, null);
            if (stringId == null) {
                item.id = UUID.randomUUID();
            } else {
                try {
                    item.id = UUID.fromString(stringId);
                } catch (Exception e) {
                    item.id = UUID.randomUUID();
                }
            }
        }
    }
    // END - Save

    private static final String DEFAULT_BACKGROUND_COLOR = "#99999999";

    @Nullable @RequireSave @SaveKey(key = "id") private UUID id;
    @Nullable private ItemController controller;
    private final CallbackStorage<ItemCallback> itemCallbacks = new CallbackStorage<>();
    private final ItemStat stat = new ItemStat();
    @SaveKey(key = "viewMinHeight") @RequireSave private int viewMinHeight = 0; // минимальная высота
    @SaveKey(key = "viewBackgroundColor") @RequireSave private int viewBackgroundColor = Color.parseColor(DEFAULT_BACKGROUND_COLOR); // фоновый цвет
    @SaveKey(key = "viewCustomBackgroundColor") @RequireSave private boolean viewCustomBackgroundColor = false; // юзаем ли фоновый цвет
    @SaveKey(key = "minimize") @RequireSave private boolean minimize = false;
    @NonNull @SaveKey(key = "notifications") @RequireSave private List<ItemNotification> notifications = new ArrayList<>();
    @NonNull @SaveKey(key = "tags") @RequireSave private List<ItemTag> tags = new ArrayList<>();
    @NonNull private final NotificationController notificationController = new ItemNotificationController();
    private boolean cachedNotificationStatus;

    // Copy constructor
    protected Item(@Nullable Item copy) {
        // unattached
        this.id = null;
        this.controller = null;
        this.cachedNotificationStatus = true;

        // copy
        if (copy != null) {
            this.viewMinHeight = copy.viewMinHeight;
            this.viewBackgroundColor = copy.viewBackgroundColor;
            this.viewCustomBackgroundColor = copy.viewCustomBackgroundColor;
            this.minimize = copy.minimize;
            this.notifications = ItemNotificationUtil.copy(copy.notifications);
            for (ItemNotification notification : this.notifications) {
                notification.attach(notificationController);
            }
            this.tags = TagsUtil.copy(copy.tags);
        }
    }

    protected Item() {
        this(null);
    }

    public abstract ItemType getItemType();

    // For fast get text (method overrides by TextItem)
    public String getText() {
        return "[Item not have text. Ops...]";
    }

    // For fast (method overrides by TextItem)
    @Getter public boolean isParagraphColorize() {return false;}

    public void delete() {
        if (isAttached()) controller.delete(this);
        itemCallbacks.run((callbackStorage, callback) -> callback.delete(Item.this));
    }

    public void save() {
        if (isAttached()) controller.save(this);
        itemCallbacks.run((callbackStorage, callback) -> callback.save(Item.this));
    }

    public void visibleChanged() {
        if (isAttached()) controller.updateUi(this);
        itemCallbacks.run((callbackStorage, callback) -> callback.updateUi(Item.this));
    }

    public ItemsRoot getRoot() {
        if (isAttached()) {
            return controller.getRoot();
        }
        return null;
    }

    public boolean isAttached() {
        return controller != null;
    }

    /**
     * set controller and regenerate ids
     * @param itemController controller
     */
    protected void attach(ItemController itemController) {
        this.controller = itemController;
        regenerateId();
        itemCallbacks.run((callbackStorage, callback) -> callback.attached(Item.this));
    }

    protected void detach() {
        this.controller = null;
        this.id = null;
        itemCallbacks.run((callbackStorage, callback) -> callback.detached(Item.this));
    }

    public void tick(TickSession tickSession) {
        if (!tickSession.isAllowed(this)) return;
        Debug.tickedItems++;
        if (tickSession.isTickTargetAllowed(TickTarget.ITEM_NOTIFICATIONS)) {
            profPush(tickSession, "item_notifications_tick");
            ItemNotificationUtil.tick(tickSession, notifications);
            profPop(tickSession);
        }
        if (tickSession.isTickTargetAllowed(TickTarget.ITEM_CALLBACKS)) {
            profPush(tickSession, "callbacks");
            itemCallbacks.run((callbackStorage, callback) -> callback.tick(Item.this));
            profPop(tickSession);
        }

        profPush(tickSession, "cachedNotificationStatus");
        boolean isUpdateNotifications = tickSession.isTickTargetAllowed(TickTarget.ITEM_NOTIFICATION_UPDATE);
        if (isUpdateNotifications != cachedNotificationStatus && !tickSession.isPlannedTick(this)) {
            cachedNotificationStatus = isUpdateNotifications;
            itemCallbacks.run((callbackStorage, callback) -> callback.cachedNotificationStatusChanged(Item.this, isUpdateNotifications));
        }
        profPop(tickSession);
    }

    protected void profPush(TickSession t, String s) {
        t.getProfiler().push(s);
    }

    protected void profSwap(TickSession t, String s) {
        t.getProfiler().swap(s);
    }

    protected void profPop(TickSession t) {
        t.getProfiler().pop();
    }

    protected void regenerateId() {
        this.id = controller != null ? controller.generateId(this) : UUID.randomUUID();
    }

    public int getChildrenItemCount() {
        if (this instanceof ContainerItem containerItem) {
            int c = 0;
            for (Item item : containerItem.getAllItems()) {
                c+= 1 + item.getChildrenItemCount();
            }
            return c;
        }
        return 0;
    }

    protected void updateStat() {
        stat.setNotifications(notifications.size());
        stat.tick();
    }

    public CallbackStorage<ItemCallback> getItemCallbacks() {
        return itemCallbacks;
    }

    public void dispatchClick() {
        itemCallbacks.run((callbackStorage, callback) -> callback.click(Item.this));
    }

    // Getters & Setters
    @Nullable @Override @Getter public UUID getId() { return id; }

    /**
     * <h1>Protected!!!</h1>
     * <h2>Do not use this method. It is only needed to import the controller installation after importing from the codec</h2>
     */
    @Setter protected void setController(@Nullable ItemController controller) { this.controller = controller; }
    @Getter public int getViewMinHeight() { return viewMinHeight; }
    @Setter public void setViewMinHeight(int v) { this.viewMinHeight = v; }

    @Getter public int getViewBackgroundColor() { return viewBackgroundColor; }
    @Setter public void setViewBackgroundColor(int v) { this.viewBackgroundColor = v; }

    @Getter public boolean isViewCustomBackgroundColor() { return viewCustomBackgroundColor; }
    @Setter public void setViewCustomBackgroundColor(boolean v) { this.viewCustomBackgroundColor = v; }

    @Getter public boolean isMinimize() { return minimize; }
    @Setter public void setMinimize(boolean minimize) { this.minimize = minimize; }

    @Getter @NonNull public ItemNotification[] getNotifications() { return notifications.toArray(new ItemNotification[0]); }

    public void addNotifications(ItemNotification... notifications) {
        for (ItemNotification notification : notifications) {
            notification.attach(notificationController);
            this.notifications.add(notification);
        }
        updateStat();
        visibleChanged();
    }

    public void removeNotifications(ItemNotification... notifications) {
        for (ItemNotification notification : notifications) {
            notification.detach();
            this.notifications.remove(notification);
        }
        updateStat();
        visibleChanged();
    }

    public ItemNotification getNotificationById(UUID notifyId) {
        for (ItemNotification notification : notifications) {
            if (notifyId.equals(notification.getId())) return notification;
        }
        return null;
    }

    public void moveNotifications(int positionFrom, int positionTo) {
        if (!(positionFrom >= 0 && positionTo >= 0 && positionFrom < notifications.size() && positionTo < notifications.size())) {
            throw new IndexOutOfBoundsException("failed move notifications... from=" + positionFrom + "; to=" + positionTo);
        }
        ItemNotification itemNotification = notifications.get(positionFrom);
        notifications.remove(itemNotification);
        notifications.add(positionTo, itemNotification);
    }

    public void removeAllNotifications() {
        removeNotifications(getNotifications());
    }

    public boolean isNotifications() {
        return !notifications.isEmpty();
    }

    public boolean getCachedNotificationStatus() {
        return cachedNotificationStatus;
    }

    @Getter @NonNull public ItemStat getStat() {
        return stat;
    }

    @Getter public ItemsStorage getParentItemsStorage() {
        if (isAttached()) {
            return controller.getParentItemsStorage(this);
        }
        return null;
    }

    @NonNull
    public ItemTag[] getTags() {
        return tags.toArray(new ItemTag[0]);
    }

    public void removeTag(ItemTag t) {
        tags.remove(t);
    }

    public void addTag(ItemTag t) {
        t.bind();
        tags.add(t);
    }

    @NotNull
    @Override
    public String toString() {
        String text = getText().replace("\n", "");
        int max = Math.min(text.length(), 30);
        text = text.substring(0, max);
        return getClass().getSimpleName()+"@[ID:"+getId()+" HASH:"+hashCode() +" TEXT:'"+text+"']";
    }

    private class ItemNotificationController implements NotificationController {
        @Override
        public UUID generateId(ItemNotification notification) {
            return UUID.randomUUID();
        }

        @Override
        public Item getParentItem(ItemNotification itemNotification) {
            return Item.this;
        }
    }
}

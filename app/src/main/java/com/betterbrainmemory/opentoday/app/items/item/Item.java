package com.betterbrainmemory.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.betterbrainmemory.opentoday.Debug;
import com.betterbrainmemory.opentoday.app.data.Cherry;
import com.betterbrainmemory.opentoday.app.items.ItemsRoot;
import com.betterbrainmemory.opentoday.app.items.ItemsStorage;
import com.betterbrainmemory.opentoday.app.items.Unique;
import com.betterbrainmemory.opentoday.app.items.callback.ItemCallback;
import com.betterbrainmemory.opentoday.app.items.notification.ItemNotification;
import com.betterbrainmemory.opentoday.app.items.notification.ItemNotificationCodecUtil;
import com.betterbrainmemory.opentoday.app.items.notification.ItemNotificationUtil;
import com.betterbrainmemory.opentoday.app.items.notification.NotificationController;
import com.betterbrainmemory.opentoday.app.items.tag.ItemTag;
import com.betterbrainmemory.opentoday.app.items.tag.TagsCodecUtil;
import com.betterbrainmemory.opentoday.app.items.tag.TagsUtil;
import com.betterbrainmemory.opentoday.app.items.tick.TickSession;
import com.betterbrainmemory.opentoday.app.items.tick.TickTarget;
import com.betterbrainmemory.opentoday.app.items.tick.Tickable;
import com.betterbrainmemory.opentoday.util.Checks;
import com.betterbrainmemory.opentoday.util.ColorUtil;
import com.betterbrainmemory.opentoday.util.Identifier;
import com.betterbrainmemory.opentoday.util.Logger;
import com.betterbrainmemory.opentoday.util.annotation.Getter;
import com.betterbrainmemory.opentoday.util.annotation.Setter;
import com.betterbrainmemory.opentoday.util.callback.CallbackStorage;

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

 */
public abstract class Item implements Unique, Tickable {
    private static final String TAG = "Item";
    private static final String DEFAULT_BACKGROUND_COLOR = "#99999999";
    private static final int DEFAULT_BACKGROUND_COLOR_CACHED = ColorUtil.hexToColor(DEFAULT_BACKGROUND_COLOR);

    private UUID id;
    @Nullable private ItemController controller;
    private final CallbackStorage<ItemCallback> itemCallbacks = new CallbackStorage<>();
    private final ItemStat stat = new ItemStat();
    private int viewMinHeight = 0;
    private int viewBackgroundColor = DEFAULT_BACKGROUND_COLOR_CACHED;
    private boolean viewCustomBackgroundColor = false;
    private boolean minimize = false;
    private List<ItemNotification> notifications = new ArrayList<>();
    private List<ItemTag> tags = new ArrayList<>();
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

    public Item() {
        this(null);
    }

    public Identifier getItemType() {
        var reg = ItemsRegistry.REGISTRY.getByKey(this.getClass());
        if (reg == null) {
            throw new RuntimeException("Item class=" + this.getClass() + " is not registered in ItemsRegistry!");
        }
        return reg.getIdentifier();
    }

    // For fast get text (method overrides by TextItem)
    public String getText() {
        Logger.w(TAG, "getText call...");
        return "[Item not have text. Ops...]";
    }

    // For fast (method overrides by TextItem)
    @Getter public boolean isFormatting() {return false;}

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
    protected void attach(@NotNull ItemController itemController) {
        Checks.throwIsNull(itemController, "itemController");
        this.controller = itemController;
        regenerateId();
        ItemsRoot root = controller.getRoot();
        Checks.throwIsNull(root, "itemsRoot");
        onAttached(root);
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
            profilerPush(tickSession, "item_notifications_tick");
            ItemNotificationUtil.tick(tickSession, notifications);
            profilerPop(tickSession);
        }
        if (tickSession.isTickTargetAllowed(TickTarget.ITEM_CALLBACKS)) {
            profilerPush(tickSession, "callbacks");
            itemCallbacks.run((callbackStorage, callback) -> callback.tick(Item.this));
            profilerPop(tickSession);
        }

        profilerPush(tickSession, "cachedNotificationStatus");
        boolean isUpdateNotifications = tickSession.isTickTargetAllowed(TickTarget.ITEM_NOTIFICATION_UPDATE);
        if (isUpdateNotifications != cachedNotificationStatus && !tickSession.isPlannedTick(this)) {
            cachedNotificationStatus = isUpdateNotifications;
            itemCallbacks.run((callbackStorage, callback) -> callback.cachedNotificationStatusChanged(Item.this, isUpdateNotifications));
        }
        profilerPop(tickSession);
    }

    protected void profilerPush(TickSession t, String s) {
        t.getProfiler().push(s);
    }

    protected void profilerSwap(TickSession t, String s) {
        t.getProfiler().swap(s);
    }

    protected void profilerPop(TickSession t) {
        t.getProfiler().pop();
    }

    protected void regenerateId() {
        if (!isAttached()) {
            Logger.w(TAG, "regenerateId call on unattached item!");
        }
        this.id = (isAttached()) ? controller.generateId(this) : UUID.randomUUID();
    }

    /**
     * Calc total children count exclude this.
     */
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

    protected void onAttached(@NotNull ItemsRoot itemsRoot) {
        // for override
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


    public static class ItemCodec extends AbstractItemCodec {
        private static final String KEY_ID = "id";
        private static final String KEY_VIEW_MIN_HEIGHT = "view_minimal_height";
        private static final String KEY_VIEW_BACKGROUND_COLOR = "view_background_color";
        private static final String KEY_VIEW_CUSTOM_BACKGROUND_COLOR = "view_background_color_is_custom";
        private static final String KEY_NOTIFICATIONS = "notifications";
        private static final String KEY_MINIMIZE = "minimize";
        private static final String KEY_TAGS = "tags";

        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            return new Cherry()
                    .put(KEY_ID, item.id.toString())
                    .put(KEY_VIEW_MIN_HEIGHT, item.viewMinHeight)
                    .put(KEY_VIEW_BACKGROUND_COLOR, ColorUtil.colorToHex(item.viewBackgroundColor))
                    .put(KEY_VIEW_CUSTOM_BACKGROUND_COLOR, item.viewCustomBackgroundColor)
                    .put(KEY_MINIMIZE, item.minimize)
                    .put(KEY_NOTIFICATIONS, ItemNotificationCodecUtil.exportNotificationList(item.notifications))
                    .put(KEY_TAGS, TagsCodecUtil.exportTagsList(item.tags));
        }

        private final Item defaultValues = new Item(){};
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            Checks.throwIsNull(item, "item is null");
            applyId(item, cherry);
            item.viewMinHeight = cherry.optInt(KEY_VIEW_MIN_HEIGHT, defaultValues.viewMinHeight);
            item.viewBackgroundColor = ColorUtil.hexToColor(cherry.optString(KEY_VIEW_BACKGROUND_COLOR, DEFAULT_BACKGROUND_COLOR));
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
                    Logger.e(TAG, "Failed parse item id. Use UUID.randomUUID().", e);
                    item.id = UUID.randomUUID();
                }
            }
        }
    }
}

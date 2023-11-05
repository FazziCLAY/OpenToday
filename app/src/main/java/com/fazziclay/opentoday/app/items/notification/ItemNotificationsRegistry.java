package com.fazziclay.opentoday.app.items.notification;

public class ItemNotificationsRegistry {
    public static final ItemNotificationsRegistry REGISTRY = new ItemNotificationsRegistry();

    private static final ItemNotificationInfo[] NOTIFICATIONS = new ItemNotificationInfo[] {
        new ItemNotificationInfo(DayItemNotification.class, "DayItemNotification", DayItemNotification.CODEC, ItemNotificationType.DAY)
    };

    public ItemNotificationInfo[] getAllNotifications() {
        return NOTIFICATIONS.clone();
    }

    public static class ItemNotificationInfo {
        private final Class<? extends ItemNotification> clazz;
        private final String stringType;
        private final AbstractItemNotificationCodec codec;
        private final ItemNotificationType type;

        public ItemNotificationInfo(Class<? extends ItemNotification> clazz, String v, AbstractItemNotificationCodec codec, ItemNotificationType type) {
            this.clazz = clazz;
            this.stringType = v;
            this.codec = codec;
            this.type = type;
        }

        public Class<? extends ItemNotification> getClazz() {
            return clazz;
        }

        public String getStringType() {
            return stringType;
        }

        public AbstractItemNotificationCodec getCodec() {
            return codec;
        }

        public ItemNotificationType getType() {
            return type;
        }
    }

    public ItemNotificationInfo getByStringType(String v) {
        for (ItemNotificationInfo info : NOTIFICATIONS) {
            if (info.stringType.equals(v)) {
                return info;
            }
        }
        return null;
    }

    public ItemNotificationInfo getByClass(Class<? extends ItemNotification> v) {
        for (ItemNotificationInfo info : NOTIFICATIONS) {
            if (info.clazz == v) {
                return info;
            }
        }
        return null;
    }

    public ItemNotificationInfo getByType(ItemNotificationType type) {
        for (ItemNotificationInfo info : NOTIFICATIONS) {
            if (info.type == type) {
                return info;
            }
        }
        return null;
    }
}

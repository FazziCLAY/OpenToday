package com.fazziclay.opentoday.app.items.notification;

public class ItemNotificationsRegistry {
    public static final ItemNotificationsRegistry REGISTRY = new ItemNotificationsRegistry();

    private static final ItemNotificationInfo[] INFOS = new ItemNotificationInfo[] {
        new ItemNotificationInfo(DayItemNotification.class, "DayItemNotification", DayItemNotification.IE_TOOL)
    };

    public ItemNotificationInfo[] getAllNotifications() {
        return INFOS.clone();
    }

    public static class ItemNotificationInfo {
        private final Class<? extends ItemNotification> clazz;
        private final String stringType;
        private final ItemNotificationCodec codec;

        public ItemNotificationInfo(Class<? extends ItemNotification> clazz, String v, ItemNotificationCodec codec) {
            this.clazz = clazz;
            this.stringType = v;
            this.codec = codec;
        }

        public Class<? extends ItemNotification> getClazz() {
            return clazz;
        }

        public String getStringType() {
            return stringType;
        }

        public ItemNotificationCodec getCodec() {
            return codec;
        }
    }

    public ItemNotificationInfo getByStringType(String v) {
        for (ItemNotificationInfo info : INFOS) {
            if (info.stringType.equals(v)) {
                return info;
            }
        }
        return null;
    }

    public ItemNotificationInfo getByClass(Class<? extends ItemNotification> v) {
        for (ItemNotificationInfo info : INFOS) {
            if (info.clazz == v) {
                return info;
            }
        }
        return null;
    }
}

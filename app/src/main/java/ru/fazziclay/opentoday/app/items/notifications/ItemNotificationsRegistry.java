package ru.fazziclay.opentoday.app.items.notifications;

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
        private final ItemNotificationIETool ieTool;

        public ItemNotificationInfo(Class<? extends ItemNotification> clazz, String v, ItemNotificationIETool ieTool) {
            this.clazz = clazz;
            stringType = v;
            this.ieTool = ieTool;
        }

        public Class<? extends ItemNotification> getClazz() {
            return clazz;
        }

        public String getStringType() {
            return stringType;
        }

        public ItemNotificationIETool getIeTool() {
            return ieTool;
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

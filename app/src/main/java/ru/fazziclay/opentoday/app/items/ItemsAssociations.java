package ru.fazziclay.opentoday.app.items;

public class ItemsAssociations {
    private static final String ITEMTYPE_ITEM = "Item";
    private static final String ITEMTYPE_TEXT_ITEM = "TextItem";
    private static final String ITEMTYPE_CHECKBOX_ITEM = "CheckboxItem";
    private static final String ITEMTYPE_DAY_REPEATABLE_CHECKBOX_ITEM = "DayRepeatableCheckboxItem";
    private static final String ITEMTYPE_CYCLE_LIST_ITEM = "CycleListItem";
    private static final String ITEMTYPE_COUNTER_ITEM = "CounterItem";
    private static final String ITEMTYPE_GROUP_ITEM = "GroupItem";

    public static Item.ItemIETool getIETool(Class<? extends Item> item) {
        if (item == Item.class) {
            return Item.IE_TOOL;
        } else if (item == TextItem.class) {
            return TextItem.IE_TOOL;
        } else if (item == CheckboxItem.class) {
            return CheckboxItem.IE_TOOL;
        } else if (item == DayRepeatableCheckboxItem.class) {
            return DayRepeatableCheckboxItem.IE_TOOL;
        } else if (item == CycleListItem.class) {
            return CycleListItem.IE_TOOL;
        } else if (item == CounterItem.class) {
            return CounterItem.IE_TOOL;
        } else if (item == GroupItem.class) {
            return GroupItem.IE_TOOL;
        }
        throw new RuntimeException("Unknown class '" + item.getName() + "' extends Item! (check ItemIEManager!)");
    }

    public static String stringItemTypeFromItem(Item item) {
        Class<? extends Item> clazz = item.getClass();
        if (clazz == Item.class) {
            return ITEMTYPE_ITEM;
        } else if (clazz == TextItem.class) {
            return ITEMTYPE_TEXT_ITEM;
        } else if (clazz == CheckboxItem.class) {
            return ITEMTYPE_CHECKBOX_ITEM;
        } else if (clazz == DayRepeatableCheckboxItem.class) {
            return ITEMTYPE_DAY_REPEATABLE_CHECKBOX_ITEM;
        } else if (clazz == CycleListItem.class) {
            return ITEMTYPE_CYCLE_LIST_ITEM;
        } else if (clazz == CounterItem.class) {
            return ITEMTYPE_COUNTER_ITEM;
        } else if (clazz == GroupItem.class) {
            return ITEMTYPE_GROUP_ITEM;
        }
        throw new RuntimeException("Unknown class '" + clazz.getName() + "' extends Item! (check ItemIEManager!)");
    }

    public static Class<? extends Item> stringItemTypeToClass(String itemType) {
        switch (itemType) {
            case ITEMTYPE_ITEM:
                return Item.class;
            case ITEMTYPE_TEXT_ITEM:
                return TextItem.class;
            case ITEMTYPE_CHECKBOX_ITEM:
                return CheckboxItem.class;
            case ITEMTYPE_DAY_REPEATABLE_CHECKBOX_ITEM:
                return DayRepeatableCheckboxItem.class;
            case ITEMTYPE_CYCLE_LIST_ITEM:
                return CycleListItem.class;
            case ITEMTYPE_COUNTER_ITEM:
                return CounterItem.class;
            case ITEMTYPE_GROUP_ITEM:
                return GroupItem.class;
        }
        throw new RuntimeException("Unknown itemType '" + itemType + "' (check ItemIEManager!)");
    }

    public static Item createItem(Class<? extends Item> type) {
        Item item;
        if (type == TextItem.class) {
            item = new TextItem("");
        } else if (type == CheckboxItem.class) {
            item = new CheckboxItem("", false);
        } else if (type == DayRepeatableCheckboxItem.class) {
            item = new DayRepeatableCheckboxItem("", false, false, 0);
        } else if (type == CycleListItem.class) {
            item = new CycleListItem("");
        } else if (type == CounterItem.class) {
            item = new CounterItem("");
        } else if (type == GroupItem.class) {
            item = new GroupItem("");
        } else {
            throw new RuntimeException("Illegal item type! (check DialogItem)");
        }
        return item;
    }

    public static Item copy(Item item) {
        Class<? extends Item> type = item.getClass();
        Item copyItem;
        if (type == TextItem.class) {
            copyItem = new TextItem((TextItem) item);
        } else if (type == CheckboxItem.class) {
            copyItem = new CheckboxItem((CheckboxItem) item);
        } else if (type == DayRepeatableCheckboxItem.class) {
            copyItem = new DayRepeatableCheckboxItem((DayRepeatableCheckboxItem) item);
        } else if (type == CycleListItem.class) {
            copyItem = new CycleListItem((CycleListItem) item);
        } else if (type == CounterItem.class) {
            copyItem = new CounterItem((CounterItem) item);
        } else if (type == GroupItem.class) {
            copyItem = new GroupItem((GroupItem) item);
        } else {
            throw new RuntimeException("Illegal item type! (check DialogItem)");
        }
        return copyItem;
    }
}

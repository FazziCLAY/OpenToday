package ru.fazziclay.opentoday.app.items;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.items.item.CheckboxItem;
import ru.fazziclay.opentoday.app.items.item.CounterItem;
import ru.fazziclay.opentoday.app.items.item.CycleListItem;
import ru.fazziclay.opentoday.app.items.item.DayRepeatableCheckboxItem;
import ru.fazziclay.opentoday.app.items.item.GroupItem;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.item.TextItem;

public class ItemsRegistry {
    public static final ItemsRegistry REGISTRY = new ItemsRegistry();

    private static final ItemInfo[] ITEMS = new ItemInfo[]{
            new ItemInfo(TextItem.class,                           "TextItem",                      TextItem.IE_TOOL,                      TextItem::createEmpty,                    (i) -> new TextItem((TextItem) i),                                    R.string.item_text),
            new ItemInfo(CheckboxItem.class,                       "CheckboxItem",                  CheckboxItem.IE_TOOL,                  CheckboxItem::createEmpty,                (i) -> new CheckboxItem((CheckboxItem) i),                            R.string.item_checkbox),
            new ItemInfo(DayRepeatableCheckboxItem.class,          "DayRepeatableCheckboxItem",     DayRepeatableCheckboxItem.IE_TOOL,     DayRepeatableCheckboxItem::createEmpty,   (i) -> new DayRepeatableCheckboxItem((DayRepeatableCheckboxItem) i),  R.string.item_checkboxDayRepeatable),
            new ItemInfo(CycleListItem.class,                      "CycleListItem",                 CycleListItem.IE_TOOL,                 CycleListItem::createEmpty,               (i) -> new CycleListItem((CycleListItem) i),                          R.string.item_cycleList),
            new ItemInfo(CounterItem.class,                        "CounterItem",                   CounterItem.IE_TOOL,                   CounterItem::createEmpty,                 (i) -> new CounterItem((CounterItem) i),                              R.string.item_counter),
            new ItemInfo(GroupItem.class,                          "GroupItem",                     GroupItem.IE_TOOL,                     GroupItem::createEmpty,                   (i) -> new GroupItem((GroupItem) i),                                  R.string.item_group)
    };

    public ItemInfo[] getAllItems() {
        return ITEMS.clone();
    }

    public ItemInfo getItemInfoByStringName(@NonNull String s) {
        for (ItemInfo item : ITEMS) {
            if (s.equals(item.stringType)) return item;
        }
        return null;
    }

    public ItemInfo getItemInfoByClass(Class<? extends Item> s) {
        for (ItemInfo item : ITEMS) {
            if (s == item.classType) return item;
        }
        return null;
    }

    public static class ItemInfo {
        private final Class<? extends Item> classType;
        private final String stringType;
        private final ItemImportExportTool itemImportExportTool;
        private final ItemCreateInterface createInterface;
        private final ItemCopyInterface copyInterface;
        private final int nameResId;

        public ItemInfo(Class<? extends Item> c, String v, ItemImportExportTool t, ItemCreateInterface ici, ItemCopyInterface icopi, @StringRes int nameResID) {
            this.classType = c;
            this.stringType = v;
            this.itemImportExportTool = t;
            this.createInterface = ici;
            this.copyInterface = icopi;
            this.nameResId = nameResID;
        }

        public Class<? extends Item> getClassType() {
            return classType;
        }

        public String getStringType() {
            return stringType;
        }

        public ItemImportExportTool getItemIETool() {
            return itemImportExportTool;
        }

        public Item create() {
            return createInterface.create();
        }

        public Item copy(Item item) {
            return copyInterface.copy(item);
        }

        @StringRes
        public int getNameResId() {
            return nameResId;
        }
    }

    public interface ItemCopyInterface {
        Item copy(Item item);
    }

    public interface ItemCreateInterface {
        Item create();
    }
}

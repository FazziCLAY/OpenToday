package ru.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.FeatureFlag;

public class ItemsRegistry {
    @NonNull
    public static final ItemsRegistry REGISTRY = new ItemsRegistry();

    @NonNull
    private static final ItemInfo[] ITEMS = new ItemInfo[]{
            new ItemInfo(TextItem.class,                           "TextItem",                      TextItem.IE_TOOL,                      TextItem::createEmpty,                    (i) -> new TextItem((TextItem) i).regenerateId(),                                    R.string.item_text),
            new ItemInfo(CheckboxItem.class,                       "CheckboxItem",                  CheckboxItem.IE_TOOL,                  CheckboxItem::createEmpty,                (i) -> new CheckboxItem((CheckboxItem) i).regenerateId(),                            R.string.item_checkbox),
            new ItemInfo(DayRepeatableCheckboxItem.class,          "DayRepeatableCheckboxItem",     DayRepeatableCheckboxItem.IE_TOOL,     DayRepeatableCheckboxItem::createEmpty,   (i) -> new DayRepeatableCheckboxItem((DayRepeatableCheckboxItem) i).regenerateId(),  R.string.item_checkboxDayRepeatable),
            new ItemInfo(CycleListItem.class,                      "CycleListItem",                 CycleListItem.IE_TOOL,                 CycleListItem::createEmpty,               (i) -> new CycleListItem((CycleListItem) i).regenerateId(),                          R.string.item_cycleList),
            new ItemInfo(CounterItem.class,                        "CounterItem",                   CounterItem.IE_TOOL,                   CounterItem::createEmpty,                 (i) -> new CounterItem((CounterItem) i).regenerateId(),                              R.string.item_counter),
            new ItemInfo(GroupItem.class,                          "GroupItem",                     GroupItem.IE_TOOL,                     GroupItem::createEmpty,                   (i) -> new GroupItem((GroupItem) i).regenerateId(),                                  R.string.item_group),
            new ItemInfo(FilterGroupItem.class,                    "FilterGroupItem",               FilterGroupItem.IE_TOOL,               FilterGroupItem::createEmpty,             (i) -> new FilterGroupItem((FilterGroupItem) i).regenerateId(),                      R.string.item_filterGroup),
            new ItemInfo(LongTextItem.class,                       "LongTextItem",                  LongTextItem.IE_TOOL,                  LongTextItem::createEmpty,                (i) -> new LongTextItem((LongTextItem) i).regenerateId(),                            R.string.item_longTextItem),
            new ItemInfo(DebugTickCounterItem.class,               "DebugTickCounterItem",          DebugTickCounterItem.IE_TOOL,          DebugTickCounterItem::createEmpty,        (i) -> new DebugTickCounterItem((DebugTickCounterItem) i).regenerateId(),            R.string.item_debugTickCounter).requiredFeatureFlag(FeatureFlag.ITEM_DEBUG_TICK_COUNTER)
    };

    private ItemsRegistry() {}

    @NonNull
    public ItemInfo[] getAllItems() {
        return ITEMS.clone();
    }

    public int count() {
        return ITEMS.length;
    }

    @Nullable
    public ItemInfo get(@NonNull String stringType) {
        for (ItemInfo item : ITEMS) {
            if (stringType.equals(item.stringType)) return item;
        }
        return null;
    }

    @Nullable
    public ItemInfo get(@NonNull Class<? extends Item> classType) {
        for (ItemInfo item : ITEMS) {
            if (classType == item.classType) return item;
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
        private FeatureFlag requiredFeatureFlag;

        public ItemInfo(@NonNull Class<? extends Item> classType, @NonNull String stringType, @NonNull ItemImportExportTool itemImportExportTool, @NonNull ItemCreateInterface createInterface, @NonNull ItemCopyInterface copyInterface, @StringRes int nameResId) {
            this.classType = classType;
            this.stringType = stringType;
            this.itemImportExportTool = itemImportExportTool;
            this.createInterface = createInterface;
            this.copyInterface = copyInterface;
            this.nameResId = nameResId;
        }

        @NonNull
        public Class<? extends Item> getClassType() {
            return classType;
        }

        @NonNull
        public String getStringType() {
            return stringType;
        }

        @NonNull
        public ItemImportExportTool getItemIETool() {
            return itemImportExportTool;
        }

        @NonNull
        public Item create() {
            return createInterface.create();
        }

        @NonNull
        public Item copy(@NonNull Item item) {
            return copyInterface.copy(item);
        }

        @StringRes
        public int getNameResId() {
            return nameResId;
        }

        public ItemInfo requiredFeatureFlag(FeatureFlag flag) {
            this.requiredFeatureFlag = flag;
            return this;
        }

        public boolean isCompatibility(FeatureFlag[] flags) {
            if (requiredFeatureFlag == null) return true;
            for (FeatureFlag f : flags) {
                if (f == requiredFeatureFlag) {
                    return true;
                }
            }
            return false;
        }
    }

    private interface ItemCopyInterface {
        @NonNull
        Item copy(@NonNull Item item);
    }

    private interface ItemCreateInterface {
        @NonNull
        Item create();
    }
}

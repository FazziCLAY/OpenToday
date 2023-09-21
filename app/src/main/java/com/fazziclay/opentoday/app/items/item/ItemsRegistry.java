package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.FeatureFlag;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Registry of items. Contains links between the following:
 * <p>* Java class. E.g. TextItem.class</p>
 * <p>* String type. E.g. "TextItem"</p>
 * <p>* ItemType enum</p>
 * <p>* Codec. See {@link AbstractItemCodec} E.g. TextItem.CODEC</p>
 * <p>* Instructions for creating an empty instance</p>
 * <p>* Instructions for copying</p>
 */
public class ItemsRegistry {
    @NonNull
    public static final ItemsRegistry REGISTRY = new ItemsRegistry();

    @NonNull
    private static final ItemInfo[] ITEMS = new ItemInfo[]{
            new ItemInfo(DebugTickCounterItem.class,               "DebugTickCounterItem",          ItemType.DEBUG_TICK_COUNTER,         DebugTickCounterItem.CODEC,           DebugTickCounterItem::createEmpty,        (i) -> new DebugTickCounterItem((DebugTickCounterItem) i)).requiredFeatureFlag(FeatureFlag.ITEM_DEBUG_TICK_COUNTER),
            new ItemInfo(TextItem.class,                           "TextItem",                      ItemType.TEXT,                       TextItem.CODEC,                       TextItem::createEmpty,                    (i) -> new TextItem((TextItem) i)),
            new ItemInfo(LongTextItem.class,                       "LongTextItem",                  ItemType.LONG_TEXT,                  LongTextItem.CODEC,                   LongTextItem::createEmpty,                (i) -> new LongTextItem((LongTextItem) i)),
            new ItemInfo(CheckboxItem.class,                       "CheckboxItem",                  ItemType.CHECKBOX,                   CheckboxItem.CODEC,                   CheckboxItem::createEmpty,                (i) -> new CheckboxItem((CheckboxItem) i)),
            new ItemInfo(DayRepeatableCheckboxItem.class,          "DayRepeatableCheckboxItem",     ItemType.CHECKBOX_DAY_REPEATABLE,    DayRepeatableCheckboxItem.CODEC,      DayRepeatableCheckboxItem::createEmpty,   (i) -> new DayRepeatableCheckboxItem((DayRepeatableCheckboxItem) i)),
            new ItemInfo(CounterItem.class,                        "CounterItem",                   ItemType.COUNTER,                    CounterItem.CODEC,                    CounterItem::createEmpty,                 (i) -> new CounterItem((CounterItem) i)),
            new ItemInfo(CycleListItem.class,                      "CycleListItem",                 ItemType.CYCLE_LIST,                 CycleListItem.CODEC,                  CycleListItem::createEmpty,               (i) -> new CycleListItem((CycleListItem) i)),
            new ItemInfo(GroupItem.class,                          "GroupItem",                     ItemType.GROUP,                      GroupItem.CODEC,                      GroupItem::createEmpty,                   (i) -> new GroupItem((GroupItem) i)),
            new ItemInfo(FilterGroupItem.class,                    "FilterGroupItem",               ItemType.FILTER_GROUP,               FilterGroupItem.CODEC,                FilterGroupItem::createEmpty,             (i) -> new FilterGroupItem((FilterGroupItem) i)),
            new ItemInfo(MathGameItem.class,                       "MathGameItem",                  ItemType.MATH_GAME,                  MathGameItem.CODEC,                   MathGameItem::createEmpty,                (i) -> new MathGameItem((MathGameItem) i)),
            new ItemInfo(SleepTimeItem.class,                      "SleepTimeItem",                 ItemType.SLEEP_TIME,                 SleepTimeItem.CODEC,                  SleepTimeItem::createEmpty,               (i) -> new SleepTimeItem((SleepTimeItem) i)).requiredFeatureFlag(FeatureFlag.ITEM_SLEEP_TIME),
            new ItemInfo(MissingNoItem.class,                      "MissingNoItem",                 ItemType.MISSING_NO,                 MissingNoItem.CODEC,                  TextItem::createEmpty,                    (i) -> new TextItem("'missing no' item not support copy :(")).noAvailableToCreate(),
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

    @NonNull
    public ItemInfo get(@NonNull ItemType itemType) {
        for (ItemInfo itemInfo : ITEMS) {
            if (itemType == itemInfo.itemType) return itemInfo;
        }
        throw new NoSuchElementException("Not found item for ItemType: " + itemType);
    }

    @NonNull
    public Item copyItem(@NonNull Item item) {
        return get(item.getClass()).copy(item);
    }

    public static class ItemInfo {
        private final Class<? extends Item> classType;
        private final String stringType;
        private final ItemType itemType;
        private final AbstractItemCodec codec;
        private final ItemCreateInterface createInterface;
        private final ItemCopyInterface copyInterface;
        private boolean noAvailableToCreate;
        private FeatureFlag requiredFeatureFlag;

        public ItemInfo(@NonNull Class<? extends Item> classType, @NonNull String stringType, @NonNull ItemType itemType, @NonNull AbstractItemCodec codec, @NonNull ItemCreateInterface createInterface, @NonNull ItemCopyInterface copyInterface) {
            this.classType = classType;
            this.stringType = stringType;
            this.itemType = itemType;
            this.codec = codec;
            this.createInterface = createInterface;
            this.copyInterface = copyInterface;
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
        public AbstractItemCodec getCodec() {
            return codec;
        }

        @NonNull
        public Item create() {
            return createInterface.create();
        }

        @NonNull
        public Item copy(@NonNull Item item) {
            return copyInterface.copy(item);
        }

        public ItemType getItemType() {
            return itemType;
        }

        public ItemInfo requiredFeatureFlag(FeatureFlag flag) {
            this.requiredFeatureFlag = flag;
            return this;
        }

        public boolean isCompatibility(List<FeatureFlag> flags) {
            return isCompatibility(flags.toArray(new FeatureFlag[0]));
        }

        public boolean isCompatibility(FeatureFlag[] flags) {
            if (noAvailableToCreate) return false;
            if (requiredFeatureFlag == null) return true;
            for (FeatureFlag f : flags) {
                if (f == requiredFeatureFlag) {
                    return true;
                }
            }
            return false;
        }

        public ItemInfo noAvailableToCreate() {
            this.noAvailableToCreate = true;
            return this;
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

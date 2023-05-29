package com.fazziclay.opentoday.app.items.item.filter;

public class FiltersRegistry {
    public static final FiltersRegistry REGISTRY = new FiltersRegistry();

    private static final FilterInfo[] FILTERS = new FilterInfo[]{
            new FilterInfo(DateItemFilter.class,           "DateItemFilter",           FilterType.DATE,             DateItemFilter.CODEC,              DateItemFilter::new),
            new FilterInfo(LogicContainerItemFilter.class, "LogicContainerItemFilter", FilterType.LOGIC_CONTAINER,  LogicContainerItemFilter.CODEC,    LogicContainerItemFilter::new),
            new FilterInfo(ItemStatItemFilter.class,       "ItemStatItemFilter",       FilterType.ITEM_STAT,        ItemStatItemFilter.CODEC,          ItemStatItemFilter::new),
    };

    private FiltersRegistry() {}

    public FilterInfo[] getAllFilters() {
        return FILTERS.clone();
    }

    public int count() {
        return FILTERS.length;
    }

    public FilterInfo getByClass(Class<? extends ItemFilter> c) {
        for (FilterInfo info : FILTERS) {
            if (info.clazz == c) return info;
        }
        return null;
    }

    public FilterInfo getByType(String c) {
        for (FilterInfo info : FILTERS) {
            if (info.stringType.equals(c)) return info;
        }
        return null;
    }

    public static class FilterInfo {
        private final Class<? extends ItemFilter> clazz;
        private final FilterType type;
        private final String stringType;
        private final FilterCodec codec;
        private final CreateFilterInterface createFilterInterface;

        public FilterInfo(Class<? extends ItemFilter> clazz, String stringType, FilterType type, FilterCodec codec, CreateFilterInterface createFilterInterface) {
            this.clazz = clazz;
            this.stringType = stringType;
            this.type = type;
            this.codec = codec;
            this.createFilterInterface = createFilterInterface;
        }

        public Class<? extends ItemFilter> getClazz() {
            return clazz;
        }

        public String getStringType() {
            return stringType;
        }

        public FilterCodec getCodec() {
            return codec;
        }

        public CreateFilterInterface getCreateFilterInterface() {
            return createFilterInterface;
        }

        public FilterType getType() {
            return type;
        }
    }

    public interface CreateFilterInterface {
        ItemFilter create();
    }

    public enum FilterType {
        DATE,
        LOGIC_CONTAINER,
        ITEM_STAT
    }
}

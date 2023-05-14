package com.fazziclay.opentoday.app.items.item.filter;

public class FiltersRegistry {
    public static final FiltersRegistry REGISTRY = new FiltersRegistry();

    private static final FilterInfo[] OBJECTS = new FilterInfo[]{
            new FilterInfo(DateItemFilter.class,           "DateItemFilter",           DateItemFilter.CODEC,              DateItemFilter::new),
            new FilterInfo(LogicContainerItemFilter.class, "LogicContainerItemFilter", LogicContainerItemFilter.CODEC,    LogicContainerItemFilter::new),
    };

    private FiltersRegistry() {}

    public FilterInfo[] getAllFilters() {
        return OBJECTS.clone();
    }

    public int count() {
        return OBJECTS.length;
    }

    public FilterInfo getByClass(Class<? extends ItemFilter> c) {
        for (FilterInfo info : OBJECTS) {
            if (info.clazz == c) return info;
        }
        return null;
    }

    public FilterInfo getByType(String c) {
        for (FilterInfo info : OBJECTS) {
            if (info.stringType.equals(c)) return info;
        }
        return null;
    }

    public static class FilterInfo {
        private final Class<? extends ItemFilter> clazz;
        private final String stringType;
        private final FilterCodec codec;
        private final CreateFilterInterface createFilterInterface;

        public FilterInfo(Class<? extends ItemFilter> clazz, String stringType, FilterCodec codec, CreateFilterInterface createFilterInterface) {
            this.clazz = clazz;
            this.stringType = stringType;
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
    }

    public interface CreateFilterInterface {
        ItemFilter create();
    }
}

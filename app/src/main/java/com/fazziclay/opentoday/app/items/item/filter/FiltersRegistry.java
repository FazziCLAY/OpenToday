package com.fazziclay.opentoday.app.items.item.filter;

public class FiltersRegistry {
    public static final FiltersRegistry REGISTRY = new FiltersRegistry();

    private static final FilterInfo[] INFOS = new FilterInfo[]{
            new FilterInfo(DateItemFilter.class,           "DateItemFilter",           DateItemFilter.IE_TOOL,           DateItemFilter::new),
            new FilterInfo(LogicContainerItemFilter.class, "LogicContainerItemFilter", LogicContainerItemFilter.IE_TOOL, LogicContainerItemFilter::new),
    };

    public FilterInfo[] getAllFilters() {
        return INFOS.clone();
    }

    public int count() {
        return INFOS.length;
    }

    public FilterInfo getByClass(Class<? extends ItemFilter> c) {
        for (FilterInfo info : INFOS) {
            if (info.clazz == c) return info;
        }
        return null;
    }

    public FilterInfo getByType(String c) {
        for (FilterInfo info : INFOS) {
            if (info.stringType.equals(c)) return info;
        }
        return null;
    }

    public static class FilterInfo {
        private final Class<? extends ItemFilter> clazz;
        private final String stringType;
        private final FilterImportExportTool ietool;
        private final CreateFilterInterface createFilterInterface;

        public FilterInfo(Class<? extends ItemFilter> clazz, String stringType, FilterImportExportTool ietool, CreateFilterInterface createFilterInterface) {
            this.clazz = clazz;
            this.stringType = stringType;
            this.ietool = ietool;
            this.createFilterInterface = createFilterInterface;
        }

        public Class<? extends ItemFilter> getClazz() {
            return clazz;
        }

        public String getStringType() {
            return stringType;
        }

        public FilterImportExportTool getIETool() {
            return ietool;
        }

        public CreateFilterInterface getCreateFilterInterface() {
            return createFilterInterface;
        }
    }

    public interface CreateFilterInterface {
        ItemFilter create();
    }
}

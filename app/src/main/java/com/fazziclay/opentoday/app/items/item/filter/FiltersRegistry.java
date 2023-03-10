package com.fazziclay.opentoday.app.items.item.filter;

public class FiltersRegistry {
    public static final FiltersRegistry REGISTRY = new FiltersRegistry();

    private static final FilterInfo[] INFOS = new FilterInfo[]{
            new FilterInfo(DateItemFilter.class, "DateItemFilter", DateItemFilter.IE_TOOL),
            new FilterInfo(LogicContainerItemFilter.class, "LogicContainerItemFilter", LogicContainerItemFilter.IE_TOOL),
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
        private final FilterImportExportTool iteool;

        public FilterInfo(Class<? extends ItemFilter> clazz, String stringType, FilterImportExportTool iteool) {
            this.clazz = clazz;
            this.stringType = stringType;
            this.iteool = iteool;
        }

        public Class<? extends ItemFilter> getClazz() {
            return clazz;
        }

        public String getStringType() {
            return stringType;
        }

        public FilterImportExportTool getIETool() {
            return iteool;
        }
    }
}

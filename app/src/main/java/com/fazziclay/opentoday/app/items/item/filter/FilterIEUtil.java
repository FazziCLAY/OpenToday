package com.fazziclay.opentoday.app.items.item.filter;

import org.json.JSONObject;

public class FilterIEUtil {
    private static final String KEY_FILTER_TYPE = "filterType";

    public static ItemFilter importFilter(JSONObject j) throws Exception {
        // !!! DO NOT REMOVE OPT: THIS IS PART OF DATAFIXER !!!
        return FiltersRegistry.REGISTRY.getByType(j.optString(KEY_FILTER_TYPE, "DateItemFilter")).getIETool().importFilter(j, null);
    }

    public static JSONObject exportFilter(ItemFilter filter) throws Exception {
        FiltersRegistry.FilterInfo f = FiltersRegistry.REGISTRY.getByClass(filter.getClass());
        return f.getIETool().exportFilter(filter).put(KEY_FILTER_TYPE, f.getStringType());
    }
}

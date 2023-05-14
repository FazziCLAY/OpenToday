package com.fazziclay.opentoday.app.items.item.filter;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.data.CherryOrchard;

import java.util.Arrays;
import java.util.List;

public class FilterCodecUtil {
    private static final FiltersRegistry REGISTRY = FiltersRegistry.REGISTRY;
    private static final String KEY_FILTER_TYPE = "filterType";
    private static final String DEFAULT_FILTER_TYPE = REGISTRY.getByClass(DateItemFilter.class).getStringType();

    public static ItemFilter importFilter(Cherry cherry) {
        return REGISTRY.getByType(cherry.optString(KEY_FILTER_TYPE, DEFAULT_FILTER_TYPE)).getCodec().importFilter(cherry, null);
    }

    public static List<ItemFilter> importFiltersList(CherryOrchard orchard) {
        ItemFilter[] ret = new ItemFilter[orchard.length()];
        int i = 0;
        while (i < orchard.length()) {
            Cherry cherry = orchard.getCherryAt(i);
            ret[i] = FilterCodecUtil.importFilter(cherry);
            i++;
        }
        return Arrays.asList(ret);
    }

    public static Cherry exportFilter(ItemFilter filter) {
        FiltersRegistry.FilterInfo f = FiltersRegistry.REGISTRY.getByClass(filter.getClass());
        return f.getCodec().exportFilter(filter).put(KEY_FILTER_TYPE, f.getStringType());
    }

    public static CherryOrchard exportFiltersList(List<ItemFilter> filters) {
        CherryOrchard ret = new CherryOrchard();

        for (ItemFilter filter : filters) {
            ret.put(FilterCodecUtil.exportFilter(filter));
        }

        return ret;
    }
}

package com.fazziclay.opentoday.app.items.item.filter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LogicContainerItemFilter extends ItemFilter {
    public static final FilterImportExportTool IE_TOOL = new FilterImportExportTool() {
        @Override
        public JSONObject exportFilter(ItemFilter filter) throws Exception {
            LogicContainerItemFilter l = (LogicContainerItemFilter) filter;
            JSONArray filters = new JSONArray();
            for (ItemFilter itemFilter : l.filters) {
                filters.put(FilterIEUtil.exportFilter(itemFilter));
            }

            return new JSONObject()
                    .put("filters", filters);
        }

        @Override
        public ItemFilter importFilter(JSONObject json, ItemFilter d) throws Exception {
            LogicContainerItemFilter l = new LogicContainerItemFilter();

            JSONArray filters = json.optJSONArray("filters");
            if (filters == null) filters = new JSONArray();

            int i = 0;
            while (i < filters.length()) {
                JSONObject j = filters.getJSONObject(i);
                l.filters.add(FilterIEUtil.importFilter(j));
                i++;
            }

            return l;
        }
    };

    private final List<ItemFilter> filters = new ArrayList<>();
    private LogicMode logicMode = LogicMode.AND;
    private boolean reverse = false;

    private boolean isFit0(FitEquip fitEquip) {
        if (logicMode == LogicMode.AND) {
            for (ItemFilter filter : filters) {
                if (!filter.isFit(fitEquip)) {
                    return false;
                }
            }

            return true;
        } else if (logicMode == LogicMode.OR) {
            for (ItemFilter filter : filters) {
                if (filter.isFit(fitEquip)) {
                    return true;
                }
            }

            return false;
        }
        return true;
    }

    @Override
    public boolean isFit(FitEquip fitEquip) {
        return reverse != isFit0(fitEquip); // apply reverse
    }

    public void add(ItemFilter itemFilter) {
        filters.add(itemFilter);
    }

    public void remove(ItemFilter filter) {
        filters.remove(filter);
    }

    public void setLogicMode(LogicMode logicMode) {
        this.logicMode = logicMode;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public LogicMode getLogicMode() {
        return logicMode;
    }

    public boolean isReverse() {
        return reverse;
    }

    @Override
    public ItemFilter copy() {
        throw new RuntimeException("Todo: not done"); // TODO: 3/10/23 not done!
    }
}

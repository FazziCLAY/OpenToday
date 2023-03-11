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
                    .put("filters", filters)
                    .put("description", l.description);
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

            l.description = json.optString("description", l.description);

            return l;
        }
    };

    private final List<ItemFilter> filters = new ArrayList<>();
    private LogicMode logicMode = LogicMode.AND;
    private boolean reverse = false;
    private String description = "";

    public LogicContainerItemFilter() {
    }

    public LogicContainerItemFilter(LogicContainerItemFilter copy) {
        this.reverse = copy.reverse;
        this.logicMode = copy.logicMode;
        for (ItemFilter filter : copy.filters) {
            this.filters.add(filter.copy());
        }
        this.description = copy.description;
    }

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

    public ItemFilter[] getFilters() {
        return filters.toArray(new ItemFilter[0]);
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
        return new LogicContainerItemFilter(this);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String s) {
        this.description = s;
    }
}

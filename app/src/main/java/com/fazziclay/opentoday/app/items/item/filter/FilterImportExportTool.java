package com.fazziclay.opentoday.app.items.item.filter;

import org.json.JSONObject;

public abstract class FilterImportExportTool {
    public abstract JSONObject exportFilter(ItemFilter filter) throws Exception;
    public abstract ItemFilter importFilter(JSONObject json, ItemFilter d) throws Exception;
}

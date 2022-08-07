package ru.fazziclay.opentoday.app.items;

import org.json.JSONObject;

import ru.fazziclay.opentoday.app.items.item.Item;

public abstract class ItemImportExportTool {
    public abstract JSONObject exportItem(Item item) throws Exception;
    public abstract Item importItem(JSONObject json) throws Exception;
}

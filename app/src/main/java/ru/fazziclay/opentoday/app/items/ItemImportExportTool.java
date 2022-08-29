package ru.fazziclay.opentoday.app.items;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import ru.fazziclay.opentoday.app.items.item.Item;

public abstract class ItemImportExportTool {
    @NonNull public abstract JSONObject exportItem(@NonNull Item item) throws Exception;
    @NonNull public abstract Item importItem(@NonNull JSONObject json, @Nullable Item item) throws Exception;
}

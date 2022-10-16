package ru.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

public abstract class ItemImportExportTool {
    @NonNull public abstract JSONObject exportItem(@NonNull Item item) throws Exception;
    @NonNull public abstract Item importItem(@NonNull JSONObject json, @Nullable Item item) throws Exception;
}

package com.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

public abstract class TabImportExportTool {
    @NonNull public abstract JSONObject exportTab(@NonNull Tab tab) throws Exception;
    @NonNull public abstract Tab importTab(@NonNull JSONObject json, @Nullable Tab tab) throws Exception;
}

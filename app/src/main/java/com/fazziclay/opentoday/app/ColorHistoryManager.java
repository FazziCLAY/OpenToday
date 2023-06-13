package com.fazziclay.opentoday.app;

import androidx.annotation.IntRange;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.opentoday.util.Logger;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class ColorHistoryManager {
    private static final String TAG = "ColorHistoryManager";
    private final int[] history;
    private int size;
    private final int maxSize;
    private final File dataFile;
    private boolean locked = false;

    public ColorHistoryManager(@Nullable File dataFile, @IntRange(from = 1) int maxSize) {
        if (maxSize < 1) throw new RuntimeException("maxSize can't be <1");
        this.dataFile = dataFile;
        this.maxSize = maxSize;
        this.history = new int[maxSize];
        this.size = 0;
        load();
    }

    public int[] getHistory(int maxCount) {
        if (maxCount == 0 || size == 0) {
            return new int[0];
        }
        if (maxCount > size) maxCount = size;
        int[] result = new int[maxCount];
        int i = 0;
        while (i < result.length) {
            result[i] = history[i];
            i++;
        }
        return result;
    }

    public void addColor(int color) {
        if (locked) return;
        if (size != 0) {
            int latest = history[0];
            if (color == latest) {
                return;
            }
        }
        int i = history.length-1;
        while (i >= 1) {
            history[i] = history[i-1];
            i--;
        }
        history[0] = color;
        if (size < maxSize) size++;
        save();
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        save();
    }

    public boolean isLocked() {
        return locked;
    }

    private void load() {
        if (dataFile == null) return;
        if (FileUtil.isExist(dataFile)) {
            try {
                JSONObject j = new JSONObject(FileUtil.getText(dataFile, "{}"));
                locked = j.optBoolean("locked", false);
                JSONArray ja = j.getJSONArray("history");
                int i = 0;
                while (i < Math.min(ja.length(), maxSize)) {
                    int color = ja.getInt(i);
                    history[i] = color;
                    i++;
                }
                size = i;
            } catch (Exception e) {
                Logger.e(TAG, "exception while load()", e);
            }
        }
    }

    private void save() {
        if (dataFile == null) return;
        try {
            JSONObject j = exportJSONColorHistory();
            FileUtil.setText(dataFile, j.toString());
        } catch (Exception e) {
            throw new RuntimeException("ColorHistoryManager Save exception", e);
        }
    }

    public void importData(JSONObject colorHistory) {
        if (dataFile == null) return;
        FileUtil.setText(dataFile, colorHistory.toString());
        size = 0;
        load();
    }

    public JSONObject exportJSONColorHistory() throws JSONException {
        JSONArray ja = new JSONArray();
        for (Integer i : history) {
            ja.put(i);
        }
        JSONObject j = new JSONObject();
        j.put("history", ja);
        j.put("locked", locked);
        return j;
    }
}

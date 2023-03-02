package com.fazziclay.opentoday.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fazziclay.javaneoutil.FileUtil;

public class ColorHistoryManager {
    // TODO: How to optimize this to primitive int?
    private final List<Integer> history;
    private final int maxSave;
    private final File dataFile;
    private boolean locked = false;

    public ColorHistoryManager(File dataFile, int maxSave) {
        this.dataFile = dataFile;
        this.maxSave = maxSave;
        this.history = load();
    }

    public int[] getHistory(int maxCount) {
        if (history.size() == 0) {
            return new int[0];
        }
        if (maxCount > history.size()) {
            maxCount = history.size();
        }
        Integer[] clazz = history.subList(0, maxCount).toArray(new Integer[0]);
        int[] result = new int[clazz.length];
        int i = 0;
        while (i < result.length) {
            result[i] = clazz[i];
            i++;
        }
        return result;
    }

    public void addColor(int color) {
        if (locked) return;
        if (!history.isEmpty()) {
            int latest = history.get(0);
            if (color == latest) {
                return;
            }
        }
        history.add(0, color);
        if (history.size() > maxSave) {
            history.subList(maxSave, history.size()-1).clear();
        }
        save();
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    private List<Integer> load() {
        if (FileUtil.isExist(dataFile)) {
            try {
                List<Integer> temp = new ArrayList<>();
                JSONObject j = new JSONObject(FileUtil.getText(dataFile, "{}"));
                locked = j.optBoolean("locked", false);
                JSONArray ja = j.getJSONArray("history");
                int i = 0;
                while (i < ja.length()) {
                    int color = ja.getInt(i);
                    temp.add(color);
                    i++;
                }
                return temp;
            } catch (Exception ignored) {}
        }
        return new ArrayList<>();
    }

    public void save() {
        try {
            JSONObject j = exportJSONColorHistory();
            FileUtil.setText(dataFile, j.toString());
        } catch (JSONException e) {
            throw new RuntimeException("Save exception", e);
        }
    }

    public void importData(JSONObject colorHistory) {
        FileUtil.setText(dataFile, colorHistory.toString());
        List<Integer> r = load();
        this.history.clear();
        this.history.addAll(r);
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

package com.fazziclay.opentoday.app.data;

import androidx.annotation.NonNull;

import org.json.JSONArray;

import java.util.List;

/**
 * This is wrapper of JSONArray [
 *      "value1", "value2", 3, 4
 * ]
 */
public class CherryOrchard {
    public static CherryOrchard of(JSONArray array) {
        return new CherryOrchard(array);
    }

    public static CherryOrchard of(long[] array) {
        JSONArray jsonArray = new JSONArray();
        for (long l : array) {
            jsonArray.put(l);
        }
        return new CherryOrchard(jsonArray);
    }

    public static CherryOrchard of(String[] list) {
        JSONArray array = new JSONArray();
        for (String s : list) {
            array.put(s);
        }
        return CherryOrchard.of(array);
    }

    private final JSONArray json;

    public CherryOrchard() {
        json = new JSONArray();
    }

    public CherryOrchard(JSONArray json) {
        this.json = json;
    }

    public static long[] parseLongArray(CherryOrchard orchard, long[] def) {
        if (orchard == null) return def;

        long[] result = new long[orchard.length()];
        orchard.forEachLong((i, value) -> result[i] = value);
        return result;
    }

    public static String[] parseStringArray(CherryOrchard orchard, String[] def) {
        if (orchard == null) return def;

        String[] result = new String[orchard.length()];
        orchard.forEachString((i, value) -> result[i] = value);
        return result;
    }

    public JSONArray toJSONArray() {
        return json;
    }

    public void put(Cherry cherry) {
        json.put(cherry.toJSONObject());
    }

    public void put(String s) {
        json.put(s);
    }

    public int length() {
        return json.length();
    }

    public Cherry getCherryAt(int i) {
        try {
            return Cherry.of(json.getJSONObject(i));
        } catch (Exception e) {
            throw new CherryException("Exception while getCherryAt", e);
        }
    }

    public String getStringAt(int i) {
        try {
            return json.getString(i);
        } catch (Exception e) {
            throw new CherryException("Exception while getStringAt", e);
        }
    }

    private long getLongAt(int i) {
        try {
            return json.getLong(i);
        } catch (Exception e) {
            throw new CherryException("Exception while getLongAt", e);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "CherryOrchard" + json.toString();
    }

    public void forEachString(StringProvider provider) {
        // make reverse loop
        int i = 0;
        while (i < length()) {
            provider.provide(i, getStringAt(i));
            i++;
        }
    }

    public void forEachLong(LongProvider provider) {
        // make reverse loop
        int i = 0;
        while (i < length()) {
            provider.provide(i, getLongAt(i));
            i++;
        }
    }

    public void forEachCherry(CherryProvider provider) {
        // make reverse loop
        int i = 0;
        while (i < length()) {
            provider.provide(i, getCherryAt(i));
            i++;
        }
    }

    public Cherry createAndAdd() {
        Cherry cherry = new Cherry();
        put(cherry);
        return cherry;
    }

    public interface CherryProvider {
        void provide(int index, Cherry cherry);
    }

    public interface LongProvider {
        void provide(int index, long value);
    }

    public interface StringProvider {
        void provide(int index, String value);
    }
}

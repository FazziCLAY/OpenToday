package com.fazziclay.opentoday.app.data;

import androidx.annotation.NonNull;

import org.json.JSONArray;

public class CherryOrchard {
    public static CherryOrchard of(JSONArray array) {
        return new CherryOrchard(array);
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

    public void forEachLong(LongConsumer consumer) {
        // make reverse loop
        int i = 0;
        while (i < length()) {
            consumer.consume(i, getLongAt(i));
            i++;
        }
    }

    public void forEachCherry(CherryConsumer consumer) {
        // make reverse loop
        int i = 0;
        while (i < length()) {
            consumer.consume(i, getCherryAt(i));
            i++;
        }
    }

    public interface CherryConsumer {
        void consume(int index, Cherry cherry);
    }

    public interface LongConsumer {
        void consume(int index, long value);
    }
}

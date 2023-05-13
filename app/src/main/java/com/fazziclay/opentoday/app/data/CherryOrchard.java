package com.fazziclay.opentoday.app.data;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;

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
            throw new CherryException("Exception while getCherryAt", e);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "CherryOrchard" + json.toString();
    }

    public void forEachCherry(CherryConsumer cherryConsumer) {
        // make reverse loop
        int i = 0;
        while (i < length()) {
            cherryConsumer.consume(getCherryAt(i));
            i++;
        }
    }

    public interface CherryConsumer {
        void consume(Cherry cherry);
    }
}

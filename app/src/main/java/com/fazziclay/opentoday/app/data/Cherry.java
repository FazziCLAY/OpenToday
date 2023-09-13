package com.fazziclay.opentoday.app.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This is wrapper for JSONObject {
 *     "key": "value"
 * }
 */
public class Cherry {
    public static Cherry of(JSONObject j) {
        return new Cherry(j);
    }

    private final JSONObject json;

    public Cherry() {
        json = new JSONObject();
    }

    public Cherry(JSONObject json) {
        this.json = json;
    }

    public Cherry put(String key, int v) {
        try {
            json.put(key, v);
        } catch (Exception e) {
            throw getPutException(key, v, e);
        }
        return this;
    }

    public Cherry put(String key, float v) {
        try {
            json.put(key, v);
        } catch (Exception e) {
            throw getPutException(key, v, e);
        }
        return this;
    }

    public Cherry put(String key, double v) {
        try {
            json.put(key, v);
        } catch (Exception e) {
            throw getPutException(key, v, e);
        }
        return this;
    }

    public Cherry put(String key, boolean v) {
        try {
            json.put(key, v);
        } catch (Exception e) {
            throw getPutException(key, v, e);
        }
        return this;
    }

    public Cherry put(String key, String v) {
        if (v == null) {
            json.remove(key);
            return this;
        }

        try {
            json.put(key, v);
        } catch (Exception e) {
            throw getPutException(key, v, e);
        }
        return this;
    }

    public Cherry put(String key, short v) {
        try {
            json.put(key, v);
        } catch (Exception e) {
            throw getPutException(key, v, e);
        }
        return this;
    }

    public Cherry put(String key, CherryOrchard v) {
        if (v == null) {
            json.remove(key);
            return this;
        }

        try {
            json.put(key, v.toJSONArray());
        } catch (Exception e) {
            throw getPutException(key, v, e);
        }
        return this;
    }

    public Cherry put(String key, Enum<?> anEnum) {
        if (anEnum == null) {
            json.remove(key);
            return this;
        }

        try {
            json.put(key, anEnum == null ? null : anEnum.name());
        } catch (Exception e) {
            throw getPutException(key, anEnum, e);
        }
        return this;
    }

    public Cherry put(String key, Cherry v) {
        if (v == null) {
            json.remove(key);
            return this;
        }
        try {
            json.put(key, v.toJSONObject());
        } catch (Exception e) {
            throw getPutException(key, v, e);
        }
        return this;
    }

    public JSONObject toJSONObject() {
        return json;
    }

    private RuntimeException getPutException(String key, Object v, Exception e) {
        return new CherryException("Put '"+v+"' by key '"+key+"' exception", e);
    }

    public CherryOrchard createOrchard(String key) {
        CherryOrchard orchard = new CherryOrchard();
        put(key, orchard);
        return orchard;
    }

    public String optString(String key, String def) {
        return json.optString(key, def);
    }

    public boolean optBoolean(String key, boolean def) {
        return json.optBoolean(key, def);
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<?>> T optEnum(String key, T def) {
        try {
            return (T) Enum.valueOf(def.getClass(), json.optString(key, def.name()));
        } catch (Exception e) {
            return def;
        }
    }

    public int optInt(String key, int def) {
        return json.optInt(key, def);
    }

    public CherryOrchard optOrchard(String key) {
        JSONArray jsonArray = json.optJSONArray(key);
        if (jsonArray == null) jsonArray = new JSONArray();
        return CherryOrchard.of(jsonArray);
    }

    public CherryOrchard getOrchard(String key) {
        if (!has(key)) return null;
        JSONArray jsonArray = json.optJSONArray(key);
        if (jsonArray == null) jsonArray = new JSONArray();
        return CherryOrchard.of(jsonArray);
    }

    public Cherry optCherry(String key) {
        JSONObject jsonObject = json.optJSONObject(key);
        if (jsonObject == null) jsonObject = new JSONObject();
        return Cherry.of(jsonObject);
    }

    public Cherry getCherry(String key) {
        if (!has(key)) return null;
        return Cherry.of(json.optJSONObject(key));
    }

    public double optDouble(String key, double def) {
        return json.optDouble(key, def);
    }

    public boolean has(String key) {
        return json.has(key);
    }

    public boolean isEmpty() {
        return json.length() == 0;
    }

    @NonNull
    @Override
    public String toString() {
        return "Cherry"+json.toString();
    }

    @Nullable
    public String getString(String key) {
        if (!has(key)) return null;
        try {
            return json.getString(key);
        } catch (Exception e) {
            throw new CherryException("getString exception", e);
        }
    }
}

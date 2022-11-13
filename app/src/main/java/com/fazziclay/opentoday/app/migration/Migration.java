package com.fazziclay.opentoday.app.migration;

import android.util.Log;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.AppType;
import com.fazziclay.opentoday.app.FeatureFlag;
import com.fazziclay.opentoday.util.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

// TODO: 13.11.2022 DELETe afred mignration
public class Migration {
    public static final boolean IS_LOCAL_HOST = false;
    public static final String LOCAL_HOST = "http://192.168.8.143/";
    public static final String URL_HOST = IS_LOCAL_HOST ? LOCAL_HOST : "https://fazziclay.github.io/";
    public static final String URL_IS = URL_HOST + "/api/project_3/v1/the_end/is";
    public static final String URL_M =  URL_HOST + "/api/project_3/v1/the_end/m.json";
    public static final boolean ENABLED = App.APP_TYPE == AppType.OLD_RED;
    public static final int LOAD_DELAY = 0;


    public static void is(IsTime isTime) {
        if (!ENABLED) {
            Log.e("Migration", "Not enabled.");
            isTime.run(false, null, null);
            return;
        }
        new Thread(() -> {
            try {
                if (LOAD_DELAY > 0) Thread.sleep(LOAD_DELAY);
                String parseIs = NetworkUtil.parseTextPage(URL_IS);
                boolean is = parseIs.contains("1");
                if (is) {
                    String parseM = NetworkUtil.parseTextPage(URL_M);
                    isTime.run(true, M.from(new JSONObject(parseM)), null);
                } else {
                    isTime.run(false, null, null);
                }

            } catch (Exception e) {
                e.printStackTrace();
                isTime.run(false, null, e);
                App.exception(null, e);
            }
        }).start();
    }

    public interface IsTime {
        void run(boolean isTime, M m, Exception e);
    }

    public static class M {
        public boolean isActive;
        public String updateApkDirectUrl;
        public boolean previewMode;
        public boolean allowOnThisVersion;

        // texts
        public JSONObject defaultTexts;
        public JSONObject localedTexts;
        public String defaultLocale;
        public String userLocale;

        public String firstMessage;
        public String downloadTitle;
        public String downloadButtonText;
        public String downloadDescription;
        public String exportTitle;
        public String exportButtonText;
        public String exportDescription;

        public static M from(JSONObject j) throws JSONException {
            M m = new M();
            m.isActive = j.getBoolean("isActive");
            m.updateApkDirectUrl = j.getString("updateApkDirectUrl");
            m.previewMode = j.getBoolean("previewMode");

            m.allowOnThisVersion = false;
            JSONArray allowedVersions = j.getJSONArray("allowedBuilds");
            int i = 0;
            while (i < allowedVersions.length()) {
                int ver = allowedVersions.getInt(i);
                if (App.VERSION_CODE == ver) {
                    m.allowOnThisVersion = true;
                    break;
                }
                i++;
            }

            JSONObject texts = j.getJSONObject("texts");
            m.defaultLocale = j.getString("texts_defaultLocale");
            m.userLocale = Locale.getDefault().getLanguage();

            m.defaultTexts = texts.getJSONObject(m.defaultLocale);
            m.localedTexts = m.defaultTexts;
            if (texts.has(m.userLocale)) {
                try {
                    m.localedTexts = texts.getJSONObject(m.userLocale);
                } catch (Exception ignored) {}
            }
            m.loadTexts();
            return m;
        }

        private void loadTexts() {
            firstMessage = parseText("firstMessage");
            downloadTitle = parseText("downloadTitle");
            downloadButtonText = parseText("downloadButtonText");
            downloadDescription = parseText("downloadDescription");
            exportTitle = parseText("exportTitle");
            exportButtonText = parseText("exportButtonText");
            exportDescription = parseText("exportDescription");
        }

        private String parseText(String key) {
            return localedTexts.optString(key, defaultTexts.optString(key, "$[-#ff0000;=#666666]OpenToday error: Message '"+key+"' not found in locale & default"));
        }

        public boolean isTimeForMe(App app) {
            if (isActive) {
                if (previewMode) {
                    return app.isFeatureFlag(FeatureFlag.PREVIEW_MIGRATION);
                } else {
                    return true;
                }
            }
            return false;
        }
    }
}

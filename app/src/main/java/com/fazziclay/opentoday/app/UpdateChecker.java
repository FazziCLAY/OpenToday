package com.fazziclay.opentoday.app;

import android.content.Context;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.NetworkUtil;

import org.json.JSONObject;

import java.io.File;

public class UpdateChecker {
    private static final String V2_URL_LATEST_BUILD = "https://fazziclay.github.io/api/project_3/v2/latest_build";
    private static final String V2_URL_LATEST = "https://fazziclay.github.io/api/project_3/v2/latest.json";

    private static final long CACHE_TIMEOUT_MILLIS = 5 * 60 * 60 * 1000; // 5 hours
    private static final String TAG = "UpdateChecker";

    public static void check(Context context, Result result) {
        new Thread(() -> {
            final long CURRENT_TIME = System.currentTimeMillis();
            File cacheFile = new File(context.getExternalCacheDir(), "latest_update_check");
            if (FileUtil.isExist(cacheFile)) {
                try {
                    long latestCheck = Long.parseLong(FileUtil.getText(cacheFile));
                    if (CURRENT_TIME - latestCheck < CACHE_TIMEOUT_MILLIS) {
                        result.run(false, null);
                        Logger.d(TAG, "run. available=false (CACHED!!!)");
                        return;
                    }
                } catch (Exception e) {
                    Logger.d(TAG, "cache exception", e);
                }
            }

            try {
                String stringLatestBuild = NetworkUtil.parseTextPage(V2_URL_LATEST_BUILD);
                int latestBuild = Integer.parseInt(stringLatestBuild);
                Logger.d(TAG, "latestBuild (remote) = " + latestBuild);

                if (App.VERSION_CODE < latestBuild) {
                    String latestJsonString = NetworkUtil.parseTextPage(V2_URL_LATEST);
                    JSONObject latestJson = new JSONObject(latestJsonString);
                    Logger.d(TAG, "latest.json (remote) = " + latestJsonString);


                    String url = latestJson.getString("page_url");
                    Logger.d(TAG, "url (remote) = " + url);
                    result.run(true, url);
                    Logger.d(TAG, "run. available=true");
                } else {
                    result.run(false, null);
                    Logger.d(TAG, "run. available=false");
                    FileUtil.setText(cacheFile, String.valueOf(CURRENT_TIME));
                }

            } catch (Exception e) {
                Logger.d(TAG, "check exception", e);
            }
        }).start();
    }

    public interface Result {
        void run(boolean available, String url);
    }
}

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

    private static final long CACHE_TIMEOUT_MILLIS = 48 * 60 * 60 * 1000; // 48 hours
    private static final String TAG = "UpdateChecker";

    public static void check(Context context, Result result) {
        Thread thread = new UpdateCheckedThread(context, result);
        thread.start();
    }

    public interface Result {
        void run(boolean available, String url, String name);
    }

    private static class UpdateCheckedThread extends Thread {
        private final Result result;
        private final File cacheFile;

        public UpdateCheckedThread(Context context, Result result) {
            setName("UpdateCheckerThread");
            this.result = result;
            this.cacheFile = new File(context.getExternalCacheDir(), "latest_update_check");
        }

        private void callback(boolean available, String pageURL, String name, boolean cached) {
            result.run(available, pageURL, name);
            Logger.d(TAG, "callback run."+(cached ? " (cached!)" : "")+" available="+available+" pageURL="+pageURL+" name="+name);
        }

        @Override
        public void run() {
            final long currentTime = System.currentTimeMillis();

            if (FileUtil.isExist(cacheFile)) {
                try {
                    long latestCheck = Long.parseLong(FileUtil.getText(cacheFile));
                    if ((currentTime - latestCheck) < CACHE_TIMEOUT_MILLIS) {
                        callback(false, null, null, true);
                        return;
                    }
                } catch (Exception e) {
                    Logger.e(TAG, "cache exception", e);
                }
            }

            try {
                String stringLatestBuild = NetworkUtil.parseTextPage(V2_URL_LATEST_BUILD);
                int latestBuild = Integer.parseInt(stringLatestBuild);
                Logger.d(TAG, "latest_build (remote) = " + latestBuild);

                if (App.VERSION_CODE < latestBuild) {
                    String latestJsonString = NetworkUtil.parseTextPage(V2_URL_LATEST);
                    JSONObject latestJson = new JSONObject(latestJsonString);
                    Logger.d(TAG, "latest.json (remote) = " + latestJsonString);

                    String url = latestJson.getString("page_url");
                    String name = latestJson.optString("name", "OT");
                    Logger.d(TAG, "latest.json->url (remote) = " + url);
                    callback(true, url, name, false);
                } else {
                    callback(false, null, null, false);
                    FileUtil.setText(cacheFile, String.valueOf(currentTime));
                    Logger.d(TAG, "Cache file saved. Content: " + currentTime);
                }

            } catch (Exception e) {
                Logger.e(TAG, "check exception", e);
                ImportantDebugCallback.pushStatic("UpdateChecker exception: " + e);
            }
        }
    }
}

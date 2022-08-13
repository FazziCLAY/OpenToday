package ru.fazziclay.opentoday.app.updatechecker;

import android.content.Context;
import android.util.Log;

import java.io.File;

import ru.fazziclay.javaneoutil.FileUtil;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.util.NetworkUtil;

public class UpdateChecker {
    private static final String URL = "https://fazziclay.github.io/api/project_3/v1/latest/url";
    private static final String BUILD = "https://fazziclay.github.io/api/project_3/v1/latest/build";
    private static final long CACHE_TIMEOUT_MILLIS = 1000 * 60 * 60;
    
    public static void check(Context context, Check check) {
        new Thread(() -> {
            final long CURRENT_TIME = System.currentTimeMillis();
            File cacheFile = new File(context.getExternalCacheDir(), "latest_update_check");
            if (FileUtil.isExist(cacheFile)) {
                try {
                    long latestCheck = Long.parseLong(FileUtil.getText(cacheFile));
                    if (CURRENT_TIME - latestCheck < CACHE_TIMEOUT_MILLIS) {
                        check.run(false, null);
                        Log.d("UpdateChecker", "run. available=false (CACHED!!!)");
                        return;
                    }
                } catch (Exception e) {
                    Log.d("UpdateChecker", "cache exception", e);
                }
            }

            try {
                String stringLatestBuild = NetworkUtil.parseTextPage(BUILD);
                int latestBuild = Integer.parseInt(stringLatestBuild);
                Log.d("UpdateChecker", "latestBuild (remote) = " + latestBuild);

                if (App.VERSION_CODE < latestBuild) {
                    String url = NetworkUtil.parseTextPage(URL);
                    Log.d("UpdateChecker", "url (remote) = " + url);
                    check.run(true, url);
                    Log.d("UpdateChecker", "run. available=true");
                } else {
                    check.run(false, null);
                    Log.d("UpdateChecker", "run. available=false");
                    FileUtil.setText(cacheFile, String.valueOf(CURRENT_TIME));
                }

            } catch (Exception e) {
                Log.d("UpdateChecker", "check exception", e);
            }
        }).start();
    }

    public interface Check {
        void run(boolean available, String url);
    }
}

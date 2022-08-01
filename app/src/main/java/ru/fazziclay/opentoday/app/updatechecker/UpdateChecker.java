package ru.fazziclay.opentoday.app.updatechecker;

import android.util.Log;

import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.util.NetworkUtil;

public class UpdateChecker {
    private static final String URL = "https://fazziclay.github.io/api/project_3/v1/latest/url";
    private static final String BUILD = "https://fazziclay.github.io/api/project_3/v1/latest/build";

    public UpdateChecker() {
    }

    public void check(Check check) {
        new Thread(() -> {
            try {
                String stringLatestBuild = NetworkUtil.parseTextPage(BUILD);
                int latestBuild = Integer.parseInt(stringLatestBuild);
                Log.d("UpdateChecker", "latestBuild (remote) = " + latestBuild);

                if (App.VERSION_CODE < latestBuild) {
                    String url = NetworkUtil.parseTextPage(URL);
                    Log.d("UpdateChecker", "url (remote) = " + url);
                    check.run(true, url);
                } else {
                    check.run(false, null);
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

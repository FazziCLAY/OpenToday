package com.fazziclay.opentoday.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * for network
 * **/
public class NetworkUtil {
    private static final String TAG = "NetworkUtil";
    public static final List<LogInterface> NETWORK_LISTENERS = new ArrayList<>();
    private static final HashMap<String, String> DEBUG_CONTENTS = new HashMap<>();

    static {
        if (App.DEBUG) NETWORK_LISTENERS.add((logKey, state, url, result) -> Logger.d(TAG, "Networking '"+logKey+"' State: " + state + " URL: " + url + " Result: " + result));
        if (Debug.DEBUG_NETWORK_UTIL_SHADOWCONTENT) {
            // Update checked test
            DEBUG_CONTENTS.put("https://fazziclay.github.io/api/project_3/v2/latest_build", "999");
        }
    }

    /**
     * @return text of site
     * **/
    public static String parseTextPage(String url) throws IOException {
        // LOG START
        int logKey = 0;
        if (!NETWORK_LISTENERS.isEmpty()) logKey = generateLogKey();
        for (LogInterface logInterface : NETWORK_LISTENERS) {
            logInterface.parseTextPage(logKey, 0, url, null);
        }
        // LOG END

        final StringBuilder result = new StringBuilder();
        String debugMessage = "";
        if (!isDebugURL(url)) {
            final URL pageUrl = new URL(url);
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pageUrl.openStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line).append("\n");
            }
            bufferedReader.close();
        } else {
            result.append(getDebugURLContent(url)).append("\n");
            debugMessage = "(!) WARNING THIS CONTENT IS SHADOW-DEBUG-CONTENT IN NETWORK UTIL: ";
        }

        final String ret = result.substring(0, result.lastIndexOf("\n"));
        for (LogInterface logInterface : NETWORK_LISTENERS) {
            logInterface.parseTextPage(logKey, 1, url, debugMessage + ret);
        }

        return ret;
    }

    private static String getDebugURLContent(String url) {
        return DEBUG_CONTENTS.get(url);
    }

    private static boolean isDebugURL(String url) {
        return DEBUG_CONTENTS.containsKey(url);
    }

    private static int generateLogKey() {
        return RandomUtil.nextIntPositive();
    }
    
    public static void openBrowser(Activity activity, String url) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(browserIntent);
        } catch (Exception e) {
            Logger.e(TAG, "openBrowser", e);
            Toast.makeText(activity, R.string.abc_error_browserNotFound, Toast.LENGTH_LONG).show();
        }
    }

    public interface LogInterface {
        void parseTextPage(int logKey, int state, String url, String result);
    }
}

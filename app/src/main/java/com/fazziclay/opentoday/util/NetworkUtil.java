package com.fazziclay.opentoday.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.fazziclay.opentoday.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * for network
 * **/
public class NetworkUtil {
    private static final String TAG = "NetworkUtil";
    public static final List<LogInterface> NETWORK_LISTENERS = new ArrayList<>();

    static {
        NETWORK_LISTENERS.add((logKey, state, url, result) -> Logger.d(TAG, "Networking '"+logKey+"' State: " + state + " URL: " + url + " Result: " + result));
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
        final URL pageUrl = new URL(url);
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pageUrl.openStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line).append("\n");
        }
        bufferedReader.close();

        final String ret = result.substring(0, result.lastIndexOf("\n"));
        for (LogInterface logInterface : NETWORK_LISTENERS) {
            logInterface.parseTextPage(logKey, 1, url, ret);
        }

        return ret;
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

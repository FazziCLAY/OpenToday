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
import java.util.Random;

/**
 * for network
 * **/
public class NetworkUtil {
    public static final List<LogInterface> NETWORK_LISTENERS = new ArrayList<>();

    /**
     * @return text of site
     * **/
    public static String parseTextPage(String url) throws IOException {
        int logKey = 0;
        if (NETWORK_LISTENERS.size() > 0) logKey = generateLogKey();
        for (LogInterface logInterface : NETWORK_LISTENERS) {
            logInterface.parseTextPage(logKey, 0, url, null);
        }

        StringBuilder result = new StringBuilder();
        URL pageUrl = new URL(url);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pageUrl.openStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line).append("\n");
        }
        bufferedReader.close();

        String ret = result.substring(0, result.lastIndexOf("\n"));
        for (LogInterface logInterface : NETWORK_LISTENERS) {
            logInterface.parseTextPage(logKey, 1, url, ret);
        }

        return ret;
    }

    private static int generateLogKey() {
        int logKey = new Random().nextInt();
        if (logKey < 0) logKey *= -1;
        return logKey;
    }
    
    public static void openBrowser(Activity activity, String url) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(activity, R.string.abc_error_browserNotFound, Toast.LENGTH_LONG).show();
        }
    }

    public interface LogInterface {
        void parseTextPage(int logKey, int state, String url, String result);
    }
}

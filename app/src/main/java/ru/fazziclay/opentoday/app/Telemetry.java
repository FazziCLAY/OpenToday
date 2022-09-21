package ru.fazziclay.opentoday.app;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import ru.fazziclay.javaneoutil.FileUtil;
import ru.fazziclay.opentoday.telemetry.Client;
import ru.fazziclay.opentoday.util.DebugUtil;
import ru.fazziclay.opentoday.util.NetworkUtil;

public class Telemetry {
    private static final boolean NO_DELAY = true;

    private final App app;
    private final File latestAutoSendTimeFile;
    private Client client = null;

    public Telemetry(App app) {
        this.app = app;
        this.latestAutoSendTimeFile = new File(app.getExternalCacheDir(), "latest-telemetry-auto-send");
    }

    public void applicationStart() {
        try {
            Log.e("Telemetry", "App started");

            boolean timeToSend = System.currentTimeMillis() - Long.parseLong(FileUtil.getText(latestAutoSendTimeFile, "0")) > 24 * 60 * 60 * 1000;
            if (timeToSend || NO_DELAY) {
                FileUtil.setText(latestAutoSendTimeFile, String.valueOf(System.currentTimeMillis()));

                Log.e("Telemetry", "app started yeah");

                send("App");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mainActivityStart() {
        try {
            Log.e("Telemetry", "mainActivityStarted");

            boolean timeToSend = System.currentTimeMillis() - Long.parseLong(FileUtil.getText(latestAutoSendTimeFile, "0")) > 24 * 60 * 60 * 1000;
            if (timeToSend || NO_DELAY) {
                FileUtil.setText(latestAutoSendTimeFile, String.valueOf(System.currentTimeMillis()));

                Log.e("Telemetry", "mainActivity started yeah");


                send("MainActivity");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void send(String from) {
        send(from, null);
    }

    private void send(String from, JSONObject otherSend) {
        if (otherSend == null) {
            otherSend = new JSONObject();
        }
        try {
            otherSend.put("from", from);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject finalOtherSend = otherSend;
        new Thread(() -> {
            try {
                JSONObject telemetryJson = new JSONObject(NetworkUtil.parseTextPage("https://fazziclay.github.io/api/project_3/v1/telemetry/telemetry_v1.json"));
                if (!telemetryJson.getBoolean("enabled")) {
                    Log.e("Telemetry", "not enabled");
                    return;
                }

                client = new Client(telemetryJson.getString("host"), telemetryJson.getInt("port"), app.getSettingsManager().getInstanceId(), new JSONObject()
                        .put("versionData", app.getVersionData())
                        .put("otherSend", finalOtherSend));

                client.start();
                Log.e("Telemetry", "start, wait 5 seconds");

                DebugUtil.sleep(5000);

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void crash(Context context, CrashReport crashReport) {
        try {
            send("Crash", new JSONObject()
                    .put("crash", new JSONObject()
                            .put("crashId", crashReport.getID().toString())
                            .put("crashText", crashReport.convertToText())
                    ));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

package com.fazziclay.opentoday.app;

import static com.fazziclay.opentoday.util.InlineUtil.IPROF;

import androidx.annotation.NonNull;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.neosocket.Client;
import com.fazziclay.neosocket.PacketHandler;
import com.fazziclay.neosocket.packet.Packet;
import com.fazziclay.neosocket.packet.PacketsRegistry;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.UUID;

import ru.fazziclay.opentoday.telemetry.TelemetryPackets;
import ru.fazziclay.opentoday.telemetry.packet.Packet20004Login;
import ru.fazziclay.opentoday.telemetry.packet.Packet20005Handshake;
import ru.fazziclay.opentoday.telemetry.packet.Packet20006CrashReport;
import ru.fazziclay.opentoday.telemetry.packet.Packet20007DataFixerLogs;
import ru.fazziclay.opentoday.telemetry.packet.Packet20008UIOpen;
import ru.fazziclay.opentoday.telemetry.packet.Packet20009UIClosed;
import ru.fazziclay.opentoday.telemetry.packet.PacketSetVersion;

public class Telemetry {
    private static final String TAG = "Telemetry";
    public static final PacketsRegistry REGISTRY = new TelemetryPackets();
    private static final boolean NO_DELAY = (App.DEBUG && false);
    private static final String URL = "https://fazziclay.github.io/api/project_3/v2/telemetry_v1.json";
    private static final boolean DEBUG_LOCAL_URL_CONTENT_ENABLED = App.debug(false);
    private static final String DEBUG_LOCAL_URL_CONTENT = "{\"enabled\":true,\"host\":\"192.168.10.143\",\"port\":5999}";

    private final App app;
    private final File lastFile;
    private TelemetryStatus telemetryStatus = null;
    private boolean isTelemetryStatusQuerying = false;
    private boolean isEnabled;

    public Telemetry(App app, boolean isEnabled) {
        this.app = app;
        this.lastFile = new File(app.getExternalCacheDir(), "telemetry-lasts.json");
        this.isEnabled = isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void queryTelemetryStatus() {
        if (!isEnabled) return;
        isTelemetryStatusQuerying = true;
        new Thread(() -> {
            try {
                JSONObject telemetryJson;
                if (DEBUG_LOCAL_URL_CONTENT_ENABLED) {
                    telemetryJson = new JSONObject(DEBUG_LOCAL_URL_CONTENT);
                } else {
                    telemetryJson = new JSONObject(NetworkUtil.parseTextPage(URL));
                }
                telemetryStatus = TelemetryStatus.fromJson(telemetryJson);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            isTelemetryStatusQuerying = false;
        }).start();
    }

    public void send(LPacket lPacket) {
        IPROF.push("Telemetry:send");
        if (!isEnabled) {
            IPROF.pop();
            return;
        }
        Logger.d(TAG, "send(): " + lPacket);
        if (lPacket.isDelay() && !NO_DELAY) {
            long last = getLastSend(lPacket.getClass().getName());
            long curr = System.currentTimeMillis();
            Logger.d(TAG, "send() last=", last, "curr=", curr);
            boolean hoursNoDelayed = curr - last < 24*60*60*1000;
            if (hoursNoDelayed) {
                GregorianCalendar g = new GregorianCalendar();
                g.setTimeInMillis(last);
                int d1 = g.get(Calendar.DAY_OF_MONTH);
                g = new GregorianCalendar();
                g.setTimeInMillis(curr);
                int d2 = g.get(Calendar.DAY_OF_MONTH);
                Logger.d(TAG, "send() d1=", d1, "d2=", d2 + " sanding-canceled: " + (d1 == d2));
                if (d1 == d2) {
                    IPROF.pop();
                    return;
                }
            }
        }

        SendThread thread = new SendThread(lPacket.getPacket());
        thread.start();
        Logger.d(TAG, "send(): wait");
        while (thread.isBusy() && lPacket.isBlocking()) {
            thread.tick();
        }
        Logger.d(TAG, "send(): wait: done");
        setLastSend(lPacket.getClass().getName());
        IPROF.pop();
    }

    @NonNull
    private JSONObject getLastJson() {
        JSONObject json;
        if (FileUtil.isExist(lastFile)) {
            try {
                json = new JSONObject(FileUtil.getText(lastFile, "{}"));
            } catch (JSONException e) {
                json = new JSONObject();
            }
        } else {
            json = new JSONObject();
        }
        return json;
    }

    private void setLastSend(@NonNull String name) {
        try {
            FileUtil.setText(lastFile, getLastJson()
                    .put(name, System.currentTimeMillis())
                    .toString());
        } catch (JSONException e) {
            throw new RuntimeException("setLastSend JSONException", e);
        }
    }

    private long getLastSend(@NonNull String name) {
        return getLastJson().optLong(name, 0);
    }

    public class SendThread extends Thread {
        private static final String TAG = Telemetry.TAG + "-[SendThread]";
        private boolean isBusy = true;
        private final Packet packet;
        private boolean send = false;

        public SendThread(Packet packet) {
            this.packet = packet;
        }

        @Override
        public void run() {
            isBusy = true;
            try {
                Logger.d(TAG, "SaveThread run()");
                if (!isTelemetryStatusQuerying && telemetryStatus == null) {
                    queryTelemetryStatus();
                }
                if (isTelemetryStatusQuerying && telemetryStatus == null) {
                    while (Telemetry.this.isTelemetryStatusQuerying) {
                        tick();
                    }
                }
                Logger.d(TAG, "SaveThread: query done");
                if (telemetryStatus == null) {
                    throw new RuntimeException("WTF: don't query telemetryStatus");
                }
                if (telemetryStatus.isEnabled()) {
                    Logger.d(TAG, "SaveThread: enabled! client new");
                    Client client = new Client(telemetryStatus.host(), telemetryStatus.port(), REGISTRY, new PacketHandler() {
                        @Override
                        public void received(Client client, Packet packet) {
                            Logger.d(TAG, "[client] received: ", packet.toString());
                        }

                        @Override
                        public void setup(Client client) {
                            Logger.d(TAG, "[client] setup");
                            try {
                                client.send(new PacketSetVersion(2));
                                client.send(new Packet20004Login(app.getInstanceId()));
                                client.send(new Packet20005Handshake(App.VERSION_CODE));
                                client.send(packet);

                                Thread.sleep(1000);
                                send = true;

                            } catch (Exception e) {
                                Logger.e(TAG, "[client] setup exception", e);
                            }
                        }

                        @Override
                        public void preDisconnect(Client client) {
                            Logger.d(TAG, "[client] preDisconnect");
                        }

                        @Override
                        public void fatalException(Client client, Exception e) {
                            Logger.d(TAG, "[client] fatalException", e);
                        }
                    });
                    new Thread(() -> {
                        try {
                            client.run();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();

                    Logger.d(TAG, "wait send or 10 ser");
                    long start = System.currentTimeMillis();
                    while (!send && System.currentTimeMillis() - start < 10 * 1000) {
                        tick();
                    }
                    Logger.d(TAG, "wait send or 10 ser: done");

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            isBusy = false;
        }

        public boolean isBusy() {
            return this.isBusy;
        }

        public void tick() {

        }
    }

    public static class LPacket {
        private final boolean isDelay;
        private final boolean blocking;
        private final Packet packet;

        public LPacket(boolean isDelay, boolean blocking, Packet packet) {
            this.isDelay = isDelay;
            this.blocking = blocking;
            this.packet = packet;
        }

        public Packet getPacket() {
            return packet;
        }

        public boolean isDelay() {
            return isDelay;
        }

        public boolean isBlocking() {
            return blocking;
        }
    }

    public static class UiOpenLPacket extends LPacket {
        public UiOpenLPacket() {
            super(true, false, new Packet20008UIOpen());
        }
    }

    public static class UiClosedLPacket extends LPacket {
        public UiClosedLPacket() {
            super(true, false, new Packet20009UIClosed());
        }
    }

    public static class CrashReportLPacket extends LPacket {
        public CrashReportLPacket(CrashReport crashReport) {
            super(false, true, new Packet20006CrashReport(crashReport.getID(), crashReport.getThrowable().toString(), crashReport.convertToText()));
        }

        public CrashReportLPacket(UUID id, String throwable, String crashText) {
            super(false, true, new Packet20006CrashReport(id, throwable, crashText));
        }
    }

    public static class DataFixerLogsLPacket extends LPacket {
        public DataFixerLogsLPacket(int dataVersion, String logs) {
            super(false, false, new Packet20007DataFixerLogs(dataVersion, logs));
        }
    }

    private static final class TelemetryStatus {
        private final boolean isEnabled;
        private final String host;
        private final int port;

        private TelemetryStatus(boolean isEnabled, String host, int port) {
            this.isEnabled = isEnabled;
            this.host = host;
            this.port = port;
        }

        public static TelemetryStatus fromJson(JSONObject j) throws JSONException {
            if (j == null) return null;
            return new TelemetryStatus(
                    j.optBoolean("enabled", false),
                    j.optString("host", null),
                    j.optInt("port", 0)
            );
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public String host() {
            return host;
        }

        public int port() {
            return port;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (TelemetryStatus) obj;
            return this.isEnabled == that.isEnabled &&
                    Objects.equals(this.host, that.host) &&
                    this.port == that.port;
        }

        @Override
        public int hashCode() {
            return Objects.hash(isEnabled, host, port);
        }

        @NonNull
        @Override
        public String toString() {
            return "TelemetryStatus[" +
                    "isEnabled=" + isEnabled + ", " +
                    "host=" + host + ", " +
                    "port=" + port + ']';
        }
    }
}

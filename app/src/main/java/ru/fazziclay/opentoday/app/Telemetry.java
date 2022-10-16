package ru.fazziclay.opentoday.app;

import androidx.annotation.NonNull;

import com.fazziclay.neosocket.Client;
import com.fazziclay.neosocket.PacketHandler;
import com.fazziclay.neosocket.packet.Packet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.fazziclay.javaneoutil.FileUtil;
import ru.fazziclay.opentoday.telemetry.TelemetryPackets;
import ru.fazziclay.opentoday.telemetry.packet.PacketCrashReport;
import ru.fazziclay.opentoday.telemetry.packet.PacketDataFixerLogs;
import ru.fazziclay.opentoday.telemetry.packet.PacketHandshake;
import ru.fazziclay.opentoday.telemetry.packet.PacketLogin;
import ru.fazziclay.opentoday.telemetry.packet.PacketSetVersion;
import ru.fazziclay.opentoday.telemetry.packet.PacketUIClosed;
import ru.fazziclay.opentoday.telemetry.packet.PacketUIOpen;
import ru.fazziclay.opentoday.util.L;
import ru.fazziclay.opentoday.util.NetworkUtil;

public class Telemetry {
    private static final boolean NO_DELAY = (App.DEBUG && true);
    private static final String URL = "https://fazziclay.github.io/api/project_3/v1/telemetry/telemetry_v2.json";

    private final App app;
    private final File lastFile;
    private TelemetryStatus telemetryStatus = null;
    private boolean isTelemetryStatusQuerying = false;

    public Telemetry(App app) {
        this.app = app;
        this.lastFile = new File(app.getExternalCacheDir(), "telemetry-lasts.json");
    }

    public void queryTelemetryStatus() {
        isTelemetryStatusQuerying = true;
        new Thread(() -> {
            try {
                JSONObject telemetryJson = new JSONObject(NetworkUtil.parseTextPage(URL));
                telemetryStatus = TelemetryStatus.fromJson(telemetryJson);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            isTelemetryStatusQuerying = false;
        }).start();
    }

    public void send(LPacket LPacket) {
        L.o("Telemetry send");
        if (LPacket.isDelay() && !NO_DELAY) {
            long last = getLastSend(LPacket.getClass().getName());
            long curr = System.currentTimeMillis();
            L.o("Telemetry last=", last, "curr=", curr);
            boolean hoursNoDelayed = curr - last < 24*60*60*1000;
            if (hoursNoDelayed) {
                GregorianCalendar g = new GregorianCalendar();
                g.setTimeInMillis(last);
                int d1 = g.get(Calendar.DAY_OF_MONTH);
                g = new GregorianCalendar();
                g.setTimeInMillis(curr);
                int d2 = g.get(Calendar.DAY_OF_MONTH);
                L.o("Telemetry d1=", d1, "d2=", d2);
                if (d1 == d2) {
                    return;
                }
            }
        }

        SendThread thread = new SendThread(LPacket.getPacket());
        thread.start();
        L.o("Telemetry send: wait");
        while (thread.isBusy() && LPacket.isBlocking()) {
            thread.tick();
        }
        L.o("Telemetry send: wait: done");
        setLastSend(LPacket.getClass().getName());
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
                L.o("Telemetry SaveThread: ");
                if (!isTelemetryStatusQuerying && telemetryStatus == null) {
                    queryTelemetryStatus();
                }
                if (isTelemetryStatusQuerying && telemetryStatus == null) {
                    while (Telemetry.this.isTelemetryStatusQuerying) {
                        tick();
                    }
                }
                L.o("Telemetry SaveThread: query done");
                if (telemetryStatus == null) {
                    throw new RuntimeException("WTF: don't query telemetryStatus");
                }
                if (telemetryStatus.isEnabled()) {
                    L.o("Telemetry SaveThread: enabled! client new");
                    Client client = new Client(telemetryStatus.getHost(), telemetryStatus.getPort(), new TelemetryPackets(), new PacketHandler() {
                        @Override
                        public void received(Client client, Packet packet) {
                            L.o("Telemetry", "Received: ", packet.toString());
                        }

                        @Override
                        public void setup(Client client) {
                            try {
                                client.send(new PacketSetVersion(2));
                                client.send(new PacketLogin(app.getInstanceId()));
                                client.send(new PacketHandshake(App.VERSION_CODE));
                                client.send(packet);

                                Thread.sleep(1000);
                                send = true;

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    new Thread(() -> {
                        try {
                            client.run();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();

                    L.o("Telemetry wait send or 10 ser");
                    long start = System.currentTimeMillis();
                    while (!send && System.currentTimeMillis() - start < 10 * 1000) {
                        tick();
                    }
                    L.o("Telemetry wait send or 10 ser: done");

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

    private static class LPacket {
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
            super(true, false, new PacketUIOpen());
        }
    }

    public static class UiClosedLPacket extends LPacket {
        public UiClosedLPacket() {
            super(true, false, new PacketUIClosed());
        }
    }

    public static class CrashReportLPacket extends LPacket {
        public CrashReportLPacket(CrashReport crashReport) {
            super(false, true, new PacketCrashReport(crashReport.getID(), crashReport.getThrowable().toString(), crashReport.convertToText()));
        }
    }

    public static class DataFixerLogsLPacket extends LPacket {
        public DataFixerLogsLPacket(int dataVersion, String logs) {
            super(false, false, new PacketDataFixerLogs(dataVersion, logs));
        }
    }

    private static class TelemetryStatus {
        private final boolean isEnabled;
        private final String host;
        private final int port;

        public static TelemetryStatus fromJson(JSONObject j) throws JSONException {
            if (j == null) return null;
            return new TelemetryStatus(
                    j.optBoolean("isEnabled", false),
                    j.optString("host", null),
                    j.optInt("port", 0)
            );
        }

        public TelemetryStatus(boolean isEnabled, String host, int port) {
            this.isEnabled = isEnabled;
            this.host = host;
            this.port = port;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }
}

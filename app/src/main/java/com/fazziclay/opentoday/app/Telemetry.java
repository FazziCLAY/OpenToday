package com.fazziclay.opentoday.app;

import java.util.UUID;


/**
 * Telemetry disabled because it not using encryption
 */
public class Telemetry {
    public Telemetry(App app, boolean isEnabled) {
    }

    public void setEnabled(boolean enabled) {
    }

    public boolean isEnabled() {
        return false;
    }

    public void send(LPacket lPacket) {
    }

    public static class LPacket {
    }

    public static class UiOpenLPacket extends LPacket {
    }

    public static class UiClosedLPacket extends LPacket {
    }

    public static class CrashReportLPacket extends LPacket {
        public CrashReportLPacket(CrashReport crashReport) {}

        public CrashReportLPacket(UUID id, String throwable, String crashText) {}
    }

    public static class DataFixerLogsLPacket extends LPacket {
        public DataFixerLogsLPacket(int dataVersion, String logs) {}
    }
}

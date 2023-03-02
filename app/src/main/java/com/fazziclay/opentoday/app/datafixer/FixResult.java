package com.fazziclay.opentoday.app.datafixer;

import java.io.File;

public class FixResult {
    public static FixResult NO_FIX = new FixResult();

    private final boolean fixed;
    private boolean versionFileExist = false;
    private boolean versionFileOutdated = false;
    private int dataVersion;
    private File logFile;
    private String logs;

    public FixResult(int dataVersion, File logFile, String logs) {
        this.dataVersion = dataVersion;
        this.logFile = logFile;
        this.logs = logs;
        this.fixed = true;
    }

    private FixResult() {
        this.fixed = false;
    }

    public FixResult versionFileExist(boolean b) {
        this.versionFileExist = b;
        return this;
    }

    public FixResult versionFileOutdated(boolean b) {
        this.versionFileOutdated = b;
        return this;
    }

    public boolean isFixed() {
        return fixed;
    }

    public boolean isVersionFileUpdateRequired() {
        return !versionFileExist || versionFileOutdated;
    }

    public int getDataVersion() {
        return dataVersion;
    }

    public File getLogFile() {
        return logFile;
    }

    public String getLogs() {
        return logs;
    }
}

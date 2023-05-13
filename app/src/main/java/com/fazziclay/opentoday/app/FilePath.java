package com.fazziclay.opentoday.app;

import android.content.Context;

import java.io.File;

@Deprecated
public class FilePath {
    public static final String FILES = "files";
    public static final String CACHE = "cache";

    private final String path;

    public FilePath(String path) {
        this.path = path;
    }

    public FilePath(String prefix, String path) {
        this.path = prefix + "://" + path;
    }

    public String getPath() {
        return path;
    }

    public File create(Context context) {
        String[] s = path.split("://");
        String prefix = s[0];
        String data = s[1];

        File prefixFile = null;
        if (FILES.equalsIgnoreCase(prefix)) {
            prefixFile = context.getExternalFilesDir("");
        } else if (CACHE.equalsIgnoreCase(prefix)) {
            prefixFile = context.getExternalCacheDir();
        }

        return new File(prefixFile, data);
    }
}

package com.fazziclay.opentoday.util;

import com.fazziclay.javaneoutil.FileUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class SafeFileUtil {
    public static boolean isExistOrBack(File origin, File back) {
        return FileUtil.isExist(origin) || FileUtil.isExist(back);
    }

    /**
     * Null if file not exist
     */
    public static InputStream getFileInputSteam(@NotNull File f) throws IOException {
        if (!FileUtil.isExist(f)) return null;
        return Files.newInputStream(f.toPath());
    }

    public static OutputStream getFileOutputSteam(@NotNull File f) throws IOException {
        return Files.newOutputStream(f.toPath());
    }

    /**
     * Make file backup. Nothing if origin file not exist
     */
    public static void makeBackup(@NotNull File origin, @NotNull File backup) throws IOException {
        if (!FileUtil.isExist(origin)) return;
        if (FileUtil.isExist(backup)) FileUtil.delete(backup);
        InputStream stream = getFileInputSteam(origin);
        Files.copy(stream, backup.toPath());
        stream.close();
    }

    public static boolean isExist(@NotNull File file) {
        return FileUtil.isExist(file);
    }
}

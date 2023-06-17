package com.fazziclay.opentoday.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class StreamUtil {
    public static String read(InputStream inputStream) throws IOException {
        Reader reader = new InputStreamReader(inputStream);
        final StringBuilder result = new StringBuilder();

        final char[] buff = new char[1024];
        int i;
        while ((i = reader.read(buff)) > 0) {
            result.append(new String(buff, 0, i));
        }
        reader.close();
        return result.toString();
    }
}

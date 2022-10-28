package ru.fazziclay.opentoday.app.items;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.item.ItemIEUtil;

public class ImportWrapper {
    public static final int VERSION = 1;

    private final int importVersion = VERSION;

    private final List<Item> items;

    public ImportWrapper(List<Item> items) {
        this.items = items;
    }

    public String finalExport() throws Exception {
        JSONObject jsonObject = new JSONObject()
                .put("importVersion", importVersion);


        jsonObject.put("items", ItemIEUtil.exportItemList(items));

        String data = jsonObject.toString();
        byte[] bytes = toGzip(data);
        return "--OPENTODAY-IMPORT-START--\n" + importVersion + "\n" + Base64.getEncoder().encodeToString(bytes) + "\n--OPENTODAY-IMPORT-END--";
    }

    public static ImportWrapper finalImport(String content) throws Exception {
        content = content.trim();
        if (!content.startsWith("--OPENTODAY-IMPORT-START--")) {
            throw new Exception("Error");
        }
        if (!content.endsWith("--OPENTODAY-IMPORT-END--")) {
            throw new Exception("Error");
        }

        int version = Integer.parseInt(content.split("\n")[1]);
        if (version != 0 && version != 1) throw new Exception("Version not compatible");

        byte[] bytes = Base64.getDecoder().decode(content.split("\n")[2].getBytes(StandardCharsets.UTF_8));
        JSONObject jsonObject = null;
        if (version == 0) {
            jsonObject = new JSONObject(new String(bytes, StandardCharsets.UTF_8));
        }
        if (version == 1) {
            String data = fromGzip(bytes);
            jsonObject = new JSONObject(data);
        }

        int importVersion = jsonObject.getInt("importVersion");
        if (importVersion != 0 && importVersion != 1) {
            throw new Exception("Version not compatible");
        }

        return new ImportWrapper(ItemIEUtil.importItemList(jsonObject.getJSONArray("items")));
    }


    private static byte[] toGzip(String s) throws IOException {
        ByteArrayOutputStream oo = new ByteArrayOutputStream();
        GZIPOutputStream o = new GZIPOutputStream(oo);
        Writer writer = new OutputStreamWriter(o);
        writer.write(s);
        writer.flush();
        writer.close();
        return oo.toByteArray();
    }

    private static String fromGzip(byte[] s) throws IOException {
        ByteArrayInputStream oo = new ByteArrayInputStream(s);
        GZIPInputStream o = new GZIPInputStream(oo);
        Reader reader = new InputStreamReader(o);

        final StringBuilder result = new StringBuilder();

        final char[] buff = new char[1024];
        int i;
        while ((i = reader.read(buff)) > 0) {
            result.append(new String(buff, 0, i));
        }

        reader.close();

        return result.toString();
    }

    public List<Item> getItems() {
        return items;
    }

    public static Builder createImport() {
        return new Builder();
    }

    public static class Builder {
        private final List<Item> items = new ArrayList<>();

        public Builder addItem(Item item) {
            this.items.add(item);
            return this;
        }

        public Builder addItemAll(Item... item) {
            this.items.addAll(Arrays.asList(item));
            return this;
        }

        public ImportWrapper build() {
            return new ImportWrapper(items);
        }
    }
}

package ru.fazziclay.opentoday.app.items;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.item.ItemIEUtil;

public class ImportWrapper {
    public static final int VERSION = 0;

    private final int importVersion = VERSION;

    private final List<Item> items;

    public ImportWrapper(List<Item> items) {
        this.items = items;
    }

    public String finalExport() throws Exception {
        JSONObject jsonObject = new JSONObject()
                .put("importVersion", importVersion);


        jsonObject.put("items", ItemIEUtil.exportItemList(items));

        return "--OPENTODAY-IMPORT-START--\n" + importVersion + "\n" + Base64.getEncoder().encodeToString(jsonObject.toString().getBytes(StandardCharsets.UTF_8)) + "\n--OPENTODAY-IMPORT-END--";
    }

    public static ImportWrapper finalImport(String content) throws Exception {
        if (!content.startsWith("--OPENTODAY-IMPORT-START--")) {
            throw new Exception("Error");
        }
        if (!content.endsWith("--OPENTODAY-IMPORT-END--")) {
            throw new Exception("Error");
        }

        int version = Integer.parseInt(content.split("\n")[1]);
        if (version != 0) throw new Exception("Version not compatible");

        String data = new String(Base64.getDecoder().decode(content.split("\n")[2].getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(data);

        int importVersion = jsonObject.getInt("importVersion");
        if (importVersion != 0) {
            throw new Exception("Version not compatible");
        }

        return new ImportWrapper(ItemIEUtil.importItemList(jsonObject.getJSONArray("items")));
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

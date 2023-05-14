package com.fazziclay.opentoday.app;

import com.fazziclay.opentoday.app.data.CherryOrchard;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemCodecUtil;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.app.items.tab.TabCodecUtil;

import org.json.JSONArray;
import org.json.JSONException;
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

public class ImportWrapper {
    public static final int VERSION = 2;

    private final int importVersion = VERSION;
    private final Permission[] permissions;
    private final List<Tab> tabs;
    private final List<Item> items;
    private final String dialogMessage;
    private final JSONObject settings;
    private final JSONObject colorHistory;

    private ImportWrapper(Permission[] permissions, List<Tab> tabs, List<Item> items, String dialogMessage, JSONObject settings, JSONObject colorHistory) {
        this.permissions = permissions;

        if (tabs != null) checkPerm(Permission.ADD_TABS, "Tabs not allowed by permissions");
        this.tabs = tabs;

        if (items != null) checkPerm(Permission.ADD_ITEMS_TO_CURRENT, "Items not allowed by permissions");
        this.items = items;

        if (dialogMessage != null) checkPerm(Permission.PRE_IMPORT_SHOW_DIALOG, "Not allowed dialog message");
        this.dialogMessage = dialogMessage;

        if (settings != null) checkPerm(Permission.OVERWRITE_SETTINGS, "Settings changes not allowed");
        this.settings = settings;

        if (colorHistory != null) checkPerm(Permission.OVERWRITE_COLOR_HISTORY, "Color history overwrite not allowed");
        this.colorHistory = colorHistory;
    }

    public String finalExport() throws Exception {
        byte[] bytes = finalExportBytes();
        return "--OPENTODAY-IMPORT-START--\n" +
                importVersion + "\n" +
                Base64.getEncoder().encodeToString(bytes) + "\n" +
                "--OPENTODAY-IMPORT-END--";
    }

    private byte[] finalExportBytes() throws Exception {
        JSONObject jsonObject = new JSONObject()
                .put("importVersion", importVersion)
                .put("permissions", exportPermissions());

        if (isPerm(Permission.ADD_ITEMS_TO_CURRENT)) {
            jsonObject.put("items", ItemCodecUtil.exportItemList(items));
        }

        if (isPerm(Permission.ADD_TABS)) {
            jsonObject.put("tabs", TabCodecUtil.exportTabList(tabs));
        }

        if (isPerm(Permission.PRE_IMPORT_SHOW_DIALOG)) {
            jsonObject.put("dialogMessage", dialogMessage);
        }

        if (isPerm(Permission.OVERWRITE_SETTINGS)) {
            jsonObject.put("settings", settings);
        }

        if (isPerm(Permission.OVERWRITE_COLOR_HISTORY)) {
            jsonObject.put("colorHistory", colorHistory);
        }

        String data = jsonObject.toString();
        return toGzip(data);
    }

    private JSONArray exportPermissions() {
        JSONArray ja = new JSONArray();
        for (Permission permission : permissions) {
            ja.put(permission.name());
        }
        return ja;
    }

    private static Permission[] importPermissions(JSONArray ja) throws JSONException {
        int i = 0;
        Permission[] result = new Permission[ja.length()];
        while (i < ja.length()) {
            result[i] = Permission.valueOf(ja.getString(i));
            i++;
        }
        return result;
    }

    public static boolean isImportText(String content) {
        content = content.trim();
        return content.startsWith("--OPENTODAY-IMPORT-START--") && content.endsWith("--OPENTODAY-IMPORT-END--");
    }

    public static ImportWrapper finalImport(String content) throws Exception {
        content = content.trim();
        if (!isImportText(content)) {
            throw new Exception("Not import text");
        }

        int version = Integer.parseInt(content.split("\n")[1]);
        if (!isVersionSupport(version)) throw new Exception("Version not compatible");
        byte[] bytes = Base64.getDecoder().decode(content.split("\n")[2].getBytes(StandardCharsets.UTF_8));
        if (version == 0) {
            return importV0(bytes);
        } else if (version == 1) {
            return importV1(bytes);
        } else if (version == 2) {
            return importV2(bytes);
        } else {
            throw new RuntimeException("Version not compatible");
        }
    }

    private static ImportWrapper importV0(byte[] bytes) throws Exception {
        JSONObject jsonObject = new JSONObject(new String(bytes, StandardCharsets.UTF_8));

        int importVersion = jsonObject.getInt("importVersion");
        if (importVersion != 0) {
            throw new Exception("Version not compatible");
        }

        List<Item> items = ItemCodecUtil.importItemList(CherryOrchard.of(jsonObject.getJSONArray("items")));
        return new ImportWrapper(new Permission[]{Permission.ADD_ITEMS_TO_CURRENT}, null, items, null, null, null);
    }

    private static ImportWrapper importV1(byte[] bytes) throws Exception {
        String data = fromGzip(bytes);
        JSONObject jsonObject = new JSONObject(data);

        int importVersion = jsonObject.getInt("importVersion");
        if (importVersion != 1) {
            throw new Exception("Version not compatible");
        }

        List<Item> items = ItemCodecUtil.importItemList(CherryOrchard.of(jsonObject.getJSONArray("items")));
        return new ImportWrapper(new Permission[]{Permission.ADD_ITEMS_TO_CURRENT}, null, items, null, null, null);
    }

    private static ImportWrapper importV2(byte[] bytes) throws Exception {
        String data = fromGzip(bytes);
        JSONObject jsonObject = new JSONObject(data);

        int importVersion = jsonObject.getInt("importVersion");
        if (importVersion != 2) {
            throw new Exception("Version not compatible");
        }

        Permission[] perms = importPermissions(jsonObject.getJSONArray("permissions"));
        List<Tab> tabs = null;
        List<Item> items = null;
        String dialogMessage = null;
        JSONObject settings = null;
        JSONObject colorHistory = null;

        if (isPerm(perms, Permission.ADD_TABS)) {
            tabs = new ArrayList<>(TabCodecUtil.importTabList(CherryOrchard.of(jsonObject.getJSONArray("tabs"))));
        }

        if (isPerm(perms, Permission.ADD_ITEMS_TO_CURRENT)) {
            items = new ArrayList<>(ItemCodecUtil.importItemList(CherryOrchard.of(jsonObject.getJSONArray("items"))));
        }

        if (isPerm(perms, Permission.PRE_IMPORT_SHOW_DIALOG)) {
            dialogMessage = jsonObject.getString("dialogMessage");
        }

        if (isPerm(perms, Permission.OVERWRITE_SETTINGS)) {
            settings = jsonObject.getJSONObject("settings");
        }

        if (isPerm(perms, Permission.OVERWRITE_COLOR_HISTORY)) {
            colorHistory = jsonObject.getJSONObject("colorHistory");
        }

        return new ImportWrapper(perms, tabs, items, dialogMessage, settings, colorHistory);
    }

    private static boolean isVersionSupport(int v) {
        return (v == 0 || v == 1 || v == 2);
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

    public Permission[] getPermissions() {
        return permissions;
    }

    public String getDialogMessage() {
        return dialogMessage;
    }

    public JSONObject getSettings() {
        return settings;
    }

    public JSONObject getColorHistory() {
        return colorHistory;
    }

    public int getImportVersion() {
        return importVersion;
    }

    public List<Tab> getTabs() {
        return tabs;
    }

    public boolean isPerm(Permission p) {
        return isPerm(permissions, p);
    }

    public static boolean isPerm(Permission[] permissions, Permission p) {
        for (Permission p1 : permissions) {
            if (p1 == p) return true;
        }
        return false;
    }

    private void checkPerm(Permission p, String m) {
        if (!isPerm(p)) throw new SecurityException("Permission " + p + " not found: " + m);
    }

    public static Builder createImport(Permission... permissions) {
        return new Builder(permissions);
    }

    public static class Builder {
        private final Permission[] permissions;
        private final List<Item> items = new ArrayList<>();
        private final List<Tab> tabs = new ArrayList<>();
        private String dialogMessage = null;
        private JSONObject settings = null;
        private JSONObject colorHistory = null;

        private boolean isPerm(Permission p) {
            for (Permission p1 : permissions) {
                if (p1 == p) return true;
            }
            return false;
        }

        private void checkPerm(Permission p, String m) {
            if (!isPerm(p)) throw new SecurityException("Permission " + p + " not found: " + m);
        }

        private Builder(Permission... permissions) {
            this.permissions = permissions;
        }

        public Builder addItem(Item item) {
            checkPerm(Permission.ADD_ITEMS_TO_CURRENT, "Not allowed add item without permission");
            this.items.add(item);
            return this;
        }

        public Builder addItemAll(Item... item) {
            checkPerm(Permission.ADD_ITEMS_TO_CURRENT, "Not allowed add items without permission");
            this.items.addAll(Arrays.asList(item));
            return this;
        }

        public Builder addTab(Tab tab) {
            checkPerm(Permission.ADD_TABS, "Not allowed add tab without permission");
            this.tabs.add(tab);
            return this;
        }

        public Builder addTabAll(Tab... tab) {
            checkPerm(Permission.ADD_TABS, "Not allowed add tabs without permission");
            this.tabs.addAll(Arrays.asList(tab));
            return this;
        }

        public Builder setDialogMessage(String dialog) {
            checkPerm(Permission.PRE_IMPORT_SHOW_DIALOG, "Now allowed by permission.");
            this.dialogMessage = dialog;
            return this;
        }

        public Builder setSettings(JSONObject settings) {
            checkPerm(Permission.OVERWRITE_SETTINGS, "Now allowed by permission.");
            this.settings = settings;
            return this;
        }

        public Builder setColorHistory(JSONObject colorHistory) {
            checkPerm(Permission.OVERWRITE_COLOR_HISTORY, "Now allowed by permission.");
            this.colorHistory = colorHistory;
            return this;
        }

        public ImportWrapper build() {
            return new ImportWrapper(permissions, tabs.isEmpty() ? null : tabs, items.isEmpty() ? null : items, dialogMessage, settings, colorHistory);
        }
    }

    public enum Permission {
        ADD_ITEMS_TO_CURRENT,
        ADD_TABS,
        OVERWRITE_SETTINGS,
        OVERWRITE_COLOR_HISTORY,
        PRE_IMPORT_SHOW_DIALOG
    }
}

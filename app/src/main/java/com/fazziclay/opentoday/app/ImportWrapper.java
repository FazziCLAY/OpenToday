package com.fazziclay.opentoday.app;

import com.fazziclay.opentoday.app.data.CherryOrchard;
import com.fazziclay.opentoday.app.datafixer.DataFixer;
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

/*
Version 3 specifications
Header and footer as in all previous versions
--OPENTODAY-IMPORT-START--
<version>
<data>
--OPENTODAY-IMPORT-END--

<version> is a 3
<data> as in 1 & 2 version. base64(gzip(  <StringJsonObject>  ))
<StringJsonObject> is a JSONObject without indentSpaces with this scheme:
  {
    "importVersion": 3, // like a 2... version? but 3 value
    "permissions": [""], // like a 2 version. Contains ENUM.name() of Permission
    "dataVersion": <App.APPLICATION_DATA_VERSION>, // Added. Contains version of application data. This is a APPLICATION_DATA_VERSION constant in App.class
    "applicationVersionData": { // Added. HERE CONTAINS A App.getVersionData() JSONObject
        "data_version": -1, // App.APPLICATION_DATA_VERSION
        "product": "OpenToday",
        "developer": "FazziCLAY ( https://fazziclay.github.io )"
        ......
    }
  }

 */
public class ImportWrapper {
    public static final int VERSION = 3;

    private final int importVersion = VERSION;
    private final boolean isError;
    private final ErrorCode errorCode;
    private final Permission[] permissions;
    private final List<Tab> tabs;
    private final List<Item> items;
    private final String dialogMessage;
    private final JSONObject settings;
    private final JSONObject colorHistory;
    private boolean newestVersion = false;

    private static ImportWrapper error(ErrorCode code) {
        return new ImportWrapper(null, null, null, null, null, null, true, code);
    }

    private ImportWrapper(Permission[] permissions, List<Tab> tabs, List<Item> items, String dialogMessage, JSONObject settings, JSONObject colorHistory, boolean isError, ErrorCode errorCode) {
        this.permissions = permissions;
        this.isError = isError;
        this.errorCode = errorCode;

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

    private ImportWrapper setFromNewestVersion(boolean b) {
        this.newestVersion = b;
        return this;
    }

    public boolean isNewestVersion() {
        return newestVersion;
    }

    public boolean isError() {
        return isError;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
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
                .put("dataVersion", App.APPLICATION_DATA_VERSION)
                .put("applicationVersionData", App.get().getVersionData())
                .put("importVersion", importVersion)
                .put("applicationVersion", App.VERSION_CODE)
                .put("permissions", exportPermissions());

        if (isPerm(Permission.ADD_ITEMS_TO_CURRENT)) {
            jsonObject.put("items", ItemCodecUtil.exportItemList(items).toJSONArray());
        }

        if (isPerm(Permission.ADD_TABS)) {
            jsonObject.put("tabs", TabCodecUtil.exportTabList(tabs).toJSONArray());
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
        if (content == null) return false;
        content = content.trim();
        return content.startsWith("--OPENTODAY-IMPORT-START--") && content.endsWith("--OPENTODAY-IMPORT-END--");
    }

    public static ImportWrapper finalImport(String content) throws Exception {
        content = content.trim();
        if (!isImportText(content)) {
            return error(ErrorCode.NOT_IMPORT_TEXT);
        }

        int version = Integer.parseInt(content.split("\n")[1]);
        if (!isVersionSupport(version)) return error(ErrorCode.VERSION_NOT_COMPATIBLE);
        byte[] bytes = Base64.getDecoder().decode(content.split("\n")[2].getBytes(StandardCharsets.UTF_8));
        if (version == 0) {
            return importV0(bytes);
        } else if (version == 1) {
            return importV1(bytes);
        } else if (version == 2) {
            return importV2(bytes);
        } else if (version == 3) {
            return importV3(bytes);
        } else {
            return error(ErrorCode.VERSION_NOT_COMPATIBLE);
        }
    }

    private static ImportWrapper importV0(byte[] bytes) throws Exception {
        JSONObject jsonObject = new JSONObject(new String(bytes, StandardCharsets.UTF_8));

        int importVersion = jsonObject.getInt("importVersion");
        if (importVersion != 0) {
            return error(ErrorCode.VERSION_NOT_COMPATIBLE);
        }

        List<Item> items = ItemCodecUtil.importItemList(CherryOrchard.of(fixItems(8, jsonObject.getJSONArray("items"))));
        return new ImportWrapper(new Permission[]{Permission.ADD_ITEMS_TO_CURRENT}, null, items, null, null, null, false, null);
    }

    // uses GZip
    private static ImportWrapper importV1(byte[] bytes) throws Exception {
        String data = fromGzip(bytes);
        JSONObject jsonObject = new JSONObject(data);

        int importVersion = jsonObject.getInt("importVersion");
        if (importVersion != 1) {
            return error(ErrorCode.VERSION_NOT_COMPATIBLE);
        }

        List<Item> items = ItemCodecUtil.importItemList(CherryOrchard.of(fixItems(8, jsonObject.getJSONArray("items"))));
        return new ImportWrapper(new Permission[]{Permission.ADD_ITEMS_TO_CURRENT}, null, items, null, null, null, false, null);
    }

    // Add tabs, settings, colorHistory, permissions, dialogMessage
    private static ImportWrapper importV2(byte[] bytes) throws Exception {
        String data = fromGzip(bytes);
        JSONObject jsonObject = new JSONObject(data);

        int importVersion = jsonObject.getInt("importVersion");
        if (importVersion != 2) {
            return error(ErrorCode.VERSION_NOT_COMPATIBLE);
        }

        Permission[] perms = importPermissions(jsonObject.getJSONArray("permissions"));
        List<Tab> tabs = null;
        List<Item> items = null;
        String dialogMessage = null;
        JSONObject settings = null;
        JSONObject colorHistory = null;


        if (isPerm(perms, Permission.ADD_TABS)) {
            // BEGIN INSERTED WHILE DEVELOPING VER 3
            JSONArray _tabs = jsonObject.getJSONArray("tabs");
            _tabs = fixTabs(8, _tabs);
            // END INSERTED WHILE DEVELOPING VER 3

            tabs = new ArrayList<>(TabCodecUtil.importTabList(CherryOrchard.of(/*PART OF DATAFIXING*/_tabs/*END*/)));
        }

        if (isPerm(perms, Permission.ADD_ITEMS_TO_CURRENT)) {
            // BEGIN INSERTED WHILE DEVELOPING VER 3
            JSONArray _items = jsonObject.getJSONArray("items");
            _items = fixItems(8, _items);
            // END INSERTED WHILE DEVELOPING VER 3

            items = new ArrayList<>(ItemCodecUtil.importItemList(CherryOrchard.of(/*PART OF DATAFIXING*/_items/*END*/)));
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

        return new ImportWrapper(perms, tabs, items, dialogMessage, settings, colorHistory, false, null);
    }

    // Add datafixer & more info while creating (e.g. dataVersion, appVersion and etc...)
    private static ImportWrapper importV3(byte[] bytes) throws Exception {
        String data = fromGzip(bytes);
        JSONObject jsonObject = new JSONObject(data);

        int importVersion = jsonObject.getInt("importVersion");
        if (importVersion != 3) {
            return error(ErrorCode.VERSION_NOT_COMPATIBLE);
        }

        Permission[] perms = importPermissions(jsonObject.getJSONArray("permissions"));
        int dataVersion = jsonObject.getInt("dataVersion");
        List<Tab> tabs = null;
        List<Item> items = null;
        String dialogMessage = null;
        JSONObject settings = null;
        JSONObject colorHistory = null;

        if (isPerm(perms, Permission.ADD_TABS)) {
            JSONArray _tabs = jsonObject.getJSONArray("tabs");
            _tabs = fixTabs(dataVersion, _tabs);

            tabs = new ArrayList<>(TabCodecUtil.importTabList(CherryOrchard.of(_tabs)));
        }

        if (isPerm(perms, Permission.ADD_ITEMS_TO_CURRENT)) {
            JSONArray _items = jsonObject.getJSONArray("items");
            _items = fixItems(dataVersion, _items);

            items = new ArrayList<>(ItemCodecUtil.importItemList(CherryOrchard.of(_items)));
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

        return new ImportWrapper(perms, tabs, items, dialogMessage, settings, colorHistory, false, null)
                .setFromNewestVersion(dataVersion > App.APPLICATION_DATA_VERSION);
    }

    private static boolean isVersionSupport(int v) {
        return (v == 0 || v == 1 || v == 2 || v == 3);
    }


    private static JSONArray fixTabs(int from, JSONArray tabs) throws Exception {
        if (from == App.APPLICATION_DATA_VERSION) return tabs;

        final DataFixer dataFixer = App.get().getDataFixer();

        tabs = dataFixer.fixTabs(from, tabs);

        return tabs;
    }

    private static JSONArray fixItems(int from, JSONArray items) throws Exception {
        if (from == App.APPLICATION_DATA_VERSION) return items;

        final DataFixer dataFixer = App.get().getDataFixer();

        items = dataFixer.fixItems(from, items);

        return items;
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
            return new ImportWrapper(permissions, tabs.isEmpty() ? null : tabs, items.isEmpty() ? null : items, dialogMessage, settings, colorHistory, false, null);
        }
    }

    public enum Permission {
        ADD_ITEMS_TO_CURRENT,
        ADD_TABS,
        OVERWRITE_SETTINGS,
        OVERWRITE_COLOR_HISTORY,
        PRE_IMPORT_SHOW_DIALOG
    }

    public enum ErrorCode {
        VERSION_NOT_COMPATIBLE,
        NOT_IMPORT_TEXT
    }
}

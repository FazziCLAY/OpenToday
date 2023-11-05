package com.fazziclay.opentoday.app.settings;

import static com.fazziclay.opentoday.util.InlineUtil.IPROF;

import android.graphics.Color;
import android.util.ArraySet;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.items.item.ItemType;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.settings.enums.DateAndTimePreset;
import com.fazziclay.opentoday.app.settings.enums.FirstDayOfWeek;
import com.fazziclay.opentoday.app.settings.enums.FirstTab;
import com.fazziclay.opentoday.app.settings.enums.ItemAction;
import com.fazziclay.opentoday.app.settings.enums.ItemAddPosition;
import com.fazziclay.opentoday.app.settings.enums.ThemeEnum;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.Setter;
import com.fazziclay.opentoday.util.callback.Callback;
import com.fazziclay.opentoday.util.callback.CallbackStorage;
import com.fazziclay.opentoday.util.callback.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class SettingsManager {
    // static
    private static final String TAG = "SettingsManager";
    private static final Set<Option> REGISTERED_OPTIONS = new ArraySet<>();

    public static final EnumOption<ThemeEnum>       THEME                            = registerOption(new EnumOption<>("theme",                             false,  ThemeEnum.AUTO));
    public static final EnumOption<FirstDayOfWeek>  FIRST_DAY_OF_WEEK                = registerOption(new EnumOption<>("first_day_of_week",                 false,  FirstDayOfWeek.SATURDAY));
    public static final BooleanOption               ANALOG_CLOCK_ENABLE              = registerOption(new BooleanOption("analog_clock.enable",              false,  false));
    public static final IntegerOption               ANALOG_CLOCK_SIZE                = registerOption(new IntegerOption("analog_clock.size",                true,  90));
    public static final IntegerOption               ANALOG_CLOCK_TRANSPARENCY        = registerOption(new IntegerOption("analog_clock.transparency",        true,  65));
    public static final ColorOption                 ANALOG_CLOCK_COLOR_SECONDS       = registerOption(new ColorOption("analog_clock.color.second",        true, Color.BLACK));
    public static final ColorOption                 ANALOG_CLOCK_COLOR_MINUTE        = registerOption(new ColorOption("analog_clock.color.minute",        true,  Color.BLACK));
    public static final ColorOption                 ANALOG_CLOCK_COLOR_HOUR          = registerOption(new ColorOption("analog_clock.color.hour",          true,  Color.BLACK));
    public static final BooleanOption               QUICK_NOTE_NOTIFICATION_ENABLE   = registerOption(new BooleanOption("quick_note.notification.enable",   false,  true));
    public static final BooleanOption               QUICK_NOTE_PARSE_TIME_FROM_ITEM  = registerOption(new BooleanOption("quick_note.parse_time_from_item",  false,  true));
    public static final EnumOption<ItemType>        QUICK_NOTE_ITEM_TYPE             = registerOption(new EnumOption<>("quick_note.item_type",              false,  ItemType.CHECKBOX));
    public static final BooleanOption               ITEM_MINIMIZE_GRAY_COLOR         = registerOption(new BooleanOption("item.minimize.gray_color",         false,  true));
    public static final BooleanOption               ITEM_TRIM_NAMES_IN_EDITOR        = registerOption(new BooleanOption("item.editor.trim_names",           false,  true));
    public static final BooleanOption               ITEM_RANDOM_BACKGROUND           = registerOption(new BooleanOption("item.random_background_on_create", false,  true));
    public static final BooleanOption               ITEM_EDITOR_BACKGROUND_AS_ITEM   = registerOption(new BooleanOption("item.use_container_editor_background_from_item", false,  false));
    public static final BooleanOption               ITEM_IS_SCROLL_TO_ADDED          = registerOption(new BooleanOption("item.is_scroll_to_added",          false,  true));
    public static final EnumOption<ItemAction>      ITEM_ACTION_CLICK                = registerOption(new EnumOption<>("item.action.click",                 false,  ItemAction.OPEN_EDITOR));
    public static final EnumOption<ItemAction>      ITEM_ACTION_LEFT_SWIPE           = registerOption(new EnumOption<>("item.action.left_swipe",            false,  ItemAction.MINIMIZE_REVERT));
    public static final EnumOption<ItemAddPosition>      ITEM_ADD_POSITION           = registerOption(new EnumOption<>("item.add_position",                 false,  ItemAddPosition.BOTTOM));
    public static final BooleanOption               ITEM_PATH_VISIBLE                = registerOption(new BooleanOption("item.path_visible",                false,  true));
    public static final BooleanOption               IS_TELEMETRY                     = registerOption(new BooleanOption("telemetry.enable",                 false,  true));
    public static final EnumOption<FirstTab>        DEFAULT_FIRST_TAB                = registerOption(new EnumOption<>("first_tab",                         false,  FirstTab.TAB_ON_CLOSING));
    public static final EnumOption<DateAndTimePreset>   DATE_TIME_FORMAT             = registerOption(new EnumOption<>("date_time_format",                  false,  DateAndTimePreset.DEFAULT));
    public static final BooleanOption                FAST_CHANGES_CONFIRM            = registerOption(new BooleanOption("fast_changes.confirm",             false,  true));
    public static final BooleanOption                IS_FIRST_LAUNCH                 = registerOption(new BooleanOption("is_first_launch",                  false,  true));
    public static final StringOption                 PLUGINS                         = registerOption(new StringOption("plugins",                           true,   "")); // empty string is default
    public static final BooleanOption                TOOLBAR_AUTOMATICALLY_CLOSE     = registerOption(new BooleanOption("toolbar.auto_close",               false,  true));
    public static final EnumOption<ActionBarPosition>      ACTIONBAR_POSITION        = registerOption(new EnumOption<>("actionbar.position",                false,  ActionBarPosition.TOP));
    public static final BooleanOption                COLOR_HISTORY_ENABLED           = registerOption(new BooleanOption("color_history.enabled",            false,  true));


    // object
    private final HashMap<Option, Object> optionsValue = new HashMap<>();
    private final File saveFile;
    private final SaveThread saveThread;
    public final CallbackStorage<OptionChangedCallback> callbacks = new CallbackStorage<>();

    protected void setOption(Option option, Object value) {
        optionsValue.put(option, value);
        callbacks.run((callbackStorage, callback) -> callback.run(option, value));
    }

    protected Object getOption(Option option) {
        return optionsValue.get(option);
    }

    protected void clearOption(Option option) {
        optionsValue.remove(option);
        callbacks.run((callbackStorage, callback) -> callback.run(option, option.defVal));
    }

    private static <T extends Option> T registerOption(T option) {
        REGISTERED_OPTIONS.add(option);
        return option;
    }


    public SettingsManager(File saveFile) {
        this.saveFile = saveFile;
        this.saveThread = new SaveThread();
        load();
        saveThread.start();
    }

    private void load() {
        IPROF.push("SettingsManager:load");
        try {
            String text = FileUtil.getText(saveFile, "{}");
            JSONObject json = new JSONObject(text);

            for (Option option : REGISTERED_OPTIONS) {
                String key = option.saveKey;
                if (json.has(key)) {
                    setOption(option, option.parseValue(json.get(key)));
                } else if (!option.maybeUndefined) {
                    setOption(option, option.defVal);
                }
            }

        } catch (Exception e) {
            Logger.e(TAG, "load", e);
            App.exception(null, e);
        }
        IPROF.pop();
    }

    private class SaveThread extends Thread {
        boolean requested = false;

        public SaveThread() {
            setName("SettingsManager.SaveThread");
        }

        @Override
        public void run() {
            while (true) {
                if (requested) {
                    requested = false;
                    directSave();
                }
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void request() {
            this.requested = true;
        }
    }

    public void directSave() {
        try {
            JSONObject j = exportJSONSettings();
            FileUtil.setText(saveFile, j.toString());
        } catch (Exception e) {
            Logger.e(TAG, "save", e);
            App.exception(null, e);
        }
    }

    public void save() {
        IPROF.push("SettingsManager:save");
        saveThread.request();
        IPROF.pop();
    }

    public JSONObject exportJSONSettings() throws JSONException {
        JSONObject j = new JSONObject();

        for (Option option : optionsValue.keySet()) {
            Object value = optionsValue.get(option);
            j.put(option.saveKey, option.writeValue(value));
        }

        return j;
    }

    public void importData(JSONObject settings) {
        FileUtil.setText(saveFile, settings.toString());
        load();
    }

    public boolean isChangeDefaultQuickNoteInLongSendClick() {
        return false; // TODO: 22.10.2023 make editable
    }






    @Getter
    public int getFirstDayOfWeek() {
        return FIRST_DAY_OF_WEEK.get(this).id();
    }

    @Setter
    public void setFirstDayOfWeek(int firstDayOfWeek) {
        FIRST_DAY_OF_WEEK.set(this, FirstDayOfWeek.of(firstDayOfWeek));
    }




    public int getTheme() {
        return THEME.get(this).id();
    }

    @Setter
    public void setTheme(int theme) {
        THEME.set(this, ThemeEnum.ofId(theme));
    }





    @Getter
    public boolean isQuickNoteNotification() {
        return QUICK_NOTE_NOTIFICATION_ENABLE.get(this);
    }

    @Setter
    public void setQuickNoteNotification(boolean quickNoteNotification) {
        QUICK_NOTE_NOTIFICATION_ENABLE.set(this, quickNoteNotification);
    }

    @Getter
    public boolean isMinimizeGrayColor() {
        return ITEM_MINIMIZE_GRAY_COLOR.get(this);
    }

    @Setter
    public void setMinimizeGrayColor(boolean ff) {
        ITEM_MINIMIZE_GRAY_COLOR.set(this, ff);
    }

    @Getter
    public boolean isParseTimeFromQuickNote() {
        return QUICK_NOTE_PARSE_TIME_FROM_ITEM.get(this);
    }

    @Setter
    public void setParseTimeFromQuickNote(boolean parseTimeFromQuickNote) {
        QUICK_NOTE_PARSE_TIME_FROM_ITEM.set(this, parseTimeFromQuickNote);
    }

    @Getter
    public boolean isTrimItemNamesOnEdit() {
        return ITEM_TRIM_NAMES_IN_EDITOR.get(this);
    }

    @Setter
    public void setTrimItemNamesOnEdit(boolean trimItemNamesOnEdit) {
        ITEM_TRIM_NAMES_IN_EDITOR.set(this, isTrimItemNamesOnEdit());
    }

    @Getter
    public ItemAction getItemOnClickAction() {
        return ITEM_ACTION_CLICK.get(this);
    }

    @Setter
    public void setItemOnClickAction(ItemAction itemOnClickAction) {
        ITEM_ACTION_CLICK.set(this, itemOnClickAction);
    }

    @Getter
    public ItemAction getItemOnLeftAction() {
        return ITEM_ACTION_LEFT_SWIPE.get(this);
    }

    @Setter
    public void setItemOnLeftAction(ItemAction itemOnLeftAction) {
        ITEM_ACTION_LEFT_SWIPE.set(this, itemOnLeftAction);
    }

    @Getter
    public UUID getQuickNoteNotificationItemsStorageId() {
        return null; // TODO: 30.10.2023 uuid not supported currently
    }

    @Setter
    public void setQuickNoteNotificationItemsStorageId(UUID quickNoteNotificationItemsStorageId) {
        // TODO: 30.10.2023 uuid not supported currently
    }


    @Getter
    public ItemsRegistry.ItemInfo getDefaultQuickNoteType() {
        return ItemsRegistry.REGISTRY.get(QUICK_NOTE_ITEM_TYPE.get(this));
    }

    @Setter
    public void setDefaultQuickNoteType(ItemsRegistry.ItemInfo defaultQuickNoteType) {
        QUICK_NOTE_ITEM_TYPE.set(this, defaultQuickNoteType.getItemType());
    }

    @Getter
    public FirstTab getFirstTab() {
        return DEFAULT_FIRST_TAB.get(this);
    }

    @Setter
    public void setFirstTab(FirstTab firstTab) {
        DEFAULT_FIRST_TAB.set(this, firstTab);
    }

    @Getter
    public String getDatePattern() {
        return DATE_TIME_FORMAT.get(this).getDate();
    }

    @Getter
    public String getTimePattern() {
        return DATE_TIME_FORMAT.get(this).getTime();
    }


    public void applyDateAndTimePreset(DateAndTimePreset p) {
        DATE_TIME_FORMAT.set(this, p);
    }

    @Setter
    public void setItemAddPosition(ItemAddPosition e) {
        ITEM_ADD_POSITION.set(this, e);
    }

    @Getter
    public ItemAddPosition getItemAddPosition() {
        return ITEM_ADD_POSITION.get(this);
    }

    @Getter
    public boolean isConfirmFastChanges() {
        return FAST_CHANGES_CONFIRM.get(this);
    }

    @Setter
    public void setConfirmFastChanges(boolean b) {
        FAST_CHANGES_CONFIRM.set(this, b);
    }

    @Getter
    public boolean isItemEditorBackgroundFromItem() {
        return ITEM_EDITOR_BACKGROUND_AS_ITEM.get(this);
    }

    @Setter
    public void setItemEditorBackgroundFromItem(boolean b) {
        ITEM_EDITOR_BACKGROUND_AS_ITEM.set(this, b);
    }

    @Getter
    public boolean isScrollToAddedItem() {
        return ITEM_IS_SCROLL_TO_ADDED.get(this);
    }

    @Setter
    public void setScrollToAddedItem(boolean b) {
        ITEM_IS_SCROLL_TO_ADDED.set(this, b);
    }

    @Getter
    public boolean isAutoCloseToolbar() {
        return TOOLBAR_AUTOMATICALLY_CLOSE.get(this);
    }

    @Setter
    public void setAutoCloseToolbar(boolean b) {
        TOOLBAR_AUTOMATICALLY_CLOSE.set(this, b);
    }

    @Getter
    public boolean isRandomItemBackground() {
        return ITEM_RANDOM_BACKGROUND.get(this);
    }

    @Setter
    public void setRandomItemBackground(boolean b) {
        ITEM_RANDOM_BACKGROUND.set(this, b);
    }

    @Getter
    public String getPlugins() {
        return PLUGINS.get(this);
    }

    @Setter
    public void setPlugins(String plugins) {
        PLUGINS.set(this, plugins);
    }


    public boolean isSetupDone() {
        return IS_FIRST_LAUNCH.get(this);
    }

    public void setIsFirstLaunch(boolean b) {
        IS_FIRST_LAUNCH.set(this, b);
    }


    public interface OptionChangedCallback extends Callback {
        Status run(Option option, Object value);
    }
}

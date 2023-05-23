package com.fazziclay.opentoday.gui

import android.content.Context
import androidx.annotation.StringRes
import com.fazziclay.opentoday.R
import com.fazziclay.opentoday.app.ImportWrapper
import com.fazziclay.opentoday.app.SettingsManager
import com.fazziclay.opentoday.app.items.item.ItemType
import com.fazziclay.opentoday.app.items.item.filter.FiltersRegistry
import com.fazziclay.opentoday.app.items.item.filter.LogicContainerItemFilter

// GUI-only
object EnumsRegistry {
    private val INFOS = arrayOf(
        EnumInfo(LogicContainerItemFilter.LogicMode.AND,            R.string.logic_container_logicMode_AND),
        EnumInfo(LogicContainerItemFilter.LogicMode.OR,             R.string.logic_container_logicMode_OR),
        EnumInfo(SettingsManager.FirstTab.FIRST,                    R.string.settings_firstTab_first),
        EnumInfo(SettingsManager.FirstTab.TAB_ON_CLOSING,           R.string.settings_firstTab_onClosed),
        EnumInfo(SettingsManager.ItemAction.OPEN_EDITOR,            R.string.itemAction_OPEN_EDITOR),
        EnumInfo(SettingsManager.ItemAction.SELECT_REVERT,          R.string.itemAction_SELECT_REVERT),
        EnumInfo(SettingsManager.ItemAction.SELECT_ON,              R.string.itemAction_SELECT_ON),
        EnumInfo(SettingsManager.ItemAction.SELECT_OFF,             R.string.itemAction_SELECT_OFF),
        EnumInfo(SettingsManager.ItemAction.DELETE_REQUEST,         R.string.itemAction_DELETE_REQUEST),
        EnumInfo(SettingsManager.ItemAction.MINIMIZE_REVERT,        R.string.itemAction_MINIMIZE_REVERT),
        EnumInfo(SettingsManager.ItemAction.MINIMIZE_ON,            R.string.itemAction_MINIMIZE_ON),
        EnumInfo(SettingsManager.ItemAction.MINIMIZE_OFF,           R.string.itemAction_MINIMIZE_OFF),
        EnumInfo(ImportWrapper.ErrorCode.NOT_IMPORT_TEXT,           R.string.importWrapper_errorCode_NOT_IMPORT_TEXT),
        EnumInfo(ImportWrapper.ErrorCode.VERSION_NOT_COMPATIBLE,    R.string.importWrapper_errorCode_VERSION_NOT_COMPATIBLE),
        EnumInfo(FiltersRegistry.FilterType.DATE,                   R.string.filterRegistry_filterType_DATE),
        EnumInfo(FiltersRegistry.FilterType.LOGIC_CONTAINER,        R.string.filterRegistry_filterType_LOGIC_CONTAINER),

        EnumInfo(ItemType.DEBUG_TICK_COUNTER,            R.string.item_debugTickCounter),
        EnumInfo(ItemType.TEXT,                          R.string.item_text),
        EnumInfo(ItemType.LONG_TEXT,                     R.string.item_longTextItem),
        EnumInfo(ItemType.CHECKBOX,                      R.string.item_checkbox),
        EnumInfo(ItemType.CHECKBOX_DAY_REPEATABLE,       R.string.item_checkboxDayRepeatable),
        EnumInfo(ItemType.COUNTER,                       R.string.item_counter),
        EnumInfo(ItemType.CYCLE_LIST,                    R.string.item_cycleList),
        EnumInfo(ItemType.GROUP,                         R.string.item_group),
        EnumInfo(ItemType.FILTER_GROUP,                  R.string.item_filterGroup),
    )

    fun missingChecks() {
        missingCheck(LogicContainerItemFilter.LogicMode.values().toList())
        missingCheck(SettingsManager.FirstTab.values().toList())
        missingCheck(SettingsManager.ItemAction.values().toList())
        missingCheck(ImportWrapper.ErrorCode.values().toList())
        missingCheck(FiltersRegistry.FilterType.values().toList())
        missingCheck(ItemType.values().toList())
    }

    private fun missingCheck(values: List<Enum<*>>) {
        for (value in values) {
            getInfo(value)
        }
    }

    private fun getInfo(e: Enum<*>): EnumInfo {
        for (info in INFOS) {
            if (info.e === e) {
                return info
            }
        }
        throw RuntimeException("EnumsRegistry: enum $e not found!")
    }

    @StringRes
    fun nameResId(e: Enum<*>): Int {
        return getInfo(e).nameResId
    }

    fun name(e: Enum<*>, context: Context): String = context.getString(nameResId(e))

    class EnumInfo(var e: Enum<*>, var nameResId: Int)
}
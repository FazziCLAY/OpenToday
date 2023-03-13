package com.fazziclay.opentoday.gui

import android.content.Context
import androidx.annotation.StringRes
import com.fazziclay.opentoday.R
import com.fazziclay.opentoday.app.items.item.filter.LogicMode
import com.fazziclay.opentoday.app.settings.SettingsManager

// GUI-only
object EnumsRegistry {
    private val INFOS = arrayOf(
        EnumInfo(LogicMode.AND,                                     R.string.logic_container_logicMode_AND),
        EnumInfo(LogicMode.OR,                                      R.string.logic_container_logicMode_OR),
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
    )

    @StringRes
    fun nameResId(e: Enum<*>): Int {
        for (info in INFOS) {
            if (info.e === e) {
                return info.nameResId
            }
        }
        throw RuntimeException("EnumsRegistry: enum $e not found!")
    }

    fun name(e: Enum<*>, context: Context): String = context.getString(nameResId(e))

    class EnumInfo(var e: Enum<*>, var nameResId: Int)
}
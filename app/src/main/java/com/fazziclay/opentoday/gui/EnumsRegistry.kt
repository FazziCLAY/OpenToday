package com.fazziclay.opentoday.gui

import android.content.Context
import androidx.annotation.StringRes
import com.fazziclay.opentoday.R
import com.fazziclay.opentoday.app.ImportWrapper
import com.fazziclay.opentoday.app.SettingsManager
import com.fazziclay.opentoday.app.items.item.CycleListItem
import com.fazziclay.opentoday.app.items.item.FilterGroupItem
import com.fazziclay.opentoday.app.items.item.ItemType
import com.fazziclay.opentoday.app.items.item.filter.FiltersRegistry
import com.fazziclay.opentoday.app.items.item.filter.LogicContainerItemFilter

// GUI-only
object EnumsRegistry {
    private val ENUMS = arrayOf(
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
        EnumInfo(FiltersRegistry.FilterType.ITEM_STAT,              R.string.filterRegistry_filterType_ITEM_STAT),

        EnumInfo(ItemType.DEBUG_TICK_COUNTER,            R.string.item_debugTickCounter).setItemDescription(R.string.item_debugTickCounter_description),
        EnumInfo(ItemType.TEXT,                          R.string.item_text).setItemDescription(R.string.item_text_description),
        EnumInfo(ItemType.LONG_TEXT,                     R.string.item_longTextItem).setItemDescription(R.string.item_longTextItem_description),
        EnumInfo(ItemType.CHECKBOX,                      R.string.item_checkbox).setItemDescription(R.string.item_checkbox_description),
        EnumInfo(ItemType.CHECKBOX_DAY_REPEATABLE,       R.string.item_checkboxDayRepeatable).setItemDescription(R.string.item_checkboxDayRepeatable_description),
        EnumInfo(ItemType.COUNTER,                       R.string.item_counter).setItemDescription(R.string.item_counter_description),
        EnumInfo(ItemType.CYCLE_LIST,                    R.string.item_cycleList).setItemDescription(R.string.item_cycleList_description),
        EnumInfo(ItemType.GROUP,                         R.string.item_group).setItemDescription(R.string.item_group_description),
        EnumInfo(ItemType.FILTER_GROUP,                  R.string.item_filterGroup).setItemDescription(R.string.item_filterGroup_description),

        EnumInfo(FilterGroupItem.TickBehavior.ALL,           R.string.filter_group_item_filter_tickBehavoir_ALL),
        EnumInfo(FilterGroupItem.TickBehavior.NOTHING,       R.string.filter_group_item_filter_tickBehavoir_NOTHING),
        EnumInfo(FilterGroupItem.TickBehavior.ACTIVE,        R.string.filter_group_item_filter_tickBehavoir_ACTIVE),
        EnumInfo(FilterGroupItem.TickBehavior.NOT_ACTIVE,    R.string.filter_group_item_filter_tickBehavoir_NOT_ACTIVE),

        EnumInfo(CycleListItem.TickBehavior.ALL,            R.string.cycleListItem_tickBehavior_all),
        EnumInfo(CycleListItem.TickBehavior.NOTHING,        R.string.cycleListItem_tickBehavior_nothing),
        EnumInfo(CycleListItem.TickBehavior.CURRENT,        R.string.cycleListItem_tickBehavior_current),
        EnumInfo(CycleListItem.TickBehavior.NOT_CURRENT,    R.string.cycleListItem_tickBehavior_notCurrent),

        EnumInfo(SettingsManager.ItemAddPosition.TOP,       R.string.settings_itemAddPosition_TOP),
        EnumInfo(SettingsManager.ItemAddPosition.BOTTOM,    R.string.settings_itemAddPosition_BOTTOM),

    )

    fun missingChecks() {
        missingCheck(LogicContainerItemFilter.LogicMode.values().toList())
        missingCheck(SettingsManager.FirstTab.values().toList())
        missingCheck(SettingsManager.ItemAction.values().toList())
        missingCheck(ImportWrapper.ErrorCode.values().toList())
        missingCheck(FiltersRegistry.FilterType.values().toList())
        missingCheck(ItemType.values().toList())
        missingCheck(FilterGroupItem.TickBehavior.values().toList())
        missingCheck(CycleListItem.TickBehavior.values().toList())
        missingCheck(SettingsManager.ItemAddPosition.values().toList())
    }

    private fun missingCheck(values: List<Enum<*>>) {
        for (value in values) {
            getInfo(value)
        }
    }

    private fun getInfo(e: Enum<*>): EnumInfo {
        for (info in ENUMS) {
            if (info.e === e) {
                return info
            }
        }
        throw RuntimeException("EnumsRegistry: enum $e not found!")
    }

    @StringRes
    fun itemDescriptionResId(enum: Enum<*>): Int {
        return getInfo(enum).itemDescriptionResId
    }

    @StringRes
    fun nameResId(e: Enum<*>): Int {
        return getInfo(e).nameResId
    }
    fun name(e: Enum<*>, context: Context): String = context.getString(nameResId(e))

    class EnumInfo(var e: Enum<*>, var nameResId: Int) {
        var itemDescriptionResId: Int = R.string.unknown

        fun setItemDescription(i: Int): EnumInfo {
            itemDescriptionResId = i
            return this
        }
    }
}
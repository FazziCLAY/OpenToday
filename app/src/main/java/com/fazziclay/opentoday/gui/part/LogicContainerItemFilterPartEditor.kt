package com.fazziclay.opentoday.gui.part

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.fazziclay.opentoday.app.items.item.filter.LogicContainerItemFilter
import com.fazziclay.opentoday.databinding.PartLogitContainerItemFilterEditorBinding
import com.fazziclay.opentoday.gui.interfaces.Destroy

class LogicContainerItemFilterPartEditor(private var context: Context, layoutInflater: LayoutInflater, logicContainerItemFilter: LogicContainerItemFilter, function: Runnable) : Destroy {

    private var binding: PartLogitContainerItemFilterEditorBinding = PartLogitContainerItemFilterEditorBinding.inflate(layoutInflater)

    init {
        binding.filters.adapter
    }

    override fun destroy() {

    }

    fun getRootView(): View {
        return binding.root
    }
}
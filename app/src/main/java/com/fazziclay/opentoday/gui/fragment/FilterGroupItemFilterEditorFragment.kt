package com.fazziclay.opentoday.gui.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fazziclay.opentoday.R
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.app.items.ItemsRoot
import com.fazziclay.opentoday.app.items.item.FilterGroupItem
import com.fazziclay.opentoday.app.items.item.Item
import com.fazziclay.opentoday.app.items.item.filter.DateItemFilter
import com.fazziclay.opentoday.app.items.item.filter.ItemFilter
import com.fazziclay.opentoday.app.items.item.filter.ItemStatItemFilter
import com.fazziclay.opentoday.app.items.item.filter.LogicContainerItemFilter
import com.fazziclay.opentoday.databinding.FragmentFilterGroupItemFilterEditorBinding
import com.fazziclay.opentoday.gui.ActivitySettings
import com.fazziclay.opentoday.gui.UI
import com.fazziclay.opentoday.gui.interfaces.ActivitySettingsMember
import com.fazziclay.opentoday.gui.interfaces.Destroy
import com.fazziclay.opentoday.gui.part.DateItemFilterPartEditor
import com.fazziclay.opentoday.gui.part.ItemStatFilterPartEditor
import com.fazziclay.opentoday.gui.part.LogicContainerItemFilterPartEditor
import kotlinx.coroutines.Runnable
import java.util.UUID

class FilterGroupItemFilterEditorFragment : Fragment(), ActivitySettingsMember {
    companion object {
        private const val KEY_FILTER_GROUP = "filterGroupItemFilterEditorFragment:filterGroupId"
        private const val KEY_ITEM = "filterGroupItemFilterEditorFragment:itemId"
        private const val CONTAINER_ID = R.id.itemFilter_root_container

        @JvmStatic
        fun create(filterGroupId: UUID, itemId: UUID): FilterGroupItemFilterEditorFragment {
            val fragment = FilterGroupItemFilterEditorFragment()

            val arguments = Bundle()
            arguments.putString(KEY_FILTER_GROUP, filterGroupId.toString())
            arguments.putString(KEY_ITEM, itemId.toString())

            fragment.arguments = arguments
            return fragment
        }

        fun openEditFilterDialog(context: Context, itemFilter: ItemFilter?, item: Item?, saveSignal: Runnable, parentFilterGroup: FilterGroupItem) {
            val view: View
            val destroy: Destroy
            when (itemFilter) {
                is DateItemFilter -> {
                    val part = DateItemFilterPartEditor(context, LayoutInflater.from(context), itemFilter, item, saveSignal)
                    destroy = part
                    view = part.rootView

                }
                is LogicContainerItemFilter -> {
                    val part = LogicContainerItemFilterPartEditor(context, LayoutInflater.from(context), parentFilterGroup, itemFilter, item, saveSignal)
                    destroy = part
                    view = part.getRootView()
                }
                is ItemStatItemFilter -> {
                    val part = ItemStatFilterPartEditor(context, LayoutInflater.from(context), itemFilter, item, saveSignal)
                    destroy = part
                    view = part.rootView
                }
                else -> {
                    val part = TextView(context)
                    part.text = context.getString(R.string.fragment_filterGroup_itemFilter_editor_error_unknownFilter, itemFilter?.javaClass?.canonicalName)
                    view = part
                    destroy = object : Destroy {
                        override fun destroy() {
                            Toast.makeText(context, "[ERROR] Unknown filter destroyed!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            AlertDialog.Builder(context)
                .setView(view)
                .setOnCancelListener { destroy.destroy() }
                .setPositiveButton(R.string.abc_ok, null)
                .show()
        }
    }

    private lateinit var binding: FragmentFilterGroupItemFilterEditorBinding
    private lateinit var app: App
    private lateinit var itemsRoot: ItemsRoot
    private lateinit var filterGroup: FilterGroupItem
    private lateinit var item: Item
    private lateinit var rootFilter: ItemFilter
    private var part: Destroy? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = App.get(requireContext())
        itemsRoot = app.itemsRoot

        if (arguments != null) {
            val filterGroupId = UUID.fromString(requireArguments().getString(KEY_FILTER_GROUP))
            filterGroup = (itemsRoot.getItemById(filterGroupId) as FilterGroupItem?)!!
            val itemId = UUID.fromString(requireArguments().getString(KEY_ITEM))
            item = filterGroup.getItemById(itemId)!!

            rootFilter = filterGroup.getItemFilter(item)!!
        }

        UI.getUIRoot(this).pushActivitySettings { a: ActivitySettings ->
            a.isNotificationsVisible = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFilterGroupItemFilterEditorBinding.inflate(inflater)
        setupView()
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        part?.destroy()
    }

    private fun setupView() {
        reloadPartEditor()

        if (rootFilter !is LogicContainerItemFilter) {
            binding.outdatedItemFilterScheme.visibility = View.VISIBLE
            binding.mergeToNew.setOnClickListener {
                val newFilter = LogicContainerItemFilter()
                newFilter.add(rootFilter)
                filterGroup.setItemFilter(item, newFilter)
                rootFilter = newFilter
                reloadPartEditor()
                binding.outdatedItemFilterScheme.visibility = View.GONE
            }
        }
    }

    private fun reloadPartEditor() {
        binding.itemFilterRootContainer.removeAllViews()

        when (rootFilter) {
            is LogicContainerItemFilter -> {
                val editor = LogicContainerItemFilterPartEditor(requireContext(), layoutInflater, filterGroup, rootFilter as LogicContainerItemFilter, item) { filterGroup.save() }
                binding.itemFilterRootContainer.addView(editor.getRootView())
                part = editor

            }
            is DateItemFilter -> {
                val editor = DateItemFilterPartEditor(requireContext(), layoutInflater, rootFilter as DateItemFilter, item) { filterGroup.save() }
                binding.itemFilterRootContainer.addView(editor.rootView)
                part = editor
            }
            is ItemStatItemFilter -> {
                val editor = ItemStatFilterPartEditor(requireContext(), layoutInflater, rootFilter as ItemStatItemFilter, item) { filterGroup.save() }
                binding.itemFilterRootContainer.addView(editor.rootView)
                part = editor
            }
        }
    }
}
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
import com.fazziclay.opentoday.app.items.ItemManager
import com.fazziclay.opentoday.app.items.item.FilterGroupItem
import com.fazziclay.opentoday.app.items.item.Item
import com.fazziclay.opentoday.app.items.item.filter.DateItemFilter
import com.fazziclay.opentoday.app.items.item.filter.ItemFilter
import com.fazziclay.opentoday.app.items.item.filter.LogicContainerItemFilter
import com.fazziclay.opentoday.databinding.FragmentFilterGroupItemFilterEditorBinding
import com.fazziclay.opentoday.gui.interfaces.Destroy
import com.fazziclay.opentoday.gui.part.DateItemFilterPartEditor
import com.fazziclay.opentoday.gui.part.LogicContainerItemFilterPartEditor
import kotlinx.coroutines.Runnable
import java.util.*

class FilterGroupItemFilterEditorFragment : Fragment() {
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

        fun openEditFilterDialog(context: Context, itemFilter: ItemFilter?, saveSignal: Runnable, parentFilterGroup: FilterGroupItem) {
            val view: View
            val destroy: Destroy
            when (itemFilter) {
                is DateItemFilter -> {
                    val part = DateItemFilterPartEditor(context, LayoutInflater.from(context), itemFilter, saveSignal)
                    destroy = part;
                    view = part.rootView

                }
                is LogicContainerItemFilter -> {
                    val part = LogicContainerItemFilterPartEditor(context, LayoutInflater.from(context), parentFilterGroup, itemFilter, saveSignal)
                    destroy = part;
                    view = part.getRootView()
                }
                else -> {
                    val part = TextView(context)
                    part.text = context.getString(R.string.filter_group_item_filter_editor_error_unknownFilter, itemFilter?.javaClass?.canonicalName)
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
                .setPositiveButton("OK", null) // TODO: make translatable
                .show()
        }
    }

    private lateinit var binding: FragmentFilterGroupItemFilterEditorBinding;
    private lateinit var app: App
    private lateinit var itemManager: ItemManager
    private lateinit var filterGroup: FilterGroupItem
    private lateinit var item: Item
    private lateinit var rootFilter: ItemFilter
    private var part: Destroy? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = App.get(requireContext())
        itemManager = app.itemManager

        if (arguments != null) {
            val filterGroupId = UUID.fromString(requireArguments().getString(KEY_FILTER_GROUP))
            filterGroup = (itemManager.getItemById(filterGroupId) as FilterGroupItem?)!!
            val itemId = UUID.fromString(requireArguments().getString(KEY_ITEM))
            item = filterGroup.getItemById(itemId)!!

            rootFilter = filterGroup.getItemFilter(item)!!
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
    }

    private fun reloadPartEditor() {
        binding.itemFilterRootContainer.removeAllViews()

        if (rootFilter is LogicContainerItemFilter) {
            val editor = LogicContainerItemFilterPartEditor(requireContext(), layoutInflater, filterGroup, rootFilter as LogicContainerItemFilter) { filterGroup.save() }
            binding.itemFilterRootContainer.addView(editor.getRootView())
            part = editor

        } else if (rootFilter is DateItemFilter) {
            val editor = DateItemFilterPartEditor(requireContext(), layoutInflater, rootFilter as DateItemFilter) { filterGroup.save() }
            binding.itemFilterRootContainer.addView(editor.rootView)
            part = editor
        }
    }
}
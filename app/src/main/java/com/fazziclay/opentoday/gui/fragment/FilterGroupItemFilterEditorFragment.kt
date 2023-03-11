package com.fazziclay.opentoday.gui.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.fazziclay.opentoday.R
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.app.items.ItemManager
import com.fazziclay.opentoday.app.items.item.FilterGroupItem
import com.fazziclay.opentoday.app.items.item.Item
import com.fazziclay.opentoday.app.items.item.filter.DateItemFilter
import com.fazziclay.opentoday.app.items.item.filter.FiltersRegistry
import com.fazziclay.opentoday.app.items.item.filter.ItemFilter
import com.fazziclay.opentoday.app.items.item.filter.LogicContainerItemFilter
import com.fazziclay.opentoday.databinding.FragmentFilterGroupItemFilterEditorBinding
import com.fazziclay.opentoday.gui.interfaces.Destroy
import com.fazziclay.opentoday.gui.part.DateItemFilterPartEditor
import com.fazziclay.opentoday.gui.part.LogicContainerItemFilterPartEditor
import com.fazziclay.opentoday.util.SimpleSpinnerAdapter
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
        binding.rootReplace.setOnClickListener {
            showRootReplaceDialog()
        }

        reloadPartEditor()
    }

    private fun showRootReplaceDialog() {
        val adapter = SimpleSpinnerAdapter<Class<out ItemFilter?>>(requireContext())
        adapter.add("DateItemFilter", DateItemFilter().javaClass)
        adapter.add("LogicContainerItemFilter", LogicContainerItemFilter().javaClass)

        val spinner = Spinner(requireContext())
        spinner.adapter = adapter

        AlertDialog.Builder(requireContext())
                .setTitle("Replace root filter")
                .setMessage("! Data maybe loss !")
                .setView(spinner)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Replace") { _: DialogInterface, _: Int ->
                    val filterInfo = FiltersRegistry.REGISTRY.getByClass(adapter.getItem(spinner.selectedItemPosition))
                    rootFilter = filterInfo.createFilterInterface.create()
                    filterGroup.setItemFilter(item, rootFilter)
                    reloadPartEditor()
                }
                .show()
    }

    private fun reloadPartEditor() {
        binding.rootType.text = rootFilter.javaClass.simpleName
        binding.itemFilterRootContainer.removeAllViews()

        if (rootFilter is LogicContainerItemFilter) {
            val editor = LogicContainerItemFilterPartEditor(requireContext(), layoutInflater, rootFilter as LogicContainerItemFilter) { filterGroup.save() }
            binding.itemFilterRootContainer.addView(editor.getRootView())
            part = editor

        } else if (rootFilter is DateItemFilter) {
            val editor = DateItemFilterPartEditor(requireContext(), layoutInflater, rootFilter as DateItemFilter) { filterGroup.save() }
            binding.itemFilterRootContainer.addView(editor.rootView)
            part = editor
        }
    }
}
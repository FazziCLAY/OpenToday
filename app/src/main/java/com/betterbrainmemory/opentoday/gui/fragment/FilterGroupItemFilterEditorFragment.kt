package com.betterbrainmemory.opentoday.gui.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.betterbrainmemory.opentoday.R
import com.betterbrainmemory.opentoday.databinding.FragmentFilterGroupItemFilterEditorBinding
import com.betterbrainmemory.opentoday.gui.UI
import com.betterbrainmemory.opentoday.gui.interfaces.Destroy
import com.betterbrainmemory.opentoday.gui.part.LogicContainerItemFilterPartEditor
import kotlinx.coroutines.Runnable
import java.util.UUID

class FilterGroupItemFilterEditorFragment : Fragment(),
    com.betterbrainmemory.opentoday.gui.interfaces.ActivitySettingsMember {
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

        fun openEditFilterDialog(context: Context, itemFilter: com.betterbrainmemory.opentoday.app.items.item.filter.ItemFilter?, item: com.betterbrainmemory.opentoday.app.items.item.Item?, saveSignal: Runnable, parentFilterGroup: com.betterbrainmemory.opentoday.app.items.item.FilterGroupItem) {
            val view: View
            val destroy: Destroy
            when (itemFilter) {
                is com.betterbrainmemory.opentoday.app.items.item.filter.DateItemFilter -> {
                    val part =
                        com.betterbrainmemory.opentoday.gui.part.DateItemFilterPartEditor(
                            context,
                            LayoutInflater.from(context),
                            itemFilter,
                            item,
                            saveSignal
                        )
                    destroy = part
                    view = part.rootView

                }
                is com.betterbrainmemory.opentoday.app.items.item.filter.LogicContainerItemFilter -> {
                    val part = LogicContainerItemFilterPartEditor(context, LayoutInflater.from(context), parentFilterGroup, itemFilter, item, saveSignal)
                    destroy = part
                    view = part.getRootView()
                }
                is com.betterbrainmemory.opentoday.app.items.item.filter.ItemStatItemFilter -> {
                    val part =
                        com.betterbrainmemory.opentoday.gui.part.ItemStatFilterPartEditor(
                            context,
                            LayoutInflater.from(context),
                            itemFilter,
                            item,
                            saveSignal
                        )
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
    private lateinit var app: com.betterbrainmemory.opentoday.app.App
    private lateinit var itemsRoot: com.betterbrainmemory.opentoday.app.items.ItemsRoot
    private lateinit var filterGroup: com.betterbrainmemory.opentoday.app.items.item.FilterGroupItem
    private lateinit var item: com.betterbrainmemory.opentoday.app.items.item.Item
    private lateinit var rootFilter: com.betterbrainmemory.opentoday.app.items.item.filter.ItemFilter
    private var part: Destroy? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = com.betterbrainmemory.opentoday.app.App.get(requireContext())
        itemsRoot = app.itemsRoot

        if (arguments != null) {
            val filterGroupId = UUID.fromString(requireArguments().getString(KEY_FILTER_GROUP))
            filterGroup = (itemsRoot.getItemById(filterGroupId) as com.betterbrainmemory.opentoday.app.items.item.FilterGroupItem?)!!
            val itemId = UUID.fromString(requireArguments().getString(KEY_ITEM))
            item = filterGroup.getItemById(itemId)!!

            rootFilter = filterGroup.getItemFilter(item)!!
        }

        UI.getUIRoot(this).pushActivitySettings { a: com.betterbrainmemory.opentoday.gui.ActivitySettings ->
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

        if (rootFilter !is com.betterbrainmemory.opentoday.app.items.item.filter.LogicContainerItemFilter) {
            binding.outdatedItemFilterScheme.visibility = View.VISIBLE
            binding.mergeToNew.setOnClickListener {
                val newFilter =
                    com.betterbrainmemory.opentoday.app.items.item.filter.LogicContainerItemFilter()
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
            is com.betterbrainmemory.opentoday.app.items.item.filter.LogicContainerItemFilter -> {
                val editor = LogicContainerItemFilterPartEditor(requireContext(), layoutInflater, filterGroup, rootFilter as com.betterbrainmemory.opentoday.app.items.item.filter.LogicContainerItemFilter, item) { filterGroup.save() }
                binding.itemFilterRootContainer.addView(editor.getRootView())
                part = editor

            }
            is com.betterbrainmemory.opentoday.app.items.item.filter.DateItemFilter -> {
                val editor =
                    com.betterbrainmemory.opentoday.gui.part.DateItemFilterPartEditor(
                        requireContext(),
                        layoutInflater,
                        rootFilter as com.betterbrainmemory.opentoday.app.items.item.filter.DateItemFilter,
                        item
                    ) { filterGroup.save() }
                binding.itemFilterRootContainer.addView(editor.rootView)
                part = editor
            }
            is com.betterbrainmemory.opentoday.app.items.item.filter.ItemStatItemFilter -> {
                val editor =
                    com.betterbrainmemory.opentoday.gui.part.ItemStatFilterPartEditor(
                        requireContext(),
                        layoutInflater,
                        rootFilter as com.betterbrainmemory.opentoday.app.items.item.filter.ItemStatItemFilter,
                        item
                    ) { filterGroup.save() }
                binding.itemFilterRootContainer.addView(editor.rootView)
                part = editor
            }
        }
    }
}
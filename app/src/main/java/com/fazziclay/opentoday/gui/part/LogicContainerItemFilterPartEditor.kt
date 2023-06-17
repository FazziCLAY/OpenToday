package com.fazziclay.opentoday.gui.part

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.os.Handler
import android.text.Editable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.fazziclay.opentoday.R
import com.fazziclay.opentoday.app.items.item.FilterGroupItem
import com.fazziclay.opentoday.app.items.item.Item
import com.fazziclay.opentoday.app.items.item.filter.FiltersRegistry
import com.fazziclay.opentoday.app.items.item.filter.FitEquip
import com.fazziclay.opentoday.app.items.item.filter.ItemFilter
import com.fazziclay.opentoday.app.items.item.filter.LogicContainerItemFilter
import com.fazziclay.opentoday.app.items.item.filter.LogicContainerItemFilter.LogicMode
import com.fazziclay.opentoday.databinding.PartLogicContainerItemFilterEditorBinding
import com.fazziclay.opentoday.gui.EnumsRegistry
import com.fazziclay.opentoday.gui.fragment.FilterGroupItemFilterEditorFragment
import com.fazziclay.opentoday.gui.interfaces.Destroy
import com.fazziclay.opentoday.util.EnumUtil
import com.fazziclay.opentoday.util.MinTextWatcher
import com.fazziclay.opentoday.util.ResUtil
import com.fazziclay.opentoday.util.SimpleSpinnerAdapter
import java.util.GregorianCalendar

class LogicContainerItemFilterPartEditor(private val context: Context, layoutInflater: LayoutInflater, private val parentFilterGroup: FilterGroupItem, private val logicContainerItemFilter: LogicContainerItemFilter, private val item: Item?, private val saveSignal: Runnable) : Destroy {
    companion object {
        private const val FILTERS_DECOR_LEFT = 0
        private const val FILTERS_DECOR_TOP = 5
        private const val FILTERS_DECOR_RIGHT = 0
        private const val FILTERS_DECOR_BOTTOM = 5
        private val FILTERS_ITEM_BACKGROUND = Color.parseColor("#66444444")
        private const val FILTERS_ITEM_TEXT_SIZE = 18f
    }


    private var colorInactive: Int = ResUtil.getAttrColor(context, R.attr.itemFilterState_false)
    private var colorActive: Int = ResUtil.getAttrColor(context, R.attr.itemFilterState_true)
    private val binding: PartLogicContainerItemFilterEditorBinding = PartLogicContainerItemFilterEditorBinding.inflate(layoutInflater)
    private var handler: Handler = Handler(context.mainLooper)
    private var runnable: Runnable? = null
    private var destroyed = false
    private var cached: HashMap<ItemFilter, View> = HashMap()

    init {

        runnable = Runnable {
            if (destroyed) return@Runnable

            cached.forEach { (t, u) ->
                val fitEquip = FitEquip(GregorianCalendar());
                fitEquip.currentItem = item
                val isFit = t.isFit(fitEquip)
                u.backgroundTintList = ColorStateList.valueOf(if (isFit) colorActive else colorInactive)
            }
            handler.postDelayed(runnable!!, 1000 / 4)
        }
        handler.post(runnable!!)

        // DESCRIPTION
        binding.description.setText(logicContainerItemFilter.description)
        binding.description.addTextChangedListener(object : MinTextWatcher() {
            override fun afterTextChanged(s: Editable) {
                logicContainerItemFilter.description = s.toString()
                save()
            }
        })

        // REVERSE
        binding.invert.isChecked = logicContainerItemFilter.isReverse
        binding.invert.setOnClickListener {
            logicContainerItemFilter.isReverse = binding.invert.isChecked
            save()
        }

        // LOGIC MODE
        val adapter: SimpleSpinnerAdapter<LogicMode> = SimpleSpinnerAdapter<LogicMode>(context);
        EnumUtil.addToSimpleSpinnerAdapter(context, adapter, LogicMode.values())
        binding.logicMode.adapter = adapter
        binding.logicMode.setSelection(adapter.getValuePosition(logicContainerItemFilter.logicMode))
        binding.logicMode.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                logicContainerItemFilter.logicMode = adapter.getItem(position)
                save()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // RECYCLE VIEW
        binding.filters.layoutManager = LinearLayoutManager(context)
        binding.filters.adapter = Adapter()
        binding.filters.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.set(FILTERS_DECOR_LEFT, FILTERS_DECOR_TOP, FILTERS_DECOR_RIGHT, FILTERS_DECOR_BOTTOM)
            }
        })

        // ADD BUTTON (FAB)
        binding.add.setOnClickListener {
            val p = PopupMenu(context, binding.add, Gravity.CENTER)
            for (filterInfo in FiltersRegistry.REGISTRY.allFilters) {
                val menuItem = p.menu.add(EnumsRegistry.nameResId(filterInfo.type))
                menuItem.setOnMenuItemClickListener {
                    val addPos = logicContainerItemFilter.add(filterInfo.createFilterInterface.create())
                    binding.filters.adapter!!.notifyItemInserted(addPos)
                    save()

                    return@setOnMenuItemClickListener true
                }
            }
            p.show()
        }
    }

    private fun save() {
        saveSignal.run()
    }

    override fun destroy() {
        destroyed = true;
    }

    fun getRootView(): View {
        return binding.root
    }

    private inner class Adapter : RecyclerView.Adapter<Adapter.Holder>() {

        private inner class Holder(context: Context) : RecyclerView.ViewHolder(FrameLayout(context)) {
            val frameLayout: FrameLayout = itemView as FrameLayout
            val description: TextView = TextView(context)

            init {
                frameLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                frameLayout.setBackgroundColor(FILTERS_ITEM_BACKGROUND)
                frameLayout.addView(description)
                description.textSize = FILTERS_ITEM_TEXT_SIZE
                description.background = AppCompatResources.getDrawable(context, R.drawable.shape)
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): Holder {
            return Holder(viewGroup.context)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: Holder, position: Int) {
            val itemFilter = logicContainerItemFilter.filters[position]
            viewHolder.description.text = itemFilter.description.ifBlank { context.getString(R.string.logic_container_item_filter_part_editor_name_empty) }
            viewHolder.frameLayout.setOnClickListener {
                FilterGroupItemFilterEditorFragment.openEditFilterDialog(context, itemFilter, item, {
                    notifyItemChanged(logicContainerItemFilter.getFilterPosition(itemFilter))
                    save()
                }, parentFilterGroup)
            }
            viewHolder.frameLayout.setOnLongClickListener {
                val popup = PopupMenu(context, viewHolder.frameLayout)
                popup.inflate(R.menu.menu_logincontainerfilter_item)
                popup.setOnMenuItemClickListener { menuItem ->
                    if (menuItem.itemId == R.id.menu_logincontainerfilter_item_delete) {
                        val index = logicContainerItemFilter.remove(itemFilter)
                        notifyItemRemoved(index)
                    }
                    return@setOnMenuItemClickListener true
                }
                popup.show()
                return@setOnLongClickListener true
            }
            cached[itemFilter] = viewHolder.description
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount(): Int = logicContainerItemFilter.filters.size
    }

}
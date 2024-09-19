package com.hilldating.test

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.general.BaseFragment
import com.general.BaseViewModel
import com.general.ext.randomColor
import com.hilldating.test.databinding.FragmentRecyclerBinding
import java.util.Collections

class RecyclerViewFragment : BaseFragment<BaseViewModel, FragmentRecyclerBinding>() {

    class Item(
        val index: Int,
        val spanSize: Int,
        var beforeSize: Int = 0,
    )

    private val items = arrayListOf<Item>()
    private val adapter = Adapter(items)
    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
        0
    ) {

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            (viewHolder as? Adapter.Holder)?.onSelectChange(actionState == 2)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            (viewHolder as? Adapter.Holder)?.onSelectChange(false)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            var fromPosition = viewHolder.bindingAdapterPosition
            var toPosition = target.bindingAdapterPosition
            val from = items[fromPosition]
            val to = items[toPosition]
            if (from.spanSize == to.spanSize) {
                onMove(fromPosition, toPosition)
            } else if (from.spanSize == 1) { //1x1
                if (from.beforeSize % 2 == 0) { //左侧
                    val followItem = items[fromPosition + 1]
                    if (fromPosition < toPosition) { //上->下
                        onMove(fromPosition, toPosition)
                        fromPosition = items.indexOf(followItem)
                        toPosition = items.indexOf(from)
                        onMove(fromPosition, toPosition)
                    } else { //下->上
                        onMove(fromPosition, toPosition)
                        fromPosition = items.indexOf(followItem)
                        toPosition = items.indexOf(to)
                        onMove(fromPosition, toPosition)
                    }
                } else { //右侧
                    val followItem = items[fromPosition - 1]
                    onMove(fromPosition, toPosition)
                    if (fromPosition < toPosition) { //上->下
                        fromPosition = items.indexOf(followItem)
                        toPosition = items.indexOf(to)
                    } else { //下->上
                        fromPosition = items.indexOf(followItem)
                        toPosition = items.indexOf(from)
                    }
                    onMove(fromPosition, toPosition)
                }
            } else { //1x2
                if (fromPosition < toPosition) { //上->下
                    onMove(fromPosition, fromPosition + 2)
                } else { //下->上
                    onMove(fromPosition, fromPosition - 2)
                }
            }
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    })

    override fun init(view: View) {
        items.add(Item(0, 2))
        items.add(Item(1, 1))
        items.add(Item(2, 1))
        items.add(Item(3, 1))
        items.add(Item(4, 1))
        items.add(Item(5, 2))
        items.add(Item(6, 1))
        items.add(Item(7, 1))
        refreshBeforeSize()
        binding.root.apply {
            adapter = this@RecyclerViewFragment.adapter
            layoutManager = GridLayoutManager(context, 2).also {
                it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int) = items[position].spanSize
                }
            }
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    private fun refreshBeforeSize() {
        var beforeSize = 0
        items.forEach {
            it.beforeSize = beforeSize
            beforeSize += it.spanSize
        }
    }

    private fun onMove(form: Int, to: Int) {
        val item = items.removeAt(form)
        items.add(to, item)
        refreshBeforeSize()
        adapter.notifyItemMoved(form, to)
    }

    class Adapter(
        val data: List<Item>
    ) : RecyclerView.Adapter<Adapter.Holder>() {

        class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val title = itemView.findViewById<TextView>(R.id.title)

            fun onSelectChange(selected: Boolean) {
                val color = if (selected) {
                    Color.RED
                } else {
                    0
                }
                title.setBackgroundColor(color)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false))
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = data[position]
            holder.title.text = item.index.toString()
            val color = (holder.itemView.tag as? String) ?: randomColor()
            holder.itemView.tag = color
            holder.itemView.setBackgroundColor(Color.parseColor(color))
        }
    }
}

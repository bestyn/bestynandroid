package com.gbksoft.neighbourhood.ui.widgets.linear_list

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

class LinearList : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private val viewLayouts = mutableListOf<ViewHolder>()
    var adapter: Adapter<out ViewHolder>? = null
        set(value) {
            field = value
            viewLayouts.clear()
            field?.linearList = this
        }

    private fun notifyDataSetChanged(adapter: Adapter<out ViewHolder>) {
        if (this.adapter != adapter) return

        val actualCount = adapter.getItemCount()
        when {
            viewLayouts.size == actualCount -> {
                replace(adapter, 0, viewLayouts.size - 1)
            }
            viewLayouts.size > actualCount -> {
                removeExtra(viewLayouts.size)
                replace(adapter, 0, viewLayouts.size - 1)
            }
            viewLayouts.size < actualCount -> {
                replace(adapter, 0, viewLayouts.size - 1)
                addNew(adapter, viewLayouts.size, actualCount - 1)
            }
        }
    }

    private fun replace(adapter: Adapter<out ViewHolder>, fromIndex: Int, toIndex: Int) {
        for (i in fromIndex..toIndex) {
            adapter.onSetupViewLayout(viewLayouts[i], i)
        }
    }

    private fun removeExtra(from: Int) {
        for (i in childCount - 1..from) {
            viewLayouts.removeAt(i)
            removeViewAt(i)
        }
    }

    private fun addNew(adapter: Adapter<out ViewHolder>, fromIndex: Int, toIndex: Int) {
        for (i in fromIndex..toIndex) {
            val viewHolder = adapter.inflateViewLayout(this)
            adapter.onSetupViewLayout(viewHolder, i)
            viewLayouts.add(viewHolder)
            addView(viewHolder.getView(), i)
        }
    }

    abstract class Adapter<T : ViewHolder> {
        internal var linearList: LinearList? = null
        internal fun onSetupViewLayout(viewHolder: ViewHolder, position: Int) {
            setupViewLayout(viewHolder as T, position)
        }

        protected fun notifyDataSetChanged() {
            linearList?.notifyDataSetChanged(this)
        }

        abstract fun getItemCount(): Int
        abstract fun inflateViewLayout(parent: ViewGroup): T
        abstract fun setupViewLayout(viewHolder: T, position: Int)
    }

    abstract class ViewHolder {
        abstract fun getView(): View
    }
}
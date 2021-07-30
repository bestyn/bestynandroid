package com.gbksoft.neighbourhood.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class VisiblePositionsChangeListener : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is LinearLayoutManager) {
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            visiblePositionsChanged(firstVisibleItemPosition, lastVisibleItemPosition)
        }
    }

    protected abstract fun visiblePositionsChanged(firstPosition: Int, lastPosition: Int)
}
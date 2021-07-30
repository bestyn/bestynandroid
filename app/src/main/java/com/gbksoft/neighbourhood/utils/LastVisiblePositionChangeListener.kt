package com.gbksoft.neighbourhood.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class LastVisiblePositionChangeListener : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is LinearLayoutManager) {
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            lastVisiblePositionChanged(lastVisibleItemPosition)
        }
    }

    protected abstract fun lastVisiblePositionChanged(lastVisibleItemPosition: Int)
}
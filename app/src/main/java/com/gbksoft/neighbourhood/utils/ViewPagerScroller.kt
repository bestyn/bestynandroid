package com.gbksoft.neighbourhood.utils

import android.os.Handler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewpager2.widget.ViewPager2

class ViewPagerScroller(lifecycle: Lifecycle, val viewPager2: ViewPager2) : LifecycleObserver {
    private val handler = Handler()
    private val callback: Runnable? = null

    init {
        lifecycle.addObserver(this)
    }

    fun setCurrentPosition(position: Int) {
        if (viewPager2.currentItem == position) return
        //viewPager2.
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        callback?.let { handler.removeCallbacks(it) }
    }
}
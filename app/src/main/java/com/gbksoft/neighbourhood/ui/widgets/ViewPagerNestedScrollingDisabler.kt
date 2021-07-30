package com.gbksoft.neighbourhood.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import timber.log.Timber
import kotlin.math.abs

class ViewPagerNestedScrollingDisabler : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private var touchSlop = 0
    private var initialX = 0f
    private var initialY = 0f

    private var childRecyclerView: RecyclerView? = null

    init {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        childRecyclerView = findRecyclerView()
    }

    private fun findRecyclerView(): RecyclerView? {
        val viewPager = if (childCount > 0) getChildAt(0) as? ViewPager2 else return null

        for (field in ViewPager2::class.java.declaredFields) {
            if (field.type == RecyclerView::class.java) {
                field.isAccessible = true
                return field.get(viewPager) as? RecyclerView
            }
        }
        return null
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        childRecyclerView?.let { recyclerView ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    Timber.tag("NestedTag").d("ACTION_DOWN")
                    initialX = e.x
                    initialY = e.y
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
                MotionEvent.ACTION_MOVE -> {
                    val isHorizontalScroll = isHorizontalScroll(e.x, e.y)
                    recyclerView.isNestedScrollingEnabled = isHorizontalScroll
                    parent?.requestDisallowInterceptTouchEvent(isHorizontalScroll)
                }
                else -> {
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
            }

        }
        return super.onInterceptTouchEvent(e)
    }

    private fun isHorizontalScroll(moveX: Float, moveY: Float): Boolean {
        val dx = abs(moveX - initialX)
        val dy = abs(moveY - initialY)
        return dx > dy
    }


}
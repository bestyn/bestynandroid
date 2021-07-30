package com.gbksoft.neighbourhood.ui.widgets.swipe_to_refresh

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.gbksoft.neighbourhood.R


class ColoredSwipeRefreshLayout : SwipeRefreshLayout {
    constructor(context: Context) : super(context) {
        setColors()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setColors()
    }

    private fun setColors() {
        setColorSchemeColors(
            ContextCompat.getColor(context, R.color.colorAccent),
            ContextCompat.getColor(context, R.color.colorAccent),
            ContextCompat.getColor(context, R.color.colorAccent))
    }
}

package com.gbksoft.neighbourhood.ui.data_binding

import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.databinding.BindingAdapter
import timber.log.Timber

object ViewAdapters {
    @JvmStatic
    @BindingAdapter("visible")
    fun loadImage(view: View, isVisible: Boolean) {
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
    }


    @JvmStatic
    @BindingAdapter("marginTouchDelegate")
    fun marginTouchDelegate(view: View, marginTouchDelegate: Boolean) {
        if (marginTouchDelegate) {
            view.post {
                val touchRect = Rect()
                touchRect.left = (view.x - view.marginLeft).toInt()
                touchRect.top = (view.y - view.marginTop).toInt()
                touchRect.right = (view.x + view.measuredWidth + view.marginRight).toInt()
                touchRect.bottom = (view.y + view.measuredHeight + view.marginBottom).toInt()
                Timber.tag("TouchDelegateTag").d("touchRect: $touchRect")
                view.touchDelegate = TouchDelegate(touchRect, view)
            }
        }
    }
}
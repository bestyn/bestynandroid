package com.gbksoft.neighbourhood.ui.data_binding

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.databinding.BindingAdapter

object MarginAdapters {
    @JvmStatic
    @BindingAdapter("layout_marginLeft")
    fun setLayoutMarginLeft(view: View, marginLeft: Float) {
        if (view.layoutParams is MarginLayoutParams) {
            val p = view.layoutParams as MarginLayoutParams
            p.setMargins(marginLeft.toInt(), p.topMargin, p.rightMargin, p.bottomMargin)
            view.requestLayout()
        }
    }

    @JvmStatic
    @BindingAdapter("layout_marginTop")
    fun setLayoutMarginTop(view: View, marginTop: Float) {
        if (view.layoutParams is MarginLayoutParams) {
            val p = view.layoutParams as MarginLayoutParams
            p.setMargins(p.leftMargin, marginTop.toInt(), p.rightMargin, p.bottomMargin)
            view.requestLayout()
        }
    }

    @JvmStatic
    @BindingAdapter("layout_marginRight")
    fun setLayoutMarginRight(view: View, marginRight: Float) {
        if (view.layoutParams is MarginLayoutParams) {
            val p = view.layoutParams as MarginLayoutParams
            p.setMargins(p.leftMargin, p.topMargin, marginRight.toInt(), p.bottomMargin)
            view.requestLayout()
        }
    }

    @JvmStatic
    @BindingAdapter("layout_marginBottom")
    fun setLayoutMarginBottom(view: View, marginBottom: Float) {
        if (view.layoutParams is MarginLayoutParams) {
            val p = view.layoutParams as MarginLayoutParams
            p.setMargins(p.leftMargin, p.topMargin, p.rightMargin, marginBottom.toInt())
            view.requestLayout()
        }
    }
}
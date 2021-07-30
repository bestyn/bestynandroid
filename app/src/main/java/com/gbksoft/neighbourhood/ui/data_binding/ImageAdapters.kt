package com.gbksoft.neighbourhood.ui.data_binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object ImageAdapters {
    @BindingAdapter("app:loadImage")
    fun loadImage(imageView: ImageView?, uri: String?) {
        Glide.with(imageView!!)
            .load(uri)
            .into(imageView)
    }
}
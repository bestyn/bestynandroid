package com.gbksoft.neighbourhood.utils.media

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.gbksoft.neighbourhood.R
import java.lang.ref.SoftReference

object PlaceholderProvider {

    private const val imagePref = "image_"
    private const val videoPref = "video_"
    private const val newsPref = "news_"

    private val placeholderMap = HashMap<String, SoftReference<Drawable>>()

    fun getPicturePlaceholder(context: Context, cornerRadius: Float? = null): Drawable {
        val radius = cornerRadius ?: getDefaultCornerRadius(context)
        val key = imagePref + String.format ("%.2f", radius)
        return placeholderMap[key]?.get() ?: getPicturePlaceholderDrawable(context, radius)
    }

    fun getVideoPlaceholder(context: Context, cornerRadius: Float? = null): Drawable {
        val radius = cornerRadius ?: getDefaultCornerRadius(context)
        val key = videoPref + String.format("%.2f", radius)
        return placeholderMap[key]?.get() ?: getVideoPlaceholderDrawable(context, radius)
    }

    fun getNewsPlaceholder(context: Context, id: Long): Drawable? {
        val key = newsPref + id
        return placeholderMap[key]?.get() ?: when (((id % 3) + 1).toInt()) {
            1 -> getNewsPlaceholder1Drawable(context)
            2 -> getNewsPlaceholder2Drawable(context)
            3 -> getNewsPlaceholder3Drawable(context)
            else -> null
        }
    }

    private fun getPicturePlaceholderDrawable(context: Context, cornerRadius: Float): Drawable {
        val drawable = getPlaceholderDrawable(context, R.drawable.placeholder_picture, cornerRadius)
        val key = imagePref + String.format("%.2f", cornerRadius)
        placeholderMap[key] = SoftReference(drawable)
        return drawable
    }

    private fun getVideoPlaceholderDrawable(context: Context, cornerRadius: Float): Drawable {
        val drawable = getPlaceholderDrawable(context, R.drawable.placeholder_video, cornerRadius)
        val key = videoPref + String.format("%.2f", cornerRadius)
        placeholderMap[key] = SoftReference(drawable)
        return drawable
    }

    private fun getNewsPlaceholder1Drawable(context: Context): Drawable {
        val drawable = getPlaceholderDrawable(context, R.drawable.default_news_pic_1, getDefaultCornerRadius(context))
        val key = newsPref + 1
        placeholderMap[key] = SoftReference(drawable)
        return drawable
    }

    private fun getNewsPlaceholder2Drawable(context: Context): Drawable {
        val drawable = getPlaceholderDrawable(context, R.drawable.default_news_pic_2, getDefaultCornerRadius(context))
        val key = newsPref + 2
        placeholderMap[key] = SoftReference(drawable)
        return drawable
    }

    private fun getNewsPlaceholder3Drawable(context: Context): Drawable {
        val drawable = getPlaceholderDrawable(context, R.drawable.default_news_pic_3, getDefaultCornerRadius(context))
        val key = newsPref + 3
        placeholderMap[key] = SoftReference(drawable)
        return drawable
    }

    private fun getPlaceholderDrawable(context: Context, placeholderResourceId: Int, cornerRadius: Float): Drawable {
        val resources = context.resources
        val placeholder = BitmapFactory.decodeResource(resources, placeholderResourceId)
        val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, placeholder)
        circularBitmapDrawable.cornerRadius = cornerRadius
        return circularBitmapDrawable
    }

    private fun getDefaultCornerRadius(context: Context): Float {
        return context.resources.getDimensionPixelSize(R.dimen.news_item_picture_corner).toFloat()
    }
}
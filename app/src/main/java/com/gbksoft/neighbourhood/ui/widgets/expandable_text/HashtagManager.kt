package com.gbksoft.neighbourhood.ui.widgets.expandable_text

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.annotation.ColorInt
import com.gbksoft.neighbourhood.utils.Constants
import java.util.regex.Pattern

class HashtagManager(private val onHashtagClickListener: OnHashtagClickListener? = null) {
    @ColorInt
    var hashtagColor: Int = Color.BLUE

    private val hashtagPattern = Pattern.compile("${Constants.REGEX_HASHTAG}")
    private val hashtags = mutableListOf<Hashtag>()
    private var hashtagClickTime: Long = 0

    fun parseOriginText(text: CharSequence?) {
        val spannableString = SpannableString(text)
        val matcher = hashtagPattern.matcher(spannableString)
        var index = -1
        while (matcher.find()) {
            index++
            val start = matcher.start()
            val end = matcher.end()
            val value = spannableString.substring(start, end)
            addHashtag(index, start, end, value)
        }
        reduceHashtagList(index)
    }

    private fun addHashtag(index: Int, start: Int, end: Int, value: String) {
        if (index < hashtags.size) {
            hashtags[index].start = start
            hashtags[index].end = end
            hashtags[index].value = value
        } else {
            hashtags.add(Hashtag(start, end, value))
        }
    }

    private fun reduceHashtagList(lastHashtagIndex: Int) {
        val size = hashtags.size
        if (size == 0) return

        val iterator = hashtags.listIterator()
        var index = size - 1
        while (iterator.hasPrevious() && index > lastHashtagIndex) {
            iterator.previous()
            iterator.remove()
            index--
        }
    }

    /**
     * Call after [parseOriginText]
     */
    fun spanHashtags(text: CharSequence?, typeface: Typeface? = null): Spannable? {
        if (text == null || text.isEmpty()) return null
        val spannableString = SpannableString(text)
        val matcher = hashtagPattern.matcher(spannableString)
        var index = -1
        while (matcher.find()) {
            index++
            val start = matcher.start()
            val end = matcher.end()
            val hashtagSpan = HashtagSpan(hashtags[index], typeface)
            spannableString.setSpan(hashtagSpan, start, end, 0)
        }
        return spannableString
    }

    fun spanHashtags(spannable: Spannable?, typeface: Typeface? = null) {
        if (spannable?.toString() == null || spannable.isEmpty()) return
        val matcher = hashtagPattern.matcher(spannable)
        var index = -1
        while (matcher.find()) {
            index++
            val start = matcher.start()
            val end = matcher.end()
            val hashtagSpan = HashtagSpan(hashtags[index], typeface)
            spannable.setSpan(hashtagSpan, start, end, 0)
        }
    }

    fun wasHashtagClick(): Boolean {
        return System.currentTimeMillis() - hashtagClickTime <= 100
    }

    inner class Hashtag(
            var start: Int,
            var end: Int,
            var value: String
    )

    inner class HashtagSpan(val hashtag: Hashtag, val typeface: Typeface? = null): ClickableSpan() {
        override fun onClick(widget: View) {
            hashtagClickTime = System.currentTimeMillis()
            onHashtagClickListener?.onHashTagClick(hashtag.value)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = hashtagColor
            ds.isUnderlineText = false
            if (typeface != null) {
                ds.typeface = typeface
            }
        }

    }
}
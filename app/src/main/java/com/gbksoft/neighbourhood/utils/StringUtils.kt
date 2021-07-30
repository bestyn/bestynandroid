package com.gbksoft.neighbourhood.utils

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan

object StringUtils {

    fun getColoredSpannableString(text: String, color: Int): SpannableString {
        val spannableStr = SpannableString(text)
        spannableStr.setSpan(ForegroundColorSpan(color), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableStr
    }
}
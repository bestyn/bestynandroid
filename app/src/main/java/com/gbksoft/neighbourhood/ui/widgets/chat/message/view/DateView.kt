package com.gbksoft.neighbourhood.ui.widgets.chat.message.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutMessageDateBinding

class DateView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var layout: LayoutMessageDateBinding

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layout = DataBindingUtil.inflate(LayoutInflater.from(context),
            R.layout.layout_message_date, this, true)
    }

    fun setDate(date: String?) {
        layout.textView.text = date
    }
}
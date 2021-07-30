package com.gbksoft.neighbourhood.ui.widgets.chat.message.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DimenRes
import com.gbksoft.neighbourhood.R

class UnreadMsgHighlighterView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @DimenRes
    private var horMarginMinus: Int? = null

    init {
        layoutParams = MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.layout_unread_msg_highlighter, this, true)
    }

    fun setHorizontalMarginMinus(@DimenRes horMarginMinus: Int) {
        this.horMarginMinus = horMarginMinus
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val horMarginMinus = horMarginMinus?.let { resources.getDimensionPixelSize(it) } ?: return
        (layoutParams as? MarginLayoutParams)?.let {
            it.leftMargin = -horMarginMinus
            it.rightMargin = -horMarginMinus
        }
    }

}
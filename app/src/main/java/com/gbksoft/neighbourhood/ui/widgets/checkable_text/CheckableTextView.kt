package com.gbksoft.neighbourhood.ui.widgets.checkable_text

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckedTextView
import com.gbksoft.neighbourhood.R

class CheckableTextView : AppCompatCheckedTextView {
    private lateinit var originTextColor: ColorStateList
    private var checkedTextColor: Int? = null

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        attrs?.let { extractAttrs(it) }
        originTextColor = textColors
    }

    private fun extractAttrs(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CheckableTextView)
        try {
            if (a.hasValue(R.styleable.CheckableTextView_checkedTextColor)) {
                checkedTextColor = a.getColor(R.styleable.CheckableTextView_checkedTextColor,
                    Color.BLACK)
            }
        } finally {
            a.recycle()
        }
    }

    override fun setChecked(checked: Boolean) {
        super.setChecked(checked)
        setTextChecked(checked)
    }

    private fun setTextChecked(checked: Boolean) {
        val checkedTextColor = checkedTextColor ?: return

        if (checked) {
            setTextColor(checkedTextColor)
        } else {
            setTextColor(originTextColor)
        }
    }
}
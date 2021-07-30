package com.gbksoft.neighbourhood.ui.widgets.text_input_layout

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.gbksoft.neighbourhood.R
import com.google.android.material.textfield.TextInputLayout

class AutoClearErrorInputLayout : TextInputLayout {
    private var isAutoClearErrorEnabled = true

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
    }

    private fun extractAttrs(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.AutoClearErrorInputLayout)
        try {
            val disableAutoClearError = a.getBoolean(
                R.styleable.AutoClearErrorInputLayout_disableAutoClearError, false)
            isAutoClearErrorEnabled = !disableAutoClearError
        } finally {
            a.recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (isAutoClearErrorEnabled) {
            addWatcher()
        }
    }

    private fun addWatcher() {
        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                clearError()
            }
        })
    }

    fun clearError() {
        if (error?.isNotEmpty() == true) {
            error = null
            isErrorEnabled = false
        }
    }
}
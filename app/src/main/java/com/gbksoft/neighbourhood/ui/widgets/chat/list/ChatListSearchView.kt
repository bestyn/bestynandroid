package com.gbksoft.neighbourhood.ui.widgets.chat.list

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutChatListSearchBinding

class ChatListSearchView
@JvmOverloads
constructor(context: Context? = null, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val layout: LayoutChatListSearchBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
        R.layout.layout_chat_list_search, this, true)

    var onSearchQueryChangedListener: ((CharSequence) -> Unit)? = null
    var onChangeBackgroundClickListener: (() -> Unit)? = null

    fun getCurrentQuery(): String {
        return layout.etSearch.text.toString()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setup()
    }

    private fun setup() {
        layout.etSearch.addTextChangedListener(SearchTextWatcher(::onSearchQueryChanged))
        layout.etSearch.setOnFocusChangeListener { v, hasFocus ->
            onSearchFiledFocusChange(v, hasFocus)
        }
        layout.ivClear.setOnClickListener {
            layout.etSearch.text = null
        }
        layout.tvCancel.setOnClickListener {
            layout.etSearch.text = null
            hideKeyboard()
            layout.etSearch.clearFocus()
            layout.ivSearch.requestFocus()
        }
        layout.ivChangeBackground.setOnClickListener {
            onChangeBackgroundClickListener?.invoke()
        }
    }

    private fun onSearchQueryChanged(text: CharSequence) {
        updateUI(text)
        onSearchQueryChangedListener?.invoke(text)
    }

    private fun updateUI(text: CharSequence) {
        if (text.isEmpty()) {
            layout.ivSearch.visibility = View.VISIBLE
            layout.ivClear.visibility = View.GONE
        } else {
            layout.ivSearch.visibility = View.GONE
            layout.ivClear.visibility = View.VISIBLE
        }
    }

    private fun onSearchFiledFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            layout.ivChangeBackground.visibility = View.GONE
            layout.tvCancel.visibility = View.VISIBLE
        } else {
            layout.tvCancel.visibility = View.GONE
            layout.ivChangeBackground.visibility = View.VISIBLE
        }
    }

    private fun hideKeyboard() {
        (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?)
            ?.hideSoftInputFromWindow(layout.etSearch.windowToken, 0)
    }

}
package com.gbksoft.neighbourhood.ui.widgets.post_list

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView.OnEditorActionListener
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutPostListSearchBinding
import com.gbksoft.neighbourhood.ui.widgets.base.SimpleTextWatcher
import timber.log.Timber


class GlobalSearchView
@JvmOverloads
constructor(context: Context? = null, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val layout: LayoutPostListSearchBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
        R.layout.layout_post_list_search, this, true)

    var onSearchClickListener: ((CharSequence) -> Unit)? = null
    private var lastSearchQuery: CharSequence = ""

    override fun onFinishInflate() {
        super.onFinishInflate()
        setup()
        setClickListeners()
    }

    private fun setup() {
        layout.etSearch.setOnFocusChangeListener { v, hasFocus ->
            onSearchFiledFocusChange(v, hasFocus)
        }
        layout.etSearch.addTextChangedListener(SimpleTextWatcher {
            checkQueryIsEmpty()
        })
    }

    private fun onSearchFiledFocusChange(v: View, hasFocus: Boolean) {
        Timber.tag("SearchTag").d("hasFocus: $hasFocus")
        if (hasFocus) {
            layout.ivCancel.visibility = View.VISIBLE
        } else {
            checkQueryIsEmpty()
        }
    }

    private fun checkQueryIsEmpty() {
        val visibility = layout.ivCancel.visibility
        if (getCurrentQuery().isEmpty()) {
            if (visibility != View.GONE) layout.ivCancel.visibility = View.GONE
        } else {
            if (visibility != View.VISIBLE) layout.ivCancel.visibility = View.VISIBLE
        }
    }

    private fun setClickListeners() {
        layout.ivSearch.setOnClickListener {
            Timber.tag("SearchTag").d("ivSearch.OnClick")
            hideKeyboard()
            notifySearchQuery()
        }
        layout.ivCancel.setOnClickListener {
            Timber.tag("SearchTag").d("ivCancel.OnClick")
            layout.etSearch.text = null
            hideKeyboard()
            layout.ivCancel.visibility = View.GONE
            notifySearchQuery()
        }
        layout.etSearch.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                notifySearchQuery()
                return@OnEditorActionListener true
            }
            false
        })
    }

    fun setQuery(query: String) {
        layout.etSearch.setText(query)
        hideKeyboard()
        notifySearchQuery()
    }

    private fun notifySearchQuery() {
        val searchQuery = getCurrentQuery()
        Timber.tag("SearchTag").d("searchQuery: $searchQuery  lastSearchQuery: $lastSearchQuery")
        onSearchClickListener?.invoke(searchQuery)
        lastSearchQuery = searchQuery
    }

    private fun hideKeyboard() {
        (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?)
            ?.hideSoftInputFromWindow(layout.etSearch.windowToken, 0)
        layout.etSearch.clearFocus()
        layout.ivSearch.requestFocus()
    }

    fun focus() {
        layout.etSearch.requestFocus()
        (layout.etSearch.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.showSoftInput(layout.etSearch, 0)
    }

    fun getCurrentQuery(): String {
        return layout.etSearch.text.toString()
    }

}
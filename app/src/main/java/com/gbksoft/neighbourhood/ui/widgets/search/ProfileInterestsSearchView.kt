package com.gbksoft.neighbourhood.ui.widgets.search

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutInterestsSearchViewBinding

class ProfileInterestsSearchView
@JvmOverloads
constructor(context: Context? = null, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val layout: LayoutInterestsSearchViewBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_interests_search_view,
            this,
            true)
    var onCancelButtonClicked: (() -> Unit)? = null

    val editText: EditText
    get() = layout.etSearch

    override fun onFinishInflate() {
        super.onFinishInflate()
        setup()
    }

    private fun setup() {
        layout.etSearch.setOnFocusChangeListener { _, hasFocus -> updateUI(hasFocus) }
        layout.ivClear.setOnClickListener {
            layout.etSearch.text = null
        }
        layout.btnCancel.setOnClickListener {
            layout.etSearch.text = null
            onCancelButtonClicked?.invoke()
        }
    }

    private fun updateUI(hasFocus: Boolean) {
        if (hasFocus) {
            layout.ivClear.visibility = View.VISIBLE
            layout.btnCancel.visibility = View.VISIBLE
        } else {
            layout.ivClear.visibility = View.GONE
            layout.btnCancel.visibility = View.GONE
        }
    }
}